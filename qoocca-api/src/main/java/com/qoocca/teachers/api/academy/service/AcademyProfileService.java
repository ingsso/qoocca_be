package com.qoocca.teachers.api.academy.service;

import com.qoocca.teachers.api.academy.model.request.AcademyRequest;
import com.qoocca.teachers.api.academy.model.request.AcademyResubmitRequest;
import com.qoocca.teachers.api.academy.model.request.AcademyUpdateRequest;
import com.qoocca.teachers.api.academy.model.response.AcademyResponse;
import com.qoocca.teachers.api.academy.model.response.AcademyImageUploadEnqueueResponse;
import com.qoocca.teachers.api.academy.model.response.AcademyImageUploadJobStatusResponse;
import com.qoocca.teachers.api.age.model.AgeResponse;
import com.qoocca.teachers.api.subject.model.SubjectResponse;
import com.qoocca.teachers.api.global.config.CacheConfig;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.academy.entity.AcademyAgeEntity;
import com.qoocca.teachers.db.academy.entity.AcademyEntity;
import com.qoocca.teachers.db.academy.entity.AcademyImageEntity;
import com.qoocca.teachers.db.academy.entity.AcademySubjectEntity;
import com.qoocca.teachers.db.academy.entity.ApprovalStatus;
import com.qoocca.teachers.db.academy.repository.AcademyAgeRepository;
import com.qoocca.teachers.db.academy.repository.AcademyImageRepository;
import com.qoocca.teachers.db.academy.repository.AcademyRepository;
import com.qoocca.teachers.db.academy.repository.AcademySubjectRepository;
import com.qoocca.teachers.db.age.repository.AgeRepository;
import com.qoocca.teachers.db.subject.repository.SubjectRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcademyProfileService {

    private final AcademyRepository academyRepository;
    private final AcademyImageRepository academyImageRepository;
    private final AcademyImageUploadQueueService academyImageUploadQueueService;
    private final AcademySubjectRepository academySubjectRepository;
    private final AcademyAgeRepository academyAgeRepository;
    private final AgeRepository ageRepository;
    private final SubjectRepository subjectRepository;

    @Value("${file.upload.path}")
    private String imageSavePath;

    @Value("${file.upload.base-url}")
    private String imageBaseUrl;

    @Value("${academy.image-upload.queue-capacity:300}")
    private int imageUploadQueueCapacity;

    @Value("${academy.image-upload.worker-count:4}")
    private int imageUploadWorkerCount;

    @Value("${academy.image-upload.tmp-dir:_upload_tmp}")
    private String imageUploadTmpDir;

    @Value("${academy.image-upload.job-retention-minutes:30}")
    private int imageUploadJobRetentionMinutes;

    private ArrayBlockingQueue<ImageUploadJob> imageUploadQueue;
    private final Map<String, ImageUploadJob> imageUploadJobs = new ConcurrentHashMap<>();
    private ExecutorService imageUploadWorkerPool;

    @Transactional(readOnly = true)
    public AcademyResponse getAcademyDetail(Long id) {
        AcademyEntity academy = academyRepository.findDetailById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));
        return AcademyResponse.from(academy);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.ACADEMY_SUBJECTS, key = "#academyId")
    public List<SubjectResponse> getAcademySubjects(Long academyId) {
        List<AcademySubjectEntity> mappings = academySubjectRepository.findAllByAcademyId(academyId);
        return mappings.stream()
                .map(mapping -> SubjectResponse.from(mapping.getSubject()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.ACADEMY_AGES, key = "#academyId")
    public List<AgeResponse> getAcademyAges(Long academyId) {
        List<AcademyAgeEntity> mappings = academyAgeRepository.findAllByAcademyId(academyId);
        return mappings.stream()
                .map(mapping -> AgeResponse.from(mapping.getAge()))
                .collect(Collectors.toList());
    }

    @PostConstruct
    void initImageUploadQueue() {
        imageUploadQueue = new ArrayBlockingQueue<>(imageUploadQueueCapacity);
        imageUploadWorkerPool = Executors.newFixedThreadPool(imageUploadWorkerCount);
        for (int i = 0; i < imageUploadWorkerCount; i++) {
            imageUploadWorkerPool.submit(this::runImageUploadWorker);
        }
        log.info("academy image upload queue initialized: capacity={}, workers={}", imageUploadQueueCapacity, imageUploadWorkerCount);
    }

    @PreDestroy
    void shutdownImageUploadQueue() {
        if (imageUploadWorkerPool != null) {
            imageUploadWorkerPool.shutdownNow();
        }
    }

    public AcademyImageUploadEnqueueResponse enqueueAcademyImages(Long academyId, List<MultipartFile> images, Long userId) {
        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        if (!academy.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }
        if (images == null || images.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return academyImageUploadQueueService.enqueue(academyId, images);
    }

    public AcademyImageUploadJobStatusResponse getImageUploadJobStatus(Long academyId, String jobId, Long userId) {
        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));
        if (!academy.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        return academyImageUploadQueueService.getStatus(academyId, jobId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ME_ACADEMIES, key = "#userId"),
            @CacheEvict(cacheNames = CacheConfig.ACADEMY_SUBJECTS, key = "#id"),
            @CacheEvict(cacheNames = CacheConfig.ACADEMY_AGES, key = "#id")
    })
    public void updateAcademy(Long id, AcademyUpdateRequest req, Long userId) {
        AcademyEntity academy = academyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        if (!academy.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        academy.updateInfo(
                req.getName(),
                req.getPhoneNumber(),
                req.getBriefInfo(),
                req.getDetailInfo()
        );

        academy.updateFullAddress(
                req.getBaseAddress(),
                req.getDetailAddress()
        );

        updateRelationalData(academy, req);

        // ✅ 인증서 처리
        if (req.getCertificateFile() != null && !req.getCertificateFile().isEmpty()) {
            updateCertificateFile(academy, req.getCertificateFile());
        }

    }

    public void uploadAcademyImages(Long academyId, List<MultipartFile> images, Long userId) {
        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        if (!academy.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        List<String> imageUrls = saveImages(academyId, images);
        persistImageUrls(academyId, imageUrls);
    }

    public void uploadAcademyFiles(Long academyId, MultipartFile certificateFile, List<MultipartFile> images, Long userId) {
        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        if (!academy.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        if (certificateFile != null && !certificateFile.isEmpty()) {
            updateCertificateFile(academy, certificateFile);
            academyRepository.save(academy);
        }

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = saveImages(academyId, images);
            persistImageUrls(academyId, imageUrls);
        }
    }


    /* ================= 이미지 삭제 ================= */
    @Transactional
    public void deleteAcademyImage(Long academyId, Long imageId, Long userId) {
        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        if (!academy.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        AcademyImageEntity image = academy.getAcademyImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_IMAGE_NOT_FOUND));

        // DB에서 먼저 제거
        academy.getAcademyImages().remove(image);

        // 실제 파일 삭제
        deletePhysicalFile(image.getImageUrl());
    }
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ME_ACADEMIES, key = "#userId"),
            @CacheEvict(cacheNames = CacheConfig.ACADEMY_SUBJECTS, key = "#id"),
            @CacheEvict(cacheNames = CacheConfig.ACADEMY_AGES, key = "#id")
    })
    public void resubmitAcademy(Long id, AcademyResubmitRequest req, Long userId) {
        AcademyEntity academy = academyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        if (!academy.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        if (academy.getApprovalStatus() != ApprovalStatus.REJECTED) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        academy.updateInfo(req.getName(), null, null, null);
        academy.updateFullAddress(req.getBaseAddress(), req.getDetailAddress());

        if (req.getCertificateFile() != null && !req.getCertificateFile().isEmpty()) {
            updateCertificateFile(academy, req.getCertificateFile());
        }

        academy.updateApprovalStatus(ApprovalStatus.PENDING);
        academy.setRejectionReason(null);
    }

    private void deletePhysicalFile(String fileUrl) {
        try {
            String relativePath = fileUrl.replace(imageBaseUrl, "");
            File fileToDelete = new File(imageSavePath + relativePath);

            if (fileToDelete.exists()) {
                fileToDelete.delete();
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.ACADEMY_IMAGE_DELETE_FAILED);
        }
    }

    private void updateCertificateFile(AcademyEntity academy, MultipartFile certificateFile) {
        String academyFolderPath = imageSavePath + academy.getId() + "/";
        File folder = new File(academyFolderPath);
        if (!folder.exists()) folder.mkdirs();

        if (academy.getCertificate() != null) {
            deletePhysicalFile(academy.getCertificate());
        }

        String certFileName = "cert_" + UUID.randomUUID() + "_" + certificateFile.getOriginalFilename();
        try {
            certificateFile.transferTo(new File(academyFolderPath + certFileName));
            academy.setCertificate(imageBaseUrl + academy.getId() + "/" + certFileName);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.ACADEMY_CERTIFICATE_SAVE_FAILED);
        }
    }

    private List<String> saveImages(Long academyId, List<MultipartFile> images) {
        List<String> imageUrls = new ArrayList<>();
        String folderPath = imageSavePath + academyId + "/";
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();

        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) continue;

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            try {
                file.transferTo(new File(folderPath + filename));
                imageUrls.add(imageBaseUrl + academyId + "/" + filename);

            } catch (IOException e) {
                throw new CustomException(ErrorCode.ACADEMY_IMAGE_SAVE_FAILED);
            }
        }
        return imageUrls;
    }

    @Transactional
    protected void persistImageUrls(Long academyId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        AcademyEntity academyRef = academyRepository.getReferenceById(academyId);
        List<AcademyImageEntity> entities = imageUrls.stream()
                .map(url -> AcademyImageEntity.builder()
                        .academy(academyRef)
                        .imageUrl(url)
                        .build())
                .toList();
        academyImageRepository.saveAll(entities);
    }

    @Transactional
    protected void persistUploadedFiles(Long academyId, List<QueuedFile> queuedFiles) {
        if (queuedFiles == null || queuedFiles.isEmpty()) {
            return;
        }

        List<String> imageUrls = moveQueuedFilesToAcademyFolder(academyId, queuedFiles);
        persistImageUrls(academyId, imageUrls);
    }

    private ImageUploadJob createImageUploadJob(Long academyId, List<MultipartFile> images) {
        String jobId = UUID.randomUUID().toString();
        LocalDateTime submittedAt = LocalDateTime.now();
        List<QueuedFile> queuedFiles = spoolToTemp(jobId, images);
        ImageUploadJob job = new ImageUploadJob(jobId, academyId, queuedFiles, submittedAt);
        imageUploadJobs.put(jobId, job);
        return job;
    }

    private List<QueuedFile> spoolToTemp(String jobId, List<MultipartFile> images) {
        List<QueuedFile> queuedFiles = new ArrayList<>();
        Path tmpBase = Path.of(imageSavePath, imageUploadTmpDir, jobId);
        try {
            Files.createDirectories(tmpBase);
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                String safeOriginalName = file.getOriginalFilename() == null ? "image.bin" : file.getOriginalFilename();
                String tempName = UUID.randomUUID() + "_" + safeOriginalName;
                Path tempPath = tmpBase.resolve(tempName);
                file.transferTo(tempPath.toFile());
                queuedFiles.add(new QueuedFile(tempPath, safeOriginalName));
            }
            return queuedFiles;
        } catch (IOException e) {
            cleanupTempFiles(queuedFiles);
            throw new CustomException(ErrorCode.ACADEMY_IMAGE_SAVE_FAILED);
        }
    }

    private List<String> moveQueuedFilesToAcademyFolder(Long academyId, List<QueuedFile> queuedFiles) {
        List<String> imageUrls = new ArrayList<>();
        Path targetFolder = Path.of(imageSavePath, String.valueOf(academyId));
        try {
            Files.createDirectories(targetFolder);
            for (QueuedFile queuedFile : queuedFiles) {
                String filename = UUID.randomUUID() + "_" + queuedFile.originalName;
                Path targetPath = targetFolder.resolve(filename);
                Files.move(queuedFile.tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                imageUrls.add(imageBaseUrl + academyId + "/" + filename);
            }
            return imageUrls;
        } catch (IOException e) {
            throw new CustomException(ErrorCode.ACADEMY_IMAGE_SAVE_FAILED);
        } finally {
            cleanupTempFiles(queuedFiles);
        }
    }

    private void runImageUploadWorker() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ImageUploadJob job = imageUploadQueue.take();
                processImageUploadJob(job);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("academy image upload worker error", e);
            }
        }
    }

    private void processImageUploadJob(ImageUploadJob job) {
        job.markProcessing();
        try {
            persistUploadedFiles(job.academyId, job.queuedFiles);
            job.markCompleted();
        } catch (Exception e) {
            cleanupTempFiles(job.queuedFiles);
            job.markFailed(e.getMessage());
            log.error("academy image upload failed: academyId={}, jobId={}", job.academyId, job.jobId, e);
        } finally {
            cleanupFinishedJobs();
        }
    }

    private void cleanupTempFiles(List<QueuedFile> queuedFiles) {
        if (queuedFiles == null || queuedFiles.isEmpty()) {
            return;
        }
        for (QueuedFile queuedFile : queuedFiles) {
            try {
                Files.deleteIfExists(queuedFile.tempPath);
            } catch (IOException ignore) {
            }
        }
    }

    private void cleanupFinishedJobs() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(imageUploadJobRetentionMinutes);
        imageUploadJobs.entrySet().removeIf(entry -> {
            ImageUploadJob job = entry.getValue();
            if (job.completedAt == null) {
                return false;
            }
            return job.completedAt.isBefore(threshold);
        });
    }

    private enum ImageUploadState {
        QUEUED,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    private static final class QueuedFile {
        private final Path tempPath;
        private final String originalName;

        private QueuedFile(Path tempPath, String originalName) {
            this.tempPath = tempPath;
            this.originalName = originalName;
        }
    }

    private static final class ImageUploadJob {
        private final String jobId;
        private final Long academyId;
        private final List<QueuedFile> queuedFiles;
        private final LocalDateTime submittedAt;
        private volatile LocalDateTime completedAt;
        private volatile String errorMessage;
        private volatile ImageUploadState state;

        private ImageUploadJob(String jobId, Long academyId, List<QueuedFile> queuedFiles, LocalDateTime submittedAt) {
            this.jobId = jobId;
            this.academyId = academyId;
            this.queuedFiles = queuedFiles;
            this.submittedAt = submittedAt;
            this.state = ImageUploadState.QUEUED;
        }

        private void markProcessing() {
            this.state = ImageUploadState.PROCESSING;
        }

        private void markCompleted() {
            this.state = ImageUploadState.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }

        private void markFailed(String errorMessage) {
            this.state = ImageUploadState.FAILED;
            this.completedAt = LocalDateTime.now();
            this.errorMessage = errorMessage;
        }
    }

    private void updateRelationalData(AcademyEntity academy, AcademyRequest req) {
        if (req.getAgeIds() != null) {
            academy.updateAges(ageRepository.findAllById(req.getAgeIds()));
        }

        if (req.getSubjects() != null) {
            academy.updateSubjects(subjectRepository.findAllById(req.getSubjects()));
        }
    }


}

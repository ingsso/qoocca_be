package com.qoocca.teachers.api.academy.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoocca.teachers.api.academy.model.response.AcademyImageUploadEnqueueResponse;
import com.qoocca.teachers.api.academy.model.response.AcademyImageUploadJobStatusResponse;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.academy.entity.AcademyEntity;
import com.qoocca.teachers.db.academy.entity.AcademyImageEntity;
import com.qoocca.teachers.db.academy.repository.AcademyImageRepository;
import com.qoocca.teachers.db.academy.repository.AcademyRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcademyImageUploadQueueService {

    private static final String QUEUE_KEY = "queue:academy:image-upload:v1";
    private static final String JOB_KEY_PREFIX = "job:academy:image-upload:v1:";
    private static final TypeReference<List<QueuedFilePayload>> FILE_LIST_TYPE = new TypeReference<>() {};

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final AcademyRepository academyRepository;
    private final AcademyImageRepository academyImageRepository;

    @Value("${file.upload.path}")
    private String imageSavePath;

    @Value("${file.upload.base-url}")
    private String imageBaseUrl;

    @Value("${academy.image-upload.queue-capacity:300}")
    private int queueCapacity;

    @Value("${academy.image-upload.worker-count:2}")
    private int workerCount;

    @Value("${academy.image-upload.tmp-dir:_upload_tmp}")
    private String tmpDir;

    @Value("${academy.image-upload.job-retention-minutes:30}")
    private int jobRetentionMinutes;

    private ExecutorService workerPool;

    public AcademyImageUploadEnqueueResponse enqueue(Long academyId, List<MultipartFile> images) {
        String jobId = UUID.randomUUID().toString();
        LocalDateTime submittedAt = LocalDateTime.now();

        List<QueuedFilePayload> files = spoolToTemp(jobId, images);
        if (files.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Long qSize = redis.opsForList().size(QUEUE_KEY);
        if (qSize != null && qSize >= queueCapacity) {
            cleanupFiles(files);
            throw new CustomException(ErrorCode.ACADEMY_IMAGE_UPLOAD_QUEUE_FULL);
        }

        JobPayload payload = new JobPayload(jobId, academyId, files, submittedAt);
        writeJobState(payload, "QUEUED", null, null);
        redis.opsForList().leftPush(QUEUE_KEY, toJson(payload));

        return new AcademyImageUploadEnqueueResponse(jobId, "QUEUED", submittedAt);
    }

    public AcademyImageUploadJobStatusResponse getStatus(Long academyId, String jobId) {
        Map<Object, Object> data = redis.opsForHash().entries(jobKey(jobId));
        if (data == null || data.isEmpty()) {
            throw new CustomException(ErrorCode.ACADEMY_IMAGE_NOT_FOUND);
        }
        Long storedAcademyId = parseLong((String) data.get("academyId"));
        if (!academyId.equals(storedAcademyId)) {
            throw new CustomException(ErrorCode.ACADEMY_IMAGE_NOT_FOUND);
        }

        return new AcademyImageUploadJobStatusResponse(
                jobId,
                storedAcademyId,
                (String) data.get("status"),
                parseDateTime((String) data.get("submittedAt")),
                parseDateTime((String) data.get("completedAt")),
                (String) data.get("errorMessage")
        );
    }

    @PostConstruct
    void initWorkers() {
        workerPool = Executors.newFixedThreadPool(workerCount);
        for (int i = 0; i < workerCount; i++) {
            workerPool.submit(this::runWorker);
        }
        log.info("academy image redis queue workers initialized: workers={}", workerCount);
    }

    @PreDestroy
    void shutdownWorkers() {
        if (workerPool != null) {
            workerPool.shutdownNow();
        }
    }

    private void runWorker() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String raw = redis.opsForList().rightPop(QUEUE_KEY, 2, TimeUnit.SECONDS);
                if (raw == null || raw.isBlank()) {
                    continue;
                }
                JobPayload payload = objectMapper.readValue(raw, JobPayload.class);
                process(payload);
            } catch (Exception e) {
                log.error("academy image queue worker failed", e);
            }
        }
    }

    private void process(JobPayload payload) {
        writeJobState(payload, "PROCESSING", null, null);
        try {
            List<String> urls = moveToAcademyFolder(payload.academyId(), payload.files());
            persistImageUrls(payload.academyId(), urls);
            writeJobState(payload, "COMPLETED", LocalDateTime.now(), null);
        } catch (Exception e) {
            cleanupFiles(payload.files());
            writeJobState(payload, "FAILED", LocalDateTime.now(), e.getMessage());
            log.error("academy image queue job failed: academyId={}, jobId={}", payload.academyId(), payload.jobId(), e);
        }
    }

    private List<QueuedFilePayload> spoolToTemp(String jobId, List<MultipartFile> images) {
        List<QueuedFilePayload> out = new ArrayList<>();
        Path tmpBase = Path.of(imageSavePath, tmpDir, "image", jobId);
        try {
            Files.createDirectories(tmpBase);
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                String original = file.getOriginalFilename() == null ? "image.bin" : file.getOriginalFilename();
                String tempName = UUID.randomUUID() + "_" + original;
                Path tempPath = tmpBase.resolve(tempName);
                file.transferTo(tempPath.toFile());
                out.add(new QueuedFilePayload(tempPath.toString(), original));
            }
            return out;
        } catch (IOException e) {
            cleanupFiles(out);
            throw new CustomException(ErrorCode.ACADEMY_IMAGE_SAVE_FAILED);
        }
    }

    private List<String> moveToAcademyFolder(Long academyId, List<QueuedFilePayload> files) {
        List<String> urls = new ArrayList<>();
        Path targetFolder = Path.of(imageSavePath, String.valueOf(academyId));
        try {
            Files.createDirectories(targetFolder);
            for (QueuedFilePayload file : files) {
                String filename = UUID.randomUUID() + "_" + file.originalName();
                Path source = Path.of(file.tempPath());
                Path target = targetFolder.resolve(filename);
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                urls.add(imageBaseUrl + academyId + "/" + filename);
            }
            return urls;
        } catch (IOException e) {
            throw new CustomException(ErrorCode.ACADEMY_IMAGE_SAVE_FAILED);
        } finally {
            cleanupFiles(files);
        }
    }

    private void persistImageUrls(Long academyId, List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        AcademyEntity academyRef = academyRepository.getReferenceById(academyId);
        List<AcademyImageEntity> entities = urls.stream()
                .map(url -> AcademyImageEntity.builder().academy(academyRef).imageUrl(url).build())
                .toList();
        academyImageRepository.saveAll(entities);
    }

    private void cleanupFiles(List<QueuedFilePayload> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        for (QueuedFilePayload file : files) {
            try {
                Files.deleteIfExists(Path.of(file.tempPath()));
            } catch (IOException ignore) {
            }
        }
    }

    private void writeJobState(JobPayload payload, String status, LocalDateTime completedAt, String errorMessage) {
        String key = jobKey(payload.jobId());
        redis.opsForHash().put(key, "jobId", payload.jobId());
        redis.opsForHash().put(key, "academyId", String.valueOf(payload.academyId()));
        redis.opsForHash().put(key, "status", status);
        redis.opsForHash().put(key, "submittedAt", payload.submittedAt().toString());
        redis.opsForHash().put(key, "completedAt", completedAt == null ? "" : completedAt.toString());
        redis.opsForHash().put(key, "errorMessage", errorMessage == null ? "" : errorMessage);
        redis.expire(key, Duration.ofMinutes(jobRetentionMinutes));
    }

    private String jobKey(String jobId) {
        return JOB_KEY_PREFIX + jobId;
    }

    private String toJson(JobPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            cleanupFiles(payload.files());
            throw new CustomException(ErrorCode.ACADEMY_IMAGE_SAVE_FAILED);
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.parseLong(value);
    }

    private record JobPayload(
            String jobId,
            Long academyId,
            List<QueuedFilePayload> files,
            LocalDateTime submittedAt
    ) {
    }

    private record QueuedFilePayload(
            String tempPath,
            String originalName
    ) {
    }
}

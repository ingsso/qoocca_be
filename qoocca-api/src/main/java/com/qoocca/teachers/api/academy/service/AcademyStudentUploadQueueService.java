package com.qoocca.teachers.api.academy.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentUploadEnqueueResponse;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentUploadError;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentUploadJobStatusResponse;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentUploadResponse;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcademyStudentUploadQueueService {

    private static final String QUEUE_KEY = "queue:academy:student-upload:v1";
    private static final String JOB_KEY_PREFIX = "job:academy:student-upload:v1:";
    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<AcademyStudentUploadError>> ERROR_LIST_TYPE = new TypeReference<>() {};

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final AcademyStudentUploadService academyStudentUploadService;

    @Value("${file.upload.path}")
    private String fileUploadPath;

    @Value("${academy.student-upload.queue-capacity:200}")
    private int queueCapacity;

    @Value("${academy.student-upload.worker-count:1}")
    private int workerCount;

    @Value("${academy.student-upload.tmp-dir:_upload_tmp}")
    private String tmpDir;

    @Value("${academy.student-upload.job-retention-minutes:30}")
    private int jobRetentionMinutes;

    private ExecutorService workerPool;

    public AcademyStudentUploadEnqueueResponse enqueue(
            Long academyId,
            MultipartFile file,
            Long classId,
            boolean useAi,
            boolean dryRun
    ) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Long size = redis.opsForList().size(QUEUE_KEY);
        if (size != null && size >= queueCapacity) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String jobId = UUID.randomUUID().toString();
        LocalDateTime submittedAt = LocalDateTime.now();
        String storedPath = spool(jobId, file);

        JobPayload payload = new JobPayload(jobId, academyId, storedPath, classId, useAi, dryRun, submittedAt);
        writeState(payload, "QUEUED", null, null, null);
        redis.opsForList().leftPush(QUEUE_KEY, toJson(payload));

        return new AcademyStudentUploadEnqueueResponse(jobId, "QUEUED", submittedAt);
    }

    public AcademyStudentUploadJobStatusResponse getStatus(Long academyId, String jobId) {
        Map<Object, Object> data = redis.opsForHash().entries(jobKey(jobId));
        if (data == null || data.isEmpty()) {
            throw new CustomException(ErrorCode.STUDENT_NOT_FOUND);
        }
        Long storedAcademyId = parseLong((String) data.get("academyId"));
        if (!academyId.equals(storedAcademyId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        return new AcademyStudentUploadJobStatusResponse(
                jobId,
                storedAcademyId,
                (String) data.get("status"),
                parseDateTime((String) data.get("submittedAt")),
                parseDateTime((String) data.get("completedAt")),
                parseInt((String) data.get("totalRows")),
                parseInt((String) data.get("successCount")),
                parseInt((String) data.get("failureCount")),
                parseHeader((String) data.get("headerMapping")),
                parseErrors((String) data.get("errors")),
                (String) data.get("errorMessage")
        );
    }

    @PostConstruct
    void initWorkers() {
        workerPool = Executors.newFixedThreadPool(workerCount);
        for (int i = 0; i < workerCount; i++) {
            workerPool.submit(this::runWorker);
        }
        log.info("academy student upload redis queue workers initialized: workers={}", workerCount);
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
                log.error("academy student upload queue worker failed", e);
            }
        }
    }

    private void process(JobPayload payload) {
        writeState(payload, "PROCESSING", null, null, null);
        try {
            AcademyStudentUploadResponse result = academyStudentUploadService.uploadFromStoredFile(
                    payload.academyId(),
                    payload.storedPath(),
                    payload.classId(),
                    payload.useAi(),
                    payload.dryRun()
            );
            writeState(payload, "COMPLETED", LocalDateTime.now(), null, result);
        } catch (Exception e) {
            writeState(payload, "FAILED", LocalDateTime.now(), e.getMessage(), null);
            log.error("academy student upload queue job failed: academyId={}, jobId={}", payload.academyId(), payload.jobId(), e);
        } finally {
            cleanup(payload.storedPath());
        }
    }

    private String spool(String jobId, MultipartFile file) {
        Path dir = Path.of(fileUploadPath, tmpDir, "student-upload", jobId);
        try {
            Files.createDirectories(dir);
            String original = file.getOriginalFilename() == null ? "upload.xlsx" : file.getOriginalFilename();
            Path target = dir.resolve(UUID.randomUUID() + "_" + original);
            file.transferTo(target.toFile());
            return target.toString();
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void cleanup(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(storedPath));
        } catch (IOException ignore) {
        }
    }

    private void writeState(
            JobPayload payload,
            String status,
            LocalDateTime completedAt,
            String errorMessage,
            AcademyStudentUploadResponse result
    ) {
        String key = jobKey(payload.jobId());
        redis.opsForHash().put(key, "jobId", payload.jobId());
        redis.opsForHash().put(key, "academyId", String.valueOf(payload.academyId()));
        redis.opsForHash().put(key, "status", status);
        redis.opsForHash().put(key, "submittedAt", payload.submittedAt().toString());
        redis.opsForHash().put(key, "completedAt", completedAt == null ? "" : completedAt.toString());
        redis.opsForHash().put(key, "errorMessage", errorMessage == null ? "" : errorMessage);

        if (result != null) {
            redis.opsForHash().put(key, "totalRows", String.valueOf(result.getTotalRows()));
            redis.opsForHash().put(key, "successCount", String.valueOf(result.getSuccessCount()));
            redis.opsForHash().put(key, "failureCount", String.valueOf(result.getFailureCount()));
            redis.opsForHash().put(key, "headerMapping", toJson(result.getHeaderMapping()));
            redis.opsForHash().put(key, "errors", toJson(result.getErrors()));
        }
        redis.expire(key, Duration.ofMinutes(jobRetentionMinutes));
    }

    private String jobKey(String jobId) {
        return JOB_KEY_PREFIX + jobId;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.parseLong(value);
    }

    private Integer parseInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.parseInt(value);
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    private Map<String, String> parseHeader(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private List<AcademyStudentUploadError> parseErrors(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, ERROR_LIST_TYPE);
        } catch (Exception e) {
            return List.of();
        }
    }

    private record JobPayload(
            String jobId,
            Long academyId,
            String storedPath,
            Long classId,
            boolean useAi,
            boolean dryRun,
            LocalDateTime submittedAt
    ) {
    }
}

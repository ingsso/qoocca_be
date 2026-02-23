package com.qoocca.teachers.api.academy.controller;

import com.qoocca.teachers.api.academy.model.request.AcademyStudentCreateRequest;
import com.qoocca.teachers.api.academy.model.request.AcademyStudentModifyRequest;
import com.qoocca.teachers.api.academy.model.request.AcademyStudentWithParentCreateRequest;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentResponse;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentUploadEnqueueResponse;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentUploadJobStatusResponse;
import com.qoocca.teachers.api.academy.service.AcademyStudentService;
import com.qoocca.teachers.api.academy.service.AcademyStudentUploadQueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "학원 학생 API", description = "학원 학생 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academy/{academyId}/student")
public class AcademyStudentController {

    private final AcademyStudentService academyStudentService;
    private final AcademyStudentUploadQueueService academyStudentUploadQueueService;

    @Operation(summary = "학생 등록")
    @PostMapping
    public AcademyStudentResponse register(
            @Parameter(description = "학원 ID") @PathVariable Long academyId,
            @RequestBody @Valid AcademyStudentCreateRequest request
    ) {
        return academyStudentService.registerStudent(academyId, request);
    }

    @Operation(summary = "학부모 포함 학생 등록")
    @PostMapping("/with-parent")
    public AcademyStudentResponse registerWithParent(
            @Parameter(description = "학원 ID") @PathVariable Long academyId,
            @RequestBody @Valid AcademyStudentWithParentCreateRequest request
    ) {
        return academyStudentService.registerStudentWithParent(academyId, request);
    }

    @Operation(summary = "학생 수정")
    @PutMapping("/{studentId}")
    public AcademyStudentResponse modifyStudent(
            @Parameter(description = "학원 ID") @PathVariable Long academyId,
            @Parameter(description = "학생 ID") @PathVariable Long studentId,
            @RequestBody @Valid AcademyStudentModifyRequest request
    ) {
        return academyStudentService.modifyStudent(academyId, studentId, request);
    }

    @Operation(summary = "학생 목록 조회")
    @GetMapping
    public List<AcademyStudentResponse> getStudents(
            @Parameter(description = "학원 ID") @PathVariable Long academyId
    ) {
        return academyStudentService.getStudents(academyId);
    }

    @Operation(summary = "학생 삭제")
    @DeleteMapping("/{studentId}")
    public void delete(
            @Parameter(description = "학원 ID") @PathVariable Long academyId,
            @Parameter(description = "학생 ID") @PathVariable Long studentId
    ) {
        academyStudentService.deleteStudent(academyId, studentId);
    }

    @Operation(summary = "학생 엑셀 업로드 작업 등록")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AcademyStudentUploadEnqueueResponse upload(
            @Parameter(description = "학원 ID") @PathVariable Long academyId,
            @Parameter(description = "엑셀 파일") @RequestPart("file") MultipartFile file,
            @Parameter(description = "클래스 컬럼이 없을 때 사용할 기본 클래스 ID")
            @RequestParam(value = "classId", required = false) Long classId,
            @Parameter(description = "AI 헤더 매핑 사용 여부")
            @RequestParam(value = "useAi", defaultValue = "true") boolean useAi,
            @Parameter(description = "검증만 수행 여부")
            @RequestParam(value = "dryRun", defaultValue = "false") boolean dryRun
    ) {
        return academyStudentUploadQueueService.enqueue(academyId, file, classId, useAi, dryRun);
    }

    @Operation(summary = "학생 엑셀 업로드 작업 상태 조회")
    @GetMapping("/upload/jobs/{jobId}")
    public AcademyStudentUploadJobStatusResponse getUploadJobStatus(
            @PathVariable Long academyId,
            @PathVariable String jobId
    ) {
        return academyStudentUploadQueueService.getStatus(academyId, jobId);
    }
}

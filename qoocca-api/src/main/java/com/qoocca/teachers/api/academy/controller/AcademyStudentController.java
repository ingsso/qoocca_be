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

@Tag(name = "Academy Student API", description = "Academy student management API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academy/{academyId}/student")
public class AcademyStudentController {

    private final AcademyStudentService academyStudentService;
    private final AcademyStudentUploadQueueService academyStudentUploadQueueService;

    @Operation(summary = "Register student")
    @PostMapping
    public AcademyStudentResponse register(
            @Parameter(description = "Academy ID") @PathVariable Long academyId,
            @RequestBody @Valid AcademyStudentCreateRequest request
    ) {
        return academyStudentService.registerStudent(academyId, request);
    }

    @Operation(summary = "Register student with parent")
    @PostMapping("/with-parent")
    public AcademyStudentResponse registerWithParent(
            @Parameter(description = "Academy ID") @PathVariable Long academyId,
            @RequestBody @Valid AcademyStudentWithParentCreateRequest request
    ) {
        return academyStudentService.registerStudentWithParent(academyId, request);
    }

    @Operation(summary = "Modify student")
    @PutMapping("/{studentId}")
    public AcademyStudentResponse modifyStudent(
            @Parameter(description = "Academy ID") @PathVariable Long academyId,
            @Parameter(description = "Student ID") @PathVariable Long studentId,
            @RequestBody @Valid AcademyStudentModifyRequest request
    ) {
        return academyStudentService.modifyStudent(academyId, studentId, request);
    }

    @Operation(summary = "Get students")
    @GetMapping
    public List<AcademyStudentResponse> getStudents(
            @Parameter(description = "Academy ID") @PathVariable Long academyId
    ) {
        return academyStudentService.getStudents(academyId);
    }

    @Operation(summary = "Delete student")
    @DeleteMapping("/{studentId}")
    public void delete(
            @Parameter(description = "Academy ID") @PathVariable Long academyId,
            @Parameter(description = "Student ID") @PathVariable Long studentId
    ) {
        academyStudentService.deleteStudent(academyId, studentId);
    }

    @Operation(summary = "Enqueue excel upload")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AcademyStudentUploadEnqueueResponse upload(
            @Parameter(description = "Academy ID") @PathVariable Long academyId,
            @Parameter(description = "Excel file") @RequestPart("file") MultipartFile file,
            @Parameter(description = "Default class ID when class column is missing")
            @RequestParam(value = "classId", required = false) Long classId,
            @Parameter(description = "Use AI header mapping")
            @RequestParam(value = "useAi", defaultValue = "true") boolean useAi,
            @Parameter(description = "Validation only")
            @RequestParam(value = "dryRun", defaultValue = "false") boolean dryRun
    ) {
        return academyStudentUploadQueueService.enqueue(academyId, file, classId, useAi, dryRun);
    }

    @Operation(summary = "Get excel upload job status")
    @GetMapping("/upload/jobs/{jobId}")
    public AcademyStudentUploadJobStatusResponse getUploadJobStatus(
            @PathVariable Long academyId,
            @PathVariable String jobId
    ) {
        return academyStudentUploadQueueService.getStatus(academyId, jobId);
    }
}

package com.qoocca.teachers.api.academy.controller;

import com.qoocca.teachers.api.academy.model.request.AcademyStudentCreateRequest;
import com.qoocca.teachers.api.academy.model.request.AcademyStudentModifyRequest;
import com.qoocca.teachers.api.academy.model.request.AcademyStudentWithParentCreateRequest;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentResponse;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentUploadResponse;
import com.qoocca.teachers.api.academy.service.AcademyStudentService;
import com.qoocca.teachers.api.academy.service.AcademyStudentUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Academy Student API", description = "학원별 원생 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academy/{academyId}/student")
public class AcademyStudentController {

    private final AcademyStudentService academyStudentService;
    private final AcademyStudentUploadService academyStudentUploadService;

    @Operation(summary = "학원 원생 등록", description = "특정 학원에 학생을 등록합니다.")
    @PostMapping
    public AcademyStudentResponse register(
            @Parameter(description = "학원 고유 ID", example = "1")
            @PathVariable Long academyId,
            @RequestBody @Valid AcademyStudentCreateRequest request
    ) {
        return academyStudentService.registerStudent(academyId, request);
    }

    @Operation(summary = "학원 원생+보호자 동시 등록", description = "학생/보호자를 하나의 트랜잭션으로 등록하고, classId/classIds가 있으면 해당 반(들)에 결제자까지 함께 지정합니다.")
    @PostMapping("/with-parent")
    public AcademyStudentResponse registerWithParent(
            @Parameter(description = "학원 고유 ID", example = "1")
            @PathVariable Long academyId,
            @RequestBody @Valid AcademyStudentWithParentCreateRequest request
    ) {
        return academyStudentService.registerStudentWithParent(academyId, request);
    }

    @Operation(summary = "원생 정보 수정", description = "학원에 등록된 학생의 정보를 수정합니다.")
    @PutMapping("/{studentId}")
    public AcademyStudentResponse modifyStudent(
            @Parameter(description = "학원 고유 ID", example = "1")
            @PathVariable Long academyId,
            @Parameter(description = "학생 고유 ID", example = "10")
            @PathVariable Long studentId,
            @RequestBody @Valid AcademyStudentModifyRequest request
    ) {
        return academyStudentService.modifyStudent(academyId, studentId, request);
    }

    @Operation(summary = "학원 원생 목록 조회", description = "해당 학원에 소속된 모든 학생 리스트를 조회합니다.")
    @GetMapping
    public List<AcademyStudentResponse> getStudents(
            @Parameter(description = "학원 고유 ID", example = "1")
            @PathVariable Long academyId
    ) {
        return academyStudentService.getStudents(academyId);
    }

    @Operation(summary = "원생 삭제(퇴원)", description = "학원에서 특정 학생 정보를 삭제합니다.")
    @DeleteMapping("/{studentId}")
    public void delete(
            @Parameter(description = "학원 고유 ID", example = "1")
            @PathVariable Long academyId,
            @Parameter(description = "학생 고유 ID", example = "10")
            @PathVariable Long studentId
    ) {
        academyStudentService.deleteStudent(academyId, studentId);
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "엑셀 업로드 원생 등록", description = "엑셀 파일로 학생을 등록하고 클래스에 배정합니다.")
    public AcademyStudentUploadResponse upload(
            @Parameter(description = "학원 고유 ID", example = "1")
            @PathVariable Long academyId,
            @Parameter(description = "학생 엑셀 파일 (.xlsx)")
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "기본 클래스 ID (엑셀에 class 컬럼 없을 때)")
            @RequestParam(value = "classId", required = false) Long classId,
            @Parameter(description = "AI 헤더 매핑 사용")
            @RequestParam(value = "useAi", defaultValue = "true") boolean useAi,
            @Parameter(description = "검증만 수행 (DB 저장 안함)")
            @RequestParam(value = "dryRun", defaultValue = "false") boolean dryRun
    ) {
        return academyStudentUploadService.upload(academyId, file, classId, useAi, dryRun);
    }
}

package com.qoocca.teachers.academy.controller;

import com.qoocca.teachers.academy.model.request.AcademyStudentCreateRequest;
import com.qoocca.teachers.academy.model.request.AcademyStudentModifyRequest;
import com.qoocca.teachers.academy.model.response.AcademyStudentResponse;
import com.qoocca.teachers.academy.service.AcademyStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Academy Student API", description = "학원별 원생 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academy/{academyId}/student")
public class AcademyStudentController {

    private final AcademyStudentService academyStudentService;

    @Operation(summary = "학원 원생 등록", description = "특정 학원에 새로운 학생을 등록합니다.")
    @PostMapping
    public AcademyStudentResponse register(
            @Parameter(description = "학원 고유 ID", example = "1")
            @PathVariable Long academyId,
            @RequestBody @Valid AcademyStudentCreateRequest request
    ) {
        return academyStudentService.registerStudent(academyId, request);
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
            @PathVariable Long academyId) {
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
}
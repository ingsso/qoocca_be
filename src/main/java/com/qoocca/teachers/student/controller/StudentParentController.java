package com.qoocca.teachers.student.controller;

import com.qoocca.teachers.parent.model.ParentCreateRequest;
import com.qoocca.teachers.parent.model.ParentResponse;
import com.qoocca.teachers.parent.model.ParentUpdateRequest;
import com.qoocca.teachers.student.service.StudentParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Student Parent API", description = "학생별 학부모 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student")
public class StudentParentController {

    private final StudentParentService studentParentService;

    @Operation(summary = "학생의 학부모 목록 조회", description = "특정 학생에게 연결된 모든 학부모 정보를 조회합니다.")
    @GetMapping("/{studentId}/parent")
    public ResponseEntity<List<ParentResponse>> getParents(
            @Parameter(description = "학생 고유 ID", example = "1")
            @PathVariable Long studentId) {
        return ResponseEntity.ok(studentParentService.getParents(studentId));
    }

    @Operation(summary = "학부모 추가 등록", description = "특정 학생에게 새로운 학부모 정보를 생성하여 연결합니다.")
    @PostMapping("/{studentId}/parent")
    public ResponseEntity<ParentResponse> addParent(
            @Parameter(description = "학생 고유 ID", example = "1")
            @PathVariable Long studentId,
            @RequestBody @Valid ParentCreateRequest request
    ) {
        return ResponseEntity.ok(studentParentService.addParent(studentId, request));
    }

    @Operation(summary = "학부모 정보 수정", description = "특정 학생에게 연결된 학부모의 정보를 업데이트합니다.")
    @PutMapping("/{studentId}/parent/{parentId}")
    public ResponseEntity<ParentResponse> updateParent(
            @Parameter(description = "학생 고유 ID", example = "1")
            @PathVariable Long studentId,
            @Parameter(description = "수정할 학부모 고유 ID", example = "5")
            @PathVariable Long parentId,
            @RequestBody ParentUpdateRequest request
    ) {
        return ResponseEntity.ok(studentParentService.updateParent(studentId, parentId, request));
    }
}
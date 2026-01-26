package com.qoocca.teachers.api.student.controller;

import com.qoocca.teachers.api.student.model.StudentCreateRequest;
import com.qoocca.teachers.api.student.model.StudentResponse;
import com.qoocca.teachers.api.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Student API", description = "학생 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "학생 등록", description = "신규 학생 정보를 생성합니다.")
    @PostMapping
    public ResponseEntity<StudentResponse> create(@RequestBody StudentCreateRequest request) {
        return ResponseEntity.ok(studentService.create(request));
    }

    @Operation(summary = "학생 상세 조회", description = "학생 ID를 통해 특정 학생의 정보를 조회합니다.")
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentResponse> get(
            @Parameter(description = "조회할 학생의 고유 ID", example = "1")
            @PathVariable Long studentId) {
        return ResponseEntity.ok(studentService.get(studentId));
    }

    @Operation(summary = "학생 정보 수정", description = "기존 학생 정보를 업데이트합니다.")
    @PutMapping("/{studentId}")
    public ResponseEntity<StudentResponse> update(
            @Parameter(description = "수정할 학생의 고유 ID", example = "1")
            @PathVariable Long studentId,
            @RequestBody StudentCreateRequest request
    ) {
        return ResponseEntity.ok(studentService.update(studentId, request));
    }

    @Operation(summary = "학생 삭제", description = "학생 ID를 통해 학생 정보를 삭제합니다.")
    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "삭제할 학생의 고유 ID", example = "1")
            @PathVariable Long studentId) {
        studentService.delete(studentId);
        return ResponseEntity.ok().build();
    }
}
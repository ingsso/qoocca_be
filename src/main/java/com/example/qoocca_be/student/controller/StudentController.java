package com.example.qoocca_be.student.controller;

import com.example.qoocca_be.student.model.*;
import com.example.qoocca_be.student.model.*;
import com.example.qoocca_be.student.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/class/{classId}/student")
public class StudentController {

    private final StudentService studentService;

    /* =========================
     * 학생 등록
     * ========================= */
    @PostMapping
    public ResponseEntity<StudentCreateResponse> createStudent(
            @PathVariable Long classId,
            @Valid @RequestBody StudentCreateRequest request
    ) {
        System.out.println("🔥 학생 등록 API 진입");
        StudentCreateResponse response =
                studentService.createStudent(classId, request);

        return ResponseEntity.ok(response);
    }

    /* =========================
     * 수업별 학생 목록 조회
     * ========================= */
    @GetMapping
    public ResponseEntity<List<StudentListResponse>> getStudentsByClass(
            @PathVariable Long classId
    ) {
        List<StudentListResponse> responses =
                studentService.getStudentsByClass(classId);

        return ResponseEntity.ok(responses);
    }

    /* =========================
     * 학생 단건 조회
     * ========================= */
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentDetailResponse> getStudent(
            @PathVariable Long classId,
            @PathVariable Long studentId
    ) {
        StudentDetailResponse response =
                studentService.getStudent(classId, studentId);

        return ResponseEntity.ok(response);
    }

    /* =========================
     * 학생 정보 수정
     * ========================= */
    @PutMapping("/{studentId}")
    public ResponseEntity<StudentUpdateResponse> updateStudent(
            @PathVariable Long classId,
            @PathVariable Long studentId,
            @RequestBody StudentUpdateRequest request
    ) {
        StudentUpdateResponse response =
                studentService.updateStudent(classId, studentId, request);

        return ResponseEntity.ok(response);
    }

    /* =========================
     * 학생 삭제 (Soft Delete)
     * ========================= */
    @DeleteMapping("/{studentId}")
    public ResponseEntity<StudentDeleteResponse> deleteStudent(
            @PathVariable Long classId,
            @PathVariable Long studentId
    ) {
        StudentDeleteResponse response =
                studentService.deleteStudent(classId, studentId);

        return ResponseEntity.ok(response);
    }
}

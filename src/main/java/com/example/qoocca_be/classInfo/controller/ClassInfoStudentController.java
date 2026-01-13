package com.example.qoocca_be.classInfo.controller;

import com.example.qoocca_be.classInfo.model.ClassInfoStudentRequestDTO;
import com.example.qoocca_be.classInfo.model.ClassInfoStudentResponseDTO;
import com.example.qoocca_be.classInfo.service.ClassInfoStudentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/class/{classId}/student")
@Tag(name = "반 수강생 관리 API", description = "기존 학생을 반에 배정, 조회, 삭제")
public class ClassInfoStudentController {

    private final ClassInfoStudentService service;

    @PostMapping
    public ResponseEntity<Void> register(
            @PathVariable Long classId,
            @RequestBody ClassInfoStudentRequestDTO request
    ) {
        service.register(classId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<ClassInfoStudentResponseDTO>> getStudents(
            @PathVariable Long classId
    ) {
        return ResponseEntity.ok(service.getStudents(classId));
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> remove(
            @PathVariable Long classId,
            @PathVariable Long studentId
    ) {
        service.remove(classId, studentId);
        return ResponseEntity.ok().build();
    }
}

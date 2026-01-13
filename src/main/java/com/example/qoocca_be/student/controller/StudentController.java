package com.example.qoocca_be.student.controller;

import com.example.qoocca_be.student.model.StudentCreateRequest;
import com.example.qoocca_be.student.model.StudentResponse;
import com.example.qoocca_be.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentResponse> create(@RequestBody StudentCreateRequest request) {
        return ResponseEntity.ok(studentService.create(request));
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<StudentResponse> get(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentService.get(studentId));
    }

    @PutMapping("/{studentId}")
    public ResponseEntity<StudentResponse> update(
            @PathVariable Long studentId,
            @RequestBody StudentCreateRequest request
    ) {
        return ResponseEntity.ok(studentService.update(studentId, request));
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> delete(@PathVariable Long studentId) {
        studentService.delete(studentId);
        return ResponseEntity.ok().build();
    }
}

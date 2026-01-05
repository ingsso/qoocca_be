package com.example.qoocca_be.student.controller;

import com.example.qoocca_be.parent.model.ParentCreateRequest;
import com.example.qoocca_be.parent.model.ParentResponse;
import com.example.qoocca_be.parent.model.ParentUpdateRequest;
import com.example.qoocca_be.student.service.StudentParentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student")
public class StudentParentController {

    private final StudentParentService studentParentService;

    @GetMapping("/{studentId}/parent")
    public ResponseEntity<List<ParentResponse>> getParents(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentParentService.getParents(studentId));
    }

    @PostMapping("/{studentId}/parent")
    public ResponseEntity<ParentResponse> addParent(
            @PathVariable Long studentId,
            @RequestBody @Valid ParentCreateRequest request
    ) {
        return ResponseEntity.ok(studentParentService.addParent(studentId, request));
    }

    @PutMapping("/{studentId}/parent/{parentId}")
    public ResponseEntity<ParentResponse> updateParent(
            @PathVariable Long studentId,
            @PathVariable Long parentId,
            @RequestBody ParentUpdateRequest request
    ) {
        return ResponseEntity.ok(studentParentService.updateParent(studentId, parentId, request));
    }
}

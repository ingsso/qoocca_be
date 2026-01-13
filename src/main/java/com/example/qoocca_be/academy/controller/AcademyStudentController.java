package com.example.qoocca_be.academy.controller;

import com.example.qoocca_be.academy.dto.AcademyStudentCreateRequest;
import com.example.qoocca_be.academy.dto.AcademyStudentResponse;
import com.example.qoocca_be.academy.service.AcademyStudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academy/{academyId}/student")
public class AcademyStudentController {

    private final AcademyStudentService academyStudentService;

    @PostMapping
    public AcademyStudentResponse register(
            @PathVariable Long academyId,
            @RequestBody @Valid AcademyStudentCreateRequest request
    ) {
        return academyStudentService.registerStudent(academyId, request);
    }

    @GetMapping
    public List<AcademyStudentResponse> getStudents(@PathVariable Long academyId) {
        return academyStudentService.getStudents(academyId);
    }

    @GetMapping("/cnt")
    public Long getStudentCount(@PathVariable Long academyId) {
        return academyStudentService.getTotalStudentCount(academyId);
    }

    @DeleteMapping("/{studentId}")
    public void delete(
            @PathVariable Long academyId,
            @PathVariable Long studentId
    ) {
        academyStudentService.deleteStudent(academyId, studentId);
    }
}

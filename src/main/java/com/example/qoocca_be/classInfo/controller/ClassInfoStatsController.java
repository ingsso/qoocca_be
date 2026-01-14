package com.example.qoocca_be.classInfo.controller;

import com.example.qoocca_be.classInfo.model.ClassStatsResponseDTO;
import com.example.qoocca_be.classInfo.service.ClassInfoStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academy/{academyId}/class")
public class ClassInfoStatsController {

    private final ClassInfoStatsService service;

    @GetMapping("/stats")
    public ResponseEntity<List<ClassStatsResponseDTO>> getClassStats(
            @PathVariable Long academyId
    ) {
        return ResponseEntity.ok(service.getClassStats(academyId));
    }
}

package com.example.qoocca_be.classInfo.controller;

import com.example.qoocca_be.classInfo.model.ClassParentStatsResponseDTO;
import com.example.qoocca_be.classInfo.service.ClassParentStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academy/{academyId}/class")
public class ClassParentStatsController {

    private final ClassParentStatsService service;

    @GetMapping("/parentstats")
    public ResponseEntity<List<ClassParentStatsResponseDTO>> getParentStats(
            @PathVariable Long academyId
    ) {
        return ResponseEntity.ok(service.getParentStats(academyId));
    }
}

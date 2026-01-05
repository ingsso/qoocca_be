package com.qoocca.be.age.controller;

import com.qoocca.be.age.model.AgeResponseDto;
import com.qoocca.be.age.service.AgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Age API", description = "연령 관련 API")
@RestController
@RequestMapping("/api/ages")
@RequiredArgsConstructor
public class AgeController {

    private final AgeService ageService;

    @Operation(summary = "연령 조회")
    @GetMapping
    public ResponseEntity<List<AgeResponseDto>> getAllAges() {
        return ResponseEntity.ok(ageService.getAllAges());
    }
}

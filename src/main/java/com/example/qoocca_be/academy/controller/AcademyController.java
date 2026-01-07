package com.example.qoocca_be.academy.controller;

import com.example.qoocca_be.academy.dto.AcademyCreateRequest;
import com.example.qoocca_be.academy.dto.AcademyUpdateDto;
import com.example.qoocca_be.academy.dto.AcademyResponseDto;
import com.example.qoocca_be.academy.dto.AcademySearchResponseDto;
import com.example.qoocca_be.academy.service.AcademyService;
import com.example.qoocca_be.age.model.AgeResponseDto;
import com.example.qoocca_be.global.common.PageResponseDto;
import com.example.qoocca_be.subject.model.SubjectResponseDto;
import com.example.qoocca_be.user.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Academy API", description = "학원 관련 API")
@RestController
@RequestMapping("/api/academy")
@RequiredArgsConstructor
public class AcademyController {

    private final AcademyService academyService;

    @Operation(summary = "학원 등록")
    @PostMapping("/register")
    public ResponseEntity<Long> registerAcademy(
            @Valid @RequestBody AcademyCreateRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = academyService.registerAcademy(dto, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @Operation(summary = "특정 학원 상세 정보 조회")
    @GetMapping("/{id}")
    public ResponseEntity<AcademyResponseDto> getAcademyDetail(@PathVariable Long id) {
        AcademyResponseDto res = academyService.getAcademyDetail(id);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "특정 학원의 과목 조회")
    @GetMapping("/{id}/subjects")
    public ResponseEntity<List<SubjectResponseDto>> getSubjects(@PathVariable Long id) {
        return ResponseEntity.ok(academyService.getAcademySubjects(id));
    }

    @Operation(summary = "특정 학원의 나이 조회")
    @GetMapping("/{id}/ages")
    public ResponseEntity<List<AgeResponseDto>> getAges(@PathVariable Long id) {
        return ResponseEntity.ok(academyService.getAcademyAges(id));
    }

    @Operation(summary = "학원 정보 수정")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateAcademy(
            @PathVariable Long id,
            @RequestBody AcademyUpdateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        academyService.updateAcademy(id, dto, userDetails.getUserId());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "학원 이름 검색")
    @GetMapping("/search")
    public ResponseEntity<PageResponseDto<AcademySearchResponseDto>> search(@RequestParam String name,
                                                                            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {
        return ResponseEntity.ok(academyService.searchAcademiesByName(name, pageable));
    }
}


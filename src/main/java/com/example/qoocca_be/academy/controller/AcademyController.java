package com.example.qoocca_be.academy.controller;

import com.example.qoocca_be.academy.model.request.AcademyCreateRequest;
import com.example.qoocca_be.academy.model.request.AcademyUpdateRequest;
import com.example.qoocca_be.academy.model.response.AcademyCheckResponse;
import com.example.qoocca_be.academy.model.response.AcademyResponse;
import com.example.qoocca_be.academy.model.response.DashboardStatsResponse;
import com.example.qoocca_be.academy.service.AcademyService;
import com.example.qoocca_be.age.model.AgeResponse;
import com.example.qoocca_be.subject.model.SubjectResponse;
import com.example.qoocca_be.user.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    @PostMapping(value = "/register", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> registerAcademy(
            @Valid @ModelAttribute AcademyCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = academyService.registerAcademy(req, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @Operation(summary = "학원 승인 여부 확인")
    @GetMapping("/check-registration")
    public ResponseEntity<AcademyCheckResponse> checkRegistration(@AuthenticationPrincipal CustomUserDetails userDetails) {
        AcademyCheckResponse res = academyService.checkRegistrationStatus(userDetails.getUserId());
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "특정 학원 상세 정보 조회")
    @GetMapping("/{id}")
    public ResponseEntity<AcademyResponse> getAcademyDetail(@PathVariable Long id) {
        AcademyResponse res = academyService.getAcademyDetail(id);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "특정 학원의 과목 조회")
    @GetMapping("/{id}/subjects")
    public ResponseEntity<List<SubjectResponse>> getSubjects(@PathVariable Long id) {
        return ResponseEntity.ok(academyService.getAcademySubjects(id));
    }

    @Operation(summary = "특정 학원의 나이 조회")
    @GetMapping("/{id}/ages")
    public ResponseEntity<List<AgeResponse>> getAges(@PathVariable Long id) {
        return ResponseEntity.ok(academyService.getAcademyAges(id));
    }

    @Operation(summary = "학원 정보 수정")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateAcademy(
            @PathVariable Long id,
            @RequestBody AcademyUpdateRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        academyService.updateAcademy(id, dto, userDetails.getUserId());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "학원 운영 현황")
    @GetMapping("/{id}/stats")
    public DashboardStatsResponse getDashboardStats(@PathVariable Long id) {
        return academyService.getDashboardStats(id);
    }
}

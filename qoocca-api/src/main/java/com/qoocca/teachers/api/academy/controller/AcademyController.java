package com.qoocca.teachers.api.academy.controller;

import com.qoocca.teachers.api.academy.model.request.AcademyCreateRequest;
import com.qoocca.teachers.api.academy.model.request.AcademyResubmitRequest;
import com.qoocca.teachers.api.academy.model.request.AcademyUpdateRequest;
import com.qoocca.teachers.api.academy.model.response.AcademyResponse;
import com.qoocca.teachers.api.academy.model.response.AcademyImageUploadEnqueueResponse;
import com.qoocca.teachers.api.academy.model.response.AcademyImageUploadJobStatusResponse;
import com.qoocca.teachers.api.academy.model.response.DashboardStatsResponse;
import com.qoocca.teachers.api.academy.service.AcademyDashboardService;
import com.qoocca.teachers.api.academy.service.AcademyProfileService;
import com.qoocca.teachers.api.academy.service.AcademyRegistrationService;
import com.qoocca.teachers.api.age.model.AgeResponse;
import com.qoocca.teachers.api.subject.model.SubjectResponse;
import com.qoocca.teachers.auth.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "학원 API", description = "학원 등록/프로필/커리큘럼/대시보드 API")
@RestController
@RequestMapping("/api/academy")
@RequiredArgsConstructor
public class AcademyController {

    private final AcademyRegistrationService academyRegistrationService;
    private final AcademyProfileService academyProfileService;
    private final AcademyDashboardService academyDashboardService;

    @Operation(summary = "학원 등록", description = "신규 학원을 등록합니다.")
    @PostMapping(value = "/registrations", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> registerAcademy(
            @Valid @ModelAttribute AcademyCreateRequest req,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = academyRegistrationService.registerAcademy(req, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @Operation(summary = "학원 상세 조회", description = "학원 프로필 상세를 조회합니다.")
    @GetMapping("/{id}/profile")
    public ResponseEntity<AcademyResponse> getAcademyDetail(@PathVariable Long id) {
        return ResponseEntity.ok(academyProfileService.getAcademyDetail(id));
    }

    @Operation(summary = "학원 과목 목록 조회", description = "학원에서 운영하는 과목 목록을 조회합니다.")
    @GetMapping("/{id}/curriculum/subjects")
    public ResponseEntity<List<SubjectResponse>> getSubjects(@PathVariable Long id) {
        return ResponseEntity.ok(academyProfileService.getAcademySubjects(id));
    }

    @Operation(summary = "학원 연령대 목록 조회", description = "학원 대상 연령대 목록을 조회합니다.")
    @GetMapping("/{id}/curriculum/ages")
    public ResponseEntity<List<AgeResponse>> getAges(@PathVariable Long id) {
        return ResponseEntity.ok(academyProfileService.getAcademyAges(id));
    }

    /*================= 프로필 수정 ================= */

    @PatchMapping(
            value = "/{id}/profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> updateAcademyProfile(
            @PathVariable Long id,
            @ModelAttribute AcademyUpdateRequest req,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        academyProfileService.updateAcademy(id, req, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    /* ================= 이미지 업로드 ================= */

    @PostMapping(
            value = "/{id}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<AcademyImageUploadEnqueueResponse> uploadAcademyImages(
            @PathVariable Long id,
            @RequestPart("images") MultipartFile[] images,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AcademyImageUploadEnqueueResponse response =
                academyProfileService.enqueueAcademyImages(id, List.of(images), userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{id}/images/uploads/{jobId}")
    public ResponseEntity<AcademyImageUploadJobStatusResponse> getImageUploadJobStatus(
            @PathVariable Long id,
            @PathVariable String jobId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AcademyImageUploadJobStatusResponse response =
                academyProfileService.getImageUploadJobStatus(id, jobId, userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/{id}/files",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> uploadAcademyFiles(
            @PathVariable Long id,
            @RequestPart(value = "certificateFile", required = false) MultipartFile certificateFile,
            @RequestPart(value = "imageFiles", required = false) MultipartFile[] imageFiles,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<MultipartFile> images = imageFiles == null ? List.of() : List.of(imageFiles);
        academyProfileService.uploadAcademyFiles(id, certificateFile, images, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /* ================= 이미지 삭제 ================= */

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteAcademyImage(
            @PathVariable Long id,
            @PathVariable Long imageId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        academyProfileService.deleteAcademyImage(id, imageId, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "학원 재심사 요청", description = "반려된 학원 정보를 수정해 재심사 요청합니다. (신규 POST 엔드포인트)")
    @PostMapping(value = "/{id}/approval/resubmissions", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> resubmitAcademyV2(
            @PathVariable Long id,
            @Valid @ModelAttribute AcademyResubmitRequest req,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        academyProfileService.resubmitAcademy(id, req, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "학원 대시보드 통계 조회", description = "메인 대시보드 통계 데이터를 조회합니다.")
    @GetMapping("/{id}/dashboard/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(@PathVariable Long id) {
        return ResponseEntity.ok(academyDashboardService.getDashboardStats(id));
    }
}

package com.example.qoocca_be.classInfo.controller;

import com.example.qoocca_be.classInfo.model.ClassGetResponse;
import com.example.qoocca_be.classInfo.model.ClassPostRequest;
import com.example.qoocca_be.classInfo.model.ClassPostResponse;
import com.example.qoocca_be.classInfo.model.ClassSummaryResponse;
import com.example.qoocca_be.classInfo.service.ClassInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academy/{academyId}/class")
public class ClassInfoController {

    private final ClassInfoService classInfoService;

    /* =========================
     * 클래스 등록
     * ========================= */
    @PostMapping
    public ResponseEntity<ClassPostResponse> createClass(
            @PathVariable Long academyId,
            @RequestBody ClassPostRequest request
    ) {
        return ResponseEntity.ok(
                classInfoService.createClass(academyId, request)
        );
    }

    /* =========================
     * 클래스 목록 조회
     * ========================= */
    @GetMapping
    public ResponseEntity<List<ClassGetResponse>> getClasses(
            @PathVariable Long academyId
    ) {
        return ResponseEntity.ok(
                classInfoService.getClasses(academyId)
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<List<ClassSummaryResponse>> getAcademyClassSummaries(@PathVariable Long academyId) {
        List<ClassSummaryResponse> summaries = classInfoService.getAcademyDashboard(academyId);

        return ResponseEntity.ok(summaries);
    }
}

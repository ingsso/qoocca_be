package com.example.qoocca_be.classInfo.controller;

import com.example.qoocca_be.classInfo.model.ClassGetResponse;
import com.example.qoocca_be.classInfo.model.ClassPostRequest;
import com.example.qoocca_be.classInfo.model.ClassPostResponse;
import com.example.qoocca_be.classInfo.model.ClassSummaryResponse;
import com.example.qoocca_be.classInfo.model.ClassInfoStudentMoveRequest;

import com.example.qoocca_be.classInfo.service.ClassInfoService;
import com.example.qoocca_be.classInfo.service.ClassInfoStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academy/{academyId}/class")
public class ClassInfoController {

    private final ClassInfoStudentService classInfoStudentService;

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

    @PutMapping("/{classId}/student/{studentId}/move")
    public ResponseEntity<Void> moveStudent(
            @PathVariable Long academyId,
            @PathVariable Long classId,
            @PathVariable Long studentId,
            @RequestBody ClassInfoStudentMoveRequest request
    ) {
        classInfoStudentService.moveStudent(
                classId,
                studentId,
                request.getTargetClassId()
        );
        return ResponseEntity.ok().build();
    }





    @GetMapping("/summary")
    public ResponseEntity<List<ClassSummaryResponse>> getAcademyClassSummaries(@PathVariable Long academyId) {
        List<ClassSummaryResponse> summaries = classInfoService.getAcademyDashboard(academyId);

        return ResponseEntity.ok(summaries);
    }
}

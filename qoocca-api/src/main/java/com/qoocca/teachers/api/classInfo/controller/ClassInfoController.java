package com.qoocca.teachers.api.classInfo.controller;

import com.qoocca.teachers.api.classInfo.model.request.ClassCreateRequest;
import com.qoocca.teachers.api.classInfo.model.request.ClassStudentMoveRequest;
import com.qoocca.teachers.api.classInfo.model.response.ClassCreateResponse;
import com.qoocca.teachers.api.classInfo.model.response.ClassGetResponse;
import com.qoocca.teachers.api.classInfo.service.ClassInfoService;
import com.qoocca.teachers.api.classInfo.service.ClassInfoStudentService;
import com.qoocca.teachers.auth.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Class Info API", description = "Academy class management API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academy/{academyId}/class")
public class ClassInfoController {

    private final ClassInfoStudentService classInfoStudentService;
    private final ClassInfoService classInfoService;

    @Operation(summary = "Create class", description = "Creates a new class under an academy.")
    @PostMapping
    public ResponseEntity<ClassCreateResponse> createClass(
            @Parameter(description = "Academy ID") @PathVariable Long academyId,
            @Valid @RequestBody ClassCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(classInfoService.createClass(academyId, request, userDetails));
    }

    @Operation(summary = "Get classes", description = "Returns class list for an academy.")
    @GetMapping
    public ResponseEntity<List<ClassGetResponse>> getClasses(
            @Parameter(description = "Academy ID") @PathVariable Long academyId
    ) {
        return ResponseEntity.ok(classInfoService.getClasses(academyId));
    }

    @Operation(summary = "Move student", description = "Moves a student to another class in the same academy.")
    @PutMapping("/{classId}/student/{studentId}/move")
    public ResponseEntity<Void> moveStudent(
            @Parameter(description = "Academy ID") @PathVariable Long academyId,
            @Parameter(description = "Current class ID") @PathVariable Long classId,
            @Parameter(description = "Student ID") @PathVariable Long studentId,
            @Valid @RequestBody ClassStudentMoveRequest request
    ) {
        classInfoStudentService.moveStudent(academyId, classId, studentId, request.getTargetClassId());
        return ResponseEntity.ok().build();
    }
}
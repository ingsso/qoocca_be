package com.qoocca.teachers.api.academy.model.request;

import com.qoocca.teachers.api.parent.model.ParentCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AcademyStudentWithParentCreateRequest {

    @Valid
    @NotNull
    private AcademyStudentCreateRequest student;

    @Valid
    @NotNull
    private ParentCreateRequest parent;

    // Backward compatibility: single class target
    private Long classId;

    // Preferred: multi-class target
    private List<Long> classIds;
}

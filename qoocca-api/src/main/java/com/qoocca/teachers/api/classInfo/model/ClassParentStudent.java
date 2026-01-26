package com.qoocca.teachers.api.classInfo.model;

import com.qoocca.teachers.api.parent.model.ParentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ClassParentStudent {

    private Long studentId;
    private String studentName;
    private String studentPhone;
    private List<ParentResponse> parents;
}

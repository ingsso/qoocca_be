package com.example.qoocca_be.classInfo.model;

import com.example.qoocca_be.parent.model.ParentResponse;
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

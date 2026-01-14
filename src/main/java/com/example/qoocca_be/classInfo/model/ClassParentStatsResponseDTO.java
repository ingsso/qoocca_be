package com.example.qoocca_be.classInfo.model;

import lombok.Getter;

import java.util.List;

@Getter
public class ClassParentStatsResponseDTO {

    private Long classId;
    private String className;
    private List<ClassParentStudentDTO> students;

    public ClassParentStatsResponseDTO(Long classId, String className, List<ClassParentStudentDTO> students) {
        this.classId = classId;
        this.className = className;
        this.students = students;
    }
}

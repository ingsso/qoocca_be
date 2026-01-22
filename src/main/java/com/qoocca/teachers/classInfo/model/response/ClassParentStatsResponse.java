package com.qoocca.teachers.classInfo.model.response;

import com.qoocca.teachers.classInfo.model.ClassParentStudent;
import lombok.Getter;

import java.util.List;

@Getter
public class ClassParentStatsResponse {

    private Long classId;
    private String className;
    private List<ClassParentStudent> students;

    public ClassParentStatsResponse(Long classId, String className, List<ClassParentStudent> students) {
        this.classId = classId;
        this.className = className;
        this.students = students;
    }
}

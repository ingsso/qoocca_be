package com.qoocca.teachers.classInfo.model;

public interface ClassStatsProjection {

    Long getClassId();
    String getClassName();
    Long getTotalStudents();
    Long getWithdrawnStudents();
}

package com.example.qoocca_be.classInfo.model;

public interface ClassStatsProjection {

    Long getClassId();
    String getClassName();
    Long getTotalStudents();
    Long getWithdrawnStudents();
}

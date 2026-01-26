package com.qoocca.teachers.db.classInfo.model;

public interface ClassStatsProjection {
    Long getClassId();
    String getClassName();
    Long getTotalStudents();
    Long getWithdrawnStudents();
}

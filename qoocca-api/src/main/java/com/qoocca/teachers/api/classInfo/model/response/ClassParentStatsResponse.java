package com.qoocca.teachers.api.classInfo.model.response;

import com.qoocca.teachers.api.classInfo.model.ClassParentStudent;

import java.util.List;

public record ClassParentStatsResponse(Long classId, String className, List<ClassParentStudent> students) {

}

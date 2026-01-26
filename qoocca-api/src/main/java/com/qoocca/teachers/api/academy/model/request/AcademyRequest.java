package com.qoocca.teachers.api.academy.model.request;

import java.util.List;

public interface AcademyRequest {
    List<Long> getAgeIds();
    List<Long> getSubjects();
}
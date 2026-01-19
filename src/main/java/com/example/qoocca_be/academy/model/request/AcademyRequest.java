package com.example.qoocca_be.academy.model.request;

import java.util.List;

public interface AcademyRequest {
    List<Long> getAgeIds();
    List<Long> getSubjects();
}
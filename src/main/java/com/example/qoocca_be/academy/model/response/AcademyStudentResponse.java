package com.example.qoocca_be.academy.model.response;

import com.example.qoocca_be.student.entity.StudentEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AcademyStudentResponse {

    private Long studentId;
    private String studentName;

    public static AcademyStudentResponse from(StudentEntity student) {
        return AcademyStudentResponse.builder()
                .studentId(student.getStudentId())
                .studentName(student.getStudentName())
                .build();
    }
}
 
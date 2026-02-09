package com.qoocca.teachers.api.academy.model.response;

import com.qoocca.teachers.db.student.entity.StudentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademyStudentResponse {

    private Long studentId;
    private String studentName;
    private String studentPhone;

    public static AcademyStudentResponse from(StudentEntity student) {
        return AcademyStudentResponse.builder()
                .studentId(student.getStudentId())
                .studentName(student.getStudentName())
                .studentPhone(student.getStudentPhone())
                .build();
    }
}

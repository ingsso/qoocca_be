package com.qoocca.teachers.api.subject.model;

import com.qoocca.teachers.db.subject.entity.SubjectEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubjectResponse {
    private Long id;                   // 과목 고유 Id
    private String mainSubjectCode;    // 대분류 코드
    private String detailSubject;      // 과목명 (사용자에게 보여줄 값)

    public static SubjectResponse from(SubjectEntity entity) {
        return SubjectResponse.builder()
                .id(entity.getId())
                .mainSubjectCode(entity.getMainSubjectCode())
                .detailSubject(entity.getDetailSubject())
                .build();
    }
}

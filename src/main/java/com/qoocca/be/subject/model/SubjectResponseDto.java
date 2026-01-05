package com.qoocca.be.subject.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubjectResponseDto {
    private Long id;                   // 과목 고유 Id
    private String mainSubjectCode;    // 대분류 코드
    private String detailSubject;      // 과목명 (사용자에게 보여줄 값)
}

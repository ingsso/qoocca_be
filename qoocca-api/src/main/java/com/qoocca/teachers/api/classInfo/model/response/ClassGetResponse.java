package com.qoocca.teachers.api.classInfo.model.response;

import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassGetResponse {

    private Long classId;
    private String className;
    private LocalTime startTime;
    private LocalTime endTime;

    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;

    private Long price;
    private String ageCode;
    private String subjectName;

    public static ClassGetResponse fromEntity(ClassInfoEntity entity) {
        return ClassGetResponse.builder()
                .classId(entity.getClassId())
                .className(entity.getClassName())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .monday(entity.isMonday())
                .tuesday(entity.isTuesday())
                .wednesday(entity.isWednesday())
                .thursday(entity.isThursday())
                .friday(entity.isFriday())
                .saturday(entity.isSaturday())
                .sunday(entity.isSunday())
                .price(entity.getPrice())
                .ageCode(entity.getAge().getAgeCode())
                .subjectName(entity.getSubject().getDetailSubject())
                .build();
    }
}

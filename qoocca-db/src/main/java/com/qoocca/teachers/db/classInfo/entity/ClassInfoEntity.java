package com.qoocca.teachers.db.classInfo.entity;

import com.qoocca.teachers.db.academy.entity.AcademyEntity;
import com.qoocca.teachers.db.age.entity.AgeEntity;
import com.qoocca.teachers.db.subject.entity.SubjectEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;

@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "class_info", indexes = {
        @Index(name = "idx_class_info_academy", columnList = "academy_id")
})
public class ClassInfoEntity {

    /* =========================
     * PK
     * ========================= */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_id")
    private Long classId;

    /* =========================
     * 기본 정보
     * ========================= */
    @Column(name = "class_name")
    private String className;

    @Column(name = "s_time")
    private LocalTime startTime;

    @Column(name = "e_time")
    private LocalTime endTime;

    /* =========================
     * 요일 정보
     * ========================= */
    @Column(name = "is_monday")
    private boolean monday;

    @Column(name = "is_tuesday")
    private boolean tuesday;

    @Column(name = "is_wednesday")
    private boolean wednesday;

    @Column(name = "is_thursday")
    private boolean thursday;

    @Column(name = "is_friday")
    private boolean friday;

    @Column(name = "is_saturday")
    private boolean saturday;

    @Column(name = "is_sunday")
    private boolean sunday;

    /* =========================
     * 기타
     * ========================= */
    @Column(name = "price")
    private Long price;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id")
    @ToString.Exclude
    private AcademyEntity academy;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "age_id", nullable = false)
    @ToString.Exclude
    private AgeEntity age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    @ToString.Exclude
    private SubjectEntity subject;

    public static ClassInfoEntity createClass(
            String className,
            LocalTime startTime,
            LocalTime endTime,
            Long price,
            AcademyEntity academy,
            AgeEntity age,
            SubjectEntity subject,
            boolean monday, boolean tuesday, boolean wednesday,
            boolean thursday, boolean friday, boolean saturday, boolean sunday
    ) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("수업 종료 시간은 시작 시간보다 빨라야 합니다.");
        }

        return ClassInfoEntity.builder()
                .className(className)
                .startTime(startTime)
                .endTime(endTime)
                .price(price)
                .academy(academy)
                .age(age)
                .subject(subject)
                .monday(monday).tuesday(tuesday).wednesday(wednesday)
                .thursday(thursday).friday(friday).saturday(saturday).sunday(sunday)
                .build();
    }
}

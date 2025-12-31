package com.example.qoocca_be.classInfo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;

@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "class_info")
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
    private String price;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    /* =========================
     * 연관관계 (FK)
     * ========================= */
   /* @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id")
    @ToString.Exclude
    private AcademyEntity academy;

    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "age_id", nullable = false)
    @ToString.Exclude
    private AgeEntity age;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    @ToString.Exclude
    private SubjectEntity subject;*/
}

package com.example.qoocca_be.student.entity;

import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "student")
public class StudentEntity {

    /* =========================
     * PK
     * ========================= */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;

    /* =========================
     * 기본 정보
     * ========================= */
    @Column(name = "student_name")
    private String studentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_status")
    private StudentStatus studentStatus;

    /* =========================
     * 생성 / 수정일
     * ========================= */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    @ToString.Exclude
    private ClassInfoEntity classInfo;

    public enum StudentStatus {
        ACTIVE,
        INACTIVE,
        WITHDRAWN
    }
}

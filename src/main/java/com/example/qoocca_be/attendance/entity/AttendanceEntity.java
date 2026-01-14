package com.example.qoocca_be.attendance.entity;

import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import com.example.qoocca_be.student.entity.StudentEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "attendance")
public class AttendanceEntity {

    /* =========================
     * PK
     * ========================= */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long attendanceId;

    /* =========================
     * 출결 정보
     * ========================= */
    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    @Column(name = "checkin")
    private LocalTime checkIn;

    @Column(name = "checkout")
    private LocalTime checkOut;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AttendanceStatus status;

    /* =========================
     * 생성 / 수정일
     * ========================= */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* =========================
     * 연관관계 (FK)
     * ========================= */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @ToString.Exclude
    private StudentEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    @ToString.Exclude
    private ClassInfoEntity classInfo;

    /* =========================
     * enum 정의
     * ========================= */
    public enum AttendanceStatus {
        PRESENT,    // 출석
        ABSENT,     // 결석
        LATE,       // 지각
        EARLY_LEAVE // 조퇴
    }
}


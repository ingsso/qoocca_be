package com.qoocca.teachers.attendance.entity;

import com.qoocca.teachers.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.student.entity.StudentEntity;
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
    @Setter
    private LocalTime checkOut;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Setter
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

    public void calculateStatus(LocalTime startTime) {
        // 필요 시 startTime.plusMinutes(5) 처럼 유예 시간을 둘 수도 있음
        if (this.checkIn.isAfter(startTime)) {
            this.status = AttendanceStatus.LATE;
        } else {
            this.status = AttendanceStatus.PRESENT;
        }
    }

    public void processCheckOut(LocalTime endTime) {
        this.checkOut = LocalTime.now();

        // 조퇴 판별: 수업 종료 시간보다 일찍 나갔을 경우
        if (this.checkOut.isBefore(endTime)) {
            this.status = AttendanceStatus.EARLY_LEAVE;
        }
    }
}


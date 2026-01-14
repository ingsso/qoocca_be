package com.example.qoocca_be.receipt.entity;


import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import com.example.qoocca_be.student.entity.StudentEntity;
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
@Table(name = "teachers_receipt")
public class ReceiptEntity {

    /* =========================
     * PK
     * ========================= */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id")
    private Long receiptId;

    /* =========================
     * 영수증 정보
     * ========================= */
    @Column(name = "amount")
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "receipt_status")
    private ReceiptStatus receiptStatus;

    @Column(name = "receipt_date")
    private LocalDateTime receiptDate;

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
    public enum ReceiptStatus {
        PAID,        // 수납 완료
        ISSUED,     // 영수증발행
        CANCELLED  // 취소
    }
}

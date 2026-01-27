package com.qoocca.teachers.db.receipt.entity;

import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.student.entity.StudentEntity;
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
@Table(name = "receipt")
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
    @Getter
    public enum ReceiptStatus {
        NO_STUDENTS("인원 없음"),
        BEFORE_REQUEST("요청 전"),
        CANCELLED("수납 취소"),
        ISSUED("결제 대기"),
        PAID("결제 완료");

        private final String label;

        ReceiptStatus(String label) {
            this.label = label;
        }
    }

    public static ReceiptEntity createReceipt(StudentEntity student, ClassInfoEntity classInfo, Long amount, LocalDateTime date, ReceiptStatus status) {
        return ReceiptEntity.builder()
                .student(student)
                .classInfo(classInfo)
                .amount(amount)
                .receiptDate(date != null ? date : LocalDateTime.now())
                .receiptStatus(status != null ? status : ReceiptStatus.ISSUED)
                .build();
    }
}

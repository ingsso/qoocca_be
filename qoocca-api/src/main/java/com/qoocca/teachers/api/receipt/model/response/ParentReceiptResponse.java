package com.qoocca.teachers.api.receipt.model.response;

import com.qoocca.teachers.db.receipt.entity.ReceiptEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentReceiptResponse {
    private Long receiptId;
    private Long studentId;
    private String studentName;
    private Long classId;
    private String className;
    private String academyName;
    private Long amount;
    private LocalDateTime receiptDate;
    private String receiptStatus;

    public static ParentReceiptResponse fromEntity(ReceiptEntity entity) {
        return ParentReceiptResponse.builder()
                .receiptId(entity.getReceiptId())
                .studentId(entity.getStudent().getStudentId())
                .studentName(entity.getStudent().getStudentName())
                .classId(entity.getClassInfo().getClassId())
                .className(entity.getClassInfo().getClassName())
                .academyName(entity.getClassInfo().getAcademy().getName())
                .amount(entity.getAmount())
                .receiptDate(entity.getReceiptDate())
                .receiptStatus(entity.getReceiptStatus().name())
                .build();
    }
}

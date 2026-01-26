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
public class ReceiptUpdateResponse {
    private Long receiptId;
    private Long studentId;
    private LocalDateTime receiptDate;
    private String receiptStatus;

    public static ReceiptUpdateResponse fromEntity(ReceiptEntity entity) {
        return ReceiptUpdateResponse.builder()
                .receiptId(entity.getReceiptId())
                .studentId(entity.getStudent().getStudentId())
                .receiptDate(entity.getReceiptDate())
                .receiptStatus(entity.getReceiptStatus().name())
                .build();
    }
}


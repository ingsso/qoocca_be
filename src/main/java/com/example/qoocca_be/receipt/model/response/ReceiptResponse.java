package com.example.qoocca_be.receipt.model.response;

import com.example.qoocca_be.receipt.entity.ReceiptEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {
    private Long receiptId;
    private Long studentId;
    private LocalDateTime receiptDate;
    private String receiptStatus;

    public static ReceiptResponse fromEntity(ReceiptEntity entity) {
        return ReceiptResponse.builder()
                .receiptId(entity.getReceiptId())
                .studentId(entity.getStudent().getStudentId())
                .receiptDate(entity.getReceiptDate())
                .receiptStatus(entity.getReceiptStatus().name())
                .build();
    }
}


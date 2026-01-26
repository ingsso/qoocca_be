package com.qoocca.teachers.api.receipt.model;

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
public class ReceiptCreateRequest {
    private Long amount;
    private Long classId;
    private LocalDateTime receiptDate;
    private ReceiptEntity.ReceiptStatus receiptStatus;
}

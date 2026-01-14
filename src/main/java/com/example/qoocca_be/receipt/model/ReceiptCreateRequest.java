package com.example.qoocca_be.receipt.model;

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
public class ReceiptCreateRequest {
    private Long amount;
    private LocalDateTime receiptDate;
    private ReceiptEntity.ReceiptStatus receiptStatus;
}

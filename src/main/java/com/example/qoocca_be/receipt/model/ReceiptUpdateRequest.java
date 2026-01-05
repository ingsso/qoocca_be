package com.example.qoocca_be.receipt.model;

import com.example.qoocca_be.receipt.entity.ReceiptEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptUpdateRequest {
    private ReceiptEntity.ReceiptStatus receiptStatus;
}

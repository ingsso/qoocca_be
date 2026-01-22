package com.qoocca.teachers.receipt.model;

import com.qoocca.teachers.receipt.entity.ReceiptEntity;
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

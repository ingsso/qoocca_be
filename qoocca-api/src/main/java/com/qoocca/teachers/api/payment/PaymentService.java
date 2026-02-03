package com.qoocca.teachers.api.payment;

import com.qoocca.teachers.api.receipt.service.ReceiptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final ReceiptService receiptService;

    public PaymentService(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @Transactional
    public void completePayment(Long receiptId, Long parentId) {
        receiptService.payReceipt(receiptId, parentId);
    }
}

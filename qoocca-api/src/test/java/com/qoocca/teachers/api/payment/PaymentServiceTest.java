package com.qoocca.teachers.api.payment;

import com.qoocca.teachers.api.receipt.service.ReceiptService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private ReceiptService receiptService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void completePaymentDelegatesToReceiptService() {
        Long receiptId = 10L;
        Long parentId = 99L;

        paymentService.completePayment(receiptId, parentId);

        verify(receiptService).payReceipt(receiptId, parentId);
    }
}

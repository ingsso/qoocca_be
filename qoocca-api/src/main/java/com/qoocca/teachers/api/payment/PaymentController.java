package com.qoocca.teachers.api.payment;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/complete")
    public String completePayment(@RequestParam Long receiptId,
                                  @RequestParam Long parentId) {
        paymentService.completePayment(receiptId, parentId);
        return "Payment completed";
    }
}

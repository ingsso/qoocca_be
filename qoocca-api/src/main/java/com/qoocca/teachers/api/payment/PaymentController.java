package com.qoocca.teachers.api.payment;

import com.qoocca.teachers.auth.security.ParentUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment API", description = "결제 처리 API")
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "결제 완료 처리", description = "결제 완료 상태로 처리합니다")
    @PostMapping("/complete")
    public String completePayment(@RequestParam Long receiptId,
                                  @Parameter(hidden = true) @AuthenticationPrincipal ParentUserDetails parentUserDetails) {
        paymentService.completePayment(receiptId, parentUserDetails.getParentId());
        return "Payment completed";
    }
}

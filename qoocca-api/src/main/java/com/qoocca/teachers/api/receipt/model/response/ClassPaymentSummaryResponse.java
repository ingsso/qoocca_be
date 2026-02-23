package com.qoocca.teachers.api.receipt.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ClassPaymentSummaryResponse {
    private Long classId;
    private String className;
    private long beforeRequest;    // 결제 요청 전 (데이터가 없는 학생 수)
    private long paymentPending;   // 결제 대기 (ISSUED 상태인 데이터 수)
    private long paymentCompleted; // 수납 완료 (PAID 상태인 데이터 수)
    private List<StudentPaymentDetail> students; // 학생 상세 리스트

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentPaymentDetail {
        private Long studentId;
        private String studentName;
        private Long amount;
        private String status;
        @JsonProperty("cardRegistered")
        @JsonAlias({"isCardRegistered"})
        private boolean isCardRegistered;
    }
}

package com.example.qoocca_be.receipt.service;

import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import com.example.qoocca_be.classInfo.entity.ClassInfoStudentEntity;
import com.example.qoocca_be.classInfo.entity.StudentStatus;
import com.example.qoocca_be.classInfo.repository.ClassInfoRepository;
import com.example.qoocca_be.classInfo.repository.ClassInfoStudentRepository;
import com.example.qoocca_be.receipt.entity.ReceiptEntity;
import com.example.qoocca_be.receipt.model.*;
import com.example.qoocca_be.receipt.repository.ReceiptRepository;
import com.example.qoocca_be.student.entity.StudentEntity;
import com.example.qoocca_be.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final StudentRepository studentRepository;
    private final ClassInfoRepository classInfoRepository;
    private final ClassInfoStudentRepository classInfoStudentRepository;

    /* =========================
     * POST: 수납 생성 (한 달에 1회 제한 로직 추가)
     * ========================= */
    public ReceiptCreateResponse createReceipt(Long studentId, ReceiptCreateRequest request) {
        // 1. 학생 존재 확인
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));

        ClassInfoEntity classInfo = classInfoRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("클래스 정보를 찾을 수 없습니다."));

        // 2. 중복 수납 체크 (해당 월에 이미 데이터가 있는지)
        // 만약 request에 날짜가 없으면 현재 시간 기준으로 체크
        LocalDateTime targetDate = (request.getReceiptDate() != null) ? request.getReceiptDate() : LocalDateTime.now();
        YearMonth targetMonth = YearMonth.from(targetDate);

        LocalDateTime start = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime end = targetMonth.atEndOfMonth().atTime(23, 59, 59);

        boolean isAlreadyPaid = receiptRepository.existsByStudent_StudentIdAndClassInfo_ClassIdAndReceiptDateBetween(
                studentId, request.getClassId(), start, end);

        if (isAlreadyPaid) {
            throw new RuntimeException(targetMonth.getMonthValue() + "월 수납 기록이 이미 존재합니다.");
        }

        Long amount = request.getAmount();

        // 3. 수납 저장
        ReceiptEntity receipt = ReceiptEntity.builder()
                .student(student)
                .classInfo(classInfo)
                .amount(amount)
                .receiptDate(targetDate)
                .receiptStatus(request.getReceiptStatus() != null ? request.getReceiptStatus() : ReceiptEntity.ReceiptStatus.ISSUED)
                .build();

        ReceiptEntity saved = receiptRepository.save(receipt);
        return ReceiptCreateResponse.fromEntity(saved);
    }

    /* =========================
     * GET: 학생별 전체 조회
     * ========================= */
    @Transactional(readOnly = true)
    public List<ReceiptResponse> getReceiptsByStudent(Long studentId) {
        return receiptRepository.findByStudent_StudentId(studentId)
                .stream()
                .map(ReceiptResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /* =========================
     * GET: 달별 조회 (YearMonth 활용으로 가독성 개선)
     * ========================= */
    @Transactional(readOnly = true)
    public List<ReceiptResponse> getReceiptsByStudentAndMonth(Long studentId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

        return receiptRepository.findByStudent_StudentIdAndReceiptDateBetween(studentId, start, end)
                .stream()
                .map(ReceiptResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /* =========================
     * PUT: 상태 변경 (취소 등)
     * ========================= */
    public ReceiptUpdateResponse updateReceiptStatus(Long studentId, Long receiptId, ReceiptUpdateRequest request) {
        ReceiptEntity receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("영수증을 찾을 수 없습니다."));

        if (!receipt.getStudent().getStudentId().equals(studentId)) {
            throw new RuntimeException("해당 학생의 영수증이 아닙니다.");
        }

        if (request.getReceiptStatus() != null) {
            receipt.setReceiptStatus(request.getReceiptStatus());
        }

        return ReceiptUpdateResponse.fromEntity(receipt);
    }

    /* =========================
     * GET: 학원별 클래스 수납 요약 조회 (PaymentPage 테이블용)
     * ========================= */
    @Transactional(readOnly = true)
    public List<ClassPaymentSummaryResponse> getClassReceiptSummary(Long academyId, int year, int month) {
        // 1. 해당 학원의 모든 클래스 조회
        List<ClassInfoEntity> classList = classInfoRepository.findByAcademy_Id(academyId);

        // 2. 조회 기간 설정 (해당 월의 시작과 끝)
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

        return classList.stream().map(cls -> {
            // 3. 클래스별 전체 학생 수 조회
            List<ClassInfoStudentEntity> enrollments = classInfoStudentRepository.findByClassInfo_ClassId(cls.getClassId());

            // 4. 해당 월의 이 클래스 수납 내역 전체 조회
            List<ReceiptEntity> receipts = receiptRepository.findByClassInfo_ClassIdAndReceiptDateBetween(
                    cls.getClassId(), start, end);

            // 5. 상태별 카운트 계산
            List<ClassPaymentSummaryResponse.StudentPaymentDetail> studentDetails = enrollments.stream().map(enroll -> {
                // 해당 학생의 수납 레코드가 있는지 확인
                Optional<ReceiptEntity> receipt = receipts.stream()
                        .filter(r -> r.getStudent().getStudentId().equals(enroll.getStudent().getStudentId()))
                        .findFirst();

                String status = receipt.map(r -> r.getReceiptStatus().name())
                        .orElse("BEFORE_REQUEST");

                return ClassPaymentSummaryResponse.StudentPaymentDetail.builder()
                        .studentId(enroll.getStudent().getStudentId())
                        .studentName(enroll.getStudent().getStudentName())
                        .amount(Long.valueOf(cls.getPrice())) // price -> 추후 Long으로 바꿀 수 있으면 바꾸기
                        .status(status)
                        .build();
            }).collect(Collectors.toList());

            // 5. 집계 계산 (상태별 카운트)
            long completed = studentDetails.stream().filter(s -> s.getStatus().equals("PAID")).count();
            long pending = studentDetails.stream().filter(s -> s.getStatus().equals("ISSUED")).count();
            long before = studentDetails.size() - (completed + pending);

            return ClassPaymentSummaryResponse.builder()
                    .classId(cls.getClassId())
                    .className(cls.getClassName())
                    .beforeRequest(before)
                    .paymentPending(pending)
                    .paymentCompleted(completed)
                    .students(studentDetails) // 상세 명단 포함
                    .build();
        }).collect(Collectors.toList());
    }
}
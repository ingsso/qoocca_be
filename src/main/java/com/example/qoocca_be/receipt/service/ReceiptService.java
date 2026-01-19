package com.example.qoocca_be.receipt.service;

import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import com.example.qoocca_be.classInfo.entity.ClassInfoStudentEntity;
import com.example.qoocca_be.classInfo.entity.StudentStatus;
import com.example.qoocca_be.classInfo.repository.ClassInfoRepository;
import com.example.qoocca_be.classInfo.repository.ClassInfoStudentRepository;
import com.example.qoocca_be.receipt.entity.ReceiptEntity;
import com.example.qoocca_be.receipt.model.*;
import com.example.qoocca_be.receipt.model.response.*;
import com.example.qoocca_be.receipt.repository.ReceiptRepository;
import com.example.qoocca_be.student.entity.StudentEntity;
import com.example.qoocca_be.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
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


        if (isAlreadyProcessedInMonth(studentId, request.getClassId(), request.getReceiptDate())) {
            throw new IllegalStateException("해당 월의 수납 기록이 이미 존재합니다.");
        }

        ReceiptEntity receipt = ReceiptEntity.createReceipt(
                student, classInfo, request.getAmount(), request.getReceiptDate(), request.getReceiptStatus());

        return ReceiptCreateResponse.fromEntity(receiptRepository.save(receipt));
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
        // [공통 데이터 준비]
        ReceiptDataContainer data = prepareReceiptData(academyId, year, month);

        return data.classList.stream().map(cls -> {
            List<ClassInfoStudentEntity> enrollments = data.enrollmentsByClass.getOrDefault(cls.getClassId(), List.of());

            List<ClassPaymentSummaryResponse.StudentPaymentDetail> studentDetails = enrollments.stream().map(enroll -> {
                ReceiptEntity receipt = data.receiptMap.get(cls.getClassId() + "-" + enroll.getStudent().getStudentId());
                String status = (receipt != null) ? receipt.getReceiptStatus().name() : "BEFORE_REQUEST";

                return ClassPaymentSummaryResponse.StudentPaymentDetail.builder()
                        .studentId(enroll.getStudent().getStudentId())
                        .studentName(enroll.getStudent().getStudentName())
                        .amount(cls.getPrice())
                        .status(status)
                        .build();
            }).toList();

            long completed = studentDetails.stream().filter(s -> s.getStatus().equals("PAID")).count();
            long pending = studentDetails.stream().filter(s -> s.getStatus().equals("ISSUED")).count();
            long before = studentDetails.size() - (completed + pending);

            return ClassPaymentSummaryResponse.builder()
                    .classId(cls.getClassId())
                    .className(cls.getClassName())
                    .beforeRequest(before)
                    .paymentPending(pending)
                    .paymentCompleted(completed)
                    .students(studentDetails)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DashboardMainSummaryResponse> getDashboardMainSummary(Long academyId, int year, int month) {
        ReceiptDataContainer data = prepareReceiptData(academyId, year, month);

        return data.classList.stream().map(cls -> {
            List<ClassInfoStudentEntity> enrollments = data.enrollmentsByClass.getOrDefault(cls.getClassId(), List.of());

            ReceiptEntity.ReceiptStatus repStatus = calculateRepresentativeStatus(cls.getClassId(), enrollments, data.receiptMap);

            return DashboardMainSummaryResponse.builder()
                    .className(cls.getClassName())
                    .classTime(cls.getStartTime() + "~" + cls.getEndTime())
                    .status(repStatus.name())
                    .statusLabel(repStatus.getLabel())
                    .totalAmount(cls.getPrice() * enrollments.size())
                    .build();
        }).collect(Collectors.toList());
    }

    private boolean isAlreadyProcessedInMonth(Long studentId, Long classId, LocalDateTime date) {
        YearMonth ym = YearMonth.from(date != null ? date : LocalDateTime.now());
        return receiptRepository.existsByStudent_StudentIdAndClassInfo_ClassIdAndReceiptDateBetween(
                studentId, classId, ym.atDay(1).atStartOfDay(), ym.atEndOfMonth().atTime(23, 59, 59));
    }

    private ReceiptDataContainer prepareReceiptData(Long academyId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

        List<ClassInfoEntity> classList = classInfoRepository.findByAcademy_Id(academyId);
        List<ClassInfoStudentEntity> allEnrollments = classInfoStudentRepository.findAllByAcademyAndStatus(academyId, StudentStatus.ENROLLED);
        List<ReceiptEntity> allReceipts = receiptRepository.findAllByAcademyAndDateBetween(academyId, start, end);

        Map<Long, List<ClassInfoStudentEntity>> enrollmentsByClass = allEnrollments.stream()
                .collect(Collectors.groupingBy(e -> e.getClassInfo().getClassId()));

        Map<String, ReceiptEntity> receiptMap = allReceipts.stream()
                .collect(Collectors.toMap(
                        r -> r.getClassInfo().getClassId() + "-" + r.getStudent().getStudentId(),
                        r -> r,
                        (existing, replacement) -> existing
                ));

        return new ReceiptDataContainer(classList, enrollmentsByClass, receiptMap);
    }

    private record ReceiptDataContainer(
            List<ClassInfoEntity> classList,
            Map<Long, List<ClassInfoStudentEntity>> enrollmentsByClass,
            Map<String, ReceiptEntity> receiptMap
    ) {}

    private ReceiptEntity.ReceiptStatus calculateRepresentativeStatus(Long classId, List<ClassInfoStudentEntity> enrollments, Map<String, ReceiptEntity> receiptMap) {
        if (enrollments.isEmpty()) return ReceiptEntity.ReceiptStatus.PAID;

        boolean hasIssued = false;
        for (ClassInfoStudentEntity enroll : enrollments) {
            ReceiptEntity r = receiptMap.get(classId + "-" + enroll.getStudent().getStudentId());
            if (r == null) return ReceiptEntity.ReceiptStatus.BEFORE_REQUEST; // 하나라도 요청 전이면 전체 '요청 전'
            if (r.getReceiptStatus() == ReceiptEntity.ReceiptStatus.ISSUED) hasIssued = true;
        }
        return hasIssued ? ReceiptEntity.ReceiptStatus.ISSUED : ReceiptEntity.ReceiptStatus.PAID;
    }
}
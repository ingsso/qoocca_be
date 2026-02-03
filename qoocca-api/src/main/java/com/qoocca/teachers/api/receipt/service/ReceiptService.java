package com.qoocca.teachers.api.receipt.service;

import com.qoocca.teachers.api.receipt.model.ReceiptCreateRequest;
import com.qoocca.teachers.api.receipt.model.ReceiptUpdateRequest;
import com.qoocca.teachers.api.receipt.model.response.*;
import com.qoocca.teachers.api.global.service.FcmPushService;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoStudentEntity;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoRepository;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoStudentRepository;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.receipt.entity.ReceiptEntity;
import com.qoocca.teachers.api.receipt.model.*;
import com.qoocca.teachers.api.receipt.model.response.*;
import com.qoocca.teachers.db.receipt.repository.ReceiptRepository;
import com.qoocca.teachers.db.student.entity.StudentEntity;
import com.qoocca.teachers.db.student.entity.StudentParentEntity;
import com.qoocca.teachers.db.student.repository.StudentParentRepository;
import com.qoocca.teachers.db.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final StudentRepository studentRepository;
    private final ClassInfoRepository classInfoRepository;
    private final ClassInfoStudentRepository classInfoStudentRepository;
    private final StudentParentRepository studentParentRepository;
    private final FcmPushService fcmPushService;

    /* =========================
     * POST: 수납 생성 (한 달에 1회 제한 로직 추가)
     * ========================= */
    public ReceiptCreateResponse createReceipt(Long studentId, ReceiptCreateRequest request) {
        // 1. 학생 존재 확인
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        ClassInfoEntity classInfo = classInfoRepository.findById(request.getClassId())
                .orElseThrow(() -> new CustomException(ErrorCode.CLASS_NOT_FOUND));

        // 카드 등록 여부 체크
        boolean hasCard = studentParentRepository.findByStudent_StudentId(studentId).stream()
                .anyMatch(sp ->
                        Boolean.TRUE.equals(sp.getParent().getCardState())
                                || (sp.getParent().getCardNum() != null && !sp.getParent().getCardNum().isBlank())
                );

        if (!hasCard) {
            throw new CustomException(ErrorCode.PAYMENT_METHOD_NOT_FOUND);
        }


        if (isAlreadyProcessedInMonth(studentId, request.getClassId(), request.getReceiptDate())) {
            throw new CustomException(ErrorCode.DUPLICATE_RECEIPT_IN_MONTH);
        }

        ReceiptEntity receipt = ReceiptEntity.createReceipt(
                student, classInfo, request.getAmount(), request.getReceiptDate(), ReceiptEntity.ReceiptStatus.ISSUED);

        ReceiptEntity saved = receiptRepository.save(receipt);

        List<StudentParentEntity> parents = studentParentRepository.findByStudent_StudentId(studentId);
        String title = "결제 요청이 도착했어요";
        String body = String.format("%s 학생의 %s 수업 수강료 %,d원 결제를 진행해 주세요.",
                student.getStudentName(), classInfo.getClassName(), request.getAmount());
        for (StudentParentEntity sp : parents) {
            if (sp.getParent() != null && sp.getParent().getParentId() != null) {
                fcmPushService.sendPushToUser(sp.getParent().getParentId(), saved.getReceiptId(), title, body);
            }
        }

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
                .orElseThrow(() -> new CustomException(ErrorCode.RECEIPT_NOT_FOUND));

        if (!receipt.getStudent().getStudentId().equals(studentId)) {
            throw new CustomException(ErrorCode.RECEIPT_ACCESS_DENIED);
        }

        if (request.getReceiptStatus() != null) {
            receipt.setReceiptStatus(request.getReceiptStatus());
        }

        return ReceiptUpdateResponse.fromEntity(receipt);
    }

    public ReceiptUpdateResponse payReceipt(Long receiptId, Long parentId) {
        return changeReceiptStatusForParent(receiptId, parentId, ReceiptEntity.ReceiptStatus.PAID);
    }

    public ReceiptUpdateResponse cancelReceipt(Long receiptId, Long parentId) {
        return changeReceiptStatusForParent(receiptId, parentId, ReceiptEntity.ReceiptStatus.CANCELLED);
    }

    private ReceiptUpdateResponse changeReceiptStatusForParent(
            Long receiptId,
            Long parentId,
            ReceiptEntity.ReceiptStatus targetStatus
    ) {
        ReceiptEntity receipt = getReceiptForParentAction(receiptId, parentId);
        receipt.setReceiptStatus(targetStatus);
        return ReceiptUpdateResponse.fromEntity(receipt);
    }

    @Transactional(readOnly = true)
    public ParentReceiptResponse getReceiptDetailForParent(Long receiptId, Long parentId) {
        ReceiptEntity receipt = getReceiptForParentAccess(receiptId, parentId);
        return ParentReceiptResponse.fromEntity(receipt);
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
                        .isCardRegistered(data.cardStatusMap().getOrDefault(enroll.getStudent().getStudentId(), false))
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

        List<Long> studentIds = allEnrollments.stream()
                .map(e -> e.getStudent().getStudentId())
                .distinct()
                .toList();

        Map<Long, Boolean> cardStatusMap;
        if (studentIds.isEmpty()) {
            cardStatusMap = Map.of();
        } else {
            List<StudentParentEntity> studentParents = studentParentRepository.findAllByStudentIdsWithParent(studentIds);
            Map<Long, List<StudentParentEntity>> parentsByStudent = studentParents.stream()
                    .collect(Collectors.groupingBy(sp -> sp.getStudent().getStudentId()));

            cardStatusMap = studentIds.stream().collect(Collectors.toMap(
                    id -> id,
                    id -> parentsByStudent.getOrDefault(id, List.of()).stream()
                            .anyMatch(sp ->
                                    Boolean.TRUE.equals(sp.getParent().getCardState())
                                            || (sp.getParent().getCardNum() != null && !sp.getParent().getCardNum().isBlank())
                            )
            ));
        }

        return new ReceiptDataContainer(classList, enrollmentsByClass, receiptMap, cardStatusMap);
    }

    private record ReceiptDataContainer(
            List<ClassInfoEntity> classList,
            Map<Long, List<ClassInfoStudentEntity>> enrollmentsByClass,
            Map<String, ReceiptEntity> receiptMap,
            Map<Long, Boolean> cardStatusMap
    ) {}

    private ReceiptEntity.ReceiptStatus calculateRepresentativeStatus(Long classId, List<ClassInfoStudentEntity> enrollments, Map<String, ReceiptEntity> receiptMap) {
        if (enrollments.isEmpty()) return ReceiptEntity.ReceiptStatus.NO_STUDENTS;

        boolean hasIssued = false;
        for (ClassInfoStudentEntity enroll : enrollments) {
            ReceiptEntity r = receiptMap.get(classId + "-" + enroll.getStudent().getStudentId());
            if (r == null) return ReceiptEntity.ReceiptStatus.BEFORE_REQUEST; // 하나라도 요청 전이면 전체 '요청 전'
            if (r.getReceiptStatus() == ReceiptEntity.ReceiptStatus.ISSUED) hasIssued = true;
        }
        return hasIssued ? ReceiptEntity.ReceiptStatus.ISSUED : ReceiptEntity.ReceiptStatus.PAID;
    }

    private ReceiptEntity getReceiptForParentAction(Long receiptId, Long parentId) {
        ReceiptEntity receipt = getReceiptForParentAccess(receiptId, parentId);

        if (receipt.getReceiptStatus() != ReceiptEntity.ReceiptStatus.ISSUED) {
            throw new CustomException(ErrorCode.INVALID_RECEIPT_STATUS);
        }

        return receipt;
    }

    private ReceiptEntity getReceiptForParentAccess(Long receiptId, Long parentId) {
        ReceiptEntity receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECEIPT_NOT_FOUND));

        boolean isParent = studentParentRepository
                .findByStudent_StudentIdAndParent_ParentId(receipt.getStudent().getStudentId(), parentId)
                .isPresent();
        if (!isParent) {
            throw new CustomException(ErrorCode.STUDENT_PARENT_RELATION_NOT_FOUND);
        }

        return receipt;
    }

    @Transactional(readOnly = true)
    public List<ParentReceiptResponse> getPendingReceiptsByParent(Long parentId) {
        return receiptRepository.findAllByParentAndStatus(parentId, ReceiptEntity.ReceiptStatus.ISSUED)
                .stream()
                .map(ParentReceiptResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private boolean isAlreadyProcessedInMonth(Long studentId, Long classId, LocalDateTime date) {
        YearMonth ym = YearMonth.from(date != null ? date : LocalDateTime.now());
        return receiptRepository.existsByStudent_StudentIdAndClassInfo_ClassIdAndReceiptDateBetween(
                studentId, classId, ym.atDay(1).atStartOfDay(), ym.atEndOfMonth().atTime(23, 59, 59));
    }
}


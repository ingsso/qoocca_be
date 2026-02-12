package com.qoocca.teachers.api.receipt.service;

import com.qoocca.teachers.api.global.service.FcmPushService;
import com.qoocca.teachers.api.receipt.model.ReceiptCreateRequest;
import com.qoocca.teachers.api.receipt.model.ReceiptUpdateRequest;
import com.qoocca.teachers.api.receipt.model.response.ClassPaymentSummaryResponse;
import com.qoocca.teachers.api.receipt.model.response.DashboardMainSummaryResponse;
import com.qoocca.teachers.api.receipt.model.response.ParentReceiptResponse;
import com.qoocca.teachers.api.receipt.model.response.ReceiptCreateResponse;
import com.qoocca.teachers.api.receipt.model.response.ReceiptResponse;
import com.qoocca.teachers.api.receipt.model.response.ReceiptUpdateResponse;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoStudentEntity;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoRepository;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoStudentRepository;
import com.qoocca.teachers.db.parent.entity.ParentEntity;
import com.qoocca.teachers.db.receipt.entity.ReceiptEntity;
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
    private final StudentParentRepository studentParentRepository;
    private final FcmPushService fcmPushService;

    public ReceiptCreateResponse createReceipt(Long studentId, ReceiptCreateRequest request) {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        ClassInfoEntity classInfo = classInfoRepository.findById(request.getClassId())
                .orElseThrow(() -> new CustomException(ErrorCode.CLASS_NOT_FOUND));

        ClassInfoStudentEntity enrollment = classInfoStudentRepository
                .findByClassInfo_ClassIdAndStudent_StudentId(request.getClassId(), studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENROLLMENT_NOT_FOUND));

        ParentEntity payer = resolvePayerParent(enrollment.getPayerParent(), studentId);
        if (!hasPaymentMethod(payer)) {
            throw new CustomException(ErrorCode.PAYMENT_METHOD_NOT_FOUND);
        }

        if (isAlreadyProcessedInMonth(studentId, request.getClassId(), request.getReceiptDate())) {
            throw new CustomException(ErrorCode.DUPLICATE_RECEIPT_IN_MONTH);
        }

        ReceiptEntity receipt = ReceiptEntity.createReceipt(
                student, classInfo, request.getAmount(), request.getReceiptDate(), ReceiptEntity.ReceiptStatus.ISSUED);
        ReceiptEntity saved = receiptRepository.save(receipt);

        String title = "결제 요청이 도착했어요";
        String body = String.format("%s 학생의 %s 수업 수강료 %,d원 결제를 진행해 주세요.",
                student.getStudentName(), classInfo.getClassName(), request.getAmount());
        if (payer.getParentId() != null) {
            fcmPushService.sendPushToUser(payer.getParentId(), saved.getReceiptId(), title, body);
        }

        return ReceiptCreateResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ReceiptResponse> getReceiptsByStudent(Long studentId) {
        return receiptRepository.findByStudent_StudentId(studentId)
                .stream()
                .map(ReceiptResponse::fromEntity)
                .collect(Collectors.toList());
    }

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

    @Transactional(readOnly = true)
    public List<ClassPaymentSummaryResponse> getClassReceiptSummary(Long academyId, int year, int month) {
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
                            .anyMatch(sp -> hasPaymentMethod(sp.getParent()))
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

    private ReceiptEntity.ReceiptStatus calculateRepresentativeStatus(
            Long classId,
            List<ClassInfoStudentEntity> enrollments,
            Map<String, ReceiptEntity> receiptMap
    ) {
        if (enrollments.isEmpty()) {
            return ReceiptEntity.ReceiptStatus.NO_STUDENTS;
        }

        boolean hasIssued = false;
        for (ClassInfoStudentEntity enroll : enrollments) {
            ReceiptEntity receipt = receiptMap.get(classId + "-" + enroll.getStudent().getStudentId());
            if (receipt == null) {
                return ReceiptEntity.ReceiptStatus.BEFORE_REQUEST;
            }
            if (receipt.getReceiptStatus() == ReceiptEntity.ReceiptStatus.ISSUED) {
                hasIssued = true;
            }
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

        Long payerParentId = resolvePayerParentId(receipt.getClassInfo().getClassId(), receipt.getStudent().getStudentId());
        if (!payerParentId.equals(parentId)) {
            throw new CustomException(ErrorCode.STUDENT_PARENT_RELATION_NOT_FOUND);
        }
        return receipt;
    }

    @Transactional(readOnly = true)
    public List<ParentReceiptResponse> getPendingReceiptsByParent(Long parentId) {
        return receiptRepository.findByReceiptStatusOrderByReceiptDateDesc(ReceiptEntity.ReceiptStatus.ISSUED).stream()
                .filter(receipt -> parentId.equals(resolvePayerParentId(
                        receipt.getClassInfo().getClassId(),
                        receipt.getStudent().getStudentId()
                )))
                .map(ParentReceiptResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private boolean isAlreadyProcessedInMonth(Long studentId, Long classId, LocalDateTime date) {
        YearMonth ym = YearMonth.from(date != null ? date : LocalDateTime.now());
        return receiptRepository.existsByStudent_StudentIdAndClassInfo_ClassIdAndReceiptDateBetween(
                studentId, classId, ym.atDay(1).atStartOfDay(), ym.atEndOfMonth().atTime(23, 59, 59));
    }

    private ParentEntity resolvePayerParent(ParentEntity designatedPayer, Long studentId) {
        if (designatedPayer != null) {
            return studentParentRepository.findByStudent_StudentIdAndParent_ParentId(studentId, designatedPayer.getParentId())
                    .map(StudentParentEntity::getParent)
                    .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_PARENT_RELATION_NOT_FOUND));
        }

        return studentParentRepository.findFirstByStudent_StudentIdOrderByStudentParentIdAsc(studentId)
                .map(StudentParentEntity::getParent)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_PARENT_RELATION_NOT_FOUND));
    }

    private Long resolvePayerParentId(Long classId, Long studentId) {
        Optional<ClassInfoStudentEntity> enrollment = classInfoStudentRepository
                .findByClassInfo_ClassIdAndStudent_StudentId(classId, studentId);

        if (enrollment.isEmpty()) {
            throw new CustomException(ErrorCode.ENROLLMENT_NOT_FOUND);
        }

        ParentEntity payer = resolvePayerParent(enrollment.get().getPayerParent(), studentId);
        return payer.getParentId();
    }

    private boolean hasPaymentMethod(ParentEntity parent) {
        return Boolean.TRUE.equals(parent.getCardState())
                || (parent.getCardNum() != null && !parent.getCardNum().isBlank());
    }

}

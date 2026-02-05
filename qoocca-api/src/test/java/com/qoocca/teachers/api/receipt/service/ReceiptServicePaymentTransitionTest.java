package com.qoocca.teachers.api.receipt.service;

import com.qoocca.teachers.api.global.service.FcmPushService;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoStudentEntity;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoRepository;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoStudentRepository;
import com.qoocca.teachers.db.parent.entity.ParentEntity;
import com.qoocca.teachers.db.receipt.entity.ReceiptEntity;
import com.qoocca.teachers.db.receipt.repository.ReceiptRepository;
import com.qoocca.teachers.db.student.entity.StudentEntity;
import com.qoocca.teachers.db.student.entity.StudentParentEntity;
import com.qoocca.teachers.db.student.repository.StudentParentRepository;
import com.qoocca.teachers.db.student.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptServicePaymentTransitionTest {

    @Mock
    private ReceiptRepository receiptRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private ClassInfoRepository classInfoRepository;
    @Mock
    private ClassInfoStudentRepository classInfoStudentRepository;
    @Mock
    private StudentParentRepository studentParentRepository;
    @Mock
    private FcmPushService fcmPushService;

    @InjectMocks
    private ReceiptService receiptService;

    @Test
    void payReceiptSucceedsOnlyFromIssued() {
        ReceiptEntity receipt = buildReceipt(1L, 10L, 100L, ReceiptEntity.ReceiptStatus.ISSUED);
        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        stubDesignatedPayer(receipt, 99L);

        var response = receiptService.payReceipt(1L, 99L);

        assertEquals("PAID", response.getReceiptStatus());
        assertEquals(ReceiptEntity.ReceiptStatus.PAID, receipt.getReceiptStatus());
    }

    @Test
    void cancelReceiptSucceedsOnlyFromIssued() {
        ReceiptEntity receipt = buildReceipt(2L, 20L, 200L, ReceiptEntity.ReceiptStatus.ISSUED);
        when(receiptRepository.findById(2L)).thenReturn(Optional.of(receipt));
        stubDesignatedPayer(receipt, 77L);

        var response = receiptService.cancelReceipt(2L, 77L);

        assertEquals("CANCELLED", response.getReceiptStatus());
        assertEquals(ReceiptEntity.ReceiptStatus.CANCELLED, receipt.getReceiptStatus());
    }

    @Test
    void payReceiptFailsWhenStatusIsNotIssued() {
        ReceiptEntity receipt = buildReceipt(3L, 30L, 300L, ReceiptEntity.ReceiptStatus.PAID);
        when(receiptRepository.findById(3L)).thenReturn(Optional.of(receipt));
        stubDesignatedPayer(receipt, 88L);

        CustomException exception = assertThrows(CustomException.class, () -> receiptService.payReceipt(3L, 88L));

        assertEquals(ErrorCode.INVALID_RECEIPT_STATUS, exception.getErrorCode());
    }

    @Test
    void payReceiptFailsWhenParentIsNotDesignatedPayer() {
        ReceiptEntity receipt = buildReceipt(4L, 40L, 400L, ReceiptEntity.ReceiptStatus.ISSUED);
        when(receiptRepository.findById(4L)).thenReturn(Optional.of(receipt));
        stubDesignatedPayer(receipt, 101L);

        CustomException exception = assertThrows(CustomException.class, () -> receiptService.payReceipt(4L, 100L));

        assertEquals(ErrorCode.STUDENT_PARENT_RELATION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void payReceiptUsesFirstRegisteredParentWhenPayerIsNull() {
        ReceiptEntity receipt = buildReceipt(5L, 50L, 500L, ReceiptEntity.ReceiptStatus.ISSUED);
        when(receiptRepository.findById(5L)).thenReturn(Optional.of(receipt));

        ClassInfoStudentEntity enrollment = ClassInfoStudentEntity.builder()
                .classInfo(receipt.getClassInfo())
                .student(receipt.getStudent())
                .payerParent(null)
                .build();

        when(classInfoStudentRepository.findByClassInfo_ClassIdAndStudent_StudentId(500L, 50L))
                .thenReturn(Optional.of(enrollment));
        when(studentParentRepository.findFirstByStudent_StudentIdOrderByStudentParentIdAsc(50L))
                .thenReturn(Optional.of(StudentParentEntity.builder()
                        .parent(ParentEntity.builder().parentId(777L).build())
                        .build()));

        var response = receiptService.payReceipt(5L, 777L);

        assertEquals("PAID", response.getReceiptStatus());
        assertEquals(ReceiptEntity.ReceiptStatus.PAID, receipt.getReceiptStatus());
    }

    private void stubDesignatedPayer(ReceiptEntity receipt, Long payerParentId) {
        ParentEntity payer = ParentEntity.builder().parentId(payerParentId).build();
        ClassInfoStudentEntity enrollment = ClassInfoStudentEntity.builder()
                .classInfo(receipt.getClassInfo())
                .student(receipt.getStudent())
                .payerParent(payer)
                .build();

        when(classInfoStudentRepository.findByClassInfo_ClassIdAndStudent_StudentId(
                receipt.getClassInfo().getClassId(), receipt.getStudent().getStudentId()))
                .thenReturn(Optional.of(enrollment));

        when(studentParentRepository.findByStudent_StudentIdAndParent_ParentId(
                receipt.getStudent().getStudentId(), payerParentId))
                .thenReturn(Optional.of(StudentParentEntity.builder().parent(payer).build()));
    }

    private ReceiptEntity buildReceipt(Long receiptId, Long studentId, Long classId, ReceiptEntity.ReceiptStatus status) {
        StudentEntity student = StudentEntity.builder()
                .studentId(studentId)
                .build();
        ClassInfoEntity classInfo = ClassInfoEntity.builder()
                .classId(classId)
                .build();

        return ReceiptEntity.builder()
                .receiptId(receiptId)
                .student(student)
                .classInfo(classInfo)
                .receiptDate(LocalDateTime.now())
                .receiptStatus(status)
                .build();
    }
}

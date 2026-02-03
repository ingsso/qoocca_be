package com.qoocca.teachers.api.receipt.service;

import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoRepository;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoStudentRepository;
import com.qoocca.teachers.db.receipt.entity.ReceiptEntity;
import com.qoocca.teachers.db.receipt.repository.ReceiptRepository;
import com.qoocca.teachers.db.student.entity.StudentEntity;
import com.qoocca.teachers.db.student.entity.StudentParentEntity;
import com.qoocca.teachers.db.student.repository.StudentParentRepository;
import com.qoocca.teachers.db.student.repository.StudentRepository;
import com.qoocca.teachers.api.global.service.FcmPushService;
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
        ReceiptEntity receipt = buildReceipt(1L, 10L, ReceiptEntity.ReceiptStatus.ISSUED);
        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(studentParentRepository.findByStudent_StudentIdAndParent_ParentId(10L, 99L))
                .thenReturn(Optional.of(StudentParentEntity.builder().build()));

        var response = receiptService.payReceipt(1L, 99L);

        assertEquals("PAID", response.getReceiptStatus());
        assertEquals(ReceiptEntity.ReceiptStatus.PAID, receipt.getReceiptStatus());
    }

    @Test
    void cancelReceiptSucceedsOnlyFromIssued() {
        ReceiptEntity receipt = buildReceipt(2L, 20L, ReceiptEntity.ReceiptStatus.ISSUED);
        when(receiptRepository.findById(2L)).thenReturn(Optional.of(receipt));
        when(studentParentRepository.findByStudent_StudentIdAndParent_ParentId(20L, 77L))
                .thenReturn(Optional.of(StudentParentEntity.builder().build()));

        var response = receiptService.cancelReceipt(2L, 77L);

        assertEquals("CANCELLED", response.getReceiptStatus());
        assertEquals(ReceiptEntity.ReceiptStatus.CANCELLED, receipt.getReceiptStatus());
    }

    @Test
    void payReceiptFailsWhenStatusIsNotIssued() {
        ReceiptEntity receipt = buildReceipt(3L, 30L, ReceiptEntity.ReceiptStatus.PAID);
        when(receiptRepository.findById(3L)).thenReturn(Optional.of(receipt));
        when(studentParentRepository.findByStudent_StudentIdAndParent_ParentId(30L, 88L))
                .thenReturn(Optional.of(StudentParentEntity.builder().build()));

        CustomException exception = assertThrows(CustomException.class, () -> receiptService.payReceipt(3L, 88L));

        assertEquals(ErrorCode.INVALID_RECEIPT_STATUS, exception.getErrorCode());
    }

    @Test
    void payReceiptFailsWhenParentIsNotRelated() {
        ReceiptEntity receipt = buildReceipt(4L, 40L, ReceiptEntity.ReceiptStatus.ISSUED);
        when(receiptRepository.findById(4L)).thenReturn(Optional.of(receipt));
        when(studentParentRepository.findByStudent_StudentIdAndParent_ParentId(40L, 100L))
                .thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> receiptService.payReceipt(4L, 100L));

        assertEquals(ErrorCode.STUDENT_PARENT_RELATION_NOT_FOUND, exception.getErrorCode());
    }

    private ReceiptEntity buildReceipt(Long receiptId, Long studentId, ReceiptEntity.ReceiptStatus status) {
        StudentEntity student = StudentEntity.builder()
                .studentId(studentId)
                .build();

        return ReceiptEntity.builder()
                .receiptId(receiptId)
                .student(student)
                .receiptDate(LocalDateTime.now())
                .receiptStatus(status)
                .build();
    }
}

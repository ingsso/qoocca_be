package com.qoocca.teachers.api.payment;

import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.receipt.entity.ReceiptEntity;
import com.qoocca.teachers.db.receipt.repository.ReceiptRepository;
import com.qoocca.teachers.db.student.repository.StudentParentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final ReceiptRepository receiptRepository;
    private final StudentParentRepository studentParentRepository;

    public PaymentService(ReceiptRepository receiptRepository,
                          StudentParentRepository studentParentRepository) {
        this.receiptRepository = receiptRepository;
        this.studentParentRepository = studentParentRepository;
    }

    @Transactional
    public void completePayment(Long receiptId, Long parentId) {
        ReceiptEntity receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECEIPT_NOT_FOUND));

        boolean isParent = studentParentRepository
                .findByStudent_StudentIdAndParent_ParentId(receipt.getStudent().getStudentId(), parentId)
                .isPresent();
        if (!isParent) {
            throw new CustomException(ErrorCode.STUDENT_PARENT_RELATION_NOT_FOUND);
        }

        receipt.setReceiptStatus(ReceiptEntity.ReceiptStatus.PAID);
    }
}

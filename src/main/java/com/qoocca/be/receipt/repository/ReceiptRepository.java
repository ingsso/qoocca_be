package com.qoocca.be.receipt.repository;

import com.qoocca.be.receipt.entity.ReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReceiptRepository
        extends JpaRepository<ReceiptEntity, Long> {

    // 학생별 영수증 조회
    List<ReceiptEntity> findByStudent_StudentId(Long studentId);

    // 상태별 영수증 조회
    List<ReceiptEntity> findByReceiptStatus(ReceiptEntity.ReceiptStatus status);

    // 기간별 영수증 조회
    List<ReceiptEntity> findByReceiptDateBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // 학생 + 상태별 조회
    List<ReceiptEntity> findByStudent_StudentIdAndReceiptStatus(
            Long studentId,
            ReceiptEntity.ReceiptStatus status
    );
}

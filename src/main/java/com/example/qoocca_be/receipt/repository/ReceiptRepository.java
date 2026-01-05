package com.example.qoocca_be.receipt.repository;

import com.example.qoocca_be.receipt.entity.ReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReceiptRepository
        extends JpaRepository<ReceiptEntity, Long> {


    /* =========================
     * 학생 기준
     * ========================= */

    List<ReceiptEntity> findByStudent_StudentId(Long studentId);

    List<ReceiptEntity> findByStudent_StudentIdAndReceiptDateBetween(
            Long studentId,
            LocalDateTime start,
            LocalDateTime end
    );

    boolean existsByStudent_StudentIdAndReceiptDateBetween(
            Long studentId,
            LocalDateTime start,
            LocalDateTime end
    );

    /* =========================
     * 관리자 기준
     * ========================= */

    // 월 전체 수납
    List<ReceiptEntity> findByReceiptDateBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // 상태별 수납
    List<ReceiptEntity> findByReceiptStatus(
            ReceiptEntity.ReceiptStatus status
    );
}

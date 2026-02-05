package com.qoocca.teachers.db.receipt.repository;

import com.qoocca.teachers.db.receipt.entity.ReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReceiptRepository
        extends JpaRepository<ReceiptEntity, Long> {


    /* =========================
     * ?숈깮 湲곗?
     * ========================= */

    List<ReceiptEntity> findByStudent_StudentId(Long studentId);

    List<ReceiptEntity> findByStudent_StudentIdAndReceiptDateBetween(
            Long studentId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<ReceiptEntity> findByReceiptStatusOrderByReceiptDateDesc(ReceiptEntity.ReceiptStatus status);

    @Query("SELECT r FROM ReceiptEntity r " +
            "JOIN FETCH r.student " +
            "JOIN FETCH r.classInfo " +
            "WHERE r.classInfo.academy.id = :academyId " +
            "AND r.receiptDate BETWEEN :start AND :end")
    List<ReceiptEntity> findAllByAcademyAndDateBetween(
            @Param("academyId") Long academyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT r
        FROM ReceiptEntity r
        JOIN FETCH r.student s
        JOIN FETCH r.classInfo c
        JOIN FETCH c.academy a
        JOIN StudentParentEntity sp ON sp.student = s
        WHERE sp.parent.parentId = :parentId
          AND sp.parent.isPay = true
          AND r.receiptStatus = :status
        ORDER BY r.receiptDate DESC
    """)
    List<ReceiptEntity> findAllByParentAndStatus(
            @Param("parentId") Long parentId,
            @Param("status") ReceiptEntity.ReceiptStatus status
    );

    /* =========================
     * 愿由ъ옄 湲곗?
     * ========================= */

    @Query("""
    SELECT SUM(r.amount)
    FROM ReceiptEntity r
    JOIN AcademyStudentEntity ast ON r.student.studentId = ast.student.studentId
    WHERE ast.academy.id = :academyId
      AND r.receiptDate >= :start
      AND r.receiptDate < :end
      AND r.receiptStatus = 'PAID'
""")
    Long sumAmountByAcademyAndPeriod(
            @Param("academyId") Long academyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    boolean existsByStudent_StudentIdAndClassInfo_ClassIdAndReceiptDateBetween(Long studentId, Long classId, LocalDateTime start, LocalDateTime end);
}

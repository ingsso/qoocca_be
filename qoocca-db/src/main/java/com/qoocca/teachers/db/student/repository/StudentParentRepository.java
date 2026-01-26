package com.qoocca.teachers.db.student.repository;

import com.qoocca.teachers.db.student.entity.StudentParentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentParentRepository
        extends JpaRepository<StudentParentEntity, Long> {

    /* =========================
     * 기본 조회
     * ========================= */

    // 학생 기준 → 연결된 부모 목록
    List<StudentParentEntity> findByStudent_StudentId(Long studentId);

    /* =========================
     * 중복 방지 / 단건 조회
     * ========================= */

    // 특정 학생-부모 관계 단건 조회
    Optional<StudentParentEntity> findByStudent_StudentIdAndParent_ParentId(
            Long studentId,
            Long parentId
    );

    /* =========================
     * 조회 편의 (추가)
     * ========================= */

    // 학생의 보호자 ID만 조회
    @Query("""
        select sp.parent.parentId
        from StudentParentEntity sp
        where sp.student.studentId = :studentId
    """)
    List<Long> findParentIdsByStudentId(Long studentId);


    //학생한명당 부모조회를 하는 N+1 문제 해결하기 위한 메서드
    //여러학생 ID 에 대해 부모정보를 한번에 조회한다. ParentEntity도 같이 로딩한다.
    @Query("""
    SELECT sp
    FROM StudentParentEntity sp
    JOIN FETCH sp.parent
    WHERE sp.student.studentId IN :studentIds
""")
    List<StudentParentEntity> findAllByStudentIdsWithParent(
            @Param("studentIds") List<Long> studentIds
    );

}

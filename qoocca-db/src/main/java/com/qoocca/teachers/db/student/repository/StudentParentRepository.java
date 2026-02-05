package com.qoocca.teachers.db.student.repository;

import com.qoocca.teachers.db.student.entity.StudentParentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentParentRepository extends JpaRepository<StudentParentEntity, Long> {

    List<StudentParentEntity> findByStudent_StudentId(Long studentId);

    List<StudentParentEntity> findByStudent_StudentIdOrderByStudentParentIdAsc(Long studentId);

    Optional<StudentParentEntity> findFirstByStudent_StudentIdOrderByStudentParentIdAsc(Long studentId);

    Optional<StudentParentEntity> findByStudent_StudentIdAndParent_ParentId(Long studentId, Long parentId);

    @Query("""
        select sp.parent.parentId
        from StudentParentEntity sp
        where sp.student.studentId = :studentId
    """)
    List<Long> findParentIdsByStudentId(Long studentId);

    @Query("""
        SELECT sp
        FROM StudentParentEntity sp
        JOIN FETCH sp.parent
        WHERE sp.student.studentId IN :studentIds
    """)
    List<StudentParentEntity> findAllByStudentIdsWithParent(@Param("studentIds") List<Long> studentIds);
}

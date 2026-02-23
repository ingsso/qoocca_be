package com.qoocca.teachers.api.classInfo.service;

import com.qoocca.teachers.api.global.config.CacheConfig;
import com.qoocca.teachers.api.classInfo.model.request.ClassStudentModifyRequest;
import com.qoocca.teachers.api.classInfo.model.request.ClassStudentRequest;
import com.qoocca.teachers.api.classInfo.model.response.ClassStudentResponse;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoStudentEntity;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoRepository;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoStudentRepository;
import com.qoocca.teachers.db.parent.entity.ParentEntity;
import com.qoocca.teachers.db.student.entity.StudentEntity;
import com.qoocca.teachers.db.student.entity.StudentParentEntity;
import com.qoocca.teachers.db.student.repository.StudentParentRepository;
import com.qoocca.teachers.db.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassInfoStudentService {

    private final ClassInfoRepository classInfoRepository;
    private final StudentRepository studentRepository;
    private final ClassInfoStudentRepository classInfoStudentRepository;
    private final StudentParentRepository studentParentRepository;

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_CLASS_SUMMARY, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.ANALYTICS_CLASS_STATS, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.ANALYTICS_PARENT_STATS, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_STATS, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.RECEIPT_CLASS_SUMMARY, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RECEIPT_MAIN, allEntries = true)
    })
    public void register(Long academyId, Long classId, ClassStudentRequest request) {
        ClassInfoEntity classInfo = getClassInAcademy(academyId, classId);

        if (classInfoStudentRepository.existsByClassInfo_ClassIdAndStudent_StudentId(classId, request.getStudentId())) {
            throw new CustomException(ErrorCode.STUDENT_ALREADY_ENROLLED);
        }

        StudentEntity student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        ParentEntity payerParent = resolvePayerParent(student.getStudentId(), request.getPayerId());

        ClassInfoStudentEntity entity = ClassInfoStudentEntity.builder()
                .classInfo(classInfo)
                .student(student)
                .payerParent(payerParent)
                .build();

        classInfoStudentRepository.save(entity);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_CLASS_SUMMARY, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.ANALYTICS_CLASS_STATS, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.ANALYTICS_PARENT_STATS, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_STATS, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.RECEIPT_CLASS_SUMMARY, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RECEIPT_MAIN, allEntries = true)
    })
    public void modifyStatus(Long academyId, Long classId, Long studentId, ClassStudentModifyRequest request) {
        getClassInAcademy(academyId, classId);

        ClassInfoStudentEntity entity = classInfoStudentRepository
                .findByClassInfo_ClassIdAndStudent_StudentId(classId, studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENROLLMENT_NOT_FOUND));

        entity.setStatus(request.getStatus());
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_CLASS_SUMMARY, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.ANALYTICS_CLASS_STATS, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.ANALYTICS_PARENT_STATS, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_STATS, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.RECEIPT_CLASS_SUMMARY, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RECEIPT_MAIN, allEntries = true)
    })
    public void moveStudent(Long academyId, Long classId, Long studentId, Long targetClassId) {
        getClassInAcademy(academyId, classId);
        ClassInfoEntity targetClass = getClassInAcademy(academyId, targetClassId);

        ClassInfoStudentEntity current = classInfoStudentRepository
                .findByClassInfo_ClassIdAndStudent_StudentId(classId, studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (classInfoStudentRepository.existsByClassInfo_ClassIdAndStudent_StudentId(targetClassId, studentId)) {
            throw new CustomException(ErrorCode.STUDENT_ALREADY_ENROLLED);
        }

        classInfoStudentRepository.delete(current);

        ClassInfoStudentEntity newEntity = ClassInfoStudentEntity.builder()
                .classInfo(targetClass)
                .student(current.getStudent())
                .payerParent(current.getPayerParent())
                .status(StudentStatus.ENROLLED)
                .build();

        classInfoStudentRepository.save(newEntity);
    }

    @Transactional(readOnly = true)
    public List<ClassStudentResponse> getStudents(Long academyId, Long classId) {
        getClassInAcademy(academyId, classId);

        return classInfoStudentRepository.findByClassInfo_ClassId(classId).stream()
                .map(ClassStudentResponse::from)
                .toList();
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_CLASS_SUMMARY, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.ANALYTICS_CLASS_STATS, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.ANALYTICS_PARENT_STATS, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_STATS, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.RECEIPT_CLASS_SUMMARY, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RECEIPT_MAIN, allEntries = true)
    })
    public void remove(Long academyId, Long classId, Long studentId) {
        getClassInAcademy(academyId, classId);

        ClassInfoStudentEntity entity = classInfoStudentRepository
                .findByClassInfo_ClassIdAndStudent_StudentId(classId, studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENROLLMENT_NOT_FOUND));

        classInfoStudentRepository.delete(entity);
    }

    private ClassInfoEntity getClassInAcademy(Long academyId, Long classId) {
        ClassInfoEntity classInfo = classInfoRepository.findById(classId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLASS_NOT_FOUND));

        if (!classInfo.getAcademy().getId().equals(academyId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }
        return classInfo;
    }

    private ParentEntity resolvePayerParent(Long studentId, Long payerId) {
        if (payerId == null) {
            return null;
        }

        StudentParentEntity relation = studentParentRepository
                .findByStudent_StudentIdAndParent_ParentId(studentId, payerId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_PARENT_RELATION_NOT_FOUND));
        return relation.getParent();
    }
}

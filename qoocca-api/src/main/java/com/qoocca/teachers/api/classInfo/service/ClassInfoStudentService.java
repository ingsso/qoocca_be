package com.qoocca.teachers.api.classInfo.service;

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
import com.qoocca.teachers.db.student.entity.StudentEntity;
import com.qoocca.teachers.db.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
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

    public void register(Long academyId, Long classId, ClassStudentRequest request) {
        ClassInfoEntity classInfo = getClassInAcademy(academyId, classId);

        if (classInfoStudentRepository.existsByClassInfo_ClassIdAndStudent_StudentId(classId, request.getStudentId())) {
            throw new CustomException(ErrorCode.STUDENT_ALREADY_ENROLLED);
        }

        StudentEntity student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        ClassInfoStudentEntity entity = ClassInfoStudentEntity.builder()
                .classInfo(classInfo)
                .student(student)
                .build();

        classInfoStudentRepository.save(entity);
    }

    public void modifyStatus(Long academyId, Long classId, Long studentId, ClassStudentModifyRequest request) {
        getClassInAcademy(academyId, classId);

        ClassInfoStudentEntity entity = classInfoStudentRepository
                .findByClassInfo_ClassIdAndStudent_StudentId(classId, studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENROLLMENT_NOT_FOUND));

        entity.setStatus(request.getStatus());
    }

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
}

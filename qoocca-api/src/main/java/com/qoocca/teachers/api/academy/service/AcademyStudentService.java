package com.qoocca.teachers.api.academy.service;

import com.qoocca.teachers.api.academy.model.request.AcademyStudentCreateRequest;
import com.qoocca.teachers.api.academy.model.request.AcademyStudentModifyRequest;
import com.qoocca.teachers.api.academy.model.request.AcademyStudentWithParentCreateRequest;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentResponse;
import com.qoocca.teachers.api.classInfo.model.request.ClassStudentRequest;
import com.qoocca.teachers.api.classInfo.service.ClassInfoStudentService;
import com.qoocca.teachers.api.parent.model.ParentResponse;
import com.qoocca.teachers.api.student.service.StudentParentService;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.academy.entity.AcademyEntity;
import com.qoocca.teachers.db.academy.entity.AcademyStudentEntity;
import com.qoocca.teachers.db.academy.repository.AcademyRepository;
import com.qoocca.teachers.db.academy.repository.AcademyStudentRepository;
import com.qoocca.teachers.db.student.entity.StudentEntity;
import com.qoocca.teachers.db.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AcademyStudentService {

    private final AcademyRepository academyRepository;
    private final StudentRepository studentRepository;
    private final AcademyStudentRepository academyStudentRepository;
    private final StudentParentService studentParentService;
    private final ClassInfoStudentService classInfoStudentService;

    public AcademyStudentResponse registerStudent(Long academyId, AcademyStudentCreateRequest request) {

        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        // 기존 학생 있는지 확인, 없으면 새로 생성
        StudentEntity student = studentRepository
                .findByStudentNameAndStudentPhone(request.getStudentName(), request.getStudentPhone())
                .orElseGet(() -> {
                    StudentEntity newStudent = StudentEntity.builder()
                            .studentName(request.getStudentName())
                            .studentPhone(request.getStudentPhone())
                            .build();
                    return studentRepository.save(newStudent);
                });

        if (academyStudentRepository.findByAcademy_IdAndStudent_StudentId(academyId, student.getStudentId()).isPresent()) {
            return AcademyStudentResponse.from(student);
        }

        AcademyStudentEntity academyStudent = AcademyStudentEntity.builder()
                .academy(academy)
                .student(student)
                .build();

        academyStudentRepository.save(academyStudent);

        return AcademyStudentResponse.from(student);
    }

    public AcademyStudentResponse registerStudentWithParent(Long academyId, AcademyStudentWithParentCreateRequest request) {
        AcademyStudentResponse student = registerStudent(academyId, request.getStudent());
        ParentResponse parent = studentParentService.addParent(student.getStudentId(), request.getParent());

        List<Long> targetClassIds = new ArrayList<>();
        if (request.getClassId() != null) {
            targetClassIds.add(request.getClassId());
        }
        if (request.getClassIds() != null) {
            targetClassIds.addAll(request.getClassIds());
        }

        targetClassIds.stream().distinct().forEach(classId -> {
            classInfoStudentService.register(
                    academyId,
                    classId,
                    new ClassStudentRequest(student.getStudentId(), parent.getParentId())
            );
        });
        return student;
    }


    public AcademyStudentResponse modifyStudent(
            Long academyId,
            Long studentId,
            AcademyStudentModifyRequest request
    ) {

        AcademyStudentEntity relation = academyStudentRepository
                .findByAcademy_IdAndStudent_StudentId(academyId, studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_STUDENT_RELATION_NOT_FOUND));

        StudentEntity student = relation.getStudent();

        student.setStudentName(request.getStudentName());
        student.setStudentPhone(request.getStudentPhone());

        return AcademyStudentResponse.from(student);
    }


    @Transactional(readOnly = true)
    public List<AcademyStudentResponse> getStudents(Long academyId) {
        return academyStudentRepository.findByAcademy_Id(academyId).stream()
                .map(e -> AcademyStudentResponse.from(e.getStudent()))
                .toList();
    }

    public void deleteStudent(Long academyId, Long studentId) {

        AcademyStudentEntity relation = academyStudentRepository
                .findByAcademy_IdAndStudent_StudentId(academyId, studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_STUDENT_RELATION_NOT_FOUND));

        academyStudentRepository.delete(relation);
    }
}

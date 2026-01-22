package com.qoocca.teachers.academy.service;

import com.qoocca.teachers.academy.model.request.AcademyStudentCreateRequest;
import com.qoocca.teachers.academy.model.request.AcademyStudentModifyRequest;
import com.qoocca.teachers.academy.model.response.AcademyStudentResponse;
import com.qoocca.teachers.academy.entity.AcademyEntity;
import com.qoocca.teachers.academy.entity.AcademyStudentEntity;
import com.qoocca.teachers.academy.repository.AcademyRepository;
import com.qoocca.teachers.academy.repository.AcademyStudentRepository;
import com.qoocca.teachers.student.entity.StudentEntity;
import com.qoocca.teachers.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AcademyStudentService {

    private final AcademyRepository academyRepository;
    private final StudentRepository studentRepository;
    private final AcademyStudentRepository academyStudentRepository;

    public AcademyStudentResponse registerStudent(Long academyId, AcademyStudentCreateRequest request) {

        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new IllegalArgumentException("학원 없음"));

        // ✅ 전화번호까지 저장
        StudentEntity student = StudentEntity.builder()
                .studentName(request.getStudentName())
                .studentPhone(request.getStudentPhone())
                .build();

        studentRepository.save(student);

        AcademyStudentEntity academyStudent = AcademyStudentEntity.builder()
                .academy(academy)
                .student(student)
                .build();

        academyStudentRepository.save(academyStudent);

        return AcademyStudentResponse.from(student);
    }

    public AcademyStudentResponse modifyStudent(
            Long academyId,
            Long studentId,
            AcademyStudentModifyRequest request
    ) {

        AcademyStudentEntity relation = academyStudentRepository
                .findByAcademy_IdAndStudent_StudentId(academyId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생이 학원에 등록되어 있지 않습니다."));

        StudentEntity student = relation.getStudent();

        student.setStudentName(request.getStudentName());
        student.setStudentPhone(request.getStudentPhone());

        // dirty checking 으로 자동 update

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
                .orElseThrow(() -> new IllegalArgumentException("관계 없음"));

        academyStudentRepository.delete(relation);
    }
}

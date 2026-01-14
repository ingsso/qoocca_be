package com.example.qoocca_be.academy.service;

import com.example.qoocca_be.academy.dto.AcademyStudentCreateRequest;
import com.example.qoocca_be.academy.dto.AcademyStudentResponse;
import com.example.qoocca_be.academy.dto.DashboardStatsResponse;
import com.example.qoocca_be.academy.entity.AcademyEntity;
import com.example.qoocca_be.academy.entity.AcademyStudentEntity;
import com.example.qoocca_be.academy.repository.AcademyRepository;
import com.example.qoocca_be.academy.repository.AcademyStudentRepository;
import com.example.qoocca_be.attendance.entity.AttendanceEntity;
import com.example.qoocca_be.attendance.repository.AttendanceRepository;
import com.example.qoocca_be.classInfo.entity.StudentStatus;
import com.example.qoocca_be.classInfo.repository.ClassInfoRepository;
import com.example.qoocca_be.classInfo.repository.ClassInfoStudentRepository;
import com.example.qoocca_be.classInfo.service.ClassInfoService;
import com.example.qoocca_be.receipt.repository.ReceiptRepository;
import com.example.qoocca_be.student.entity.StudentEntity;
import com.example.qoocca_be.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        StudentEntity student = StudentEntity.builder()
                .studentName(request.getStudentName()).build();

        studentRepository.save(student);

        AcademyStudentEntity academyStudent = AcademyStudentEntity.builder()
                .academy(academy)
                .student(student)
                .build();

        academyStudentRepository.save(academyStudent);

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

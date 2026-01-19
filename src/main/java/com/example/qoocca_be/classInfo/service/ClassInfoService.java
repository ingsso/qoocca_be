package com.example.qoocca_be.classInfo.service;

import com.example.qoocca_be.academy.entity.AcademyEntity;
import com.example.qoocca_be.academy.repository.AcademyRepository;
import com.example.qoocca_be.age.entity.AgeEntity;
import com.example.qoocca_be.age.repository.AgeRepository;
import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import com.example.qoocca_be.classInfo.entity.StudentStatus;
import com.example.qoocca_be.classInfo.model.response.ClassGetResponse;
import com.example.qoocca_be.classInfo.model.request.ClassCreateRequest;
import com.example.qoocca_be.classInfo.model.response.ClassCreateResponse;
import com.example.qoocca_be.classInfo.model.response.ClassSummaryResponse;

import com.example.qoocca_be.classInfo.repository.ClassInfoRepository;
import com.example.qoocca_be.subject.entity.SubjectEntity;
import com.example.qoocca_be.subject.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassInfoService {

    private final ClassInfoRepository classInfoRepository;
    private final AcademyRepository academyRepository;
    private final AgeRepository ageRepository;
    private final SubjectRepository subjectRepository;


    /* =========================
     * 클래스 등록
     * ========================= */
    public ClassCreateResponse createClass(Long academyId, ClassCreateRequest request) {

        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학원입니다."));

        AgeEntity age = ageRepository.findById(request.getAgeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 연령대입니다."));

        SubjectEntity subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목입니다."));

        ClassInfoEntity entity = ClassInfoEntity.createClass(
                request.getClassName(), request.getStartTime(), request.getEndTime(),
                request.getPrice(), academy, age, subject,
                request.isMonday(), request.isTuesday(), request.isWednesday(),
                request.isThursday(), request.isFriday(), request.isSaturday(), request.isSunday()
        );

        // 3. 저장 및 통합 응답 객체 반환
        classInfoRepository.save(entity);
        return ClassCreateResponse.fromEntity(entity);
    }

    /* =========================
     * 클래스 목록 조회
     * ========================= */
    @Transactional(readOnly = true)
    public List<ClassGetResponse> getClasses(Long academyId) {

        return classInfoRepository.findByAcademy_Id(academyId).stream()
                .map(ClassGetResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClassSummaryResponse> getAcademyDashboard(Long academyId) {
        LocalDate today = LocalDate.now();
        String dayName = today.getDayOfWeek().name();

        return classInfoRepository.findDashboardSummaries(
                academyId,
                today,
                dayName,
                StudentStatus.ENROLLED
        );
    }
}

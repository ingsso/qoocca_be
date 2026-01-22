package com.qoocca.teachers.classInfo.service;

import com.qoocca.teachers.academy.entity.AcademyEntity;
import com.qoocca.teachers.academy.repository.AcademyRepository;
import com.qoocca.teachers.age.entity.AgeEntity;
import com.qoocca.teachers.age.repository.AgeRepository;
import com.qoocca.teachers.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.classInfo.entity.StudentStatus;
import com.qoocca.teachers.classInfo.model.response.ClassGetResponse;
import com.qoocca.teachers.classInfo.model.request.ClassCreateRequest;
import com.qoocca.teachers.classInfo.model.response.ClassCreateResponse;
import com.qoocca.teachers.classInfo.model.response.ClassSummaryResponse;

import com.qoocca.teachers.classInfo.repository.ClassInfoRepository;
import com.qoocca.teachers.global.exception.CustomException;
import com.qoocca.teachers.global.exception.ErrorCode;
import com.qoocca.teachers.subject.entity.SubjectEntity;
import com.qoocca.teachers.subject.repository.SubjectRepository;
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
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        AgeEntity age = ageRepository.findById(request.getAgeId())
                .orElseThrow(() -> new CustomException(ErrorCode.AGE_NOT_FOUND));

        SubjectEntity subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new CustomException(ErrorCode.SUBJECT_NOT_FOUND));

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

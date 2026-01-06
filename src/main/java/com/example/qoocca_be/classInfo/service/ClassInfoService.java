package com.example.qoocca_be.classInfo.service;

import com.example.qoocca_be.academy.entity.AcademyEntity;
import com.example.qoocca_be.academy.repository.AcademyRepository;
import com.example.qoocca_be.age.entity.AgeEntity;
import com.example.qoocca_be.age.repository.AgeRepository;
import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import com.example.qoocca_be.classInfo.model.ClassGetResponse;
import com.example.qoocca_be.classInfo.model.ClassPostRequest;
import com.example.qoocca_be.classInfo.model.ClassPostResponse;
import com.example.qoocca_be.classInfo.repository.ClassInfoRepository;
import com.example.qoocca_be.subject.entity.SubjectEntity;
import com.example.qoocca_be.subject.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ClassPostResponse createClass(Long academyId, ClassPostRequest request) {

        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학원입니다."));

        AgeEntity age = ageRepository.findById(request.getAgeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 연령대입니다."));

        SubjectEntity subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목입니다."));

        ClassInfoEntity entity = ClassInfoEntity.builder()
                .className(request.getClassName())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .monday(request.isMonday())
                .tuesday(request.isTuesday())
                .wednesday(request.isWednesday())
                .thursday(request.isThursday())
                .friday(request.isFriday())
                .saturday(request.isSaturday())
                .sunday(request.isSunday())
                .price(request.getPrice())
                .academy(academy)
                .age(age)
                .subject(subject)
                .build();

        classInfoRepository.save(entity);

        return ClassPostResponse.fromEntity(entity);
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
}

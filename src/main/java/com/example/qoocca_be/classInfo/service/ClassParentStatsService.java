package com.example.qoocca_be.classInfo.service;

import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import com.example.qoocca_be.classInfo.entity.StudentStatus;
import com.example.qoocca_be.classInfo.model.ClassParentStatsResponseDTO;
import com.example.qoocca_be.classInfo.model.ClassParentStudentDTO;
import com.example.qoocca_be.classInfo.repository.ClassParentStatsRepository;
import com.example.qoocca_be.parent.model.ParentResponse;
import com.example.qoocca_be.student.entity.StudentEntity;
import com.example.qoocca_be.student.entity.StudentParentEntity;
import com.example.qoocca_be.student.repository.StudentParentRepository;
import com.example.qoocca_be.student.service.StudentParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassParentStatsService {

    private final ClassParentStatsRepository repository;
    private final StudentParentService studentParentService;
    private final StudentParentRepository studentParentRepository;

    public List<ClassParentStatsResponseDTO> getParentStats(Long academyId) {

        List<Object[]> rows = repository.findStudentsByAcademy(academyId, null);

        Map<Long, ClassParentStatsResponseDTO> classMap = new LinkedHashMap<>();

        // 1. 학생 ID 수집
        List<Long> studentIds = rows.stream()
                .map(r -> ((StudentEntity) r[1]).getStudentId())
                .distinct()
                .toList();

        // 2. 부모정보 한번에 조회
        List<StudentParentEntity> parentEntities =
                studentParentRepository.findAllByStudentIdsWithParent(studentIds);

        // 3. studentId → parents map 만들기
        Map<Long, List<ParentResponse>> parentMap = parentEntities.stream()
                .collect(Collectors.groupingBy(
                        sp -> sp.getStudent().getStudentId(),
                        Collectors.mapping(
                                sp -> ParentResponse.from(sp.getParent()),
                                Collectors.toList()
                        )
                ));

        // 4. 반 + 학생 조립
        for (Object[] row : rows) {
            ClassInfoEntity classInfo = (ClassInfoEntity) row[0];
            StudentEntity student = (StudentEntity) row[1];

            classMap.putIfAbsent(
                    classInfo.getClassId(),
                    new ClassParentStatsResponseDTO(
                            classInfo.getClassId(),
                            classInfo.getClassName(),
                            new ArrayList<>()
                    )
            );

            List<ParentResponse> parents =
                    parentMap.getOrDefault(student.getStudentId(), List.of());

            ClassParentStudentDTO studentDTO =
                    new ClassParentStudentDTO(
                            student.getStudentId(),
                            student.getStudentName(),
                            parents
                    );

            classMap.get(classInfo.getClassId())
                    .getStudents()
                    .add(studentDTO);
        }

        return new ArrayList<>(classMap.values());
    }
}


package com.qoocca.teachers.api.academy.service;

import com.qoocca.teachers.api.academy.model.request.AcademyStudentCreateRequest;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentResponse;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentUploadError;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentUploadResponse;
import com.qoocca.teachers.api.classInfo.model.request.ClassStudentRequest;
import com.qoocca.teachers.api.classInfo.service.ClassInfoStudentService;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AcademyStudentUploadService {

    private static final int MAX_ROWS = 500;

    private static final Set<String> STUDENT_NAME_KEYS = Set.of(
            "studentname", "name", "student", "학생", "학생이름", "이름", "성함", "학생명"
    );
    private static final Set<String> STUDENT_PHONE_KEYS = Set.of(
            "studentphone", "phone", "mobile", "cell", "tel", "전화", "연락처", "휴대폰", "핸드폰"
    );
    private static final Set<String> CLASS_NAME_KEYS = Set.of(
            "classname", "class", "course", "반", "수업", "클래스", "강좌"
    );

    private final AcademyStudentService academyStudentService;
    private final ClassInfoStudentService classInfoStudentService;
    private final ClassInfoRepository classInfoRepository;
    private final OpenAiHeaderMappingClient openAiHeaderMappingClient;

    public AcademyStudentUploadResponse upload(
            Long academyId,
            MultipartFile file,
            Long classId,
            boolean useAi,
            boolean dryRun
    ) {
        List<AcademyStudentUploadError> errors = new ArrayList<>();
        Map<String, String> headerMapping = new HashMap<>();

        if (file == null || file.isEmpty()) {
            errors.add(new AcademyStudentUploadError(0, "Empty file"));
            return buildResponse(0, 0, 0, headerMapping, errors);
        }

        DataFormatter formatter = new DataFormatter();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                errors.add(new AcademyStudentUploadError(0, "Missing header row"));
                return buildResponse(0, 0, 0, headerMapping, errors);
            }

            List<String> headers = readHeaders(headerRow, formatter);
            Map<String, Integer> fieldIndex = mapHeaders(academyId, headers, useAi, classId != null);
            headerMapping.putAll(buildHeaderMapping(headers, fieldIndex));

            if (!fieldIndex.containsKey("studentName") || !fieldIndex.containsKey("studentPhone")) {
                errors.add(new AcademyStudentUploadError(0, "Required columns not found"));
                return buildResponse(0, 0, 0, headerMapping, errors);
            }
            if (classId == null && !fieldIndex.containsKey("className")) {
                errors.add(new AcademyStudentUploadError(0, "Class column not found"));
                return buildResponse(0, 0, 0, headerMapping, errors);
            }

            int totalRows = 0;
            int successCount = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row, formatter)) {
                    continue;
                }
                totalRows++;
                if (totalRows > MAX_ROWS) {
                    errors.add(new AcademyStudentUploadError(i + 1, "Row limit exceeded"));
                    break;
                }

                String studentName = getCellValue(row, fieldIndex.get("studentName"), formatter);
                String studentPhone = getCellValue(row, fieldIndex.get("studentPhone"), formatter);
                String className = classId == null
                        ? getCellValue(row, fieldIndex.get("className"), formatter)
                        : null;

                if (isBlank(studentName) || isBlank(studentPhone)) {
                    errors.add(new AcademyStudentUploadError(i + 1, "Missing required student data"));
                    continue;
                }
                if (classId == null && isBlank(className)) {
                    errors.add(new AcademyStudentUploadError(i + 1, "Missing class name"));
                    continue;
                }

                if (dryRun) {
                    if (classId == null && resolveClassId(academyId, className).isEmpty()) {
                        errors.add(new AcademyStudentUploadError(i + 1, "Class not found: " + className));
                        continue;
                    }
                    successCount++;
                    continue;
                }

                try {
                    AcademyStudentResponse student = academyStudentService.registerStudent(
                            academyId,
                            new AcademyStudentCreateRequest(studentName, studentPhone)
                    );

                    Long targetClassId = classId != null
                            ? classId
                            : resolveClassId(academyId, className).orElse(null);

                    if (targetClassId == null) {
                        errors.add(new AcademyStudentUploadError(i + 1, "Class not found: " + className));
                        continue;
                    }

                    classInfoStudentService.register(
                            academyId,
                            targetClassId,
                            new ClassStudentRequest(student.getStudentId(), null)
                    );
                    successCount++;
                } catch (Exception e) {
                    log.warn("Student upload row processing failed. academyId={}, row={}", academyId, i + 1, e);
                    errors.add(new AcademyStudentUploadError(i + 1, "Failed to process row"));
                }
            }

            return buildResponse(totalRows, successCount, totalRows - successCount, headerMapping, errors);
        } catch (Exception e) {
            errors.add(new AcademyStudentUploadError(0, "Failed to parse Excel file"));
            return buildResponse(0, 0, 0, headerMapping, errors);
        }
    }

    private List<String> readHeaders(Row headerRow, DataFormatter formatter) {
        List<String> headers = new ArrayList<>();
        short last = headerRow.getLastCellNum();
        for (int i = 0; i < last; i++) {
            headers.add(formatter.formatCellValue(headerRow.getCell(i)).trim());
        }
        return headers;
    }

    private Map<String, String> buildHeaderMapping(List<String> headers, Map<String, Integer> fieldIndex) {
        Map<String, String> mapping = new HashMap<>();
        fieldIndex.forEach((field, index) -> {
            if (index >= 0 && index < headers.size()) {
                mapping.put(field, headers.get(index));
            }
        });
        return mapping;
    }

    private Map<String, Integer> mapHeaders(Long academyId, List<String> headers, boolean useAi, boolean classIdProvided) {
        Map<String, Integer> fieldIndex = new HashMap<>();

        for (int i = 0; i < headers.size(); i++) {
            String normalized = normalize(headers.get(i));
            if (!fieldIndex.containsKey("studentName") && STUDENT_NAME_KEYS.contains(normalized)) {
                fieldIndex.put("studentName", i);
            } else if (!fieldIndex.containsKey("studentPhone") && STUDENT_PHONE_KEYS.contains(normalized)) {
                fieldIndex.put("studentPhone", i);
            } else if (!fieldIndex.containsKey("className") && CLASS_NAME_KEYS.contains(normalized)) {
                fieldIndex.put("className", i);
            }
        }

        boolean missingRequired = !fieldIndex.containsKey("studentName")
                || !fieldIndex.containsKey("studentPhone")
                || (!classIdProvided && !fieldIndex.containsKey("className"));

        if (useAi && missingRequired) {
            Map<String, String> aiMapping = openAiHeaderMappingClient.mapHeaders(academyId, headers);
            aiMapping.forEach((field, headerName) -> {
                if (!fieldIndex.containsKey(field)) {
                    Integer idx = findHeaderIndex(headers, headerName);
                    if (idx != null) {
                        fieldIndex.put(field, idx);
                    }
                }
            });
        }

        return fieldIndex;
    }

    private Integer findHeaderIndex(List<String> headers, String target) {
        if (target == null || target.isBlank()) {
            return null;
        }
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).equalsIgnoreCase(target)) {
                return i;
            }
        }
        String normalizedTarget = normalize(target);
        for (int i = 0; i < headers.size(); i++) {
            if (normalize(headers.get(i)).equals(normalizedTarget)) {
                return i;
            }
        }
        return null;
    }

    private Optional<Long> resolveClassId(Long academyId, String className) {
        if (isBlank(className)) {
            return Optional.empty();
        }
        return classInfoRepository.findByAcademy_IdAndClassName(academyId, className.trim())
                .map(ClassInfoEntity::getClassId);
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        short last = row.getLastCellNum();
        for (int i = 0; i < last; i++) {
            if (!formatter.formatCellValue(row.getCell(i)).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(Row row, Integer index, DataFormatter formatter) {
        if (index == null || index < 0) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(index)).trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase().replaceAll("[\\s_\\-]", "");
    }

    private AcademyStudentUploadResponse buildResponse(
            int totalRows,
            int successCount,
            int failureCount,
            Map<String, String> headerMapping,
            List<AcademyStudentUploadError> errors
    ) {
        return AcademyStudentUploadResponse.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .failureCount(failureCount)
                .headerMapping(headerMapping)
                .errors(errors)
                .build();
    }
}

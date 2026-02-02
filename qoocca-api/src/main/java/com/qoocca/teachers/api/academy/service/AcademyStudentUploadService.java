package com.qoocca.teachers.api.academy.service;

import com.qoocca.teachers.api.academy.model.request.AcademyStudentCreateRequest;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentResponse;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentUploadError;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentUploadResponse;
import com.qoocca.teachers.api.classInfo.model.request.ClassStudentRequest;
import com.qoocca.teachers.api.classInfo.service.ClassInfoStudentService;
import com.qoocca.teachers.api.parent.model.ParentCreateRequest;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoRepository;
import com.qoocca.teachers.db.parent.entity.ParentEntity;
import com.qoocca.teachers.db.parent.repository.ParentRepository;
import com.qoocca.teachers.db.student.entity.StudentEntity;
import com.qoocca.teachers.db.student.entity.StudentParentEntity;
import com.qoocca.teachers.db.student.repository.StudentParentRepository;
import com.qoocca.teachers.db.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Transactional
public class AcademyStudentUploadService {

    private static final int MAX_ROWS = 500;
    private static final int MAX_PARENT_GROUPS = 4;

        private static final Map<String, Integer> PARENT_GROUP_HINTS = Map.ofEntries(
            Map.entry("\uBD80\uBAA81", 1), Map.entry("\uC5C4\uB9C8", 1), Map.entry("\uC5B4\uBA38\uB2C8", 1), Map.entry("\uBAA8", 1),
            Map.entry("\uBD80\uBAA82", 2), Map.entry("\uC544\uBE60", 2), Map.entry("\uC544\uBC84\uC9C0", 2), Map.entry("\uBD80", 2),
            Map.entry("\uBD80\uBAA83", 3), Map.entry("\uD560\uBA38\uB2C8", 3), Map.entry("\uC870\uBAA8", 3),
            Map.entry("\uBD80\uBAA84", 4), Map.entry("\uD560\uC544\uBC84\uC9C0", 4), Map.entry("\uC870\uBD80", 4)
    );

    private static final Set<String> PARENT_NAME_KEYS = Set.of(
            "parentname", "name", "\uBD80\uBAA8\uC774\uB984", "\uD559\uBD80\uBAA8\uC774\uB984", "\uC774\uB984", "\uC131\uD568"
    );
    private static final Set<String> PARENT_PHONE_KEYS = Set.of(
            "parentphone", "phone", "mobile", "cell", "tel", "\uBD80\uBAA8\uC804\uD654", "\uD559\uBD80\uBAA8\uC804\uD654", "\uC5F0\uB77D\uCC98", "\uD734\uB300\uD3F0", "\uC804\uD654\uBC88\uD638"
    );
    private static final Set<String> PARENT_RELATION_KEYS = Set.of(
            "relationship", "relation", "\uAD00\uACC4", "\uAD00\uACC4\uBA85"
    );
    private static final Set<String> PARENT_CARD_KEYS = Set.of(
            "card", "cardnum", "\uCE74\uB4DC", "\uCE74\uB4DC\uBC88\uD638"
    );
    private static final Set<String> PARENT_PAY_KEYS = Set.of(
            "ispay", "pay", "\uACB0\uC81C", "\uACB0\uC81C\uC5EC\uBD80"
    );
    private static final Set<String> PARENT_ALARM_KEYS = Set.of(
            "alarm", "alert", "\uC54C\uB78C", "\uC54C\uB9BC"
    );

    private static final Map<String, String> RELATION_RULES = Map.ofEntries(
            Map.entry("\uC5C4\uB9C8", "\uBAA8"), Map.entry("\uC5B4\uBA38\uB2C8", "\uBAA8"), Map.entry("\uBAA8", "\uBAA8"), Map.entry("\uBAA8\uCE5C", "\uBAA8"),
            Map.entry("\uC544\uBE60", "\uBD80"), Map.entry("\uC544\uBC84\uC9C0", "\uBD80"), Map.entry("\uBD80", "\uBD80"), Map.entry("\uBD80\uCE5C", "\uBD80"),
            Map.entry("\uD560\uBA38\uB2C8", "\uC870\uBAA8"), Map.entry("\uC870\uBAA8", "\uC870\uBAA8"), Map.entry("\uC678\uD560\uBA38\uB2C8", "\uC870\uBAA8"), Map.entry("\uC678\uC870\uBAA8", "\uC870\uBAA8"),
            Map.entry("\uD560\uC544\uBC84\uC9C0", "\uC870\uBD80"), Map.entry("\uC870\uBD80", "\uC870\uBD80"), Map.entry("\uC678\uD560\uC544\uBC84\uC9C0", "\uC870\uBD80"), Map.entry("\uC678\uC870\uBD80", "\uC870\uBD80")
    );
    private static final Set<String> RELATION_ALLOWED = Set.of("\uBAA8", "\uBD80", "\uC870\uBAA8", "\uC870\uBD80");

    private static final Set<String> STUDENT_NAME_KEYS = Set.of(
            "studentname", "name", "student"
    );
    private static final Set<String> STUDENT_PHONE_KEYS = Set.of(
            "studentphone", "phone", "mobile", "cell", "tel"
    );
    private static final Set<String> CLASS_NAME_KEYS = Set.of(
            "classname", "class", "course"
    );

    private final AcademyStudentService academyStudentService;
    private final ClassInfoStudentService classInfoStudentService;
    private final ClassInfoRepository classInfoRepository;
    private final OpenAiHeaderMappingClient openAiHeaderMappingClient;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final StudentParentRepository studentParentRepository;

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
            Map<Integer, Map<String, Integer>> parentGroupIndex =
                    mapParentGroupFieldIndex(academyId, headers, fieldIndex, useAi);

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
                    boolean parentValid = validateParentGroups(
                            academyId, row, parentGroupIndex, formatter, useAi, errors, i + 1);
                    if (classId == null && resolveClassId(academyId, className).isEmpty()) {
                        errors.add(new AcademyStudentUploadError(i + 1, "Class not found: " + className));
                        continue;
                    }
                    if (parentValid) {
                        successCount++;
                    }
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
                            targetClassId,
                            new ClassStudentRequest(student.getStudentId())
                    );
                    boolean parentSaved = processParentGroups(
                            academyId, row, parentGroupIndex, formatter, student.getStudentId(), useAi, errors, i + 1);
                    if (parentSaved) {
                        successCount++;
                    }
                } catch (Exception e) {
                    errors.add(new AcademyStudentUploadError(i + 1, e.getMessage()));
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

    private Map<Integer, Map<String, Integer>> mapParentGroupFieldIndex(
            Long academyId,
            List<String> headers,
            Map<String, Integer> studentFieldIndex,
            boolean useAi
    ) {
        Map<Integer, String> aiLabels = useAi
                ? openAiHeaderMappingClient.mapExtendedHeaders(academyId, headers)
                : Map.of();

        Map<Integer, Map<String, Integer>> grouped = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            if (studentFieldIndex.containsValue(i)) {
                continue;
            }
            String header = headers.get(i);
            String label = detectParentFieldLabel(header);
            if (label == null) {
                label = aiLabels.get(i);
            }
            if (!isParentLabel(label)) {
                continue;
            }
            Integer groupId = detectParentGroup(header);
            if (groupId == null || groupId < 1 || groupId > MAX_PARENT_GROUPS) {
                groupId = 0;
            }
            grouped.computeIfAbsent(groupId, k -> new HashMap<>())
                    .putIfAbsent(label, i);
        }

        if (grouped.containsKey(0)) {
            int knownGroups = 0;
            int onlyGroupId = 0;
            for (int gid = 1; gid <= MAX_PARENT_GROUPS; gid++) {
                if (grouped.containsKey(gid)) {
                    knownGroups++;
                    onlyGroupId = gid;
                }
            }
            if (knownGroups == 1) {
                Map<String, Integer> unknown = grouped.remove(0);
                grouped.computeIfAbsent(onlyGroupId, k -> new HashMap<>()).putAll(unknown);
            }
        }

        return grouped;
    }

    private boolean validateParentGroups(
            Long academyId,
            Row row,
            Map<Integer, Map<String, Integer>> parentGroupIndex,
            DataFormatter formatter,
            boolean useAi,
            List<AcademyStudentUploadError> errors,
            int rowNumber
    ) {
        boolean ok = true;
        for (int groupId = 1; groupId <= MAX_PARENT_GROUPS; groupId++) {
            Map<String, Integer> fields = parentGroupIndex.get(groupId);
            if (fields == null || fields.isEmpty()) {
                continue;
            }
            ParentRowData data = readParentRowData(row, fields, formatter);
            if (data.isAllBlank()) {
                continue;
            }
            String relationship = resolveParentRelationship(academyId, data.relationshipRaw(), useAi);
            if (relationship == null) {
                errors.add(new AcademyStudentUploadError(rowNumber, "Invalid parent relationship (group " + groupId + ")"));
                ok = false;
            }
            if (isBlank(data.parentName()) || isBlank(data.parentPhone())) {
                errors.add(new AcademyStudentUploadError(rowNumber, "Missing required parent data (group " + groupId + ")"));
                ok = false;
            }
            if (data.isPay() == null || data.alarm() == null) {
                errors.add(new AcademyStudentUploadError(rowNumber, "Invalid parent flags (group " + groupId + ")"));
                ok = false;
            }
        }
        return ok;
    }

    private boolean processParentGroups(
            Long academyId,
            Row row,
            Map<Integer, Map<String, Integer>> parentGroupIndex,
            DataFormatter formatter,
            Long studentId,
            boolean useAi,
            List<AcademyStudentUploadError> errors,
            int rowNumber
    ) {
        boolean ok = true;
        StudentEntity student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            errors.add(new AcademyStudentUploadError(rowNumber, "Student not found"));
            return false;
        }

        for (int groupId = 1; groupId <= MAX_PARENT_GROUPS; groupId++) {
            Map<String, Integer> fields = parentGroupIndex.get(groupId);
            if (fields == null || fields.isEmpty()) {
                continue;
            }
            ParentRowData data = readParentRowData(row, fields, formatter);
            if (data.isAllBlank()) {
                continue;
            }
            if (isBlank(data.parentName()) || isBlank(data.parentPhone())) {
                errors.add(new AcademyStudentUploadError(rowNumber, "Missing required parent data (group " + groupId + ")"));
                ok = false;
                continue;
            }

            String relationship = resolveParentRelationship(academyId, data.relationshipRaw(), useAi);
            if (relationship == null) {
                errors.add(new AcademyStudentUploadError(rowNumber, "Invalid parent relationship (group " + groupId + ")"));
                ok = false;
                continue;
            }
            if (data.isPay() == null || data.alarm() == null) {
                errors.add(new AcademyStudentUploadError(rowNumber, "Invalid parent flags (group " + groupId + ")"));
                ok = false;
                continue;
            }

            ParentCreateRequest request = ParentCreateRequest.builder()
                    .parentName(data.parentName())
                    .cardNum(data.cardNum())
                    .parentRelationship(relationship)
                    .parentPhone(data.parentPhone())
                    .isPay(data.isPay())
                    .alarm(data.alarm())
                    .build();

            ParentEntity parent = upsertParent(request);
            if (parent == null) {
                errors.add(new AcademyStudentUploadError(rowNumber, "Failed to save parent (group " + groupId + ")"));
                ok = false;
                continue;
            }

            if (studentParentRepository.findByStudent_StudentIdAndParent_ParentId(studentId, parent.getParentId()).isEmpty()) {
                StudentParentEntity link = StudentParentEntity.builder()
                        .student(student)
                        .parent(parent)
                        .build();
                studentParentRepository.save(link);
            }
        }

        return ok;
    }

    private ParentEntity upsertParent(ParentCreateRequest request) {
        if (request == null || isBlank(request.getParentPhone())) {
            return null;
        }
        ParentEntity parent = parentRepository.findByParentPhone(request.getParentPhone())
                .orElseGet(ParentEntity::new);

        parent.setParentName(request.getParentName());
        parent.setParentRelationship(request.getParentRelationship());
        parent.setParentPhone(request.getParentPhone());
        parent.setIsPay(request.getIsPay());
        parent.setAlarm(request.getAlarm());

        if (request.getCardNum() != null) {
            boolean hasCard = !request.getCardNum().isBlank();
            parent.setCardNum(hasCard ? request.getCardNum() : null);
            parent.setCardState(hasCard);
        }

        return parentRepository.save(parent);
    }

    private ParentRowData readParentRowData(Row row, Map<String, Integer> fields, DataFormatter formatter) {
        String parentName = getCellValue(row, fields.get("parentName"), formatter);
        String parentPhone = getCellValue(row, fields.get("parentPhone"), formatter);
        String relationshipRaw = getCellValue(row, fields.get("parentRelationship"), formatter);
        String cardNum = getCellValue(row, fields.get("cardNum"), formatter);
        String isPayRaw = getCellValue(row, fields.get("isPay"), formatter);
        String alarmRaw = getCellValue(row, fields.get("alarm"), formatter);

        Boolean isPay = parseBooleanValue(isPayRaw);
        Boolean alarm = parseBooleanValue(alarmRaw);

        return new ParentRowData(parentName, parentPhone, relationshipRaw, cardNum, isPay, alarm);
    }

    private String detectParentFieldLabel(String header) {
        Integer groupId = detectParentGroup(header);
        String normalized = normalize(stripParentGroupHints(header));
        boolean parentIndicator = groupId != null
                || normalized.contains("\uBD80\uBAA8")
                || normalized.contains("\uBCF4\uD638\uC790")
                || normalized.contains("parent");

        if (!parentIndicator && !containsAny(normalized, PARENT_CARD_KEYS, PARENT_PAY_KEYS, PARENT_ALARM_KEYS)) {
            return null;
        }

        if (containsAny(normalized, PARENT_NAME_KEYS)) {
            return "parentName";
        }
        if (containsAny(normalized, PARENT_PHONE_KEYS)) {
            return "parentPhone";
        }
        if (containsAny(normalized, PARENT_RELATION_KEYS)) {
            return "parentRelationship";
        }
        if (containsAny(normalized, PARENT_CARD_KEYS)) {
            return "cardNum";
        }
        if (containsAny(normalized, PARENT_PAY_KEYS)) {
            return "isPay";
        }
        if (containsAny(normalized, PARENT_ALARM_KEYS)) {
            return "alarm";
        }
        return null;
    }

    private Integer detectParentGroup(String header) {
        String normalized = normalize(header);
        for (Map.Entry<String, Integer> entry : PARENT_GROUP_HINTS.entrySet()) {
            if (normalized.contains(normalize(entry.getKey()))) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String stripParentGroupHints(String header) {
        String normalized = normalize(header);
        for (String hint : PARENT_GROUP_HINTS.keySet()) {
            normalized = normalized.replace(normalize(hint), "");
        }
        return normalized;
    }

    private boolean containsAny(String normalizedHeader, Set<String>... keys) {
        for (Set<String> keySet : keys) {
            for (String key : keySet) {
                if (normalizedHeader.contains(normalize(key))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isParentLabel(String label) {
        return "parentName".equals(label)
                || "parentPhone".equals(label)
                || "parentRelationship".equals(label)
                || "cardNum".equals(label)
                || "isPay".equals(label)
                || "alarm".equals(label);
    }

    private String resolveParentRelationship(Long academyId, String rawValue, boolean useAi) {
        String normalized = normalizeRelationship(rawValue);
        if (normalized.isBlank()) {
            return null;
        }
        String rule = RELATION_RULES.get(normalized);
        if (rule != null) {
            return rule;
        }
        if (!useAi) {
            return null;
        }
        String ai = openAiHeaderMappingClient.classifyParentRelationship(academyId, rawValue);
        if (ai != null && RELATION_ALLOWED.contains(ai)) {
            return ai;
        }
        return null;
    }

    private String normalizeRelationship(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("\\uB2D8$", "");
    }

    private Boolean parseBooleanValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        if (normalized.isBlank()) {
            return null;
        }
        if (normalized.equals("y") || normalized.equals("yes") || normalized.equals("true")
                || normalized.equals("1") || normalized.equals("t")
                || normalized.equals("\uC608") || normalized.equals("\uB124")
                || normalized.equals("o") || normalized.equals("on")) {
            return true;
        }
        if (normalized.equals("n") || normalized.equals("no") || normalized.equals("false")
                || normalized.equals("0") || normalized.equals("f")
                || normalized.equals("\uC544\uB2C8\uC624") || normalized.equals("\uC544\uB2C8\uC694")
                || normalized.equals("x") || normalized.equals("off")) {
            return false;
        }
        return null;
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

    private record ParentRowData(
            String parentName,
            String parentPhone,
            String relationshipRaw,
            String cardNum,
            Boolean isPay,
            Boolean alarm
    ) {
        boolean isAllBlank() {
            return (parentName == null || parentName.isBlank())
                    && (parentPhone == null || parentPhone.isBlank())
                    && (relationshipRaw == null || relationshipRaw.isBlank())
                    && (cardNum == null || cardNum.isBlank())
                    && isPay == null
                    && alarm == null;
        }
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














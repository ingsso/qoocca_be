package com.qoocca.teachers.api.academy.service;

import com.qoocca.teachers.api.academy.model.response.AcademyStudentResponse;
import com.qoocca.teachers.api.academy.model.response.AcademyStudentUploadResponse;
import com.qoocca.teachers.api.classInfo.model.request.ClassStudentRequest;
import com.qoocca.teachers.api.classInfo.service.ClassInfoStudentService;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoRepository;
import com.qoocca.teachers.db.parent.entity.ParentEntity;
import com.qoocca.teachers.db.parent.repository.ParentRepository;
import com.qoocca.teachers.db.student.entity.StudentEntity;
import com.qoocca.teachers.db.student.repository.StudentParentRepository;
import com.qoocca.teachers.db.student.repository.StudentRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademyStudentUploadServiceTest {

    @Mock
    private AcademyStudentService academyStudentService;
    @Mock
    private ClassInfoStudentService classInfoStudentService;
    @Mock
    private ClassInfoRepository classInfoRepository;
    @Mock
    private OpenAiHeaderMappingClient openAiHeaderMappingClient;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private ParentRepository parentRepository;
    @Mock
    private StudentParentRepository studentParentRepository;

    @InjectMocks
    private AcademyStudentUploadService service;

    private StudentEntity studentEntity;

    @BeforeEach
    void setUp() {
        studentEntity = StudentEntity.builder()
                .studentId(1L)
                .studentName("Kim")
                .studentPhone("01000000000")
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(studentEntity));
        when(parentRepository.save(any())).thenAnswer(invocation -> {
            ParentEntity parent = invocation.getArgument(0);
            parent.setParentId(10L);
            return parent;
        });
        when(studentParentRepository.findByStudent_StudentIdAndParent_ParentId(eq(1L), eq(10L)))
                .thenReturn(Optional.empty());
    }

    @Test
    void upload_saves_parent_with_rule_based_relationship() throws Exception {
        MockMultipartFile file = buildExcel(
                List.of("학생이름", "연락처", "반", "부모1이름", "부모1폰", "부모1관계", "부모1카드번호", "부모1결제여부", "부모1알람여부"),
                List.of("김학생", "01011112222", "A반", "김엄마", "01099998888", "어머니님", "1234", "Y", "N")
        );

        when(academyStudentService.registerStudent(eq(1L), any()))
                .thenReturn(AcademyStudentResponse.builder().studentId(1L).studentName("김학생").build());

        AcademyStudentUploadResponse response = service.upload(1L, file, 1L, false, false);

        ArgumentCaptor<ParentEntity> parentCaptor = ArgumentCaptor.forClass(ParentEntity.class);
        verify(parentRepository).save(parentCaptor.capture());
        assertThat(parentCaptor.getValue().getParentRelationship()).isEqualTo("모");
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getSuccessCount()).isEqualTo(1);
        verify(openAiHeaderMappingClient, never()).classifyParentRelationship(any(), any());
        verify(classInfoStudentService).register(eq(1L), any(ClassStudentRequest.class));
    }

    @Test
    void upload_uses_ai_relationship_when_rule_missing() throws Exception {
        MockMultipartFile file = buildExcel(
                List.of("학생이름", "연락처", "반", "부모1이름", "부모1폰", "부모1관계", "부모1카드번호", "부모1결제여부", "부모1알람여부"),
                List.of("김학생", "01011112222", "A반", "김보호자", "01099998888", "할배", "1234", "Y", "N")
        );

        when(academyStudentService.registerStudent(eq(1L), any()))
                .thenReturn(AcademyStudentResponse.builder().studentId(1L).studentName("김학생").build());
        when(openAiHeaderMappingClient.classifyParentRelationship(1L, "할배"))
                .thenReturn("조부");

        AcademyStudentUploadResponse response = service.upload(1L, file, 1L, true, false);

        ArgumentCaptor<ParentEntity> parentCaptor = ArgumentCaptor.forClass(ParentEntity.class);
        verify(parentRepository).save(parentCaptor.capture());
        assertThat(parentCaptor.getValue().getParentRelationship()).isEqualTo("조부");
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getSuccessCount()).isEqualTo(1);
        verify(openAiHeaderMappingClient).classifyParentRelationship(1L, "할배");
    }

    @Test
    void upload_skips_parent_when_relationship_invalid() throws Exception {
        MockMultipartFile file = buildExcel(
                List.of("학생이름", "연락처", "반", "부모1이름", "부모1폰", "부모1관계", "부모1카드번호", "부모1결제여부", "부모1알람여부"),
                List.of("김학생", "01011112222", "A반", "김보호자", "01099998888", "삼촌", "1234", "Y", "N")
        );

        when(academyStudentService.registerStudent(eq(1L), any()))
                .thenReturn(AcademyStudentResponse.builder().studentId(1L).studentName("김학생").build());

        AcademyStudentUploadResponse response = service.upload(1L, file, 1L, false, false);

        verify(parentRepository, never()).save(any());
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getSuccessCount()).isEqualTo(0);
    }

    private MockMultipartFile buildExcel(List<String> headers, List<String> row) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet();
            var headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                headerRow.createCell(i).setCellValue(headers.get(i));
            }

            var dataRow = sheet.createRow(1);
            for (int i = 0; i < row.size(); i++) {
                dataRow.createCell(i).setCellValue(row.get(i));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new MockMultipartFile("file", "students.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        }
    }
}

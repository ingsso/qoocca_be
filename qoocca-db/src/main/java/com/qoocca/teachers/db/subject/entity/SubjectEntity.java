package com.qoocca.teachers.db.subject.entity;

import com.qoocca.teachers.db.academy.entity.AcademySubjectEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "subject")
public class SubjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    private Long id;

    @Column(name = "main_subject_code", nullable = false, length = 100)
    private String mainSubjectCode;

    @Column(name = "detail_subject", nullable = false, length = 191)
    private String detailSubject;

    @Column(name = "subject_code", nullable = false, length = 100)
    private String subjectCode;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private List<AcademySubjectEntity> academySubjects = new ArrayList<>();
}
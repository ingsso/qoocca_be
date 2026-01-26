package com.qoocca.teachers.db.academy.entity;

import com.qoocca.teachers.db.age.entity.AgeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "academy_age")
public class AcademyAgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "academy_age_id")
    private Long id;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id")
    private AcademyEntity academy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "age_id")
    private AgeEntity age;

    public AcademyAgeEntity(AcademyEntity academy, AgeEntity age) {
        this.academy = academy;
        this.age = age;
    }
}

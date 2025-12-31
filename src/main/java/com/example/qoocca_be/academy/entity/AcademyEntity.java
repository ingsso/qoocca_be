package com.example.qoocca_be.academy.entity;

import com.example.qoocca_be.user.entity.UserEntity;
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
@Table(name = "academy")
public class AcademyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "academy_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String province;

    @Column(nullable = false, length = 100)
    private String town;

    @Column(nullable = false, length = 191)
    private String address;

    @Column(name = "base_address")
    private String baseAddress;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "name", nullable = false, length = 191)
    private String name;

    @Column(name = "blog_url", columnDefinition = "TEXT")
    private String blogUrl;

    @Column(name = "brief_info", columnDefinition = "TEXT")
    private String briefInfo;

    @Column(name = "cost_info", columnDefinition = "TEXT")
    private String costInfo;

    @Column(name = "detail_info", columnDefinition = "TEXT")
    private String detailInfo;

    @Column(name = "experience_class", columnDefinition = "TEXT")
    private String experienceClass;

    @Column(name = "instagram_url", columnDefinition = "TEXT")
    private String instagramUrl;

    @Column(name = "lesson_type")
    private String lessonType;

    @Column(name = "level_test")
    private String levelTest;

    @Column(name = "operating_hours")
    private String operatingHours;

    @Column(name = "parking_info")
    private String parkingInfo;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "shuttle_info")
    private String shuttleInfo;

    @Column(name = "website_url", columnDefinition = "TEXT")
    private String websiteUrl;

    @Column(name = "academy_crawl_map_id")
    private String academyCrawlMapId;

    @Column(name = "is_show", nullable = false)
    @Builder.Default
    private Boolean isShow = true;

    private String certificate;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "academy_images", cascade = CascadeType.ALL)
    private List<AcademyImageEntity> images = new ArrayList<>();

    @OneToMany(mappedBy = "academy_subjects", cascade = CascadeType.ALL)
    private List<AcademySubjectEntity> academySubjects = new ArrayList<>();
}

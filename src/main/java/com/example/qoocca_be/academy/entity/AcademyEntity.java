package com.example.qoocca_be.academy.entity;

import com.example.qoocca_be.academy.dto.AcademyRequestDto;
import com.example.qoocca_be.age.entity.AgeEntity;
import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import com.example.qoocca_be.subject.entity.SubjectEntity;
import com.example.qoocca_be.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
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
@DynamicUpdate
@Table(name = "academy")
public class AcademyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "academy_id")
    private Long id;

    @Column(nullable = false, length = 191)
    private String address;

    @Column(nullable = false, name = "base_address")
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

    private String certificate;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "academy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AcademyImageEntity> images = new ArrayList<>();

    @OneToMany(mappedBy = "academy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AcademyAgeEntity> academyAges = new ArrayList<>();

    @OneToMany(mappedBy = "academy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AcademySubjectEntity> academySubjects = new ArrayList<>();






    public void updateApprovalStatus(ApprovalStatus status) {
        this.approvalStatus = status;
    }


    public void update(AcademyRequestDto req) {
        if (req.getName() != null) this.name = req.getName();
        if (req.getBaseAddress() != null) this.baseAddress = req.getBaseAddress();
        if (req.getDetailAddress() != null) this.detailAddress = req.getDetailAddress();
        if (req.getPhoneNumber() != null) this.phoneNumber = req.getPhoneNumber();
        if (req.getBriefInfo() != null) this.briefInfo = req.getBriefInfo();
        if (req.getDetailInfo() != null) this.detailInfo = req.getDetailInfo();

        updateAddress(req.getBaseAddress(), req.getDetailAddress());
    }

    public void updateAddress(String baseAddress, String detailAddress) {
        if (baseAddress != null && !baseAddress.isBlank()) {
            this.baseAddress = baseAddress;

            if (detailAddress != null) {
                this.detailAddress = detailAddress;
            }

            this.address = (this.baseAddress + " " + (this.detailAddress != null ? this.detailAddress : "")).trim();
        }
    }

    public void updateAges(List<AgeEntity> ages) {
        this.academyAges.clear();
        if (ages != null) {
            ages.forEach(age -> this.academyAges.add(new AcademyAgeEntity(this, age)));
        }
    }

    public void updateSubjects(List<SubjectEntity> subjects) {
        this.academySubjects.clear();
        if (subjects != null) {
            subjects.forEach(subject -> this.academySubjects.add(new AcademySubjectEntity(this, subject)));
        }
    }

    public void updateImages(List<String> imageUrls) {
        this.images.clear();
        if (imageUrls != null) {
            imageUrls.forEach(url -> this.images.add(AcademyImageEntity.builder()
                    .imageUrl(url)
                    .academy(this)
                    .build()));
        }
    }
}

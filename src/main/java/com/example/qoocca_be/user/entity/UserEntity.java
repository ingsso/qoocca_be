package com.example.qoocca_be.user.entity;

import com.example.qoocca_be.academy.entity.AcademyEntity;
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
@DynamicUpdate
@Builder
@Table(name = "user")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "kakao_id")
    private String kakaoId;

    @Column(name = "naver_id")
    private String naverId;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(name = "user_phone_number", length = 11)
    private String phoneNumber;

    @Column(nullable = false)
    @Builder.Default
    private Boolean agree = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean marketingAgree = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean alarm = true;

    @Column(nullable = false)
    private String role;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<AcademyEntity> academies = new ArrayList<>();
}
package com.example.qoocca_be.parent.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "teachers_parent")
public class ParentEntity {

    /* =========================
     * PK
     * ========================= */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parent_id")
    private Long parentId;

    /* =========================
     * 부모 정보
     * ========================= */
    @Column(name = "card_num")
    private String cardNum;

    @Column(name = "card_state")
    private Boolean cardState;

    @Column(name = "parent_relationship")
    private String parentRelationship; // 아버지 / 어머니 / 보호자 (공통 성격)

    @Column(name = "parent_phone")
    private String parentPhone;

    @Column(name = "is_pay")
    private Boolean isPay;

    @Column(name = "alarm")
    private Boolean alarm;

    /* =========================
     * 생성 / 수정일
     * ========================= */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* =========================
     * 연관관계 (Student ↔ Parent 매핑)
     * ========================= */
    /*@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<StudentParentEntity> studentParents = new ArrayList<>();*/
}

package com.qoocca.teachers.db.fcm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FirebaseTokenRepository extends JpaRepository<FirebaseTokenEntity, Long> {
    Optional<FirebaseTokenEntity> findByParentId(Long parentId);
}

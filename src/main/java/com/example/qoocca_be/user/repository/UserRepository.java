package com.example.qoocca_be.user.repository;

import com.example.qoocca_be.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByPhone(String phone);

    Optional<UserEntity> findByKakaoId(String kakaoId);
    Optional<UserEntity> findByNaverId(String naverId);
}

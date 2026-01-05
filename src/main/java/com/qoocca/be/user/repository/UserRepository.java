package com.qoocca.be.user.repository;

import com.qoocca.be.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByPhoneNumber(String phone);

    Optional<UserEntity> findByKakaoId(String kakaoId);
    Optional<UserEntity> findByNaverId(String naverId);
}

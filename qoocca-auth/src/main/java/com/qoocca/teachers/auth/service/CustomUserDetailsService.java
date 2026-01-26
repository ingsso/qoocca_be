package com.qoocca.teachers.auth.service;

import com.qoocca.teachers.db.user.entity.UserEntity;
import com.qoocca.teachers.db.user.repository.UserRepository;
import com.qoocca.teachers.auth.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userIdStr) throws UsernameNotFoundException {
        Long userId = Long.parseLong(userIdStr);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 ID로 사용자를 찾을 수 없습니다."));

        return new CustomUserDetails(userEntity);
    }
}

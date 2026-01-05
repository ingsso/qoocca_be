package com.example.qoocca_be.user.service;

import com.example.qoocca_be.user.entity.UserEntity;
import com.example.qoocca_be.user.repository.UserRepository;
import com.example.qoocca_be.user.security.CustomUserDetails;
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

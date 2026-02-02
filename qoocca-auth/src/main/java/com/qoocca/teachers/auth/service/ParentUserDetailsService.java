package com.qoocca.teachers.auth.service;

import com.qoocca.teachers.auth.security.ParentUserDetails;
import com.qoocca.teachers.db.parent.entity.ParentEntity;
import com.qoocca.teachers.db.parent.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParentUserDetailsService implements UserDetailsService {

    private final ParentRepository parentRepository;

    @Override
    public UserDetails loadUserByUsername(String parentIdStr) throws UsernameNotFoundException {
        Long parentId = Long.parseLong(parentIdStr);

        ParentEntity parentEntity = parentRepository.findById(parentId)
                .orElseThrow(() -> new UsernameNotFoundException("Parent not found"));

        return new ParentUserDetails(parentEntity);
    }
}

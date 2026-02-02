package com.qoocca.teachers.auth.security;

import com.qoocca.teachers.db.parent.entity.ParentEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record ParentUserDetails(ParentEntity parentEntity) implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_PARENT"));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(parentEntity.getParentId());
    }

    public Long getParentId() {
        return parentEntity.getParentId();
    }
}

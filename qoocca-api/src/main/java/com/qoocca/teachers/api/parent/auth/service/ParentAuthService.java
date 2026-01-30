package com.qoocca.teachers.api.parent.auth.service;

import com.qoocca.teachers.api.parent.auth.dto.ParentLoginRequest;
import com.qoocca.teachers.api.parent.auth.dto.ParentLoginResponse;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.parent.entity.ParentEntity;
import com.qoocca.teachers.db.parent.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParentAuthService {

    private final ParentRepository parentRepository;

    @Transactional(readOnly = true)
    public ParentLoginResponse login(ParentLoginRequest request) {
        if (request.getParentPhone() == null || request.getParentPhone().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        ParentEntity parent = parentRepository.findByParentPhone(request.getParentPhone())
                .orElseThrow(() -> new CustomException(ErrorCode.PARENT_NOT_FOUND));

        return ParentLoginResponse.from(parent);
    }
}

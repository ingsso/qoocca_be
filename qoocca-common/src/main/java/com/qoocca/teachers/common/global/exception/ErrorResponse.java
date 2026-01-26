package com.qoocca.teachers.common.global.exception;

import lombok.Builder;

@Builder
public record ErrorResponse(int status, String code, String message) {
}

package com.example.qoocca_be.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(400, "U002", "비밀번호가 일치하지 않습니다."),
    SOCIAL_PROVIDER_NOT_FOUND(400, "S001", "지원하지 않는 소셜 로그인 제공자입니다."),
    INVALID_SOCIAL_CODE(400, "S002", "소셜 로그인 인증 코드가 유효하지 않습니다."),
    AGE_NOT_FOUND(404, "AC002", "존재하지 않는 연령대 ID입니다."),
    SUBJECT_NOT_FOUND(404, "AC003", "존재하지 않는 과목 ID입니다."),

    INVALID_TOKEN(401, "A001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "A002", "만료된 토큰입니다."),
    LOGOUT_TOKEN(401, "A003", "이미 로그아웃된 토큰입니다."),
    TOKEN_MISMATCH(401, "A004", "서버의 토큰 정보와 일치하지 않습니다."),

    ACADEMY_NOT_APPROVED(403, "AC004", "학원 승인이 완료되지 않아 접근할 수 없습니다."),
    ACADEMY_NOT_FOUND(404, "AC001", "학원 정보를 찾을 수 없습니다."),
    NO_AUTHORITY(403, "G001", "권한이 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}

package com.qoocca.teachers.common.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(400, "U002", "비밀번호가 일치하지 않습니다."),
    PHONE_ALREADY_IN_USE(400, "U003", "이미 사용 중인 전화번호입니다."),
    REQUIRED_AGREEMENTS_MISSING(400, "U004", "필수 약관에 동의하지 않았습니다."),
    SOCIAL_PROVIDER_NOT_FOUND(400, "S001", "지원하지 않는 소셜 로그인 제공자입니다."),
    INVALID_SOCIAL_CODE(400, "S002", "소셜 로그인 인증 코드가 유효하지 않습니다."),
    SMS_INVALID_PHONE(400, "SM001", "유효하지 않은 전화번호입니다."),
    SMS_CODE_INVALID(400, "SM002", "인증 코드가 유효하지 않거나 만료되었습니다."),
    SMS_NOT_VERIFIED(400, "SM003", "전화번호 인증이 완료되지 않았습니다."),
    AGE_NOT_FOUND(404, "AC002", "존재하지 않는 연령대 ID입니다."),
    SUBJECT_NOT_FOUND(404, "AC003", "존재하지 않는 과목 ID입니다."),

    INVALID_TOKEN(401, "A001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "A002", "만료된 토큰입니다."),
    LOGOUT_TOKEN(401, "A003", "이미 로그아웃된 토큰입니다."),
    TOKEN_MISMATCH(401, "A004", "서버 토큰 정보가 일치하지 않습니다."),

    ACADEMY_NOT_APPROVED(403, "AC004", "학원 승인 완료 전에는 접근할 수 없습니다."),
    ACADEMY_NOT_FOUND(404, "AC001", "학원 정보를 찾을 수 없습니다."),
    ACADEMY_CERTIFICATE_SAVE_FAILED(500, "AC005", "학원 인증서 저장에 실패했습니다."),
    ACADEMY_IMAGE_SAVE_FAILED(500, "AC006", "학원 이미지 저장에 실패했습니다."),
    ACADEMY_IMAGE_DELETE_FAILED(500, "AC007", "학원 이미지 삭제에 실패했습니다."),
    ACADEMY_IMAGE_NOT_FOUND(404, "AC009", "학원 이미지를 찾을 수 없습니다."),
    ACADEMY_STUDENT_RELATION_NOT_FOUND(404, "AC008", "학원-학생 관계를 찾을 수 없습니다."),
    NO_AUTHORITY(403, "G001", "권한이 없습니다."),

    /**
     * 출결 관련
     */
    ATTENDANCE_ALREADY_EXISTS (400, "AT001", "이미 해당 수업에 대한 오늘자 출결 기록이 존재합니다."),
    ATTENDANCE_NOT_FOUND (404, "AT002", "출결 기록을 찾을 수 없습니다."),
    CLASS_NOT_FOUND_FOR_TIME (400, "AT003", "현재 시간에 수강하는 수업이 없습니다."),

    /**
     * 클래스 및 수강생 관련
     */
    STUDENT_NOT_FOUND(404, "ST001", "학생 정보를 찾을 수 없습니다."),
    STUDENT_PARENT_RELATION_NOT_FOUND(404, "ST002", "학생-부모 관계를 찾을 수 없습니다."),
    PARENT_NOT_FOUND(404, "ST003", "부모 정보를 찾을 수 없습니다."),
    CLASS_NOT_FOUND(404, "C001", "클래스 정보를 찾을 수 없습니다."),
    STUDENT_ALREADY_ENROLLED(400, "C002", "이미 해당 클래스에 등록된 학생입니다."),
    ENROLLMENT_NOT_FOUND(404, "C003", "수강 정보를 찾을 수 없습니다."),

    /**
     * 수납 관련
     */
    DUPLICATE_RECEIPT_IN_MONTH (400, "R001", "해당 월에 이미 수납 기록이 존재합니다."),
    RECEIPT_NOT_FOUND (404, "R002", "수납 기록(영수증)을 찾을 수 없습니다."),
    RECEIPT_ACCESS_DENIED (403, "R003", "본인의 수납 기록만 접근할 수 있습니다."),
    PAYMENT_METHOD_NOT_FOUND(400, "R004", "등록된 결제 수단이 없습니다."),
    INVALID_RECEIPT_STATUS(400, "R005", "수납 상태 변경이 허용되지 않습니다."),

    /**
     * 시스템 공통
     */
    INVALID_INPUT_VALUE (400, "G002", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR (500, "G003", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}

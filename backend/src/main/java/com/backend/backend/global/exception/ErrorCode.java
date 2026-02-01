package com.backend.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "E400-001", "입력값이 올바르지 않습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "E400-002", "입력 타입이 올바르지 않습니다."),
    MISSING_INPUT_VALUE(HttpStatus.BAD_REQUEST, "E400-003", "필수 입력값이 누락되었습니다."),
    MAPPING_INVALID(HttpStatus.BAD_REQUEST, "E400-004", "허용되지 않은 gesture 또는 action이 포함되어 있습니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E401-001", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "E401-002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "E401-003", "만료된 토큰입니다."),
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "액세스 토큰이 유효하지 않습니다."),

    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_FORBIDDEN", "해당 매핑셋을 수정할 권한이 없습니다."),
    DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "E403-001", "해당 매핑셋을 삭제할 권한이 없습니다."),

    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "E404-001", "계정을 찾을 수 없습니다."),
    GESTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "E404-002", "제스처를 찾을 수 없습니다."),
    ACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "E404-003", "기능을 찾을 수 없습니다."),
    MAPPING_NOT_FOUND(HttpStatus.NOT_FOUND, "E404-004", "존재하지 않는 매핑셋입니다."),
    MAPPING_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "E404-005", "매핑 항목을 찾을 수 없습니다."),
    SET_NOT_FOUND(HttpStatus.NOT_FOUND, "E404-006", "매핑셋을 찾을 수 없습니다."),
    MAPPING_ITEM_NOT_BELONG(HttpStatus.BAD_REQUEST, "E400-008", "해당 매핑셋에 속하지 않는 매핑 항목입니다."),

    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "E405-001", "허용되지 않은 HTTP 메서드입니다."),

    DUPLICATE_GESTURE(HttpStatus.CONFLICT, "E409-001", "동일한 제스처가 이미 매핑에 존재합니다."),
    DUPLICATE_ACTION(HttpStatus.CONFLICT, "E409-002", "동일한 기능이 이미 매핑에 존재합니다."),
    DUPLICATE_MAPPING_ITEM(HttpStatus.CONFLICT, "E409-003", "동일한 제스처-기능 조합이 이미 존재합니다."),
    MAPPING_CONFLICT(HttpStatus.CONFLICT, "E409-004", "매핑 항목이 충돌합니다."),
    // 409
    MAPPING_PRESET_ALREADY_ACTIVE(HttpStatus.CONFLICT, "MAPPING_PRESET_ALREADY_ACTIVE", "이미 대표 매핑셋으로 적용되어 있습니다."),
    MAPPING_SET_NAME_DUPLICATE(HttpStatus.CONFLICT, "MAPPING_SET_NAME_DUPLICATE", "이미 사용 중인 매핑셋 이름입니다."),

    // 404
    MAPPING_PRESET_NOT_FOUND(HttpStatus.NOT_FOUND, "MAPPING_PRESET_NOT_FOUND", "존재하지 않는 매핑셋입니다."),
    MAPPING_SET_NOT_FOUND(HttpStatus.NOT_FOUND, "MAPPING_SET_NOT_FOUND", "존재하지 않는 매핑셋입니다."),
    MAPPING_DELETE_NOT_FOUND(HttpStatus.NOT_FOUND, "E404-001", "존재하지 않는 매핑셋입니다."),

    // 409 - delete
    CANNOT_DELETE_ACTIVE_MAPPING(HttpStatus.CONFLICT, "E409-002", "대표 매핑셋은 삭제할 수 없습니다."),

    // 400
    INVALID_MAPPING_SET_NAME(HttpStatus.BAD_REQUEST, "COMMON400", "매핑셋 이름은 1자 이상 30자 이하로 입력해야 합니다."),
    OTP_EXPIRED(HttpStatus.BAD_REQUEST, "E400-004", "인증번호가 만료되었거나 존재하지 않습니다."),
    OTP_MISMATCH(HttpStatus.BAD_REQUEST, "E400-005", "인증번호가 일치하지 않습니다."),

    VERIFICATION_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "E400-006", "인증 토큰이 만료되었거나 존재하지 않습니다."),
    VERIFICATION_TOKEN_MISMATCH(HttpStatus.BAD_REQUEST, "E400-007", "인증 토큰이 일치하지 않습니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E500-001", "서버 내부 오류가 발생했습니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E500-002", "데이터베이스 오류가 발생했습니다."),
    COMMON500(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 내부 오류가 발생했습니다."),
    EMAIL_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "E500-003", "이메일 발송에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

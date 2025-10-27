package com.mudosa.musinsa.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorCode {

    // auth
    VALIDATION_ERROR("10001", "입력 값 검증 오류입니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("10002", "내부 서버 오류입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED_USER("10003", "인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_JWT("10004", "JWT 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_JWT("10005", "잘못된 JWT 토큰입니다.", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED_JWT("10006", "지원하지 않는 JWT 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EMPTY_JWT("10007", "JWT 클레임이 비어있습니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("10008", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    //payment
    PAYMENT_APPROVAL_FAILED("30001", "결제 승인에 실패했습니다", HttpStatus.BAD_REQUEST),PAYMENT_PG_NOT_FOUND("30002", "존재하지 않는 PG사 입니다", HttpStatus.BAD_REQUEST)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}

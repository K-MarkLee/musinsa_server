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
    PAYMENT_APPROVAL_FAILED("30001", "결제 승인에 실패했습니다", HttpStatus.BAD_REQUEST),
    PAYMENT_PG_NOT_FOUND("30002", "존재하지 않는 PG사 입니다", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND("30003", "존재하지 않는 결제입니다", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_APPROVED("30004", "이미 승인된 결제입니다", HttpStatus.CONFLICT),
    PAYMENT_AMOUNT_MISMATCH("30005", "결제 금액이 일치하지 않습니다", HttpStatus.BAD_REQUEST),
    
    //order
    ORDER_NOT_FOUND("40001", "존재하지 않는 주문입니다", HttpStatus.NOT_FOUND),
    ORDER_ALREADY_COMPLETED("40002", "이미 완료된 주문입니다", HttpStatus.CONFLICT),
    ORDER_ITEM_NOT_FOUND("40003", "주문 상품을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    
    //inventory
    INVENTORY_NOT_FOUND("50001", "재고 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK("50002", "재고가 부족합니다", HttpStatus.BAD_REQUEST),
    
    //coupon
    COUPON_NOT_FOUND("60001", "쿠폰을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    COUPON_ALREADY_USED("60002", "이미 사용된 쿠폰입니다", HttpStatus.BAD_REQUEST),
    COUPON_EXPIRED("60003", "만료된 쿠폰입니다", HttpStatus.BAD_REQUEST),

    //brand
    BRAND_NOT_FOUND("70001", "브랜드를 찾을 수 없습니다", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}

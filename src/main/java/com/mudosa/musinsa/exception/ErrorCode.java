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
    RESOURCE_NOT_FOUND("10009", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),USER_NOT_FOUND("10010","사용자가 없습니다",HttpStatus.NOT_FOUND ),

    //payment
    PAYMENT_APPROVAL_FAILED("30001", "결제 승인에 실패했습니다", HttpStatus.BAD_REQUEST),
    PAYMENT_PG_NOT_FOUND("30002", "존재하지 않는 PG사 입니다", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND("30003", "존재하지 않는 결제입니다", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_APPROVED("30004", "이미 승인된 결제입니다", HttpStatus.CONFLICT),
    PAYMENT_AMOUNT_MISMATCH("30005", "결제 금액이 일치하지 않습니다", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_STATUS("30006", "결제 상태가 유효하지 않습니다", HttpStatus.BAD_REQUEST ),
    INVALID_PG_TRANSACTION_ID("30007","결제 PG 트랜잭션 ID가 유효하지 않습니다", HttpStatus.BAD_REQUEST ),
    INVALID_PAYMENT_METHOD("30008", "결제수단이 유효하지 않습니다", HttpStatus.BAD_REQUEST),
    
    //order
    ORDER_NOT_FOUND("40001", "존재하지 않는 주문입니다", HttpStatus.NOT_FOUND),
    ORDER_ALREADY_COMPLETED("40002", "이미 완료된 주문입니다", HttpStatus.CONFLICT),
    ORDER_ITEM_NOT_FOUND("40003", "주문 상품을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_ORDER_STATUS("40004","유효하지 않은 주문 상태 입니다.", HttpStatus.NOT_FOUND ),
    INVALID_ORDER_STATUS_TRANSITION("40005","허용되지 않는 주문 상태입니다.", HttpStatus.BAD_REQUEST ),
    INVALID_DISCOUNT_AMOUNT("40006","할인 적용이 유효하지 않습니다",HttpStatus.BAD_REQUEST ),
    ORDER_INVALID_AMOUNT("40007","주문 상품 가격이 유효하지 않습니다", HttpStatus.BAD_REQUEST),
    PRODUCT_OPTION_NOT_FOUND("40008", "상품 옵션을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ORDER_INSUFFICIENT_STOCK("40009", "재고가 부족한 상품이 있습니다", HttpStatus.BAD_REQUEST),
    
    //inventory
    INVENTORY_NOT_FOUND("50001", "재고 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK("50002", "재고가 부족합니다", HttpStatus.BAD_REQUEST),
    
    //coupon
    COUPON_NOT_FOUND("60001", "쿠폰을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    COUPON_ALREADY_USED("60002", "이미 사용된 쿠폰입니다", HttpStatus.BAD_REQUEST),
    COUPON_EXPIRED("60003", "만료된 쿠폰입니다", HttpStatus.BAD_REQUEST),
    COUPON_APLIED_FALIED("60004", "쿠폰 적용에 실패했습니다", HttpStatus.BAD_REQUEST),
    INVALID_COUPON_TYPE("60005","지원하지 않은 쿠폰 타입입니다",HttpStatus.BAD_REQUEST ),
    COUPON_NOT_USED("60006","사용되지 않은 쿠폰은 복구할 수 없습니다", HttpStatus.BAD_REQUEST ),
    COUPON_ROLLBACK_INVALID("60007","쿠폰이 다른 주무에서 사용되어 복구할 수 없습니다",HttpStatus.BAD_REQUEST ),

    //brand
    BRAND_NOT_FOUND("70001", "브랜드를 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    //product
    PRODUCT_OPTION_NOT_AVAILABLE("80001","상품 옵션이 유효하지 않습니다",HttpStatus.BAD_REQUEST ),
    
    //inventory
    INVENTORY_NOT_AVAILABLE("90001", "재고가 유효하지 않습니다.", HttpStatus.BAD_REQUEST );

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}

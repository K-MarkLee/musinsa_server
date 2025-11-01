package com.mudosa.musinsa.product.presentation;

import com.mudosa.musinsa.common.dto.ApiResponse;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.presentation.controller.ProductController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = ProductController.class)
public class ProductExceptionHandler {

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(RuntimeException e) {
    ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
    String message = e.getMessage() != null ? e.getMessage() : errorCode.getMessage();
    log.warn("[Product] Validation 예외 발생: {}", message, e);
    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), message);
    return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
  }
}

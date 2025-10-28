package com.mudosa.musinsa.exception;

import com.mudosa.musinsa.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /* 비즈니스 예외 처리 */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn("BusinessException 발생: {} - {}", errorCode.getCode(), errorCode.getMessage(), e);
    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), errorCode.getMessage());
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  /* 유효성 검사 예외 처리 */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(
      MethodArgumentNotValidException e) {
    ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
    StringBuilder errorMessage = new StringBuilder(errorCode.getMessage());
    for (FieldError error : e.getBindingResult().getFieldErrors()) {
      errorMessage.append(String.format(" [%s: %s]", error.getField(), error.getDefaultMessage()));
    }
    log.warn("Validation 실패: {}", errorMessage.toString());
    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), errorMessage.toString());
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  /* 예상치 못한 예외 처리 */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    log.error("Unhandled 예외 발생: {}", e.getMessage(), e);
    String[] errorSplit = e.getClass().toString().split("\\.");
    ApiResponse<Void> response =
        ApiResponse.failure(errorSplit[errorSplit.length - 1], e.getMessage());

    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}

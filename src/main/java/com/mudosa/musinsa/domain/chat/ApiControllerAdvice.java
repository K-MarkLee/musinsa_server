package com.mudosa.musinsa.domain.chat;

import com.mudosa.musinsa.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiControllerAdvice {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(HandlerMethodValidationException.class)
  public ApiResponse<Object> handleValidation(HandlerMethodValidationException e) {
    String message = "요청 값이 올바르지 않습니다.";
    if (!e.getAllErrors().isEmpty()) {
      message = e.getAllErrors().get(0).getDefaultMessage();
    }
    return ApiResponse.failure("400", message, null);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(BindException.class)
  public ApiResponse<Object> bindException(BindException e) {
    ObjectError data = e.getBindingResult().getAllErrors().get(0);
    return ApiResponse.failure(
        "400",
        data.getDefaultMessage(),
        null
    );
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String paramName = ex.getName(); // 예: chatId
    String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";
    String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";

    String message = String.format(
        "요청 파라미터 ‘%s’의 값 ‘%s’은(는) 올바르지 않습니다. %s 형식의 값이어야 합니다.",
        paramName, invalidValue, requiredType
    );

    ApiResponse body = ApiResponse.builder()
        .success(false)
        .errorCode("400")
        .message(message)
        .build();

    return ResponseEntity.badRequest().body(body);
  }

}

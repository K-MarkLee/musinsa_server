package com.mudosa.musinsa.common.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiResponse<T> {

  private boolean success; // 요청 성공 여부
  private T data; // 실제 데이터 (성공 시 또는 실패 시 추가 정보)
  private String errorCode; // 실패 시 에러 코드
  private String message; // 성공/실패 시 메시지
  private LocalDateTime timestamp; // 응답 생성 시간

  // 성공 응답 생성 (데이터 + 메시지)
  public static <T> ApiResponse<T> success(T data, String message) {
    return ApiResponse.<T>builder()
        .success(true)
        .data(data)
        .message(message)
        .timestamp(LocalDateTime.now())
        .build();
  }

  // 성공 응답 생성 (데이터만)
  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }

  // 실패 응답 생성 (에러 코드 + 메시지만)
  public static <T> ApiResponse<T> failure(String errorCode, String message) {
    return ApiResponse.<T>builder()
        .success(false)
        .errorCode(errorCode)
        .message(message)
        .timestamp(LocalDateTime.now())
        .build();
  }
  // 실패 응답 생성 (데이터 포함)
  public static <T> ApiResponse<T> failure(String errorCode, String message, T data) {
    return ApiResponse.<T>builder()
        .success(false)
        .errorCode(errorCode)
        .message(message)
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }
}

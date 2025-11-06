package com.mudosa.musinsa.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mudosa.musinsa.common.dto.ApiResponse;
import com.mudosa.musinsa.exception.CustomJwtException;
import com.mudosa.musinsa.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    CustomJwtException jwtEx = (CustomJwtException) request.getAttribute("jwtException");

    ApiResponse<Void> errorResponse =
        (jwtEx != null)
            ? ApiResponse.failure(jwtEx.getErrorCode().getCode(), jwtEx.getErrorCode().getMessage())
            : ApiResponse.failure(
                ErrorCode.UNAUTHORIZED_USER.getCode(), ErrorCode.UNAUTHORIZED_USER.getMessage());

    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}

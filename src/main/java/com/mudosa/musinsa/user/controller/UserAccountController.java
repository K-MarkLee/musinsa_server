package com.mudosa.musinsa.user.controller;

import com.mudosa.musinsa.common.dto.ApiResponse;
import com.mudosa.musinsa.user.application.UserAccountService;
import com.mudosa.musinsa.user.controller.dto.UserCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "회원 관리", description = "사용자 관리 관련 API")
public class UserAccountController {
    private final UserAccountService userAccountService;

    @Operation(summary = "회원 가입", description = "회원 가입 기능")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody UserCreateRequest request) {
        userAccountService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

}

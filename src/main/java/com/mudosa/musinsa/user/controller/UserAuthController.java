package com.mudosa.musinsa.user.controller;

import com.mudosa.musinsa.common.dto.ApiResponse;
import com.mudosa.musinsa.user.application.UserAuthService;
import com.mudosa.musinsa.user.controller.dto.LoginRequest;
import com.mudosa.musinsa.user.controller.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.mudosa.musinsa.user.CookieUtils.createDeleteRefreshTokenCookie;
import static com.mudosa.musinsa.user.CookieUtils.createRefreshTokenCookie;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "회원 인증", description = " 로그인, 인증 관련 API")
public class UserAuthController {
    private final UserAuthService userAuthService;

    /* 로그인 */
    @Operation(summary = "로그인", description = "자체 로그인 후 JWT를 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request){
        log.info("로그인 요청: email={}", request.getEmail());
        TokenResponse token = userAuthService.login(request);
        log.info("로그인 성공: accessToken 발급 완료");
        return buildTokenResponse(token);
    }

    /* 토큰 재발급 */
    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> getAccessTokenByRefreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken // HttpOnly 쿠키에서 읽어옴
    ){
        log.info("토큰 재발급 요청: refreshToken={}", refreshToken);
        if(refreshToken == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //401 에러 반환
        }
        TokenResponse token = userAuthService.refreshToken(refreshToken);
        log.info("토큰 재발급 성공: accessToken 및 refreshToken 갱신 완료");
        return buildTokenResponse(token);
    }

    /* 로그아웃 */
    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ){
        String accessToken = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        }

        if (refreshToken != null) {
            userAuthService.logout(refreshToken, accessToken);
        }

        log.info("로그아웃 처리 완료: refreshToken 삭제됨");
        ResponseCookie deleteCookie = createDeleteRefreshTokenCookie(); // 만료용 쿠키 생성
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(ApiResponse.success(null));
    }

    /* accessToken 과 refreshToken을 body와 쿠키에 담아 반환 */
    private ResponseEntity<ApiResponse<TokenResponse>> buildTokenResponse(TokenResponse tokenResponse) {
        ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.refreshToken());  // refreshToken 쿠키 생성
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(tokenResponse));
    }
}

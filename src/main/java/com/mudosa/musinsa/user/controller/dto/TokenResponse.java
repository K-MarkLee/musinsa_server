package com.mudosa.musinsa.user.controller.dto;


public record TokenResponse(
        String accessToken, String refreshToken) {
}

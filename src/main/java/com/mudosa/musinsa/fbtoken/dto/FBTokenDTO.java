package com.mudosa.musinsa.fbtoken.dto;

import lombok.*;

@Getter
@RequiredArgsConstructor
public class FBTokenDTO {
    private String memberId;
    private String token;

    @Builder
    public FBTokenDTO(Long memberId, String token) {
        this.memberId = String.valueOf(memberId);
        this.token = token;
    }

    @Builder
    public FBTokenDTO(String memberId, String token) {
        this.memberId = memberId;
        this.token = token;
    }
}

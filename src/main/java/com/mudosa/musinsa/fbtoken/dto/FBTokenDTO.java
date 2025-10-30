package com.mudosa.musinsa.fbtoken.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FBTokenDTO {
    private Long tokenId;
    private String firebaseTokenKey;
    private Long userId;
}

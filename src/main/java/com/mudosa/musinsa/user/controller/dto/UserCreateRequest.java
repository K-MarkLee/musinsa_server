package com.mudosa.musinsa.user.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreateRequest {
    private String email;
    private String userName;
    private String password;
    private String contactNumber;
    private String address;
    private String avatarUrl;
}

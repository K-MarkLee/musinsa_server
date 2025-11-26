package com.mudosa.musinsa.fbtoken.controller;

import com.mudosa.musinsa.common.dto.ApiResponse;
import com.mudosa.musinsa.fbtoken.dto.FBTokenDTO;
import com.mudosa.musinsa.fbtoken.service.FirebaseTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fbtoken")
public class FirebaseTokenController {

    private final FirebaseTokenService firebaseTokenService;

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<Void>> subscribeFCM(@RequestBody FBTokenDTO fbTokenDTO){
        try {
//            log.info(fbTokenDTO.getToken());
            firebaseTokenService.createFirebaseToken(fbTokenDTO);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure("FB_TOKEN_CONFLICT", "Firebase token already exists"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }
}

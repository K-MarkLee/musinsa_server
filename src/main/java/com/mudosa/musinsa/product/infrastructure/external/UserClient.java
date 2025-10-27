package com.mudosa.musinsa.product.infrastructure.external;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 사용자 도메인 연동 Client
 * User 도메인 API를 호출하여 사용자 정보 조회
 */
@Component
@RequiredArgsConstructor
public class UserClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${user.service.url:http://localhost:8082}")
    private String userServiceUrl;
    
    /**
     * 사용자 ID로 사용자 정보 조회
     * @param userId 사용자 ID
     * @return 사용자 응답 정보
     */
    public UserResponse findById(Long userId) {
        String url = userServiceUrl + "/api/users/" + userId;
        try {
            return restTemplate.getForObject(url, UserResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("사용자 정보 조회 실패: " + userId, e);
        }
    }
    
    /**
     * 여러 사용자 ID로 한번에 조회
     * @param userIds 사용자 ID 목록
     * @return 사용자 응답 정보 목록
     */
    public UserListResponse findByIds(List<Long> userIds) {
        String url = userServiceUrl + "/api/users/batch";
        try {
            return restTemplate.postForObject(url, userIds, UserListResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("사용자 정보 일괄 조회 실패", e);
        }
    }
    
    /**
     * 사용자 ID 목록 유효성 검증
     * @param userIds 사용자 ID 목록
     * @return 유효한 사용자 수
     */
    public Boolean validateUsers(List<Long> userIds) {
        String url = userServiceUrl + "/api/users/validate";
        try {
            return restTemplate.postForObject(url, userIds, Boolean.class);
        } catch (Exception e) {
            throw new RuntimeException("사용자 유효성 검증 실패", e);
        }
    }
    
    /**
     * 사용자 응답 DTO
     */
    public static class UserResponse {
        private Long userId;
        private String username;
        private String email;
        private String status;
        
        // Getters
        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getStatus() { return status; }
        
        // Setters
        public void setUserId(Long userId) { this.userId = userId; }
        public void setUsername(String username) { this.username = username; }
        public void setEmail(String email) { this.email = email; }
        public void setStatus(String status) { this.status = status; }
    }
    
    /**
     * 사용자 목록 응답 DTO
     */
    public static class UserListResponse extends java.util.ArrayList<UserResponse> {
        public UserListResponse() { super(); }
    }
}
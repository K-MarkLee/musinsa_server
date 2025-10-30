package com.mudosa.musinsa.product.infrastructure.external;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

// 사용자 도메인 API와 통신해 사용자 정보를 제공하는 클라이언트이다.
@Component
@RequiredArgsConstructor
public class UserClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${user.service.url:http://localhost:8082}")
    private String userServiceUrl;
    
    // 단일 사용자 ID로 외부 서비스를 호출해 정보를 조회한다.
    public UserResponse findById(Long userId) {
        String url = userServiceUrl + "/api/users/" + userId;
        try {
            return restTemplate.getForObject(url, UserResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("사용자 정보 조회 실패: " + userId, e);
        }
    }
    
    // 사용자 ID 목록을 전달해 외부 서비스에서 일괄 조회한다.
    public UserListResponse findByIds(List<Long> userIds) {
        String url = userServiceUrl + "/api/users/batch";
        try {
            return restTemplate.postForObject(url, userIds, UserListResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("사용자 정보 일괄 조회 실패", e);
        }
    }
    
    // 사용자 ID 목록의 유효성을 외부 서비스에 위임해 검증한다.
    public Boolean validateUsers(List<Long> userIds) {
        String url = userServiceUrl + "/api/users/validate";
        try {
            return restTemplate.postForObject(url, userIds, Boolean.class);
        } catch (Exception e) {
            throw new RuntimeException("사용자 유효성 검증 실패", e);
        }
    }
    
    // 외부 사용자 서비스의 응답을 담는 DTO이다.
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
    
    // 사용자 응답 DTO 목록을 표현하는 헬퍼 클래스이다.
    public static class UserListResponse extends java.util.ArrayList<UserResponse> {
        public UserListResponse() { super(); }
    }
}
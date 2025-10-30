package com.mudosa.musinsa.product.infrastructure.external;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

// 브랜드 도메인 API와 통신해 브랜드 정보를 조회하는 클라이언트이다.
@Component
@RequiredArgsConstructor
public class BrandClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${brand.service.url:http://localhost:8081}")
    private String brandServiceUrl;
    
    // 단일 브랜드 ID로 외부 서비스를 호출해 정보를 조회한다.
    public BrandResponse findById(Long brandId) {
        String url = brandServiceUrl + "/api/brands/" + brandId;
        try {
            return restTemplate.getForObject(url, BrandResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("브랜드 정보 조회 실패: " + brandId, e);
        }
    }
    
    // 브랜드 ID 목록을 전달해 외부 서비스에서 일괄 조회한다.
    public BrandListResponse findByIds(java.util.List<Long> brandIds) {
        String url = brandServiceUrl + "/api/brands/batch";
        try {
            return restTemplate.postForObject(url, brandIds, BrandListResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("브랜드 정보 일괄 조회 실패", e);
        }
    }
    
    // 외부 브랜드 서비스의 응답을 담는 DTO이다.
    public static class BrandResponse {
        private Long brandId;
        private String nameKo;
        private String nameEn;
        private String status;
        
        // Getters
        public Long getBrandId() { return brandId; }
        public String getNameKo() { return nameKo; }
        public String getNameEn() { return nameEn; }
        public String getStatus() { return status; }
        
        // Setters
        public void setBrandId(Long brandId) { this.brandId = brandId; }
        public void setNameKo(String nameKo) { this.nameKo = nameKo; }
        public void setNameEn(String nameEn) { this.nameEn = nameEn; }
        public void setStatus(String status) { this.status = status; }
    }
    
    // 브랜드 응답 DTO 목록을 표현하는 헬퍼 클래스이다.
    public static class BrandListResponse extends java.util.ArrayList<BrandResponse> {
        public BrandListResponse() { super(); }
    }
}
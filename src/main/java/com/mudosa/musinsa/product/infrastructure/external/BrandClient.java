package com.mudosa.musinsa.product.infrastructure.external;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 브랜드 도메인 연동 Client
 * Brand 도메인 API를 호출하여 브랜드 정보 조회
 */
@Component
@RequiredArgsConstructor
public class BrandClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${brand.service.url:http://localhost:8081}")
    private String brandServiceUrl;
    
    /**
     * 브랜드 ID로 브랜드 정보 조회
     * @param brandId 브랜드 ID
     * @return 브랜드 응답 정보
     */
    public BrandResponse findById(Long brandId) {
        String url = brandServiceUrl + "/api/brands/" + brandId;
        try {
            return restTemplate.getForObject(url, BrandResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("브랜드 정보 조회 실패: " + brandId, e);
        }
    }
    
    /**
     * 여러 브랜드 ID로 한번에 조회
     * @param brandIds 브랜드 ID 목록
     * @return 브랜드 응답 정보 목록
     */
    public BrandListResponse findByIds(java.util.List<Long> brandIds) {
        String url = brandServiceUrl + "/api/brands/batch";
        try {
            return restTemplate.postForObject(url, brandIds, BrandListResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("브랜드 정보 일괄 조회 실패", e);
        }
    }
    
    /**
     * 브랜드 응답 DTO
     */
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
    
    /**
     * 브랜드 목록 응답 DTO
     */
    public static class BrandListResponse extends java.util.ArrayList<BrandResponse> {
        public BrandListResponse() { super(); }
    }
}
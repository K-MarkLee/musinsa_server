package com.mudosa.musinsa.product.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * 상품 옵션-옵션값 매핑 엔티티
 * 
 * 개선된 구조:
 * - ProductOption과 OptionValue만 직접 연결
 * - OptionName은 OptionValue를 통해 자연스럽게 접근
 * - 복합 키가 2개 컬럼으로 단순화
 * 
 * 상품 내 동일 옵션명 중복 방지:
 * - OptionValue가 이미 특정 OptionName에 종속되어 있음
 * - 따라서 ProductOption-OptionValue 조합만으로도 옵션명+값의 유니크성 보장
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_option_value", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_option_id", "option_value_id"}))
public class ProductOptionValue {
    
    @EmbeddedId
    private ProductOptionValueId id;
    
    @MapsId("productOptionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;
    
    @MapsId("optionValueId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_value_id")
    private OptionValue optionValue;
    
    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter
    public static class ProductOptionValueId implements Serializable {
        
        @Column(name = "product_option_id")
        private Long productOptionId;
        
        @Column(name = "option_value_id")
        private Long optionValueId;
        
        public ProductOptionValueId(Long productOptionId, Long optionValueId) {
            this.productOptionId = productOptionId;
            this.optionValueId = optionValueId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductOptionValueId that = (ProductOptionValueId) o;
            return Objects.equals(productOptionId, that.productOptionId) &&
                   Objects.equals(optionValueId, that.optionValueId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(productOptionId, optionValueId);
        }
    }

    @Builder
    public ProductOptionValue(ProductOption productOption, OptionValue optionValue) {
        // 엔티티 기본 무결성 검증
        if (productOption == null) {
            throw new IllegalArgumentException("상품 옵션은 필수입니다.");
        }
        if (optionValue == null) {
            throw new IllegalArgumentException("옵션 값은 필수입니다.");
        }
        
        this.productOption = productOption;
        this.optionValue = optionValue;
        refreshIdentifiers();
    }

    // ProductOption에 늦게 연결될 수 있으므로 식별자 갱신 메서드 분리
    void attachTo(ProductOption productOption) {
        this.productOption = productOption;
        refreshIdentifiers();
    }

    void refreshIdentifiers() {
        Long productOptionId = this.productOption != null ? this.productOption.getProductOptionId() : null;
        Long optionValueId = this.optionValue != null ? this.optionValue.getOptionValueId() : null;
        this.id = new ProductOptionValueId(productOptionId, optionValueId);
    }
}
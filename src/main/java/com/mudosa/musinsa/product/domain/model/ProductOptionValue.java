package com.mudosa.musinsa.product.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;

// 상품 옵션과 옵션 값을 매핑해 유니크한 조합을 보장하는 엔티티이다.
// OptionValue를 통해 OptionName에 접근하므로 조인 구조가 단순해진다.
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
        
        // 복합 키를 구성하는 생성자이다.
        public ProductOptionValueId(Long productOptionId, Long optionValueId) {
            this.productOptionId = productOptionId;
            this.optionValueId = optionValueId;
        }
        
        // 동일 조합 여부를 비교한다.
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductOptionValueId that = (ProductOptionValueId) o;
            return Objects.equals(productOptionId, that.productOptionId) &&
                   Objects.equals(optionValueId, that.optionValueId);
        }
        
        // 복합 키 해시 값을 계산한다.
        @Override
        public int hashCode() {
            return Objects.hash(productOptionId, optionValueId);
        }
    }

    // 외부 노출 생성 메서드 + 필수 값 검증
    public static ProductOptionValue create(ProductOption productOption, OptionValue optionValue) {
        if (productOption == null) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_REQUIRED);
        }
        if (optionValue == null) {
            throw new BusinessException(ErrorCode.OPTION_VALUE_REQUIRED);
        }
        return new ProductOptionValue(productOption, optionValue);
    }

    @Builder
    private ProductOptionValue(ProductOption productOption, OptionValue optionValue) {
        this.productOption = productOption;
        this.optionValue = optionValue;
        refreshIdentifiers();
    }

    // ProductOption이 나중에 연결될 때 식별자를 갱신한다.
    void attachTo(ProductOption productOption) {
        this.productOption = productOption;
        refreshIdentifiers();
    }

    // 현재 연관 상태에 맞춰 복합 키를 재구성한다.
    void refreshIdentifiers() {
        Long productOptionId = this.productOption != null ? this.productOption.getProductOptionId() : null;
        Long optionValueId = this.optionValue != null ? this.optionValue.getOptionValueId() : null;
        this.id = new ProductOptionValueId(productOptionId, optionValueId);
    }
}
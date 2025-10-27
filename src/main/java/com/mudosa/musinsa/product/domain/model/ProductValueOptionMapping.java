package com.mudosa.musinsa.product.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_value_option_mapping")
public class ProductValueOptionMapping {
    
    @EmbeddedId
    private ProductValueOptionMappingId id;
    
    @MapsId("productOptionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;
    
    @MapsId("optionValueId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_value_id")
    private OptionValue optionValue;
    
    @MapsId("optionNameId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_name_id")
    private OptionName optionName;
    
    @Builder
    public ProductValueOptionMapping(ProductOption productOption, OptionValue optionValue, OptionName optionName) {
        this.productOption = productOption;
        this.optionValue = optionValue;
        this.optionName = optionName;
        this.id = new ProductValueOptionMappingId(
            productOption.getProductOptionId(),
            optionValue.getOptionValueId(),
            optionName.getOptionNameId()
        );
    }
    
    // 도메인 로직: 수정
    public void modify(ProductOption productOption, OptionValue optionValue, OptionName optionName) {
        if (productOption != null) {
            this.productOption = productOption;
            this.id.productOptionId = productOption.getProductOptionId();
        }
        if (optionValue != null) {
            this.optionValue = optionValue;
            this.id.optionValueId = optionValue.getOptionValueId();
        }
        if (optionName != null) {
            this.optionName = optionName;
            this.id.optionNameId = optionName.getOptionNameId();
        }
    }
    
    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter
    public static class ProductValueOptionMappingId implements Serializable {
        
        @Column(name = "product_option_id")
        private Long productOptionId;
        
        @Column(name = "option_value_id")
        private Long optionValueId;
        
        @Column(name = "option_name_id")
        private Long optionNameId;
        
        public ProductValueOptionMappingId(Long productOptionId, Long optionValueId, Long optionNameId) {
            this.productOptionId = productOptionId;
            this.optionValueId = optionValueId;
            this.optionNameId = optionNameId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductValueOptionMappingId that = (ProductValueOptionMappingId) o;
            return Objects.equals(productOptionId, that.productOptionId) &&
                   Objects.equals(optionValueId, that.optionValueId) &&
                   Objects.equals(optionNameId, that.optionNameId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(productOptionId, optionValueId, optionNameId);
        }
    }
}
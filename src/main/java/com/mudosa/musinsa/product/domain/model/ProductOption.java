package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 상품 옵션 엔티티
 * Product 애그리거트 내부
 * 
 * 특정 옵션 조합의 가격을 나타냅니다.
 * 예: 사이즈(270) + 색상(블랙) = 150,000원
 */
@Entity
@Table(
    name = "product_option",
    indexes = {
        @Index(name = "idx_prodopt_product_id", columnList = "product_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOption extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "product_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal productPrice;
    
    /**
     * 이 상품 옵션을 구성하는 옵션값들과의 매핑
     * 예: [사이즈:270, 색상:블랙]
     */
    @OneToMany(mappedBy = "productOption", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductValueOptionMapping> optionValueMappings = new ArrayList<>();
    
    /**
     * 상품 옵션 생성
     */
    public static ProductOption create(BigDecimal price) {
        ProductOption option = new ProductOption();
        option.productPrice = price;
        return option;
    }
    
    /**
     * Product 할당 (Package Private)
     */
    void assignProduct(Product product) {
        this.product = product;
    }
    
    /**
     * 옵션값 매핑 추가
     */
    public void addOptionValueMapping(OptionValue optionValue) {
        ProductValueOptionMapping mapping = ProductValueOptionMapping.create(this, optionValue);
        this.optionValueMappings.add(mapping);
    }
    
    /**
     * 가격 변경
     */
    public void updatePrice(BigDecimal newPrice) {
        this.productPrice = newPrice;
    }
    
    /**
     * 옵션 설명 생성 (옵션값들을 조합)
     * 예: "사이즈: 270, 색상: 블랙"
     */
    public String getOptionDescription() {
        return optionValueMappings.stream()
            .map(mapping -> mapping.getOptionValue().getOptionName().getOptionName() + 
                          ": " + mapping.getOptionValue().getOptionValue())
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    }
}

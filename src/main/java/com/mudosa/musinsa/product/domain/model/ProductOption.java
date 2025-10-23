package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 상품 옵션 엔티티
 * Product 애그리거트 내부
 */
@Entity
@Table(name = "product_option")
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
    
    @Column(name = "option_name", nullable = false, length = 100)
    private String optionName;
    
    @Column(name = "product_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal productPrice;
    
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;
    
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
    
    /**
     * 상품 옵션 생성
     */
    public static ProductOption create(String optionName, BigDecimal price, Integer stock) {
        ProductOption option = new ProductOption();
        option.optionName = optionName;
        option.productPrice = price;
        option.stockQuantity = stock;
        option.isAvailable = true;
        return option;
    }
    
    /**
     * Product 할당 (Package Private)
     */
    void assignProduct(Product product) {
        this.product = product;
    }
    
    /**
     * 재고 차감
     */
    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        this.stockQuantity -= quantity;
    }
    
    /**
     * 재고 증가
     */
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }
}

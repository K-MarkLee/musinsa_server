package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inventory")
public class Inventory extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id", nullable = false, unique = true)
    private ProductOption productOption;
    
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;
    
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;
    
    @Builder
    public Inventory(ProductOption productOption, Integer stockQuantity, Boolean isAvailable) {
        this.productOption = productOption;
        this.stockQuantity = stockQuantity;
        this.isAvailable = isAvailable != null ? isAvailable : true;
    }
    
    // 도메인 로직: 정보 수정
    public void modify(Integer stockQuantity, Boolean isAvailable) {
        if (stockQuantity != null) this.stockQuantity = stockQuantity;
        if (isAvailable != null) {
            this.isAvailable = isAvailable;
        } else {
            // isAvailable이 null이면 재고 수량에 따라 자동 설정
            this.isAvailable = this.stockQuantity > 0;
        }
    }
    
    // 도메인 로직: 재고 있음 여부 확인
    public boolean isInStock() {
        return this.stockQuantity > 0;
    }
    
    // 도메인 로직: 판매 가능 여부 확인
    public boolean isAvailable() {
        return Boolean.TRUE.equals(this.isAvailable);
    }

    //decrease
    public void decrease(int quantity){
        this.stockQuantity -= quantity;

    }

    public void increase(int quantity){
        this.stockQuantity += quantity;
    }

}
package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
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
    @JoinColumn(name = "product_option_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_inventory_prodopt"))
    private ProductOption productOption;
    
    @Column(name = "stock_quantity", nullable = false)
    private StockQuantity stockQuantity;
    
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;
    
    @Builder
    public Inventory(ProductOption productOption, StockQuantity stockQuantity, Boolean isAvailable) {
        this.productOption = productOption;
        this.stockQuantity = stockQuantity;
        this.isAvailable = isAvailable != null ? isAvailable : true;
    }
    
    // 도메인 로직
    public void modify(StockQuantity stockQuantity, Boolean isAvailable) {
        if (stockQuantity != null) {
            this.stockQuantity = stockQuantity;
        }
        if (isAvailable != null) {
            this.isAvailable = isAvailable;
        } else {
            // isAvailable이 null이면 재고 수량에 따라 자동 설정
            this.isAvailable = this.stockQuantity.getValue() > 0;
        }
    }
    
    public boolean isInStock() {
        return this.stockQuantity.getValue() > 0;
    }
    
}
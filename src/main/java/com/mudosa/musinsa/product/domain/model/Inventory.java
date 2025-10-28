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
    @JoinColumn(name = "product_option_id", nullable = false, unique = true)
    private ProductOption productOption;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "stock_quantity"))
    private StockQuantity stockQuantity;
    
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;


    public void decrease(int quantity) {
        if (this.stockQuantity.getValue() < quantity) {
            throw new IllegalStateException(
                    String.format("재고가 부족합니다. 요청: %d, 현재: %d", quantity, this.stockQuantity.getValue()));
        }
        this.stockQuantity.decrease(quantity);
        if (this.stockQuantity.getValue() == 0) {
            this.isAvailable = false;
        }
    }

    public void increase(int quantity) {
        this.stockQuantity.increase(quantity);
        if (this.stockQuantity.getValue() > 0) {
            this.isAvailable = true;
        }
    }


}
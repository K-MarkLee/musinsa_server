package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 상품 옵션 재고를 관리하는 엔티티이다.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inventory")
public class Inventory extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "stock_quantity"))
    private StockQuantity stockQuantity;

    // 재고 객체를 생성하며 수량과 판매 가능 여부를 지정한다.
    @Builder
    public Inventory(StockQuantity stockQuantity) {
        // 필수 파라미터를 확인해 무결성을 보장한다.
        if (stockQuantity == null) {
            throw new IllegalArgumentException("재고 수량은 필수입니다.");
        }
        this.stockQuantity = stockQuantity;
    }

    // 요청 수량만큼 재고를 감소시키고 품절 여부를 갱신한다.
    public void decrease(int quantity) {
        if (this.stockQuantity.getValue() < quantity) {
            throw new IllegalStateException(
                String.format("재고가 부족합니다. 요청: %d, 현재: %d", quantity, this.stockQuantity.getValue()));
        }
        this.stockQuantity.decrease(quantity);
    }

    // 요청 수량만큼 재고를 증가시키고 판매 가능 상태를 갱신한다.
    public void increase(int quantity) {
        this.stockQuantity.increase(quantity);
    }
    

    public boolean isSufficientStock(int requestedQuantity) {
        if (requestedQuantity <= 0) {
            throw new IllegalArgumentException("요청 수량은 0보다 커야 합니다.");
        }
        return this.stockQuantity.getValue() >= requestedQuantity;
    }


}
package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
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

    // 외부 노출 생성 메서드 + 필수 값 검증
    public static Inventory create(StockQuantity stockQuantity) {
        if (stockQuantity == null) {
            throw new BusinessException(ErrorCode.INVENTORY_STOCK_QUANTITY_REQUIRED);
        }
        return new Inventory(stockQuantity);
    }

    @Builder
    private Inventory(StockQuantity stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    // 요청 수량만큼 재고를 감소시키고 품절 여부를 갱신한다.
    public void decrease(int quantity) {
        if (this.stockQuantity.getValue() < quantity) {
            throw new BusinessException(ErrorCode.INVENTORY_INSUFFICIENT_STOCK, "재고가 부족합니다. 현재 재고: " + this.stockQuantity.getValue() + ", 요청 수량: " + quantity);
        }
        this.stockQuantity.decrease(quantity);
    }

    // 요청 수량만큼 재고를 증가시키고 판매 가능 상태를 갱신한다.
    public void increase(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INVENTORY_UPDATE_VALUE);
        }
        this.stockQuantity.increase(quantity);
    }
    
    // 요청 수량만큼 재고가 충분한지 확인한다.
    public boolean isSufficientStock(int requestedQuantity) {
        if (requestedQuantity <= 0 || stockQuantity == null) {
            throw new BusinessException(ErrorCode.INVENTORY_STOCK_QUANTITY_REQUIRED);
        }
        return this.stockQuantity.getValue() >= requestedQuantity;
    }


}

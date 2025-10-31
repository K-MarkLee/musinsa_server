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

// 상품 옵션 재고와 판매 가능 상태를 관리하는 엔티티이다.
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

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    // 재고 객체를 생성하며 수량과 판매 가능 여부를 지정한다.
    @Builder
    public Inventory(StockQuantity stockQuantity, Boolean isAvailable) {
        // 필수 파라미터를 확인해 무결성을 보장한다.
        if (stockQuantity == null) {
            throw new IllegalArgumentException("재고 수량은 필수입니다.");
        }

        this.stockQuantity = stockQuantity;
        // 재고가 존재하면 기본적으로 판매 가능, 0이면 자동으로 false
        if (stockQuantity.getValue() > 0) {
            this.isAvailable = isAvailable != null ? isAvailable : true;
        } else {
            this.isAvailable = false;
        }
    }

    // 요청 수량만큼 재고를 감소시키고 품절 여부를 갱신한다.
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

    // 재고 수량을 새 값으로 덮어쓰고 필요 시 판매 가능 여부를 조정한다.
    public void overrideQuantity(int quantity) {
        this.stockQuantity = new StockQuantity(quantity);
        if (this.stockQuantity.getValue() == 0) {
            this.isAvailable = false;
        }
    }

    // 재고 판매 가능 여부를 직접 전환한다.
    public void changeAvailability(boolean available) {
        if (available && (this.stockQuantity == null || this.stockQuantity.getValue() <= 0)) {
            throw new IllegalStateException("재고 수량이 0 이하일 때는 판매 가능으로 전환할 수 없습니다.");
        }
        this.isAvailable = available;
    }

    // 요청 수량만큼 재고를 증가시키고 판매 가능 상태를 갱신한다.
    public void increase(int quantity) {
        this.stockQuantity.increase(quantity);
        if (this.stockQuantity.getValue() > 0) {
            this.isAvailable = true;
        }
    }


}
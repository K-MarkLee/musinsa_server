package com.mudosa.musinsa.event.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 이벤트 옵션 엔티티
 * Event 애그리거트 내부
 */
@Entity
@Table(name = "event_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventOption extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_option_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    
    @Column(name = "product_option_id", nullable = false)
    private Long productOptionId; // ProductOption 참조 (ID만)
    
    @Column(name = "event_stock_quantity")
    private Integer eventStockQuantity;
    
    @Column(name = "event_price", precision = 10, scale = 2)
    private BigDecimal eventPrice;
    
    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;
    
    /**
     * 이벤트 옵션 생성
     */
    public static EventOption create(
        Long productOptionId,
        Integer stockQuantity,
        BigDecimal eventPrice,
        BigDecimal discountRate
    ) {
        EventOption option = new EventOption();
        option.productOptionId = productOptionId;
        option.eventStockQuantity = stockQuantity;
        option.eventPrice = eventPrice;
        option.discountRate = discountRate;
        return option;
    }
    
    /**
     * Event 할당 (Package Private)
     */
    void assignEvent(Event event) {
        this.event = event;
    }
    
    /**
     * 재고 차감
     */
    public void decreaseStock(int quantity) {
        if (this.eventStockQuantity < quantity) {
            throw new IllegalStateException("이벤트 재고가 부족합니다.");
        }
        this.eventStockQuantity -= quantity;
    }
}

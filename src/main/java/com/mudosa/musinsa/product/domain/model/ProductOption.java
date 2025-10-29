package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.common.vo.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_option")
public class ProductOption extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id")
    private Long productOptionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false, unique = true)
    private Inventory inventory;
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "product_price", nullable = false, precision = 10, scale = 2))
    private Money productPrice;
    
    @OneToMany(mappedBy = "productOption", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductOptionValue> productOptionValue = new ArrayList<>();
    
    @Builder
    public ProductOption(Product product, Money productPrice, Inventory inventory) {
        // 엔티티 기본 무결성 검증
        if (product == null) {
            throw new IllegalArgumentException("상품은 옵션에 필수입니다.");
        }
        if (productPrice == null || productPrice.isLessThanOrEqual(Money.ZERO)) {
            throw new IllegalArgumentException("상품 가격은 0원보다 커야 합니다.");
        }
        if (inventory == null) {
            throw new IllegalArgumentException("재고 정보는 옵션에 필수입니다.");
        }
        
        this.product = product;
        this.productPrice = productPrice;
        this.inventory = inventory;
    }
    
    // 패키지 private: 상품 참조 설정 (Product 애그리거트에서만 사용)
    void setProduct(Product product) {
        this.product = product;
    }



}
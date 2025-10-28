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

@Entity
@Getter
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
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "product_price", nullable = false, precision = 10, scale = 2))
    private Money productPrice;

    
    @Builder
    public ProductOption(Product product, Money productPrice) {
        this.product = product;
        this.productPrice = productPrice;
    }
    
    // 도메인 로직: 정보 수정
    public void modify(Money productPrice) {
        if (productPrice != null) this.productPrice = productPrice;
    }
    
    // 도메인 로직: 상품 변경
    public void changeProduct(Product product) {
        if (product != null) this.product = product;
    }
    
    // 도메인 로직: 가격 변경
    public void changePrice(Money productPrice) {
        if (productPrice != null) this.productPrice = productPrice;
    }
    
    // 도메인 로직: 특정 상품의 옵션 여부 확인
    public boolean belongsToProduct(Product product) {
        return this.product != null && this.product.equals(product);
    }

}
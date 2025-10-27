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
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_option_product"))
    private Product product;
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "product_price", nullable = false, precision = 10, scale = 2))
    private Money productPrice;
    
    @OneToMany(mappedBy = "productOption", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductValueOptionMapping> productValueOptionMappings = new ArrayList<>();
    
    @Builder
    public ProductOption(Product product, Money productPrice) {
        this.product = product;
        this.productPrice = productPrice;
    }
    
    // 도메인 로직: 통합 수정
    public void modify(Money productPrice) {
        if (productPrice != null) {
            this.productPrice = productPrice;
        }
    }
}
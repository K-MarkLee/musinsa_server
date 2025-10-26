package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.product.domain.vo.ProductPrice;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_option", indexes = {
    @Index(name = "idx_prodopt_product_id", columnList = "product_id"),
    @Index(name = "idx_prodopt_price", columnList = "product_price")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id")
    private Long productOptionId;

    @Column(name = "product_id", nullable = false)  // FK (Product 도메인)
    private Long productId;

    @Embedded
    private ProductPrice productPrice;

    @OneToMany(mappedBy = "productOption", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductValueOptionMapping> productValueOptionMappings = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)  
    private Product product;

    // 연관관계 - Inventory 연결
    @OneToMany(mappedBy = "productOption", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inventory> inventories = new ArrayList<>();

    // 생성 메서드
    public static ProductOption create(Long productId, ProductPrice productPrice) {
        return new ProductOption(productId, productPrice);
    }

    // 비즈니스 메서드
    public void updatePrice(ProductPrice productPrice) {
        this.productPrice = productPrice;
    }

    public void updateProduct(Long productId) {
        this.productId = productId;
    }

    // 연관관계 메서드 - ProductValueOptionMapping 연결
    public void addProductValueOptionMapping(ProductValueOptionMapping mapping) {
        productValueOptionMappings.add(mapping);
    }

    public void removeProductValueOptionMapping(ProductValueOptionMapping mapping) {
        productValueOptionMappings.remove(mapping);
    }

    // 연관관계 메서드 - Inventory 연결
    public void addInventory(Inventory inventory) {
        inventories.add(inventory);
    }

    // 연관관계 메서드 - Inventory 연결 해제
    public void removeInventory(Inventory inventory) {
        inventories.remove(inventory);
    }

    // 비즈니스 메서드 - 재고 관리
    public Inventory getInventory() {
        return inventories.isEmpty() ? null : inventories.get(0);
    }

    public boolean hasInventory() {
        return !inventories.isEmpty();
    }

    public StockQuantity getTotalStock() {
        return inventories.stream()
            .map(Inventory::getStockQuantity)
            .reduce(StockQuantity.of(0), StockQuantity::add);
    }

    // JPA를 위한 protected 생성자
    protected ProductOption(Long productId, ProductPrice productPrice) {
        this.productId = productId;
        this.productPrice = productPrice;
    }
}
package com.mudosa.musinsa.product.domain.model;

/**
 * 과거 상품-카테고리 다대다 매핑을 담당하던 엔티티의 흔적을 남겨둔 플레이스홀더이다.
 * 현재는 상품이 역정규화된 categoryPath 만을 사용하므로 더 이상 JPA 매핑을 로딩하지 않는다.
 * 추후 다중 카테고리 요구가 생기면 별도 설계로 다시 도입한다.
 */
@Deprecated(forRemoval = true)
final class ProductCategory {

    private ProductCategory() {
        throw new IllegalStateException("ProductCategory is deprecated and should not be instantiated.");
    }
}
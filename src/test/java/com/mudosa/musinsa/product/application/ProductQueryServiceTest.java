package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.application.dto.ProductSearchCondition;
import com.mudosa.musinsa.product.application.dto.ProductSearchResponse;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.domain.model.*;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("ProductQueryService 단위 테스트")
class ProductQueryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductQueryService sut;

    private void setId(Object target, String fieldName, Long id) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("상품 검색")
    class SearchProducts {

        @Test
        @DisplayName("조건이 null이고 결과가 비어있으면 빈 응답을 반환한다.")
        void search_noCondition_returnsEmpty() {
            // Given: repository가 빈 페이지를 반환하도록 설정
        when(productRepository.findAllByFiltersWithPagination(anyList(), any(), any(), any(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // When: 조건 없이 검색 호출
            ProductSearchResponse resp = sut.searchProducts(null);

            // Then: 빈 결과와 페이징 정보
            assertThat(resp.getProducts()).isEmpty();
            assertThat(resp.getTotalElements()).isEqualTo(0);
            assertThat(resp.getPage()).isEqualTo(0);
        }

        @Test
        @DisplayName("키워드가 주어지면 키워드 전용 검색 메서드를 호출해 매핑된 요약을 반환한다.")
        void search_withKeyword_mapsSummary() {
            // Given: 상품 엔티티를 구성
            Product product = Product.builder()
                    .brand(null)
                    .productName("티셔츠")
                    .productInfo("편한 티")
                    .productGenderType(ProductGenderType.ALL)
                    .brandName("브랜드")
                    .categoryPath("상의/티셔츠")
                    .isAvailable(true)
                    .build();

            Image img = Image.create("http://example.com/thumb.jpg", true);
            product.addImage(img);

            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
            ProductOption option = ProductOption.create(product, new Money(10000), inv);
            product.addProductOption(option);

            // id 세팅(매퍼에서 사용)
            setId(product, "productId", 100L);
            setId(option, "productOptionId", 200L);
            setId(img, "imageId", 300L);

            Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.searchByKeywordWithFilters(anyString(), anyList(), any(), any(), any(), any(Pageable.class)))
                    .thenReturn(page);

            ProductSearchCondition condition = ProductSearchCondition.builder()
                    .keyword("티셔츠")
                    .pageable(Pageable.unpaged())
                    .build();

            // When: 검색 실행
            ProductSearchResponse resp = sut.searchProducts(condition);

            // Then: 매핑된 요약 정보 확인
            assertThat(resp.getProducts()).hasSize(1);
            ProductSearchResponse.ProductSummary s = resp.getProducts().get(0);
            assertThat(s.getProductId()).isEqualTo(100L);
            assertThat(s.getThumbnailUrl()).isEqualTo("http://example.com/thumb.jpg");
            assertThat(s.getHasStock()).isTrue();
            assertThat(s.getLowestPrice()).isEqualTo(new BigDecimal("10000.00"));
        }
    }

    @Nested
    @DisplayName("상품 상세 조회")
    class GetProductDetail {

        @Test
        @DisplayName("존재하는 상품 id를 조회하면 상세 응답을 반환한다.")
        void getProductDetail_success() {
            // Given: 상품 엔티티 구성
            Product product = Product.builder()
                    .brand(null)
                    .productName("바지")
                    .productInfo("편한 바지")
                    .productGenderType(ProductGenderType.ALL)
                    .brandName("브랜드")
                    .categoryPath("하의/바지")
                    .isAvailable(true)
                    .build();

            Image img = Image.create("http://example.com/img.jpg", false);
            product.addImage(img);
            Inventory inv = Inventory.builder().stockQuantity(new StockQuantity(2)).build();
            ProductOption option = ProductOption.create(product, new Money(30000), inv);
            product.addProductOption(option);

            setId(product, "productId", 11L);
            setId(img, "imageId", 12L);
            setId(option, "productOptionId", 13L);

            when(productRepository.findDetailById(11L)).thenReturn(Optional.of(product));

            // When: 상세 조회 실행
            ProductDetailResponse resp = sut.getProductDetail(11L);

            // Then: 상세 응답의 필수 필드를 검증
            assertThat(resp.getProductId()).isEqualTo(11L);
            assertThat(resp.getImages()).hasSize(1);
            assertThat(resp.getOptions()).hasSize(1);
        }

        @Test
        @DisplayName("존재하지 않는 상품 id를 조회하면 EntityNotFoundException이 발생한다.")
        void getProductDetail_notFound_throws() {
            // Given: repository가 비어있는 Optional을 반환하도록 설정
            when(productRepository.findDetailById(999L)).thenReturn(Optional.empty());

            // When / Then: 존재하지 않는 id로 호출하면 EntityNotFoundException 발생
            assertThatThrownBy(() -> sut.getProductDetail(999L)).isInstanceOf(EntityNotFoundException.class);
        }
    }
}

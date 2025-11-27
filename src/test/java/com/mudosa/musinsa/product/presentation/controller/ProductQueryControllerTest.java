package com.mudosa.musinsa.product.presentation.controller;

import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.application.dto.ProductSearchResponse;
import com.mudosa.musinsa.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ProductQueryController 테스트")
class ProductQueryControllerTest extends ControllerTestSupport {

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new CustomUserDetails(1L, "USER");
    }

    @Test
    @DisplayName("상품 검색을 수행한다.")
    void searchProducts() throws Exception {
        // given
        ProductSearchResponse response = ProductSearchResponse.builder()
            .products(List.of(
                ProductSearchResponse.ProductSummary.builder()
                    .productId(10L)
                    .brandId(1L)
                    .brandName("브랜드")
                    .productName("블랙 티셔츠")
                    .productInfo("설명")
                    .productGenderType("ALL")
                    .isAvailable(true)
                    .hasStock(true)
                    .lowestPrice(BigDecimal.valueOf(10000))
                    .thumbnailUrl("thumb.jpg")
                    .categoryPath("상의>티셔츠")
                    .build()
            ))
            .totalElements(1)
            .totalPages(1)
            .page(0)
            .size(24)
            .build();

        given(productQueryService.searchProducts(any()))
            .willReturn(response);

        // when // then
        mockMvc.perform(get("/api/products")
                .param("keyword", "티셔츠")
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.products[0].productId").value(10L))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("상품 상세를 조회한다.")
    void getProductDetail() throws Exception {
        // given
        ProductDetailResponse response = ProductDetailResponse.builder()
            .productId(20L)
            .brandId(1L)
            .brandName("브랜드")
            .productName("화이트 바지")
            .productInfo("설명")
            .productGenderType("ALL")
            .isAvailable(true)
            .categoryPath("하의>바지")
            .build();

        given(productQueryService.getProductDetail(20L))
            .willReturn(response);

        // when // then
        mockMvc.perform(get("/api/products/{productId}", 20L)
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(20L))
            .andExpect(jsonPath("$.productName").value("화이트 바지"));
    }
}

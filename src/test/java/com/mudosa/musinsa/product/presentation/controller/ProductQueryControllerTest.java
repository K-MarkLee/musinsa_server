package com.mudosa.musinsa.product.presentation.controller;

import com.mudosa.musinsa.product.application.ProductService;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.application.dto.ProductSearchResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("removal")
@WebMvcTest(ProductQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Nested
    @DisplayName("상품 조회")
    class ProductSearch {

        @Test
        @DisplayName("상품 검색 API 기본 응답")
        void searchProducts() throws Exception {
            ProductSearchResponse response = ProductSearchResponse.builder()
                .products(List.of())
                .totalElements(0)
                .totalPages(0)
                .page(0)
                .size(0)
                .build();

            Mockito.when(productService.searchProducts(any())).thenReturn(response);

            mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("상품 상세 조회")
        void getProductDetail() throws Exception {
            ProductDetailResponse detail = ProductDetailResponse.builder()
                .productId(1L)
                .productName("테스트 상품")
                .categoryPath("상의/티셔츠")
                .images(List.of())
                .options(List.of())
                .build();

            Mockito.when(productService.getProductDetail(1L)).thenReturn(detail);

            mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L));
        }
    }
}

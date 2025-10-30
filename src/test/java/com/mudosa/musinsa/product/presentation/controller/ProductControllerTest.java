package com.mudosa.musinsa.product.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.product.application.ProductService;
import com.mudosa.musinsa.product.application.dto.ProductCreateRequest;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.application.dto.ProductSearchResponse;
import com.mudosa.musinsa.product.application.dto.ProductUpdateRequest;
import com.mudosa.musinsa.product.domain.model.Category;
import com.mudosa.musinsa.product.domain.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final ProductService productService = Mockito.mock(ProductService.class);

    private final BrandRepository brandRepository = Mockito.mock(BrandRepository.class);

    private final CategoryRepository categoryRepository = Mockito.mock(CategoryRepository.class);

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
                .images(List.of())
                .options(List.of())
                .categories(List.of())
                .build();

            Mockito.when(productService.getProductDetail(1L)).thenReturn(detail);

            mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L));
        }
    }

    @Nested
    @DisplayName("상품 생성/수정/삭제")
    class ProductMutation {

        @Test
        @DisplayName("상품 생성")
        void createProduct() throws Exception {
            ProductCreateRequest request = ProductCreateRequest.builder()
                .brandId(1L)
                .productName("테스트")
                .productInfo("설명")
                .productGenderType("MEN")
                .brandName("브랜드")
                .categoryPath("상의/티셔츠")
                .isAvailable(true)
                .categoryId(1L)
                .images(List.of(ProductCreateRequest.ImageCreateRequest.builder()
                    .imageUrl("http://example.com/image")
                    .isThumbnail(true)
                    .build()))
                .options(List.of(ProductCreateRequest.OptionCreateRequest.builder()
                    .productPrice(BigDecimal.valueOf(10000))
                    .stockQuantity(10)
                    .inventoryAvailable(true)
                    .optionValueIds(List.of(1L))
                    .build()))
                .build();

            Mockito.when(brandRepository.findById(1L)).thenReturn(java.util.Optional.of(Brand.create("브랜드", "BRAND", BigDecimal.TEN)));
            Mockito.when(categoryRepository.findById(1L)).thenReturn(java.util.Optional.of(Category.builder().categoryName("상의").build()));
            Mockito.when(productService.createProduct(any(), any(), any())).thenReturn(10L);

            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/10"));
        }

        @Test
        @DisplayName("상품 수정")
        void updateProduct() throws Exception {
            ProductUpdateRequest request = ProductUpdateRequest.builder()
                .productName("수정된 상품")
                .productInfo("수정 설명")
                .productGenderType("WOMEN")
                .categoryPath("상의/셔츠")
                .isAvailable(true)
                .images(List.of(ProductUpdateRequest.ImageUpdateRequest.builder()
                    .imageUrl("http://example.com/thumb")
                    .isThumbnail(true)
                    .build()))
                .build();

            ProductDetailResponse detail = ProductDetailResponse.builder()
                .productId(1L)
                .productName("수정된 상품")
                .images(List.of())
                .options(List.of())
                .categories(List.of())
                .build();

            Mockito.when(productService.updateProduct(anyLong(), anyLong(), any())).thenReturn(detail);

            mockMvc.perform(put("/api/products/1")
                    .header("X-USER-ID", 99L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("수정된 상품"));
        }

        @Test
        @DisplayName("상품 삭제 (비활성화)")
        void deleteProduct() throws Exception {
            mockMvc.perform(delete("/api/products/1")
                    .header("X-USER-ID", 99L))
                .andExpect(status().isNoContent());

            Mockito.verify(productService).disableProduct(1L, 99L);
        }
    }
}

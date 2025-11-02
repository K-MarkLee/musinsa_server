package com.mudosa.musinsa.product.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.product.application.ProductService;
import com.mudosa.musinsa.product.application.dto.ProductCreateRequest;
import com.mudosa.musinsa.product.application.dto.ProductDetailResponse;
import com.mudosa.musinsa.product.application.dto.ProductOptionCreateRequest;
import com.mudosa.musinsa.product.application.dto.ProductUpdateRequest;
import com.mudosa.musinsa.product.domain.model.Category;
import com.mudosa.musinsa.product.domain.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("removal")
@WebMvcTest(BrandProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class BrandProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private BrandRepository brandRepository;

    @MockBean
    private CategoryRepository categoryRepository;

    @Nested
    @DisplayName("브랜드 관리자 상품 관리")
    class BrandProductManagement {

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
                    .optionValueIds(List.of(1L))
                    .build()))
                .build();

            Brand brand = Brand.create("브랜드", "BRAND", BigDecimal.TEN);
            Category category = Category.builder().categoryName("상의").build();

            Mockito.when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
            Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            Mockito.when(productService.createProduct(any(), any(), any())).thenReturn(10L);

            mockMvc.perform(post("/api/brands/1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/brands/1/products/10"));
        }

        @Test
        @DisplayName("상품 수정")
        void updateProduct() throws Exception {
            ProductUpdateRequest request = ProductUpdateRequest.builder()
                .productName("수정된 상품")
                .productInfo("수정 설명")
                .productGenderType("WOMEN")
                .isAvailable(true)
                .images(List.of(ProductUpdateRequest.ImageUpdateRequest.builder()
                    .imageUrl("http://example.com/thumb")
                    .isThumbnail(true)
                    .build()))
                .build();

            ProductDetailResponse detail = ProductDetailResponse.builder()
                .productId(1L)
                .productName("수정된 상품")
                .categoryPath("상의/티셔츠")
                .images(List.of())
                .options(List.of())
                .build();

            Mockito.when(productService.updateProductForBrand(anyLong(), anyLong(), any())).thenReturn(detail);

            mockMvc.perform(put("/api/brands/1/products/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("수정된 상품"));
        }

        @Test
        @DisplayName("상품 옵션 추가")
        void addProductOption() throws Exception {
            ProductOptionCreateRequest request = ProductOptionCreateRequest.builder()
                .productPrice(BigDecimal.valueOf(15000))
                .stockQuantity(5)
                .optionValueIds(List.of(1L, 2L))
                .build();

            ProductDetailResponse.OptionDetail optionDetail = ProductDetailResponse.OptionDetail.builder()
                .optionId(100L)
                .productPrice(BigDecimal.valueOf(15000))
                .stockQuantity(5)
                .hasStock(true)
                .optionValues(List.of())
                .build();

            Mockito.when(productService.addProductOption(anyLong(), anyLong(), any())).thenReturn(optionDetail);

            mockMvc.perform(post("/api/brands/1/products/1/options")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.optionId").value(100L));
        }

        @Test
        @DisplayName("상품 옵션 삭제")
        void deleteProductOption() throws Exception {
            mockMvc.perform(delete("/api/brands/1/products/1/options/200"))
                .andExpect(status().isNoContent());

            Mockito.verify(productService).removeProductOption(1L, 1L, 200L);
        }
    }
}

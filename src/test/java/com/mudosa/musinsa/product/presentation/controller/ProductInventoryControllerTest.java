package com.mudosa.musinsa.product.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mudosa.musinsa.product.application.ProductInventoryService;
import com.mudosa.musinsa.product.application.dto.ProductAvailabilityRequest;
import com.mudosa.musinsa.product.application.dto.ProductOptionStockResponse;
import com.mudosa.musinsa.product.application.dto.StockAdjustmentRequest;
import org.junit.jupiter.api.DisplayName;
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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("removal")
@WebMvcTest(ProductInventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductInventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductInventoryService productInventoryService;

    @Test
    @DisplayName("브랜드별 상품 옵션 재고 목록을 반환한다")
    void getProductOptionStocks() throws Exception {
        ProductOptionStockResponse response = ProductOptionStockResponse.builder()
            .productOptionId(200L)
            .productName("패딩")
            .productPrice(new BigDecimal("19900"))
            .stockQuantity(5)
            .hasStock(true)
            .optionValues(List.of(ProductOptionStockResponse.OptionValueSummary.builder()
                .optionValueId(400L)
                .optionName("사이즈")
                .optionValue("M")
                .build()))
            .build();

        Mockito.when(productInventoryService.getProductOptionStocks(1L, 100L))
            .thenReturn(List.of(response));

        mockMvc.perform(get("/api/brands/{brandId}/products/{productId}/inventory", 1L, 100L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].productOptionId").value(200L))
            .andExpect(jsonPath("$[0].stockQuantity").value(5));
    }

    @Test
    @DisplayName("옵션 재고를 증가시킨다")
    void increaseStock() throws Exception {
        StockAdjustmentRequest request = StockAdjustmentRequest.builder()
            .productOptionId(200L)
            .quantity(3)
            .build();

        mockMvc.perform(post("/api/brands/{brandId}/products/{productId}/inventory/increase", 1L, 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        Mockito.verify(productInventoryService)
            .addStock(eq(1L), eq(100L), argThat(r ->
                r.getProductOptionId().equals(200L) && r.getQuantity().equals(3)));
    }

    @Test
    @DisplayName("옵션 재고를 감소시킨다")
    void decreaseStock() throws Exception {
        StockAdjustmentRequest request = StockAdjustmentRequest.builder()
            .productOptionId(200L)
            .quantity(2)
            .build();

        mockMvc.perform(post("/api/brands/{brandId}/products/{productId}/inventory/decrease", 1L, 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        Mockito.verify(productInventoryService)
            .subtractStock(eq(1L), eq(100L), argThat(r ->
                r.getProductOptionId().equals(200L) && r.getQuantity().equals(2)));
    }

    @Test
    @DisplayName("상품 판매 가능 상태를 변경한다")
    void changeAvailability() throws Exception {
        ProductAvailabilityRequest request = ProductAvailabilityRequest.builder()
            .isAvailable(false)
            .build();

        mockMvc.perform(patch("/api/brands/{brandId}/products/{productId}/availability", 1L, 100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        Mockito.verify(productInventoryService)
            .updateProductAvailability(eq(1L), eq(100L), argThat(r ->
                Boolean.FALSE.equals(r.getIsAvailable())));
    }
}

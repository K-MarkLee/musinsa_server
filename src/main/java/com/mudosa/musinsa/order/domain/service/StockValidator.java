package com.mudosa.musinsa.order.domain.service;

import com.mudosa.musinsa.order.application.dto.InsufficientStockItem;
import com.mudosa.musinsa.order.domain.model.OrderProduct;
import com.mudosa.musinsa.order.domain.model.Orders;
import com.mudosa.musinsa.order.domain.model.StockValidationResult;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockValidator {


    public StockValidationResult validateStockForOrder(Orders order) {
        log.debug("재고 검증 시작 - orderId: {}, 상품 수: {}",
                order.getId(), order.getOrderProducts().size());

        List<InsufficientStockItem> insufficientItems = new ArrayList<>();

        for (OrderProduct orderProduct : order.getOrderProducts()) {
            validateStockForOrderProduct(orderProduct, insufficientItems);
        }

        if (!insufficientItems.isEmpty()) {
            log.warn("재고 부족 감지 - orderId: {}, 부족 상품 수: {}",
                    order.getId(), insufficientItems.size());
            return StockValidationResult.invalid(insufficientItems);
        }

        log.debug("재고 검증 성공 - orderId: {}", order.getId());
        return StockValidationResult.valid();
    }

    private void validateStockForOrderProduct(
            OrderProduct orderProduct,
            List<InsufficientStockItem> insufficientItems) {

        ProductOption productOption = orderProduct.getProductOption();
        Inventory inventory = productOption.getInventory();


        Integer requestedQty = orderProduct.getProductQuantity();
        Integer availableQty = inventory.getStockQuantity().getValue();

        if (availableQty < requestedQty) {
            log.warn("재고 부족 - productOptionId: {}, 요청: {}, 가능: {}",
                    productOption.getProductOptionId(), requestedQty, availableQty);

            insufficientItems.add(new InsufficientStockItem(
                    productOption.getProductOptionId(),
                    requestedQty,
                    availableQty
            ));
        }
    }
}

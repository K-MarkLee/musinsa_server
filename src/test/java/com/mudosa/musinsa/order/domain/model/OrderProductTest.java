package com.mudosa.musinsa.order.domain.model;

import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProductTest {

    @DisplayName("주문에 담기는 주문 상품을 생성한다. ")
    @Test
    void create(){
        //given
        Order order = Order.builder()
                        .orderNo("ord123").build();
        ProductOption productOption = ProductOption.builder()
                .productPrice(Money.of(2L))
                .build();

        //when
        OrderProduct result = OrderProduct.create(order, productOption, 2);

        //then
        assertThat(result.getOrder()).isEqualTo(order);
    }

}
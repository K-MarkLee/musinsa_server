package com.mudosa.musinsa.order.application.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PendingOrderResponse{
    private String orderNo;
    private List<PendingOrderItem> orderProducts;
    private List<OrderMemberCoupon> coupons;
    private String userName;
    private String userAddress;
    private String userContactNumber;
}

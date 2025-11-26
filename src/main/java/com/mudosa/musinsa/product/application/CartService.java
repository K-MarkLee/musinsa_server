package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.application.dto.CartItemCreateRequest;
import com.mudosa.musinsa.product.application.dto.CartItemDetailResponse;
import com.mudosa.musinsa.product.application.dto.CartItemResponse;
import com.mudosa.musinsa.product.domain.model.CartItem;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.repository.CartItemRepository;
import com.mudosa.musinsa.product.domain.repository.ProductOptionRepository;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductOptionRepository productOptionRepository;
    private final UserRepository userRepository;

    /* 장바구니 저장 */
    @Transactional
    public CartItemResponse addCartItem(Long userId, CartItemCreateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ProductOption productOption = productOptionRepository.findByIdWithProductAndInventory(request.getProductOptionId())
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));

        validateOptionAvailability(productOption);

        CartItem cartItem = cartItemRepository.findByUserIdAndProductOptionId(userId, productOption.getProductOptionId())
            .map(existing -> updateQuantity(existing, productOption, request.getQuantity()))
            .orElseGet(() -> createNewCartItem(user, productOption, request.getQuantity()));

        CartItem saved = cartItemRepository.save(cartItem);
        return CartItemResponse.from(saved);
    }

    /* 장바구니 조회 */
    @Transactional(readOnly = true)
    public List<CartItemDetailResponse> getCartItems(Long userId) {
        return cartItemRepository.findAllWithDetailsByUserId(userId).stream()
            .map(this::mapToDetailResponse)
            .collect(Collectors.toList());
    }

    /* 장바구니 수량 수정 */
    @Transactional
    public CartItemResponse updateCartItemQuantity(Long userId,
                                                   Long cartItemId,
                                                   int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "수량은 1개 이상이어야 합니다.");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                "장바구니 항목을 찾을 수 없습니다. cartItemId=" + cartItemId));

        if (cartItem.getUser() == null || !Objects.equals(cartItem.getUser().getId(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 장바구니 항목만 수정할 수 있습니다.");
        }

        Long productOptionId = cartItem.getProductOption() != null
            ? cartItem.getProductOption().getProductOptionId()
            : null;

        if (productOptionId == null) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_FOUND,
                "상품 옵션 정보를 찾을 수 없습니다. cartItemId=" + cartItemId);
        }

        ProductOption productOption = productOptionRepository.findByIdWithProductAndInventory(productOptionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));

        validateOptionAvailability(productOption);
        verifyRequestedQuantity(productOption, quantity);

        cartItem.changeQuantity(quantity);
        CartItem saved = cartItemRepository.save(cartItem);
        return CartItemResponse.from(saved);
    }

    @Transactional
    public void deleteCartItemsByProductOptions(Long userId, List<Long> productOptionIds) {
        if (productOptionIds == null || productOptionIds.isEmpty()) {
            return;
        }
        cartItemRepository.deleteByUserIdAndProductOptionIdIn(
                userId,
                productOptionIds
        );
    }

/* 장바구니 삭제 개별*/
    @Transactional
    public void deleteCartItem(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                "장바구니 항목을 찾을 수 없습니다. cartItemId=" + cartItemId));

        if (cartItem.getUser() == null || !Objects.equals(cartItem.getUser().getId(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 장바구니 항목만 삭제할 수 있습니다.");
        }

        cartItemRepository.delete(cartItem);
    }

    private CartItem updateQuantity(CartItem cartItem, ProductOption productOption, int additionalQuantity) {
        int targetQuantity = cartItem.getQuantity() + additionalQuantity;
        verifyRequestedQuantity(productOption, targetQuantity);
        cartItem.changeQuantity(targetQuantity);
        return cartItem;
    }

    private CartItem createNewCartItem(User user, ProductOption productOption, int quantity) {
        verifyRequestedQuantity(productOption, quantity);
        return CartItem.builder()
                .user(user)
                .productOption(productOption)
                .quantity(quantity)
                .build();
    }

    private void validateOptionAvailability(ProductOption productOption) {
        if (productOption.getProduct() == null || Boolean.FALSE.equals(productOption.getProduct().getIsAvailable())) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_NOT_AVAILABLE, "상품이 판매 중이 아닙니다.");
        }
        productOption.validateAvailable();
    }

    private void verifyRequestedQuantity(ProductOption productOption, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "수량은 1개 이상이어야 합니다.");
        }

        Integer stock = productOption.getInventory() != null && productOption.getInventory().getStockQuantity() != null
            ? productOption.getInventory().getStockQuantity().getValue()
            : null;

        if (stock == null) {
            throw new BusinessException(ErrorCode.STOCK_QUANTITY_REQUIRED, "재고 정보가 유효하지 않습니다.");
        }

        if (stock < quantity) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "요청 수량이 재고를 초과했습니다.");
        }
    }

    private CartItemDetailResponse mapToDetailResponse(CartItem cartItem) {
        return CartItemDetailResponse.from(cartItem);
    }
}

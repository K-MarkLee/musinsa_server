package com.mudosa.musinsa.brand.domain.service;

import com.mudosa.musinsa.brand.domain.dto.BrandDetailResponseDTO;
import com.mudosa.musinsa.brand.domain.dto.BrandRequestDTO;
import com.mudosa.musinsa.brand.domain.dto.BrandResponseDTO;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandStatus;
import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.domain.chat.entity.ChatRoom;
import com.mudosa.musinsa.domain.chat.enums.ChatRoomType;
import com.mudosa.musinsa.domain.chat.repository.ChatRoomRepository;
import com.mudosa.musinsa.product.application.dto.ProductSearchResponse;
import com.mudosa.musinsa.product.domain.model.Image;
import com.mudosa.musinsa.product.domain.model.Inventory;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandService {
  private final BrandRepository brandRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ProductRepository productRepository;

  /**
   * 브랜드 생성
   */
  @Transactional
  public BrandResponseDTO createBrand(BrandRequestDTO request, String logoUrl) {
    // 채팅방 생성
    log.info("request: " + request.toString());
    Brand brand = Brand.builder()
        .nameKo(request.getNameKo())
        .nameEn(request.getNameEn())
        .logoUrl(logoUrl)
        .commissionRate(request.getCommissionRate())
        .status(BrandStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    Brand createdBrand = brandRepository.save(brand);

    ChatRoom chatRoom = ChatRoom.builder()
        .brand(createdBrand)
        .type(ChatRoomType.GROUP)
        .build();

    chatRoomRepository.save(chatRoom);

    return convertToBrandResponse(brand);
  }

  // 브랜드 전체 조회
// BrandService.java (예시)

  @Transactional(readOnly = true)
  public List<BrandResponseDTO> getBrands() {
    List<Brand> brands = brandRepository.findAll();

    return brands.stream()
        .map(brand -> {
          // 브랜드별 최근 6개 상품 조회
          List<Product> products =
              productRepository.findTop6ByBrandOrderByCreatedAtDesc(brand);

          // Product → ProductSummary 변환 (핵심)
          List<ProductSearchResponse.ProductSummary> productSummaries =
              products.stream()
                  .map(this::mapToProductSummary)
                  .toList(); // Java 16+ / Java 8이면 Collectors.toList()

          // BrandResponseDTO 조립
          return BrandResponseDTO.builder()
              .brandId(brand.getBrandId())
              .nameKo(brand.getNameKo())
              .nameEn(brand.getNameEn())
              .logoURL(brand.getLogoUrl())
              .products(productSummaries)
              .build();
        })
        .toList();
  }


  public BrandDetailResponseDTO getBrandById(Long brandId) {
    BrandDetailResponseDTO dto = brandRepository.findWithGroupChatId(brandId)
        .orElseThrow(() -> new EntityNotFoundException("Brand not found: " + brandId));

    return dto;
  }

  /**
   * Entity -> DTO 변환
   */
  private BrandResponseDTO convertToBrandResponse(Brand brand) {
    return BrandResponseDTO.builder()
        .brandId(brand.getBrandId())
        .nameKo(brand.getNameKo())
        .nameEn(brand.getNameEn())
        .logoURL(brand.getLogoUrl())
        .build();
  }

  private ProductSearchResponse.ProductSummary mapToProductSummary(Product product) {
    BigDecimal lowestPrice = extractLowestPrice(product);

    boolean hasStock = Optional.ofNullable(product.getProductOptions())
        .orElse(Collections.emptyList()).stream()
        .map(ProductOption::getInventory)
        .filter(Objects::nonNull)
        .anyMatch(inv -> inv.getStockQuantity() != null && inv.getStockQuantity().getValue() > 0);

    String thumbnailUrl = Optional.ofNullable(product.getImages())
        .orElse(Collections.emptyList()).stream()
        .filter(image -> Boolean.TRUE.equals(image.getIsThumbnail()))
        .map(Image::getImageUrl) // 또는 image -> image.getImageUrl()
        .findFirst()
        .orElse(null);

    return ProductSearchResponse.ProductSummary.builder()
        .productId(product.getProductId())
        .brandId(product.getBrand() != null ? product.getBrand().getBrandId() : null)
        // 엔티티에 brandName 필드가 없다면 아래 줄을 brand.getNameKo() 등으로 교체
        .brandName(product.getBrandName())
        .productName(product.getProductName())
        .productInfo(product.getProductInfo())
        .productGenderType(product.getProductGenderType() != null
            ? product.getProductGenderType().name()
            : null)
        .isAvailable(product.getIsAvailable())
        .hasStock(hasStock)
        .lowestPrice(lowestPrice)
        .thumbnailUrl(thumbnailUrl)
        .categoryPath(product.getCategoryPath())
        .build();
  }

  // 상품 옵션 중 최저 가격을 계산해 정렬 및 요약 정보에 활용한다.
  private BigDecimal extractLowestPrice(Product product) {
    BigDecimal lowestAvailablePrice = product.getProductOptions().stream()
        .filter(option -> {
          Inventory inventory = option.getInventory();
          return inventory != null
              && inventory.getStockQuantity() != null
              && inventory.getStockQuantity().getValue() > 0;
        })
        .map(ProductOption::getProductPrice)
        .filter(Objects::nonNull)
        .map(Money::getAmount)
        .min(BigDecimal::compareTo)
        .orElse(null);

    if (lowestAvailablePrice != null) {
      return lowestAvailablePrice;
    }

    return product.getProductOptions().stream()
        .map(ProductOption::getProductPrice)
        .filter(Objects::nonNull)
        .map(Money::getAmount)
        .min(BigDecimal::compareTo)
        .orElse(BigDecimal.ZERO);
  }

}
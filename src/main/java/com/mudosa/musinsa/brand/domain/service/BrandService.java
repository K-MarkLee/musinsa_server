package com.mudosa.musinsa.brand.domain.service;

import com.mudosa.musinsa.brand.domain.dto.BrandDetailResponseDTO;
import com.mudosa.musinsa.brand.domain.dto.BrandRequestDTO;
import com.mudosa.musinsa.brand.domain.dto.BrandResponseDTO;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandStatus;
import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.domain.chat.entity.ChatRoom;
import com.mudosa.musinsa.domain.chat.enums.ChatRoomType;
import com.mudosa.musinsa.domain.chat.repository.ChatRoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandService {
  private final BrandRepository brandRepository;
  private final ChatRoomRepository chatRoomRepository;

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
            .build();


    brand = brandRepository.save(brand);

    ChatRoom chatRoom = ChatRoom.builder()
            .type(ChatRoomType.GROUP)
            .build();

    chatRoomRepository.save(chatRoom);

    return convertToBrandResponse(brand);
  }

  // 브랜드 전체 조회
  public List<BrandResponseDTO> getBrands() {
    List<Brand> brands = brandRepository.findAll();

    return brands.stream()
            .map(brand -> convertToBrandResponse(brand))
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
}
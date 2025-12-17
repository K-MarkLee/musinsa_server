package com.mudosa.musinsa.brand.domain.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mudosa.musinsa.brand.domain.dto.BrandDetailResponseDTO;
import com.mudosa.musinsa.brand.domain.dto.BrandRequestDTO;
import com.mudosa.musinsa.brand.domain.dto.BrandResponseDTO;
import com.mudosa.musinsa.brand.domain.repository.BrandMemberRepository;
import com.mudosa.musinsa.brand.domain.service.BrandService;
import com.mudosa.musinsa.common.dto.ApiResponse;
import com.mudosa.musinsa.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/brand")
@RequiredArgsConstructor
public class BrandControllerImpl implements BrandController {

  private final BrandService brandService;
  private final BrandMemberRepository brandMemberRepository;

  @Override
  @GetMapping("")
  public ApiResponse<List<BrandResponseDTO>> getBrands() {
    List<BrandResponseDTO> brands = brandService.getBrands();
    return ApiResponse.success(brands, "브랜드 목록을 성공적으로 불러왔습니다.");
  }

  @Override
  @GetMapping("/verify")
  public ApiResponse<List<Long>> verifyUserBrands(@AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUserId();
    List<Long> brandIds = brandMemberRepository.findBrandIdsByUserId(userId);
    return ApiResponse.success(brandIds, "사용자가 속한 브랜드 목록입니다.");
  }

  @Override
  @GetMapping("/{brandId}")
  public ApiResponse<BrandDetailResponseDTO> getBrand(@PathVariable Long brandId) {
    BrandDetailResponseDTO brand = brandService.getBrandById(brandId);
    return ApiResponse.success(brand, "브랜드 정보를 성공적으로 불러왔습니다.");
  }

  @Override
  @PostMapping("")
  public ApiResponse<BrandResponseDTO> createBrand(
      @RequestParam("request") String requestJson,
      @RequestPart(value = "file", required = false) MultipartFile file,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    ObjectMapper mapper = new ObjectMapper();
    BrandRequestDTO request;
    try {
      request = mapper.readValue(requestJson, BrandRequestDTO.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("JSON 파싱 실패: " + e.getMessage(), e);
    }

    Long userId = userDetails.getUserId();

    BrandResponseDTO brand = brandService.createBrand(userId, request, file);
    return ApiResponse.success(brand, "브랜드가 생성되었습니다.");
  }


}

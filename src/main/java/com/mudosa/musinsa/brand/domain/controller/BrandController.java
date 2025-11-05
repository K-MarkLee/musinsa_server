package com.mudosa.musinsa.brand.domain.controller;

import com.mudosa.musinsa.brand.domain.dto.BrandDetailResponseDTO;
import com.mudosa.musinsa.brand.domain.dto.BrandResponseDTO;
import com.mudosa.musinsa.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// swagger를 위한 인터페이스
@Tag(name = "Brand API", description = "브랜드 API")
public interface BrandController {

  @Operation(summary = "브랜드 목록", description = "현재 존재하는 모든 브랜드 목록을 불러옵니다.")
  ApiResponse<List<BrandResponseDTO>> getBrands();

  @Operation(summary = "브랜드 정보", description = "brandId에 해당하는 브랜드 정보를 불러옵니다.")
  ApiResponse<BrandDetailResponseDTO> getBrand(
      @Parameter(description = "조회할 브랜드 ID", example = "1")
      @PathVariable Long brandId);

  @Operation(
      summary = "브랜드 생성",
      description = "브랜드 이름(한글&영어), 수수료율, 로고 이미지를 입력하여 브랜드를 생성합니다."
  )
  ApiResponse<BrandResponseDTO> createBrand(
      @Parameter(
          description = "브랜드 정보 Json 형태",
          example = "{ \"nameKo\": \"아디다스\", \"nameEn\": \"Adidas\", \"commissionRate\": 10.0 }"
      )
      @RequestParam("request") String requestJson, // JSON 파트
      @Parameter(description = "브랜드 로고 이미지 파일", example = "logo.png")
      @RequestPart(value = "file", required = false) MultipartFile file
  );

}

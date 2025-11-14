package com.mudosa.musinsa.brand.domain.controller;

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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<BrandResponseDTO> createBrand(
      @RequestParam("request") String requestJson,   // JSON 파트
      @RequestPart(value = "file", required = false) MultipartFile file // 파일 파트
  ) {
    // 1. JSON 파싱
    ObjectMapper mapper = new ObjectMapper();
    BrandRequestDTO request;
    try {
      request = mapper.readValue(requestJson, BrandRequestDTO.class);
    } catch (Exception e) {
      throw new RuntimeException("JSON 파싱 실패: " + e.getMessage(), e);
    }

    // 파일 경로를 서비스에 넘길 변수 (없으면 null)
    String storedUrl = null;

    try {
      // 2. 파일이 있을 때만 처리
      if (file != null && !file.isEmpty()) {
        String uploadDir = new ClassPathResource("static/").getFile().getAbsolutePath()
            + "/brand/logo";
        Files.createDirectories(Paths.get(uploadDir));

        // 파일명 정규화 + UUID로 충돌 방지
        String safeName = java.util.UUID.randomUUID() + "_" +
            org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename());

        Path target = Paths.get(uploadDir, safeName).toAbsolutePath().normalize();

        // 실제 저장
        file.transferTo(target.toFile());

        // 정적 리소스 경로 기준으로 저장할 값
        storedUrl = "/brand/logo/" + safeName;

        log.info("파일 업로드 성공: {}", target);
        log.info("request: {}", request);
      }

      // 3. 서비스 호출 (파일이 없으면 storedUrl == null로 전달)
      BrandResponseDTO brand = brandService.createBrand(request, storedUrl);
      return ApiResponse.success(brand, "브랜드가 생성되었습니다.");

    } catch (Exception e) {
      log.error("파일 업로드/브랜드 생성 중 오류", e);
      throw new RuntimeException(e);
    }
  }


}

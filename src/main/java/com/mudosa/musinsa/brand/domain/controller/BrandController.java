package com.mudosa.musinsa.brand.domain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mudosa.musinsa.brand.domain.dto.BrandDetailResponseDTO;
import com.mudosa.musinsa.brand.domain.dto.BrandRequestDTO;
import com.mudosa.musinsa.brand.domain.dto.BrandResponseDTO;
import com.mudosa.musinsa.brand.domain.service.BrandService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@Tag(name = "Brand API", description = "브랜드 API")
@Slf4j
@RequestMapping("/api/brand")
@RequiredArgsConstructor
public class BrandController {

  private final BrandService brandService;

  @GetMapping("")
  public ResponseEntity<List<BrandResponseDTO>> getBrands() {
    List<BrandResponseDTO> brands = brandService.getBrands();
    return ResponseEntity.ok(brands);
  }

  @GetMapping("/{brandId}")
  public ResponseEntity<BrandDetailResponseDTO> getBrand(@PathVariable Long brandId) {
    BrandDetailResponseDTO brand = brandService.getBrandById(brandId);
    return ResponseEntity.ok(brand);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> createBrand(
      @RequestParam("request") String requestJson,   // JSON 파트
      @RequestPart(value = "file", required = false) MultipartFile file // 파일 파트
  ) {
    // JSON 파싱
    ObjectMapper mapper = new ObjectMapper();
    BrandRequestDTO request;
    try {
      request = mapper.readValue(requestJson, BrandRequestDTO.class);
    } catch (Exception e) {
      throw new RuntimeException("JSON 파싱 실패: " + e.getMessage());
    }

    try {
      if (file != null && !file.isEmpty()) {
        String uploadDir = new ClassPathResource("static/").getFile().getAbsolutePath()
            + "/brand/logo";
        Files.createDirectories(Paths.get(uploadDir));

        // 파일명 정규화 + UUID로 충돌 방지
        String safeName = java.util.UUID.randomUUID() + "_" +
            org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename());
        Path target = Paths.get(uploadDir, safeName).toAbsolutePath().normalize();

        file.transferTo(target.toFile());


        // 정적 서빙 경로 매핑에 맞춰 URL/상대경로를 저장 (예시는 상대경로)
        String storedUrl = "/brand/logo/" + safeName;

        log.info("파일 업로드 성공: {}", target);
        log.info("request: " + request);
        brandService.createBrand(request, storedUrl);
      }

      // TODO: request DTO 저장 로직 추가
      return ResponseEntity.ok("브랜드가 생성되었습니다.");

    } catch (Exception e) {
      log.error("파일 업로드 실패", e);
      throw new RuntimeException(e);
    }
  }
}

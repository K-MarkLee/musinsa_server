package com.mudosa.musinsa.brand.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mudosa.musinsa.product.application.dto.ProductSearchResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "브랜드 + 해당 브랜드 상품 정보 반환 DTO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BrandResponseDTO {
  @Schema(description = "브랜드 Id", example = "1")
  public long brandId;
  @Schema(description = "브랜드 한글 이름", example = "무신상")
  public String nameKo;
  @Schema(description = "브랜드 영어 이름", example = "MuSinSang")
  public String nameEn;
  @Schema(description = "로고 이미지 url", example = "/brand/logo/c166f09e-8e75-4307-8be3-63253270a459_로고.png")
  public String logoURL;

  @Schema(description = "상품 리스트[6]")
  public List<ProductSearchResponse.ProductSummary> products;
}

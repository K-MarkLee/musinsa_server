package com.mudosa.musinsa.brand.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "브랜드 생성 정보 DTO")
public class BrandRequestDTO {
  @Schema(description = "브랜드 한글 이름", example = "무신상")
  public String nameKo;
  @Schema(description = "브랜드 영어 이름", example = "MuSinSang")
  public String nameEn;
  @Schema(description = "수수료율", example = "10.00")
  public BigDecimal commissionRate;
}

package com.mudosa.musinsa.brand.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "브랜드 세부 정보 반환 DTO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BrandDetailResponseDTO {
  @Schema(description = "브랜드 Id", example = "1")
  public long brandId;
  @Schema(description = "브랜드 한글 이름", example = "무신상")
  public String nameKo;
  @Schema(description = "브랜드 영어 이름", example = "MuSinSang")
  public String nameEn;
  @Schema(description = "로고 이미지 url", example = "/brand/logo/c166f09e-8e75-4307-8be3-63253270a459_로고.png")
  public String logoURL;

  @Schema(description = "브랜드 채팅 id", example = "2")
  public Long groupChatId;
}

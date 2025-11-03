package com.mudosa.musinsa.brand.domain.dto;

import com.mudosa.musinsa.product.application.dto.ProductSearchResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponseDTO {
  public long brandId;
  public String nameKo;
  public String nameEn;
  public String logoURL;

  public List<ProductSearchResponse.ProductSummary> products;
}

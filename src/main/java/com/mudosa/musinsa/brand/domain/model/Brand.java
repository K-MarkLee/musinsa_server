package com.mudosa.musinsa.brand.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "brand")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brand extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "brand_id")
  private Long brandId;

  @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<BrandMember> brandMembers = new ArrayList<>();

  @Column(name = "name_ko", nullable = false, length = 100)
  private String nameKo;

  @Column(name = "name_en", nullable = false, length = 120)
  private String nameEn;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private BrandStatus status;

  @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
  private BigDecimal commissionRate;

  @Column(name = "logo_url")
  private String logoUrl;

  /**
   * 브랜드 생성
   */

  public static Brand create(String nameKo, String nameEn, BigDecimal commissionRate) {
    Brand brand = new Brand();
    brand.nameKo = nameKo;
    brand.nameEn = nameEn;
    brand.commissionRate = commissionRate;
    brand.status = BrandStatus.ACTIVE;
    return brand;
  }

  /**
   * 브랜드 멤버 추가
   */

  @Transactional
  public void addMember(BrandMember member) {
    this.brandMembers.add(member);
    member.assignBrand(this);
  }
}

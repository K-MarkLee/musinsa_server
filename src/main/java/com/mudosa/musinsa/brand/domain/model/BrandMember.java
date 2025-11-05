package com.mudosa.musinsa.brand.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 브랜드 멤버 엔티티
 * Brand 애그리거트 내부
 */
@Entity
@Table(name = "brand_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandMember extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "brand_member_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "brand_id", nullable = false)
  private Brand brand;

  @Column(name = "user_id", nullable = false)
  private Long userId; // User 애그리거트 참조 (ID만)

  /**
   * 브랜드 멤버 생성
   */
  public static BrandMember create(Long userId) {
    BrandMember member = new BrandMember();
    member.userId = userId;
    return member;
  }

  /**
   * Brand 할당 (Package Private)
   */
  void assignBrand(Brand brand) {
    this.brand = brand;
  }
}

package com.mudosa.musinsa.brand.domain.repository;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandMember;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandMemberRepository extends JpaRepository<BrandMember, Long> {

  boolean existsByBrand_BrandIdAndUserId(Long brandId, Long userId);

  Optional<BrandMember> findByBrand(Brand brand);

  @Query(value = """
      select bm.user_id
      from brand_member bm
      where bm.brand_id = :brandId
      """, nativeQuery = true)
  List<Long> findActiveUserIdsByBrandId(@Param("brandId") Long brandId);

  @Query(value = """
      select bm.brand_id
      from brand_member bm
      where bm.user_id = :userId
      """, nativeQuery = true)
  List<Long> findBrandIdsByUserId(@Param("userId") Long userId);
}

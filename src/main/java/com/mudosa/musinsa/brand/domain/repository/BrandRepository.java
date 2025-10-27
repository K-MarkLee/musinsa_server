package com.mudosa.musinsa.brand.domain.repository;

import com.mudosa.musinsa.brand.domain.dto.BrandDetailResponseDTO;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandStatus;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Brand Repository
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

  List<Brand> findByStatus(BrandStatus status);

  boolean existsByNameKo(String nameKo);

  @Query(value = """
      SELECT 
        b.brand_id       AS brandId,
        b.name_ko        AS nameKo,
        b.name_en        AS nameEn,
        b.logo_url       AS logoUrl,
        (
          SELECT cr.chat_id
          FROM chat_room cr
          WHERE cr.brand_id = b.brand_id
            AND cr.type = 'GROUP'
          ORDER BY cr.chat_id ASC
          LIMIT 1
        )               AS groupChatId
      FROM brand b
      WHERE b.brand_id = :brandId
      """, nativeQuery = true)
  Optional<BrandDetailResponseDTO> findWithGroupChatId(@Param("brandId") Long brandId);
}

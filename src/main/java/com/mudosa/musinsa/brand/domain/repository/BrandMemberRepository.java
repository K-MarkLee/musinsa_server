package com.mudosa.musinsa.brand.domain.repository;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandMemberRepository extends JpaRepository<BrandMember, Long> {

    boolean existsByBrand_BrandIdAndUserId(Long brandId, Long userId);
    Optional<BrandMember> findByBrand(Brand brand);
}

package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.OptionName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionNameRepository extends JpaRepository<OptionName, Long> {
}

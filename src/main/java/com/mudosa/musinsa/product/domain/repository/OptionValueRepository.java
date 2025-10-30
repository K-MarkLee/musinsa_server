package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionValueRepository extends JpaRepository<OptionValue, Long> {

    // 옵션 값 ID 목록으로 옵션 값을 일괄 조회.
    List<OptionValue> findAllByOptionValueIdIn(List<Long> optionValueIds);
}

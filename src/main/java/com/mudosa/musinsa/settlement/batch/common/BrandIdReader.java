package com.mudosa.musinsa.settlement.batch.common;

import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 브랜드 ID 목록을 읽어오는 Reader 생성기
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrandIdReader {

    private final BrandRepository brandRepository;

    /* 모든 브랜드 ID를 읽는 Reader 생성 */
    public ListItemReader<Long> createReader(String jobName) {
        log.info("=== {} 배치 시작 ===", jobName);

        List<Long> brandIds = brandRepository.findAllBrandIds();

        log.info("집계 대상 브랜드 수: {}", brandIds.size());

        return new ListItemReader<>(brandIds);
    }
}
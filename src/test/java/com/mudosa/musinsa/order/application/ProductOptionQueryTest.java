package com.mudosa.musinsa.order.application;

import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.product.domain.repository.ProductOptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ProductOption 조회 쿼리 성능 테스트
 * 
 * 목적: N+1 문제가 발생하는지 확인
 * 
 * application.yml에 다음 설정 추가:
 * logging:
 *   level:
 *     org.hibernate.SQL: DEBUG
 *     org.hibernate.type.descriptor.sql.BasicBinder: TRACE
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class ProductOptionQueryTest {

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Test
    @DisplayName("findAllById 사용 시 N+1 문제 발생 확인")
    @Transactional
    void testFindAllById_NPlusOneProblem() {
        // Given: 테스트 데이터의 ID들
        List<Long> ids = List.of(1L, 2L, 3L);

        // When: findAllById로 조회
        log.info("========== findAllById 조회 시작 ==========");
        List<ProductOption> options = productOptionRepository.findAllById(ids);
        
        // Then: 각 엔티티의 연관 객체 접근 시 추가 쿼리 발생
        log.info("========== 연관 엔티티 접근 시작 ==========");
        for (ProductOption option : options) {
            log.info("ProductOption ID: {}", option.getProductOptionId());
            
            // Inventory 접근 → 추가 쿼리 발생!
            log.info("  - Inventory ID: {}", option.getInventory().getInventoryId());
            
            // Product 접근 → 추가 쿼리 발생!
            log.info("  - Product ID: {}", option.getProduct().getProductId());
        }
        log.info("========== 테스트 종료 ==========");
        
        // 콘솔에서 "Hibernate: select" 로그를 세어보면 1 + 2N번 쿼리 확인 가능
    }

    @Test
    @DisplayName("커스텀 쿼리(JOIN FETCH) 사용 시 단일 쿼리로 해결")
    @Transactional
    void testFindAllByIdWithInventory_NoNPlusOne() {
        // Given: 테스트 데이터의 ID들
        List<Long> ids = List.of(1L, 2L, 3L);

        // When: 커스텀 쿼리로 조회
        log.info("========== 커스텀 쿼리 조회 시작 ==========");
        List<ProductOption> options = productOptionRepository.findAllByIdWithInventory(ids);
        
        // Then: 연관 엔티티 접근 시에도 추가 쿼리 없음
        log.info("========== 연관 엔티티 접근 시작 ==========");
        for (ProductOption option : options) {
            log.info("ProductOption ID: {}", option.getProductOptionId());
            
            // Inventory 접근 → 추가 쿼리 없음! (이미 로딩됨)
            log.info("  - Inventory ID: {}", option.getInventory().getInventoryId());
            
            // Product 접근 → 추가 쿼리 없음! (이미 로딩됨)
            log.info("  - Product ID: {}", option.getProduct().getProductId());
        }
        log.info("========== 테스트 종료 ==========");
        
        // 콘솔에서 "Hibernate: select" 로그를 세어보면 단 1번만 실행 확인 가능
    }

    @Test
    @DisplayName("대량 데이터 조회 시 배치 처리")
    @Transactional
    void testBatchQuery() {
        // Given: 대량의 ID 리스트
        List<Long> largeIdList = generateLargeIdList(1500); // 1500개

        // When: 배치 처리로 조회
        log.info("========== 대량 조회 시작 ({}개) ==========", largeIdList.size());
        
        // 실제 사용 시: orderServiceHelper.findProductOptionsInBatch(largeIdList)
        List<ProductOption> result = productOptionRepository.findAllByIdWithInventory(largeIdList);
        
        log.info("========== 조회 완료: {}개 ==========", result.size());
        
        // 대량 IN 절이 DB에 부담을 줄 수 있으므로 배치 처리 권장
    }

    private List<Long> generateLargeIdList(int size) {
        return java.util.stream.LongStream.rangeClosed(1, size)
                .boxed()
                .toList();
    }
}

package com.mudosa.musinsa.event.repository;

import com.mudosa.musinsa.event.model.EventOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventOptionRepository extends JpaRepository<EventOption, Long> {

    /**
     * 이벤트 ID로 EventOption 조회 (1:1 관계)
     *
     * 비즈니스 규칙: 한 이벤트당 하나의 옵션만 존재
     *
     * 조회 전략:
     * - Event, Coupon fetch join으로 N+1 방지
     * - ProductOption, Product fetch join으로 상품 정보 즉시 로딩
     *
     * 용도:
     * - 이벤트 상세 조회 (화면 표시용)
     * - 쿠폰 발급 로직 (비즈니스 로직용)
     */
    @Query("""
        SELECT eo
        FROM EventOption eo
        JOIN FETCH eo.event e
        LEFT JOIN FETCH e.coupon
        JOIN FETCH eo.productOption po
        JOIN FETCH po.product p
        WHERE e.id = :eventId
        """)
    Optional<EventOption> findByEventIdWithDetails(@Param("eventId") Long eventId);
}
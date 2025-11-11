package com.mudosa.musinsa.event.repository;

import com.mudosa.musinsa.event.model.EventOption;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventOptionRepository extends JpaRepository<EventOption, Long> {

    // 이벤트에 속한 옵션들 조회 (엔티티 반환) — 필요 시 서비스에서 사용
    List<EventOption> findByEventId(Long eventId);

    // 이벤트 옵션 목록 조회 (읽기 전용) — 네이티브로 필요한 필드만 뽑아오기
    @Query(
            value = """
            select 
              eo.event_option_id   as optionId,
              po.product_option_id as productOptionId,
              p.product_name       as productName,
              eo.event_price       as eventPrice,
              eo.event_stock       as eventStock,
              p.product_id         as productId
            from event_option eo
            join product_option po on po.product_option_id = eo.product_option_id
            join product p        on p.product_id         = po.product_id
            where eo.event_id = :eventId
            """,
            nativeQuery = true
    )
    List<Object[]> findRowsNativeByEventId(@Param("eventId") Long eventId);

    // 쿠폰 발급 등 동시성 구간에서 사용: 이벤트 ID + 옵션 ID로 행을 잠그며 조회 (비관적 쓰기 락)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select eo
        from EventOption eo
        join fetch eo.event e
        left join fetch e.coupon
        where e.id = :eventId
          and eo.id = :eventOptionId
        """)
    Optional<EventOption> findByEventIdAndIdForUpdate(@Param("eventId") Long eventId,
                                                      @Param("eventOptionId") Long eventOptionId);
}

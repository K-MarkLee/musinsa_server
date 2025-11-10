package com.mudosa.musinsa.event.repository;


import com.mudosa.musinsa.event.model.EventOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

public interface EventOptionRepository extends JpaRepository<EventOption, Long> {


    // EventService에서 사용 : 이벤트에 속한 옵션들 조회
    List<EventOption> findByEventId(Long eventId);

    // N+1 방지 : 옵션 조회 시 ProductOption/Product까지 한 방에 로드

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select eo from EventOption eo " +
            "join fetch eo.event e " +
            "join fetch eo.productOption po " +
            "join fetch po.product p " +
            "where e.id = :eventId and eo.id = :eventOptionId")
    Optional<EventOption> findByEventIdAndIdForUpdate(Long eventId, Long eventOptionId);


}



package com.mudosa.musinsa.event.repository;


import com.mudosa.musinsa.event.model.EventOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
public interface EventOptionRepository extends JpaRepository<EventOption, Long> {


    // EventService에서 사용 : 이벤트에 속한 옵션들 조회
    List<EventOption> findByEventId(Long eventId);

    // N+1 방지 : 옵션 조회 시 ProductOption/Product까지 한 방에 로드

}



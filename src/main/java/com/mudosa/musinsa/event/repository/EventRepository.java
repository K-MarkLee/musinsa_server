package com.mudosa.musinsa.event.repository;

import com.mudosa.musinsa.event.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;



/**
 * Event Repository
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {



    List<Event> findAllByEventType(
            Event.EventType eventType
    );

//    List<Event> findByEventStartTimeBeforeAndEventEndTimeAfter(
//
//    );


    // (선택) 상세 조회 시 옵션/상품까지 한 번에 가져오고 싶을 때 N+1 방지용

}

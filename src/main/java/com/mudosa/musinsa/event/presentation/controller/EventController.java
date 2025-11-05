package com.mudosa.musinsa.event.presentation.controller;

//import com.mudosa.musinsa.event.presentation.dto.req.EventCouponIssueRequest;
//import com.mudosa.musinsa.event.presentation.dto.res.EventCouponIssueResponse;

import com.mudosa.musinsa.event.presentation.dto.res.EventListResDto;
import com.mudosa.musinsa.event.model.Event;
import com.mudosa.musinsa.event.service.EventService;
import com.mudosa.musinsa.event.service.EventCouponAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//MVC
// view -> 정적 파일을 서빙하는 역할
//@Controller -> ViewResolver
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@Slf4j

public class EventController {

    private final EventService eventService;
    private final EventCouponAccessService eventEntryService;

    /*
    이벤트 목록 조회 api
    @param type ( drop, comment )
    @return 필터링된 이벤트 목록 + 특정 이벤트에 포함된 상품 list
    */

    @GetMapping
    public ResponseEntity<List<EventListResDto>> getEventList(
            @RequestParam(value = "type", defaultValue = "DROP") Event.EventType type
            //@RequestParam(value = "page", defaultValue = "0") int page,
            //@RequestParam(value = "size", defaultValue = "20") int size
    ) {
        //Event.EventType eventType;

//        try{
//            eventType = Event.EventType.valueOf(type.toUpperCase());
//        } catch (IllegalArgumentException e){
//            // 잘못된 타입을 파라미터로 넘겨줄 경우
//            return ResponseEntity.badRequest().build();  // 400 잘못된 요청
//        }
        //String text = new String("test setes");
        List<EventListResDto> eventList = eventService.getEventListByType(type); // EventService에 만들어야됨
        return ResponseEntity.ok(eventList);
    }

    /* 쿠폰 발급 트리거
    * 슬롯/상태 검증 → (eventId,couponId) 재고 차감 → 발급이력 저장 → member_coupon 생성
    * 멱등성 보장 ? 이미 발급받은 경우 200으로 기존 결과 반환 or 409로 충돌
    *
    */

    //@PostMapping

}

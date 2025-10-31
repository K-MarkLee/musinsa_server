package com.mudosa.musinsa.event.presentation.controller;

import com.mudosa.musinsa.event.presentation.dto.res.EventListResDto;
import com.mudosa.musinsa.event.model.Event;
import com.mudosa.musinsa.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@Slf4j

public class EventController {

    private final EventService eventService;

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

        List<EventListResDto> eventList = eventService.getEventListByType(type); // EventService에 만들어야됨
        return ResponseEntity.ok(eventList);
    }
}

package com.mudosa.musinsa.event.service;

import com.mudosa.musinsa.event.model.Event;
import com.mudosa.musinsa.event.model.EventImage;
import com.mudosa.musinsa.event.model.EventOption;
import com.mudosa.musinsa.event.presentation.dto.res.EventListResDto;
import com.mudosa.musinsa.event.presentation.dto.res.EventOptionResDto;
import com.mudosa.musinsa.event.repository.EventImageRepository;
import com.mudosa.musinsa.event.repository.EventOptionRepository;
import com.mudosa.musinsa.event.repository.EventRepository;
import com.mudosa.musinsa.product.domain.model.ProductOption;
import com.mudosa.musinsa.event.model.EventStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final EventOptionRepository eventOptionRepository;
    private final EventImageRepository eventImageRepository;


    //이벤트 목록 조회( 타입별 ) GET - resDto를 통해서 반환

    /*
    * 이벤트 타입에 맞는 이벤트 목록을 반환하는 메서드
    *
    *  */
    public List<EventListResDto> getEventListByType(Event.EventType eventType) {

        LocalDateTime now = LocalDateTime.now();

        List<Event> events = eventRepository.findAllByEventType(eventType); // 레포지토리에 메서드 "findAllByEventType" 구현필요,이벤트 목록을 db에서 가져오기
        return events.stream()
                .map(event -> mapEventToDto(event,now))
                .collect(Collectors.toList());//이벤트 상태 계산하고 DTO로 변환, 메서드 참조는 기존에 정의된 메서드를 직접 참조
    }

    //이벤트 목록 조회 ( 날짜와 상태에 맞춰서 필터링 ) GET - resDto를 통해서 반환

    public List<EventListResDto> getFilteredEventList(LocalDateTime currentTime) {
        List<Event> events = eventRepository.findAll();
        return events.stream()
                //필터링 해주는게 이 stream에서 filter() 연산을 통해서 !
                .filter(event ->event.getStartedAt().isAfter(currentTime)) //LocalDateTime 클래스 내장 메서드
                .map(event -> mapEventToDto(event,currentTime)) // event라는 매개변수를 받아서 메서드를 호출하는 익명함수
                .collect(Collectors.toList());
    }

    // 이벤트 객체를 DTO 로 변환한다.
    private EventListResDto mapEventToDto(Event event, LocalDateTime currentTime) {
        EventStatus status = EventStatus.calculateStatus(event, currentTime);

        // 1) 옵션 엔티티 조회
        List<EventOption> options = eventOptionRepository.findByEventId(event.getId()); // 레포지토리에 구현 필요 + List로 객체반환

        // 2) 이벤트 썸네일 조회
        String thumbnailUrl = eventImageRepository.findByEventIdAndIsThumbnailTrue(event.getId()) //레포지토리 구현 필요
                .map(EventImage::getImageUrl)
                .orElse(null);  // 썸네일 이미지가 없으면 null 반환

        // 3) EventOption -> EventOptionResDto 매핑

        List<EventOptionResDto> optionDtos = options.stream()
                .map(eo -> {
                    //필요 시 상품명만 뽑기 ( 구조에 맞게 수정 )
                    String productName = null;
                    String optionLabel = null;

                    ProductOption po = eo.getProductOption();
                    Long productOptionId = null;
                    Long productId = null;

                    if (po != null) {
                        productOptionId = po.getProductOptionId();
                        optionLabel = null; // 추후에 로직 추가 필요,

                        if(po.getProduct() != null) {
                            productName = po.getProduct().getProductName();
                            productId = po.getProduct().getProductId();

                        }
                    }

                    return EventOptionResDto.from(eo, productName, optionLabel, productOptionId, productId);

                })
                .toList();

        return EventListResDto.from(event, optionDtos, thumbnailUrl, status);

    }





}

package com.mudosa.musinsa.event.service;

import com.mudosa.musinsa.event.model.Event;
import com.mudosa.musinsa.event.model.EventImage;
import com.mudosa.musinsa.event.model.EventOption;
import com.mudosa.musinsa.event.presentation.dto.res.EventListResDto;
import com.mudosa.musinsa.event.repository.EventImageRepository;
import com.mudosa.musinsa.event.repository.EventOptionRepository;
import com.mudosa.musinsa.event.repository.EventRepository;
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
        List<Event> events = eventRepository.findAllByEventType(eventType); // 레포지토리에 메서드 "findAllByEventType" 구현필요,이벤트 목록을 db에서 가져오기
        return events.stream()
                .map(this::mapEventToDto)  //이벤트 상태 계산하고 DTO로 변환, 메서드 참조는 기존에 정의된 메서드를 직접 참조
                .collect(Collectors.toList()); //map으로 dto로 변환 후 list로 dto객체 수집
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
        Event.EventStatus status = calculateEventStatus(event, currentTime);
        List<EventOption> options = eventOptionRepository.findByEventId(event.getId()); //레포지토리에 구현 필요 + List로 객체반환
        String thumbnailUrl = eventImageRepository.findByEventIdAndIsThumbnailTrue(event.getId()) //레포지토리 구현 필요
                .map(EventImage::getImageUrl)
                .orElse(null);  // 썸네일 이미지가 없으면 null 반환

        return EventListResDto.from(event, options, thumbnailUrl, status);

    }

    // 이벤트 상태 계산 (SOON, OPEN, CLOSED)
    private Event.EventStatus calculateEventStatus(Event event, LocalDateTime currentTime) {
        if (currentTime.isBefore(event.getStartedAt())) {
            return Event.EventStatus.SOON;
        } else if (currentTime.isAfter(event.getEndedAt())) {
            return Event.EventStatus.CLOSED;
        } else {
            return Event.EventStatus.OPEN;
        }
    }


}

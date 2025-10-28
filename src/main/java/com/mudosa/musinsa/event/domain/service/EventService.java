package com.mudosa.musinsa.event.domain.service;

import com.mudosa.musinsa.event.domain.dto.EventListResDto;
import com.mudosa.musinsa.event.domain.model.Event;
import com.mudosa.musinsa.event.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;

    /** 모든 이벤트 조회 */
    public List<EventListResDto> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return events.stream()
                .map(EventListResDto::from)
                .toList();
    }

    /** 타입별 이벤트 조회 */
    public List<EventListResDto> getEventByType(Event.EventType type) {
        List<Event> events = eventRepository.findAllByEventType(type);
        return events.stream()
                .map(EventListResDto::from)
                .toList();
    }
}

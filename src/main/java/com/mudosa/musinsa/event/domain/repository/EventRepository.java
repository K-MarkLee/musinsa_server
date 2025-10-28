package com.mudosa.musinsa.event.domain.repository;

import com.mudosa.musinsa.event.domain.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event Repository
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByIsPublicTrue();

    List<Event> findByStartedAtBeforeAndEndedAtAfter(LocalDateTime start, LocalDateTime end);

    List<Event> findAllByEventType(Event.EventType eventType);
}

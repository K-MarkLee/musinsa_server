package com.mudosa.musinsa.event.repository;


import com.mudosa.musinsa.event.model.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface EventImageRepository extends JpaRepository<EventImage, Long> {

    Optional<EventImage> findByEventIdAndIsThumbnailTrue(Long eventId);

    // 여러 썸네일 정렬

    // 이벤트내의 모든 이미지

}

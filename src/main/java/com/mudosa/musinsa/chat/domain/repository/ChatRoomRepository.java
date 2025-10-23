package com.mudosa.musinsa.chat.domain.repository;

import com.mudosa.musinsa.chat.domain.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ChatRoom Repository
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    List<ChatRoom> findByBrandId(Long brandId);
    
    List<ChatRoom> findByIsActiveTrue();
}

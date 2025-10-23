package com.mudosa.musinsa.domain.chat.repository;

import com.mudosa.musinsa.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  /**
   * 브랜드의 모든 채팅방 조회
   */
  List<ChatRoom> findByBrandIdOrderByLastMessageAtDesc(Long brandId);

  /**
   * 채팅방과 참여자 정보를 함께 조회 (N+1 방지)
   */
  @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
      "LEFT JOIN FETCH cr.parts " +
      "WHERE cr.chatId = :chatId")
  Optional<ChatRoom> findByIdWithParts(@Param("chatId") Long chatId);
}


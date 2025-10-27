package com.mudosa.musinsa.domain.chat.repository;

import com.mudosa.musinsa.domain.chat.entity.ChatPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatPartRepository extends JpaRepository<ChatPart, Long> {

  /**
   * 특정 채팅방의 참여자 조회
   */
  List<ChatPart> findByChatRoom_ChatId(Long chatId);

  Optional<ChatPart> findByChatRoomChatIdAndUserId(Long chatId, Long userId);

  /**
   * 사용자가 참여 중인 모든 채팅방 조회
   */
  @Query("SELECT cp FROM ChatPart cp " +
      "JOIN FETCH cp.chatRoom " +
      "WHERE cp.userId = :userId AND cp.leftAt IS NULL")
  List<ChatPart> findActiveByUserId(@Param("userId") Long userId);

  /**
   * 채팅방에서 특정 사용자의 ChatPart 조회
   */
  Optional<ChatPart> findByChatRoom_ChatIdAndUserId(Long chatId, Long userId);

  /**
   * 사용자가 해당 채팅방에 참여 중인지 확인
   */
  @Query("SELECT COUNT(cp) > 0 FROM ChatPart cp " +
      "WHERE cp.chatRoom.chatId = :chatId " +
      "AND cp.userId = :userId " +
      "AND cp.leftAt IS NULL")
  boolean existsActiveMember(@Param("chatId") Long chatId,
                             @Param("userId") Long userId);
}

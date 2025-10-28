package com.mudosa.musinsa.domain.chat.repository;

import com.mudosa.musinsa.domain.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

  /**
   * 채팅방의 메시지 페이징 조회 (최신순)
   */
  @Query("SELECT m FROM Message m " +
      "WHERE m.chatRoom.chatId = :chatId " +
      "ORDER BY m.createdAt DESC")
  Page<Message> findByChatIdOrderByCreatedAtDesc(
      @Param("chatId") Long chatId,
      Pageable pageable
  );
  
  /**
   * 특정 메시지의 답장(스레드) 조회
   */
  List<Message> findByParent_MessageIdOrderByCreatedAtAsc(Long parentId);

  /**
   * 채팅방의 마지막 메시지 조회
   */
  Optional<Message> findFirstByChatRoom_ChatIdOrderByCreatedAtDesc(Long chatId);
}

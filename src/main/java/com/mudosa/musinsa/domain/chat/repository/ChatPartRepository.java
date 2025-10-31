package com.mudosa.musinsa.domain.chat.repository;

import com.mudosa.musinsa.domain.chat.entity.ChatPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatPartRepository extends JpaRepository<ChatPart, Long> {

  // 참여자 조회
  List<ChatPart> findByChatRoom_ChatId(Long chatId);

  // user.userId → user.id 로 변경
  Optional<ChatPart> findByChatRoomChatIdAndUser_Id(Long chatId, Long userId);

  // 활성 여부 exists (퇴장 전)
  boolean existsByChatRoom_ChatIdAndUser_IdAndLeftAtIsNull(Long chatId, Long userId);


  Optional<ChatPart> findByChatRoom_ChatIdAndUserIdAndLeftAtIsNull(Long chatId, Long userId);
}

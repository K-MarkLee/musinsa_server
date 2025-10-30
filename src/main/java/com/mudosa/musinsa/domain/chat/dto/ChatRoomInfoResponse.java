package com.mudosa.musinsa.domain.chat.dto;

import com.mudosa.musinsa.domain.chat.enums.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅방 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomInfoResponse {
  private Long chatId;
  private Long brandId;
  private String brandNameKo;
  private ChatRoomType type;
  private Long partNum;
  private LocalDateTime lastMessageAt;

  private boolean isParticipate;
}

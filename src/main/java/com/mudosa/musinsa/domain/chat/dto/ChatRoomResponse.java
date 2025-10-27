package com.mudosa.musinsa.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅방 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
  private Long chatId;
  private Long brandId;
  private String type;
  private LocalDateTime lastMessageAt;
  private List<ChatPartResponse> participants;
  private MessageResponse lastMessage;
}
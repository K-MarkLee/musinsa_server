package com.mudosa.musinsa.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅 참여자 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatPartResponse {
  private Long chatPartId;
  private Long userId;
  private String userName;
  private String role;
  private LocalDateTime joinedAt;
  private LocalDateTime leftAt;
}

package com.mudosa.musinsa.domain.chat.dto;

import com.mudosa.musinsa.domain.chat.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 서버에서 클라이언트로 전송되는 메시지 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
  private Long messageId;
  private Long chatId;
  private Long chatPartId;
  private Long userId;           // 발신자 User ID
  private String userName;       // 발신자 이름 (조인하여 가져옴)
  private MessageType type;
  private String content;
  private Long parentId;
  private List<AttachmentResponse> attachments;
  private LocalDateTime createdAt;
  private boolean isDeleted;
}
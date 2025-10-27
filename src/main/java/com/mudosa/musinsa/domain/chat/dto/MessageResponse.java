package com.mudosa.musinsa.domain.chat.dto;

import com.mudosa.musinsa.domain.chat.entity.Message;
import com.mudosa.musinsa.domain.chat.entity.MessageAttachment;
import com.mudosa.musinsa.domain.chat.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {

  private Long messageId;
  private Long chatId;
  private Long chatPartId;
  private Long userId;
  private String userName;
  private MessageType type;
  private String content;
  private List<AttachmentResponse> attachments;
  private LocalDateTime createdAt;
  private boolean isDeleted;

  private boolean isManager;
//  private ChatPartRole role;

  public static MessageResponse from(Message message, List<MessageAttachment> attachments) {
    return MessageResponse.builder()
        .messageId(message.getMessageId())
        .chatId(message.getChatRoom().getChatId())
        .chatPartId(message.getChatPart().getChatPartId())
        .userId(message.getChatPart().getUserId())
        // user entity 구현 후 연결
//        .userId(message.getChatPart().getUser().getUserId())
        .userName("임시 이름")
        .type(message.getType())
        .content(message.getContent())
        .attachments(
            attachments != null
                ? attachments.stream()
                .map(AttachmentResponse::from)
                .toList()
                : List.of()
        )
        .createdAt(message.getCreatedAt())
        .isDeleted(message.getDeletedAt() != null)
        .build();
  }

  public void setManager(boolean manager) {
    isManager = manager;
  }
}

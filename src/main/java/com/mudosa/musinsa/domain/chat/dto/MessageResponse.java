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

  private ParentMessageResponse parent;

  private boolean isManager;
//  private ChatPartRole role;

  public static MessageResponse from(Message message, List<MessageAttachment> attachments) {
    // 1) 부모 메시지 취득 (없을 수 있음)
    Message parent = message.getParent();
    ParentMessageResponse parentDto = null;

    if (parent != null) {
      // 부모 첨부 → DTO 변환
      List<MessageAttachment> parentAtt = parent.getAttachments() != null
          ? parent.getAttachments()
          : List.of();

      List<AttachmentResponse> parentAttachmentDtos = parentAtt.stream()
          .map(AttachmentResponse::from)
          .toList();

      parentDto = ParentMessageResponse.builder()
          .messageId(parent.getMessageId())
          .userId(parent.getChatPart() != null ? parent.getChatPart().getUser().getId() : null)
          .userName(parent.getChatPart() != null ? parent.getChatPart().getUser().getUserName() : "Unknown") // TODO: User 연동 시 교체
          .content(parent.getContent())
          .createdAt(parent.getCreatedAt())
          .attachments(parentAttachmentDtos)
          .isDeleted(parent.getDeletedAt() != null)
          .build();
    }

    // 2) 현재 메시지 첨부 → DTO 변환
    List<AttachmentResponse> attachmentDtos = (attachments != null ? attachments : List.<MessageAttachment>of())
        .stream()
        .map(AttachmentResponse::from)
        .toList();

    // 3) 발신자/식별자 널가드 (프록시 안전: 식별자만 꺼냄)
    Long chatId = message.getChatRoom() != null ? message.getChatRoom().getChatId() : null;
    Long chatPartId = message.getChatPart() != null ? message.getChatPart().getChatPartId() : null;
    Long userId = message.getChatPart() != null ? message.getChatPart().getUser().getId() : null;
    String userName = (message.getChatPart() != null) ? message.getChatPart().getUser().getUserName() : "Unknown"; // TODO: 실제 유저명 매핑

    return MessageResponse.builder()
        .messageId(message.getMessageId())
        .chatId(chatId)
        .chatPartId(chatPartId)
        .userId(userId)
        .userName(userName)
        .type(message.getType())
        .content(message.getContent())
        .attachments(attachmentDtos)
        .createdAt(message.getCreatedAt())
        .isDeleted(message.getDeletedAt() != null)
        .parent(parentDto)
        .build();
  }

  public void setManager(boolean manager) {
    isManager = manager;
  }
}

package com.mudosa.musinsa.domain.chat.service;

import com.mudosa.musinsa.domain.chat.dto.*;
import com.mudosa.musinsa.domain.chat.entity.ChatPart;
import com.mudosa.musinsa.domain.chat.entity.ChatRoom;
import com.mudosa.musinsa.domain.chat.entity.Message;
import com.mudosa.musinsa.domain.chat.entity.MessageAttachment;
import com.mudosa.musinsa.domain.chat.repository.ChatPartRepository;
import com.mudosa.musinsa.domain.chat.repository.ChatRoomRepository;
import com.mudosa.musinsa.domain.chat.repository.MessageAttachmentRepository;
import com.mudosa.musinsa.domain.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 채팅 비즈니스 로직 처리 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatPartRepository chatPartRepository;
  private final MessageRepository messageRepository;
  private final MessageAttachmentRepository attachmentRepository;

  /**
   * 채팅방 생성
   */
  @Transactional
  public ChatRoomResponse createChatRoom(Long brandId, String type, List<Long> userIds) {
    // 채팅방 생성
    ChatRoom chatRoom = ChatRoom.builder()
        .brandId(brandId)
        .type(com.mudosa.musinsa.domain.chat.enums.ChatRoomType.valueOf(type))
        .build();

    chatRoom = chatRoomRepository.save(chatRoom);

    // 참여자 추가
    for (Long userId : userIds) {
      ChatPart part = ChatPart.builder()
          .chatRoom(chatRoom)
          .userId(userId)
          .role(com.mudosa.musinsa.domain.chat.enums.ChatPartRole.USER)
          .build();
      chatPartRepository.save(part);
    }

    return convertToChatRoomResponse(chatRoom);
  }

  /**
   * 메시지 저장
   */
  @Transactional
  public MessageResponse saveMessage(MessageSendRequest request) {
    // 채팅방 조회
    ChatRoom chatRoom = chatRoomRepository.findById(request.getChatId())
        .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

    // ChatPart 조회
    ChatPart chatPart = chatPartRepository.findById(request.getChatPartId())
        .orElseThrow(() -> new RuntimeException("참여자 정보를 찾을 수 없습니다."));

    // 권한 검증
    if (!chatPartRepository.existsActiveMember(request.getChatId(), chatPart.getUserId())) {
      throw new RuntimeException("채팅방에 참여하지 않은 사용자입니다.");
    }

    // 부모 메시지 조회 (답장인 경우)
    Message parent = null;
    if (request.getParentId() != null) {
      parent = messageRepository.findById(request.getParentId())
          .orElseThrow(() -> new RuntimeException("부모 메시지를 찾을 수 없습니다."));
    }

    // 메시지 생성
    Message message = Message.builder()
        .chatRoom(chatRoom)
        .chatPart(chatPart)
        .parent(parent)
        .type(request.getType())
        .content(request.getContent())
        .createdAt(LocalDateTime.now())
        .build();

    message = messageRepository.save(message);

    // 첨부파일 저장
    if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
      for (AttachmentRequest attachReq : request.getAttachments()) {
        MessageAttachment attachment = MessageAttachment.builder()
            .message(message)
            .attachmentUrl(attachReq.getAttachmentUrl())
            .mimeType(attachReq.getMimeType())
            .sizeBytes(attachReq.getSizeBytes())
            .build();
        attachmentRepository.save(attachment);
      }
    }

    // 채팅방의 마지막 메시지 시간 업데이트
    chatRoom = ChatRoom.builder()
        .chatId(chatRoom.getChatId())
        .brandId(chatRoom.getBrandId())
        .type(chatRoom.getType())
        .lastMessageAt(LocalDateTime.now())
        .build();
    chatRoomRepository.save(chatRoom);

    return convertToMessageResponse(message, chatPart.getUserId());
  }

  /**
   * 채팅방 메시지 목록 조회 (페이징)
   */
  public Page<MessageResponse> getChatMessages(Long chatId, Long userId, int page, int size) {
    // 권한 검증
    if (!chatPartRepository.existsActiveMember(chatId, userId)) {
      throw new RuntimeException("채팅방에 참여하지 않은 사용자입니다.");
    }

    Pageable pageable = PageRequest.of(page, size);
    Page<Message> messages = messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable);

    return messages.map(msg -> {
      Long senderId = msg.getChatPart() != null ? msg.getChatPart().getUserId() : null;
      return convertToMessageResponse(msg, senderId);
    });
  }

  /**
   * 사용자가 참여 중인 채팅방 목록 조회
   */
  public List<ChatRoomResponse> getUserChatRooms(Long userId) {
    List<ChatPart> parts = chatPartRepository.findActiveByUserId(userId);

    return parts.stream()
        .map(part -> convertToChatRoomResponse(part.getChatRoom()))
        .collect(Collectors.toList());
  }

  /**
   * 메시지 삭제 (소프트 삭제)
   */
  @Transactional
  public void deleteMessage(Long messageId, Long userId) {
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new RuntimeException("메시지를 찾을 수 없습니다."));

    // 권한 검증 (본인만 삭제 가능)
    if (message.getChatPart() != null &&
        !message.getChatPart().getUserId().equals(userId)) {
      throw new RuntimeException("메시지를 삭제할 권한이 없습니다.");
    }

    messageRepository.delete(message); // @SQLDelete로 소프트 삭제
  }

  /**
   * Entity -> DTO 변환
   */
  private MessageResponse convertToMessageResponse(Message message, Long userId) {
    List<AttachmentResponse> attachments = attachmentRepository
        .findByMessage_MessageId(message.getMessageId())
        .stream()
        .map(att -> AttachmentResponse.builder()
            .attachmentId(att.getAttachmentId())
            .attachmentUrl(att.getAttachmentUrl())
            .mimeType(att.getMimeType())
            .sizeBytes(att.getSizeBytes())
            .build())
        .collect(Collectors.toList());

    return MessageResponse.builder()
        .messageId(message.getMessageId())
        .chatId(message.getChatRoom().getChatId())
        .chatPartId(message.getChatPart() != null ?
            message.getChatPart().getChatPartId() : null)
        .userId(userId)
        .userName("User" + userId) // 실제로는 User 엔티티에서 조회
        .type(message.getType())
        .content(message.getContent())
        .parentId(message.getParent() != null ?
            message.getParent().getMessageId() : null)
        .attachments(attachments)
        .createdAt(message.getCreatedAt())
        .isDeleted(message.getDeletedAt() != null)
        .build();
  }

  private ChatRoomResponse convertToChatRoomResponse(ChatRoom chatRoom) {
    List<ChatPartResponse> participants = chatPartRepository
        .findByChatRoom_ChatId(chatRoom.getChatId())
        .stream()
        .map(part -> ChatPartResponse.builder()
            .chatPartId(part.getChatPartId())
            .userId(part.getUserId())
            .userName("User" + part.getUserId())
            .role(part.getRole().name())
            .joinedAt(part.getJoinedAt())
            .leftAt(part.getLeftAt())
            .build())
        .collect(Collectors.toList());

    // 마지막 메시지 조회
    MessageResponse lastMessage = messageRepository
        .findFirstByChatRoom_ChatIdOrderByCreatedAtDesc(chatRoom.getChatId())
        .map(msg -> convertToMessageResponse(msg,
            msg.getChatPart() != null ? msg.getChatPart().getUserId() : null))
        .orElse(null);

    return ChatRoomResponse.builder()
        .chatId(chatRoom.getChatId())
        .brandId(chatRoom.getBrandId())
        .type(chatRoom.getType().name())
        .lastMessageAt(chatRoom.getLastMessageAt())
        .participants(participants)
        .lastMessage(lastMessage)
        .build();
  }
}
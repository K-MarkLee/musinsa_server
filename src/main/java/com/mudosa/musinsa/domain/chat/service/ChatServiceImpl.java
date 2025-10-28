package com.mudosa.musinsa.domain.chat.service;

import com.mudosa.musinsa.domain.chat.dto.AttachmentResponse;
import com.mudosa.musinsa.domain.chat.dto.ChatRoomInfoResponse;
import com.mudosa.musinsa.domain.chat.dto.MessageResponse;
import com.mudosa.musinsa.domain.chat.dto.ParentMessageResponse;
import com.mudosa.musinsa.domain.chat.entity.ChatPart;
import com.mudosa.musinsa.domain.chat.entity.ChatRoom;
import com.mudosa.musinsa.domain.chat.entity.Message;
import com.mudosa.musinsa.domain.chat.entity.MessageAttachment;
import com.mudosa.musinsa.domain.chat.enums.MessageType;
import com.mudosa.musinsa.domain.chat.event.MessageCreatedEvent;
import com.mudosa.musinsa.domain.chat.repository.ChatPartRepository;
import com.mudosa.musinsa.domain.chat.repository.ChatRoomRepository;
import com.mudosa.musinsa.domain.chat.repository.MessageAttachmentRepository;
import com.mudosa.musinsa.domain.chat.repository.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
  private final ApplicationEventPublisher eventPublisher;

  /**
   * 메시지 저장
   */
  @Override
  @Transactional
  public MessageResponse saveMessage(Long chatId, Long userId, Long parentId, String content, List<MultipartFile> files) {

    // 1) 채팅방 & 참여자 존재 여부 확인
    ChatRoom chatRoom = chatRoomRepository.findById(chatId)
        .orElseThrow(() -> new EntityNotFoundException("ChatRoom not found: " + chatId));

    ChatPart chatPart = chatPartRepository.findByChatRoomChatIdAndUserId(chatId, userId)
        .orElseThrow(() -> new EntityNotFoundException(
            "ChatPart not found: chatId=" + chatId + ", userId=" + userId));

    Message parent = null;
    if (parentId != null) {
      parent = messageRepository.findById(parentId)
          .orElseThrow(() -> new EntityNotFoundException("Parent message not found: " + parentId));
    }


    // 2) 메시지 타입 결정
    boolean hasText = content != null && !content.trim().isEmpty();
    boolean hasFiles = files != null && !files.isEmpty();
    if (!hasText && !hasFiles) {
      throw new IllegalArgumentException("메시지 내용이나 파일 중 하나는 반드시 포함되어야 합니다.");
    }

    MessageType type = resolveType(hasText, hasFiles);

    // 3) 메시지 저장
    Message message = Message.builder()
        .chatRoom(chatRoom)
        .chatPart(chatPart)
        .content(hasText ? content.trim() : null)
        .type(type)
        .parent(parent)
        .createdAt(LocalDateTime.now())
        .build();

    Message createdMessage = messageRepository.save(message);

    // 4) 첨부 저장
    List<MessageAttachment> savedAttachments = new ArrayList<>();
    if (hasFiles) {
      for (MultipartFile file : files) {
        if (file == null || file.isEmpty()) continue;

        try {
          String uploadDir = new ClassPathResource("static/").getFile().getAbsolutePath()
              + "/chat/" + chatId + "/message/" + createdMessage.getMessageId();
          Files.createDirectories(Paths.get(uploadDir));

          String original = Objects.requireNonNullElse(file.getOriginalFilename(), "unknown");
          String safeName = java.util.UUID.randomUUID() + "_" +
              org.springframework.util.StringUtils.cleanPath(original);

          Path targetPath = Paths.get(uploadDir, safeName).toAbsolutePath().normalize();
          file.transferTo(targetPath.toFile());

          // 정적 서빙 경로 매핑에 맞춰 URL/상대경로를 저장 (예시는 상대경로)
          String storedUrl = "/chat/" + chatId + "/message/" + createdMessage.getMessageId() + "/" + safeName;

          MessageAttachment att = MessageAttachment.builder()
              .attachmentUrl(storedUrl)
              .message(createdMessage)
              .mimeType(file.getContentType())
              .sizeBytes(file.getSize())
              .build();

          savedAttachments.add(attachmentRepository.save(att));
          log.info("파일 업로드 성공: {}", targetPath);

        } catch (IOException e) {
          // 전부 롤백
          throw new RuntimeException("파일 저장 실패: " + file.getOriginalFilename(), e);
        }
      }
    }

    // 5) 응답 DTO 생성
    MessageResponse dto = MessageResponse.from(createdMessage, savedAttachments);


    // 6) AFTER_COMMIT에만 브로드캐스트 (도메인 이벤트 발행)
    eventPublisher.publishEvent(new MessageCreatedEvent(dto));

    return dto;
  }

  private MessageType resolveType(boolean hasText, boolean hasFiles) {
    if (hasFiles && !hasText) return MessageType.FILE;  // 프로젝트 enum에 맞게 조정
    if (hasText && !hasFiles) return MessageType.TEXT;
    // 텍스트+파일 동시 전송이면 TEXT로 두고 첨부 포함, 혹은 별도 타입이 있으면 사용
    return MessageType.TEXT;
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
      var cp = msg.getChatPart();   // 발신자(시스템 메시지면 null)
      var parent = msg.getParent(); // 답장 원본(없을 수 있음)

      // 현재 메시지 첨부 → DTO 변환
      List<AttachmentResponse> attachmentDtos =
          (msg.getAttachments() != null ? msg.getAttachments() : List.<MessageAttachment>of())
              .stream()
              .map(a -> AttachmentResponse.builder()
                  .attachmentId(a.getAttachmentId())
                  .attachmentUrl(a.getAttachmentUrl())
                  .mimeType(a.getMimeType())
                  .sizeBytes(a.getSizeBytes())
                  .build())
              .toList();

      // 부모 메시지 DTO (있을 때만)
      ParentMessageResponse parentDto = null;
      if (parent != null) {
        List<AttachmentResponse> parentAttachmentDtos =
            (parent.getAttachments() != null ? parent.getAttachments() : List.<MessageAttachment>of())
                .stream()
                .map(a -> AttachmentResponse.builder()
                    .attachmentId(a.getAttachmentId())
                    .attachmentUrl(a.getAttachmentUrl())
                    .mimeType(a.getMimeType())
                    .sizeBytes(a.getSizeBytes())
                    .build())
                .toList();

        parentDto = ParentMessageResponse.builder()
            .messageId(parent.getMessageId())
            .userId(parent.getChatPart() != null ? parent.getChatPart().getUserId() : null)
            .userName(parent.getChatPart() != null ? "임시 이름" : "시스템") // TODO: User 연동시 교체
            .content(parent.getContent())
            .createdAt(parent.getCreatedAt())
            .attachments(parentAttachmentDtos)
            .isDeleted(parent.getDeletedAt() != null)
            .build();
      }

      return MessageResponse.builder()
          .messageId(msg.getMessageId())
          .chatId(msg.getChatRoom().getChatId()) // 식별자만 꺼내기(프록시 안전)
          .chatPartId(cp != null ? cp.getChatPartId() : null)
          .userId(cp != null ? cp.getUserId() : null)
          .userName(cp != null ? "임시 이름" : "시스템")
          .type(msg.getType())
          .content(msg.getContent())
          .attachments(attachmentDtos)           // ✅ 엔티티 컬렉션 노출 금지
          .createdAt(msg.getCreatedAt())
          .parent(parentDto)                     // ✅ 엔티티 대신 DTO
          .build();
    });
  }

  @Override
  public ChatRoomInfoResponse getChatRoomInfoByChatId(Long chatId) {
    ChatRoom chatRoom = chatRoomRepository.findById(chatId)
        .orElseThrow(() -> new EntityNotFoundException("ChatRoom not found: " + chatId));

    return ChatRoomInfoResponse.builder()
        .brandId(chatRoom.getBrand().getBrandId())
        .brandNameKo(chatRoom.getBrand().getNameKo())
        .chatId(chatRoom.getChatId())
        .type(chatRoom.getType())
        .partNum((long) chatRoom.getParts().size())
        .lastMessageAt(chatRoom.getLastMessageAt())
        .build();
  }


//
//  /**
//   * 사용자가 참여 중인 채팅방 목록 조회
//   */
//  public List<ChatRoomResponse> getUserChatRooms(Long userId) {
//    List<ChatPart> parts = chatPartRepository.findActiveByUserId(userId);
//
//    return parts.stream()
//        .map(part -> convertToChatRoomResponse(part.getChatRoom()))
//        .collect(Collectors.toList());
//  }
//
//  /**
//   * 메시지 삭제 (소프트 삭제)
//   */
//  @Transactional
//  public void deleteMessage(Long messageId, Long userId) {
//    Message message = messageRepository.findById(messageId)
//        .orElseThrow(() -> new RuntimeException("메시지를 찾을 수 없습니다."));
//
//    // 권한 검증 (본인만 삭제 가능)
//    if (message.getChatPart() != null &&
//        !message.getChatPart().getUserId().equals(userId)) {
//      throw new RuntimeException("메시지를 삭제할 권한이 없습니다.");
//    }
//
//    messageRepository.delete(message); // @SQLDelete로 소프트 삭제
//  }
//
//  /**
//   * Entity -> DTO 변환
//   */
//  private MessageResponse convertToMessageResponse(Message message, Long userId) {
//    List<AttachmentResponse> attachments = attachmentRepository
//        .findByMessage_MessageId(message.getMessageId())
//        .stream()
//        .map(att -> AttachmentResponse.builder()
//            .attachmentId(att.getAttachmentId())
//            .attachmentUrl(att.getAttachmentUrl())
//            .mimeType(att.getMimeType())
//            .sizeBytes(att.getSizeBytes())
//            .build())
//        .collect(Collectors.toList());
//
//    return MessageResponse.builder()
//        .messageId(message.getMessageId())
//        .chatId(message.getChatRoom().getChatId())
//        .chatPartId(message.getChatPart() != null ?
//            message.getChatPart().getChatPartId() : null)
//        .userId(userId)
//        .userName("User" + userId) // 실제로는 User 엔티티에서 조회
//        .type(message.getType())
//        .content(message.getContent())
//        //답장 기능 구현시
////        .parentId(message.getParent() != null ?
////            message.getParent().getMessageId() : null)
//        .attachments(attachments)
//        .createdAt(message.getCreatedAt())
//        .isDeleted(message.getDeletedAt() != null)
//        .build();
//  }
//
//  private ChatRoomResponse convertToChatRoomResponse(ChatRoom chatRoom) {
//    List<ChatPartResponse> participants = chatPartRepository
//        .findByChatRoom_ChatId(chatRoom.getChatId())
//        .stream()
//        .map(part -> ChatPartResponse.builder()
//            .chatPartId(part.getChatPartId())
//            .userId(part.getUserId())
//            .userName("User" + part.getUserId())
//            .role(part.getRole().name())
//            .joinedAt(part.getJoinedAt())
//            .leftAt(part.getLeftAt())
//            .build())
//        .collect(Collectors.toList());
//
//    // 마지막 메시지 조회
//    MessageResponse lastMessage = messageRepository
//        .findFirstByChatRoom_ChatIdOrderByCreatedAtDesc(chatRoom.getChatId())
//        .map(msg -> convertToMessageResponse(msg,
//            msg.getChatPart() != null ? msg.getChatPart().getUserId() : null))
//        .orElse(null);
//
//    return ChatRoomResponse.builder()
//        .chatId(chatRoom.getChatId())
//        .brandId(chatRoom.getBrandId())
//        .type(chatRoom.getType().name())
//        .lastMessageAt(chatRoom.getLastMessageAt())
//        .participants(participants)
//        .lastMessage(lastMessage)
//        .build();
//  }


}
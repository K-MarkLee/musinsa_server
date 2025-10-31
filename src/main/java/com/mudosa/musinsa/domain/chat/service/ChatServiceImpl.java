package com.mudosa.musinsa.domain.chat.service;

import com.mudosa.musinsa.brand.domain.repository.BrandMemberRepository;
import com.mudosa.musinsa.domain.chat.dto.*;
import com.mudosa.musinsa.domain.chat.entity.ChatPart;
import com.mudosa.musinsa.domain.chat.entity.ChatRoom;
import com.mudosa.musinsa.domain.chat.entity.Message;
import com.mudosa.musinsa.domain.chat.entity.MessageAttachment;
import com.mudosa.musinsa.domain.chat.enums.ChatPartRole;
import com.mudosa.musinsa.domain.chat.enums.MessageType;
import com.mudosa.musinsa.domain.chat.event.MessageCreatedEvent;
import com.mudosa.musinsa.domain.chat.repository.ChatPartRepository;
import com.mudosa.musinsa.domain.chat.repository.ChatRoomRepository;
import com.mudosa.musinsa.domain.chat.repository.MessageAttachmentRepository;
import com.mudosa.musinsa.domain.chat.repository.MessageRepository;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
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
  private final UserRepository userRepository;
  private final BrandMemberRepository brandMemberRepository;


  @Override
  public List<ChatRoomInfoResponse> getChatRoomByUserId(Long userId) {
    List<ChatRoom> chatRooms = chatRoomRepository.findDistinctByParts_User_IdAndParts_LeftAtIsNull(userId);

    return chatRooms.stream()
        .map(room -> ChatRoomInfoResponse.builder()
            .chatId(room.getChatId())
            .brandId(room.getBrand().getBrandId())
            .brandNameKo(room.getBrand().getNameKo())
            .type(room.getType())
            .lastMessageAt(room.getLastMessageAt())
            .partNum((long) room.getParts().size())
            .isParticipate(true)
            .logoUrl(room.getBrand().getLogoUrl())
            .build())
        .toList();
  }


  /**
   * 메시지 저장
   */
  @Override
  @Transactional
  public MessageResponse saveMessage(Long chatId, Long userId, Long parentId, String content, List<MultipartFile> files) {

    // 1) 채팅방 & 참여자 존재 여부 확인
    ChatRoom chatRoom = chatRoomRepository.findById(chatId)
        .orElseThrow(() -> new EntityNotFoundException("ChatRoom not found: " + chatId));

    ChatPart chatPart = chatPartRepository.findByChatRoom_ChatIdAndUserIdAndLeftAtIsNull(chatId, userId)
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

    chatRoom.setLastMessageAt(LocalDateTime.now());
    
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
  @Override
  public Page<MessageResponse> getChatMessages(Long chatId, Long userId, int page, int size) {
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
            .userId(parent.getChatPart() != null ? parent.getChatPart().getUser().getId() : null)
            .userName(parent.getChatPart() != null ? parent.getChatPart().getUser().getUserName() : "Unknown") // TODO: User 연동시 교체
            .content(parent.getContent())
            .createdAt(parent.getCreatedAt())
            .attachments(parentAttachmentDtos)
            .isDeleted(parent.getDeletedAt() != null)
            .build();
      }
      boolean isManager = brandMemberRepository.existsByBrand_BrandIdAndUserId(msg.getChatRoom().getBrand().getBrandId(), msg.getChatPart().getUser().getId());

      return MessageResponse.builder()
          .messageId(msg.getMessageId())
          .chatId(msg.getChatRoom().getChatId()) // 식별자만 꺼내기(프록시 안전)
          .chatPartId(cp != null ? cp.getChatPartId() : null)
          .userId(cp != null ? cp.getUser().getId() : null)
          .userName(cp != null ? cp.getUser().getUserName() : "Unknown")
          .type(msg.getType())
          .content(msg.getContent())
          .attachments(attachmentDtos)           // 엔티티 컬렉션 노출 금지
          .createdAt(msg.getCreatedAt())
          .parent(parentDto)                     // 엔티티 대신 DTO
          .isManager(isManager)
          .build();
    });
  }


  /**
   * 채팅방 정보 조회
   */
  @Override
  public ChatRoomInfoResponse getChatRoomInfoByChatId(Long chatId, Long userId) {
    ChatRoom chatRoom = chatRoomRepository.findById(chatId)
        .orElseThrow(() -> new EntityNotFoundException("ChatRoom not found: " + chatId));

    boolean isParticipate = chatPartRepository.existsByChatRoom_ChatIdAndUser_IdAndLeftAtIsNull(chatId, userId);

    return ChatRoomInfoResponse.builder()
        .brandId(chatRoom.getBrand().getBrandId())
        .brandNameKo(chatRoom.getBrand().getNameKo())
        .chatId(chatRoom.getChatId())
        .type(chatRoom.getType())
        .partNum((long) chatRoom.getParts().size())
        .lastMessageAt(chatRoom.getLastMessageAt())
        .isParticipate(isParticipate)
        .build();
  }

  /**
   * 채팅방 참여
   */
  @Override
  @Transactional
  public ChatPartResponse addParticipant(Long chatId, Long userId) {
    // 1️⃣ 채팅방 존재 확인
    ChatRoom chatRoom = chatRoomRepository.findById(chatId)
        .orElseThrow(() -> new EntityNotFoundException("ChatRoom nosaveMessaget found: " + chatId));

    // 2️⃣ 이미 참여 중인지 확인 (중복 방지)
    if (chatPartRepository.existsByChatRoom_ChatIdAndUser_IdAndLeftAtIsNull(chatId, userId)) {
      throw new IllegalStateException("User already joined this chat room.");
    }
    User user = userRepository.getById(userId);
    // 3️⃣ 참여자 생성
    ChatPart chatPart = ChatPart.builder()
        .chatRoom(chatRoom)
        .user(user)
        .role(ChatPartRole.USER)
        .build();

    chatPart = chatPartRepository.save(chatPart);

    // 4️⃣ DTO 변환
    return ChatPartResponse.builder()
        .chatPartId(chatPart.getChatPartId())
        .userId(chatPart.getUser().getId())
        .userName(chatPart.getUser().getUserName())
        .joinedAt(chatPart.getJoinedAt())
        .build();
  }

  @Transactional
  @Override
  public void leaveChat(Long chatId, Long userId) {
    // 활성 상태의 참여 기록 조회
    ChatPart chatPart = chatPartRepository
        .findByChatRoom_ChatIdAndUserIdAndLeftAtIsNull(chatId, userId)
        .orElseThrow(() -> new IllegalStateException("참여 중인 채팅방이 존재하지 않습니다."));

    // 이미 나갔는지 확인할 필요 없음 (조건상 leftAt IS NULL 보장됨)
    chatPart.setLeftAt(LocalDateTime.now());
  }

}
package com.mudosa.musinsa.domain.chat.service;

import com.mudosa.musinsa.brand.domain.repository.BrandMemberRepository;
import com.mudosa.musinsa.domain.chat.dto.*;
import com.mudosa.musinsa.domain.chat.entity.ChatPart;
import com.mudosa.musinsa.domain.chat.entity.ChatRoom;
import com.mudosa.musinsa.domain.chat.entity.Message;
import com.mudosa.musinsa.domain.chat.entity.MessageAttachment;
import com.mudosa.musinsa.domain.chat.enums.ChatPartRole;
import com.mudosa.musinsa.domain.chat.event.MessageCreatedEvent;
import com.mudosa.musinsa.domain.chat.mapper.ChatRoomMapper;
import com.mudosa.musinsa.domain.chat.repository.ChatPartRepository;
import com.mudosa.musinsa.domain.chat.repository.ChatRoomRepository;
import com.mudosa.musinsa.domain.chat.repository.MessageAttachmentRepository;
import com.mudosa.musinsa.domain.chat.repository.MessageRepository;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.notification.domain.event.NotificationRequiredEvent;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
  private final ChatRoomMapper chatRoomMapper;

  @Override
  public List<ChatRoomInfoResponse> getChatRoomByUserId(Long userId) {
    //userId, chatId 쌍이 존재하고 delete_at이 null(떠나지 않은 사용자)에 만족하는 채팅방 불러오기
    List<ChatRoom> chatRooms =
        chatRoomRepository.findDistinctByParts_User_IdAndParts_DeletedAtIsNull(userId);

    //채팅방을 dto list 형태로 변환
    return chatRooms.stream()
        .map(chatRoom -> {
          ChatRoomInfoResponse base = chatRoomMapper.toChatRoomInfoResponse(chatRoom);
          base.setParticipate(true); // 이 시점에서는 유저가 참여 중인 방만 조회했으므로 true 고정
          return base;
        })
        .toList();
  }


  /**
   * 메시지 저장
   */
  @Override
  @Transactional
  public MessageResponse saveMessage(Long chatId,
                                     Long userId,
                                     Long parentId,
                                     String content,
                                     List<MultipartFile> files) {

    //시작 시간으로 고정(여러번 호출시 시간이 달라지는 문제 발생 가능)
    LocalDateTime now = LocalDateTime.now();

    // 0) 기본 검증
    validateMessageOrFiles(content, files);

    // 1) 채팅방/참여자 확인
    ChatRoom chatRoom = getChatRoomOrThrow(chatId);

    //참여자 정보 확인
    ChatPart chatPart = getChatPartOrThrow(chatId, userId);

    // 2) 부모 메시지 확인 (같은 방인지까지 확인)
    Message parent = getParentMessageIfExists(parentId, chatId);

    // 3) 메시지 엔티티 생성/저장
    Message message = Message.builder()
        .chatRoom(chatRoom)
        .chatPart(chatPart)
        .content(StringUtils.hasText(content) ? content.trim() : null)
        .parent(parent)
        .createdAt(now)
        .build();

    Message savedMessage = messageRepository.save(message);

    // 채팅방 마지막 메시지 시간 갱신
    chatRoom.setLastMessageAt(now);

    // 4) 첨부파일 저장
    List<MessageAttachment> savedAttachments = saveAttachments(chatId, savedMessage.getMessageId(), files, savedMessage);

    // 5) 응답 생성
    MessageResponse dto = MessageResponse.from(savedMessage, savedAttachments);

    // 6) 이벤트 발행 (AFTER_COMMIT 리스너에서 실제 전송)
    publishMessageEvents(userId, chatId, dto, savedMessage.getContent());

    return dto;
  }

  /**
   * 채팅방 메시지 목록 조회 (페이징)
   */

  @Override
  public Page<MessageResponse> getChatMessages(Long chatId, Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Message> messages = messageRepository.findPageWithRelationsByChatId(chatId, pageable);

    // 비어 있으면 즉시 반환
    if (messages.isEmpty()) return Page.empty(pageable);

    // 동일 채팅방 → 동일 브랜드. 첫 건으로 brandId 획득
    Long brandId = messages.getContent().get(0).getChatRoom().getBrand().getBrandId();

    // (1) 브랜드 관리자 유저 ID 집합 미리 로딩 → per-row exists 제거
    List<Long> managerIds = brandMemberRepository.findActiveUserIdsByBrandId(brandId);
    Set<Long> managerUserIds = new HashSet<>(managerIds);

    // (2) 현재 페이지의 메시지/부모 메시지 ID 수집
    List<Long> messageIds = messages.getContent().stream()
        .map(Message::getMessageId)
        .toList();

    List<Long> parentIds = messages.getContent().stream()
        .map(Message::getParent)
        .filter(Objects::nonNull)
        .map(Message::getMessageId)
        .toList();

    // (3) 첨부 일괄 로딩 → 메시지ID별 그룹핑 맵 구성
    List<Long> allIds = Stream.concat(messageIds.stream(), parentIds.stream()).toList();
    Map<Long, List<AttachmentResponse>> attachmentMap =
        attachmentRepository.findAllByMessageIdIn(allIds).stream()
            .collect(Collectors.groupingBy(
                ma -> ma.getMessage().getMessageId(),
                Collectors.mapping(this::toAttachmentDto, Collectors.toList())
            ));

    // (4) DTO 변환 (람다 내부에서 외부 맵/셋 참조)
    return messages.map(msg -> {
      var cp = msg.getChatPart();
      var parent = msg.getParent();

      List<AttachmentResponse> currentAttachments =
          attachmentMap.getOrDefault(msg.getMessageId(), Collections.emptyList());

      ParentMessageResponse parentDto = null;
      if (parent != null) {
        List<AttachmentResponse> parentAttachments =
            attachmentMap.getOrDefault(parent.getMessageId(), Collections.emptyList());
        Long parentUserId = (parent.getChatPart() != null && parent.getChatPart().getUser() != null)
            ? parent.getChatPart().getUser().getId() : null;
        String parentUserName = (parent.getChatPart() != null && parent.getChatPart().getUser() != null)
            ? parent.getChatPart().getUser().getUserName() : "SYSTEM";

        parentDto = ParentMessageResponse.builder()
            .messageId(parent.getMessageId())
            .userId(parentUserId)
            .userName(parentUserName)
            .content(parent.getContent())
            .createdAt(parent.getCreatedAt())
            .attachments(parentAttachments)
            .isDeleted(parent.getDeletedAt() != null)
            .build();
      }

      Long senderUserId = (cp != null && cp.getUser() != null) ? cp.getUser().getId() : null;
      String senderName = (cp != null && cp.getUser() != null) ? cp.getUser().getUserName() : "SYSTEM";
      boolean isManager = (senderUserId != null) && managerUserIds.contains(senderUserId);

      return MessageResponse.builder()
          .messageId(msg.getMessageId())
          .chatId(msg.getChatRoom().getChatId())
          .chatPartId(cp != null ? cp.getChatPartId() : null)
          .userId(senderUserId)
          .userName(senderName)
          .content(msg.getContent())
          .attachments(currentAttachments)
          .createdAt(msg.getCreatedAt())
          .parent(parentDto)
          .isManager(isManager)
          .build();
    });
  }

  private AttachmentResponse toAttachmentDto(MessageAttachment a) {
    return AttachmentResponse.builder()
        .attachmentId(a.getAttachmentId())
        .attachmentUrl(a.getAttachmentUrl())
        .mimeType(a.getMimeType())
        .sizeBytes(a.getSizeBytes())
        .build();
  }

  /**
   * 채팅방 정보 조회
   */
  @Override
  public ChatRoomInfoResponse getChatRoomInfoByChatId(Long chatId, Long userId) {

    //채팅룸 찾기
    ChatRoom chatRoom = getChatRoomOrThrow(chatId);

    //참여여부
    boolean isParticipate = isParticipant(chatId, userId);

    //참여자수
    long partNum = chatPartRepository
        .countByChatRoom_ChatIdAndDeletedAtIsNull(chatId);

    //형태 변경
    return chatRoomMapper.toChatRoomInfoResponse(chatRoom, isParticipate, partNum);
  }

  /**
   * 채팅방 참여
   */
  @Override
  @Transactional
  public ChatPartResponse addParticipant(Long chatId, Long userId) {
    // 1-1) 채팅방 존재 확인
    ChatRoom chatRoom = getChatRoomOrThrow(chatId);

    // 1-2) 유저 존재 확인
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 2) 이미 참여 중인지 확인 (중복 방지)
    validateNotAlreadyParticipant(chatId, userId);

    // 3) 참여자 생성
    ChatPart chatPart = ChatPart.builder()
        .chatRoom(chatRoom)
        .user(user)
        .role(ChatPartRole.USER)
        .build();

    chatPart = chatPartRepository.save(chatPart);

    // 4) DTO 변환
    return toResponse(chatPart);
  }

  /**
   * 채팅 떠나기
   */
  @Transactional
  @Override
  public void leaveChat(Long chatId, Long userId) {
    // 활성 상태의 참여 기록 조회
    ChatPart chatPart = getChatPartOrThrow(chatId, userId);

    // 이미 나갔는지 확인할 필요 없음 (조건상 DeletedAt IS NULL 보장됨)
    chatPart.setDeletedAt(LocalDateTime.now());
  }


  /** -- helper method -- */

  /**
   * 채팅방 찾기 (없으면 오류)
   */
  private ChatRoom getChatRoomOrThrow(Long chatId) {
    return chatRoomRepository.findById(chatId)
        .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_NOT_FOUND));
  }

  /**
   * 참여정보 찾기 (없으면 오류)
   */
  private ChatPart getChatPartOrThrow(Long chatId, Long userId) {
    return chatPartRepository
        .findByChatRoom_ChatIdAndUserIdAndDeletedAtIsNull(chatId, userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));
  }

  /**
   * 이미 참여 중인 사용자인지 확인 (이미 존재하면 오류)
   */
  // 1) 존재 여부만 보는 메서드
  private boolean isParticipant(Long chatId, Long userId) {
    return chatPartRepository
        .existsByChatRoom_ChatIdAndUser_IdAndDeletedAtIsNull(chatId, userId);
  }

  // 2) 추가할 때만 쓰는 검증 메서드
  private void validateNotAlreadyParticipant(Long chatId, Long userId) {
    if (isParticipant(chatId, userId)) {
      throw new BusinessException(ErrorCode.CHAT_PARTICIPANT_ALREADY_EXISTS);
    }
  }

  /**
   * ChatPartResponse DTO 변환 메서드
   */
  private ChatPartResponse toResponse(ChatPart chatPart) {
    return ChatPartResponse.builder()
        .chatPartId(chatPart.getChatPartId())
        .userId(chatPart.getUser().getId())
        .userName(chatPart.getUser().getUserName())
        .createdAt(chatPart.getCreatedAt())
        .build();
  }

  //메시지와 파일이 모두 없는지 여부 확인
  private void validateMessageOrFiles(String content, List<MultipartFile> files) {
    boolean noMessage = (content == null || content.trim().isEmpty());
    boolean noFiles = (files == null || files.isEmpty());

    //둘 다 없으면 오류 반환
    if (noMessage && noFiles) {
      throw new BusinessException(ErrorCode.MESSAGE_OR_FILE_REQUIRED);
    }
  }

  //부모 메시지 반환
  private Message getParentMessageIfExists(Long parentId, Long chatId) {
    //부모 id가 없으면 null 반환
    if (parentId == null) {
      return null;
    }
    //해당 id에 해당하는 부모 메시지가 없으면 오류 반환
    Message parent = messageRepository.findById(parentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_PARENT_NOT_FOUND));

    // 부모 메시지가 다른 방의 메시지면 막기
    if (!parent.getChatRoom().getChatId().equals(chatId)) {
      throw new BusinessException(ErrorCode.MESSAGE_PARENT_NOT_FOUND);
    }

    //부모 메시지 반환
    return parent;
  }

  //메시지 파일 저장 후 저장된 파일 리스트 반환
  private List<MessageAttachment> saveAttachments(Long chatId,
                                                  Long messageId,
                                                  List<MultipartFile> files,
                                                  Message message) {
    //파일 없으면 빈 배열 반환
    if (files == null || files.isEmpty()) {
      return List.of();
    }

    //저장 결과 배열
    List<MessageAttachment> result = new ArrayList<>();

    //모든 파일에 대하여
    for (MultipartFile file : files) {
      //없으면 pass
      if (file == null || file.isEmpty()) continue;

      //저장
      try {
        // === 실제 경로 생성 ===
        String uploadDir = new ClassPathResource("static/").getFile().getAbsolutePath()
            + "/chat/" + chatId + "/message/" + messageId;
        Files.createDirectories(Paths.get(uploadDir));

        String original = Objects.requireNonNullElse(file.getOriginalFilename(), "unknown");
        String safeName = UUID.randomUUID() + "_" + org.springframework.util.StringUtils.cleanPath(original);

        Path targetPath = Paths.get(uploadDir, safeName).toAbsolutePath().normalize();
        file.transferTo(targetPath.toFile());

        String storedUrl = "/chat/" + chatId + "/message/" + messageId + "/" + safeName;

        //메시지 첨부 파일 객체 생성
        MessageAttachment att = MessageAttachment.builder()
            .attachmentUrl(storedUrl)
            .message(message)
            .mimeType(file.getContentType())
            .sizeBytes(file.getSize())
            .build();

        //저장
        result.add(attachmentRepository.save(att));
        log.info("파일 업로드 성공: {}", targetPath);

      } catch (IOException e) {
        // 저장 실패시 오류
        throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
      }
    }
    //저장된 첨부파일 리스트 반환
    return result;
  }

  //event 발행
  private void publishMessageEvents(Long userId,
                                    Long chatId,
                                    MessageResponse dto,
                                    String content) {
    eventPublisher.publishEvent(new MessageCreatedEvent(dto));
    eventPublisher.publishEvent(new NotificationRequiredEvent(userId, chatId, content));
  }
}
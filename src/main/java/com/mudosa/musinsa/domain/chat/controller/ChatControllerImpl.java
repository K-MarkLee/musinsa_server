package com.mudosa.musinsa.domain.chat.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.mudosa.musinsa.common.dto.ApiResponse;
import com.mudosa.musinsa.domain.chat.dto.ChatPartResponse;
import com.mudosa.musinsa.domain.chat.dto.ChatRoomInfoResponse;
import com.mudosa.musinsa.domain.chat.dto.MessageCursor;
import com.mudosa.musinsa.domain.chat.dto.MessageResponse;
import com.mudosa.musinsa.domain.chat.service.ChatService;
import com.mudosa.musinsa.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 채팅 REST API 컨트롤러
 * - 채팅방 목록 조회
 * - 메시지 히스토리 조회 (페이징)
 * - 채팅방 생성/삭제
 * - 메시지 전송
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatControllerImpl implements ChatController {

  private final ChatService chatService;

  /**
   * 채팅 메시지 전송
   * POST /api/chat/{chatId}/send
   */
  @PostMapping(
      path = "/{chatId}/send",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  @Override
  public ApiResponse<MessageResponse> sendMessage(
      @PathVariable Long chatId,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(value = "parentId", required = false) Long parentId,
      @RequestPart(value = "message", required = false) String message,
      @RequestPart(value = "files", required = false) List<MultipartFile> files
  ) throws FirebaseMessagingException {
    Long userId = userDetails.getUserId();
    log.info("[API][POST] /api/chat/{}/send userId={} parentId={} hasMessage={} fileCount={}",
        chatId,
        userId,
        parentId,
        (message != null && !message.isBlank()),
        (files != null ? files.size() : 0)
    );

    LocalDateTime now = LocalDateTime.now();
    MessageResponse savedMessage = chatService.saveMessage(chatId, userId, parentId, message, files, now);
    return ApiResponse.success(savedMessage, "메시지를 성공적으로 전송했습니다.");
  }


  /**
   * 채팅방 이전 메시지 조회 (페이징)
   * GET /api/chat/1/messages?page=0&size=20
   */
  @GetMapping("/{chatId}/messages")
  @Override
  public ApiResponse<Slice<MessageResponse>> getChatMessages(
      @PathVariable Long chatId,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(required = false) String cursorCreatedAt,
      @RequestParam(required = false)
      Long cursorMessageId,
      @RequestParam(defaultValue = "20") int size
  ) {
    Long userId = userDetails.getUserId();
    log.info("[API][GET] /api/chat/{}/messages userId={} size={}",
        chatId, userId, size);

    LocalDateTime cursorCreatedAtDt = null;

    if (cursorCreatedAt != null && !cursorCreatedAt.isBlank()) {
      try {
        // "2025-11-17T15:33:36" 전용
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        cursorCreatedAtDt = LocalDateTime.parse(cursorCreatedAt, formatter);
      } catch (DateTimeParseException e) {
        log.warn("잘못된 cursorCreatedAt 포맷: {}", cursorCreatedAt, e);
        throw new IllegalArgumentException("cursorCreatedAt는 yyyy-MM-dd'T'HH:mm:ss 형식이어야 합니다.");
      }
    }
 
    MessageCursor cursor = (cursorCreatedAtDt != null && cursorMessageId != null)
        ? new MessageCursor(cursorCreatedAtDt, cursorMessageId)
        : null;

    Slice<MessageResponse> messages = chatService.getChatMessages(chatId, cursor, size);

    return ApiResponse.success(
        messages,
        "이전 메시지를 성공적으로 조회했습니다"
    );
  }

  /**
   * 채팅방 정보 조회
   * GET /api/chat/1/info
   */
  @GetMapping("/{chatId}/info")
  @Override
  public ApiResponse<ChatRoomInfoResponse> getChatInfo(
      @PathVariable Long chatId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUserId();
    log.info("[API][GET] /api/chat/{}/info userId={}", chatId, userId);

    return ApiResponse.success(chatService.getChatRoomInfoByChatId(chatId, userId), "채팅방의 정보를 성공적으로 조회했습니다.");
  }


  /**
   * 채팅방 참가
   * POST /api/chat/1/participants
   */
  @PostMapping("/{chatId}/participants")
  @Override
  public ApiResponse<ChatPartResponse> addParticipant(
      @PathVariable Long chatId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUserId();
    log.info("[API][POST] /api/chat/{}/participants userId={}", chatId, userId);

    return ApiResponse.success(chatService.addParticipant(chatId, userId), "채팅방에 성공적으로 참여했습니다.");
  }

  /**
   * 채팅방 나가기
   * PATCH /api/chat/1/leave
   */
  @PatchMapping("/{chatId}/leave")
  @Override
  public ApiResponse<List<ChatRoomInfoResponse>> leaveChat(@PathVariable Long chatId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUserId();
    log.info("[API][PATCH] /api/chat/{}/leave userId={}", chatId, userId);

    chatService.leaveChat(chatId, userId);
    return ApiResponse.success(chatService.getChatRoomByUserId(userId), "채팅방에서 성공적으로 퇴장하셨습니다.");
  }

  /**
   * 나의 참가 채팅방 조회
   * GET /api/chat/1/my
   */
  @GetMapping("/my")
  @Override
  public ApiResponse<List<ChatRoomInfoResponse>> getMyChat(@AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUserId();
    log.info("[API][GET] /api/chat/my userId={}", userId);

    return ApiResponse.success(chatService.getChatRoomByUserId(userId), "나의 참여 채팅방 목록이 성공적으로 조회되었습니다.");
  }


}
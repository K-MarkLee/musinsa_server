package com.mudosa.musinsa.domain.chat.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.mudosa.musinsa.domain.chat.dto.ChatPartResponse;
import com.mudosa.musinsa.domain.chat.dto.ChatRoomInfoResponse;
import com.mudosa.musinsa.domain.chat.dto.MessageResponse;
import com.mudosa.musinsa.domain.chat.service.ChatService;
import com.mudosa.musinsa.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 채팅 REST API 컨트롤러
 * - 채팅방 목록 조회
 * - 메시지 히스토리 조회 (페이징)
 * - 채팅방 생성/삭제
 * - 파일 업로드 등
 */

@RestController
@RequestMapping("/api/chat")
@Slf4j
@RequiredArgsConstructor
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
  public ResponseEntity<MessageResponse> sendMessage(
      @PathVariable Long chatId,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(value = "parentId", required = false) Long parentId,
      @RequestPart(value = "message", required = false) String message,
      @RequestPart(value = "files", required = false) List<MultipartFile> files
  ) throws FirebaseMessagingException {
    Long userId = userDetails.getUserId();

    MessageResponse savedMessage = chatService.saveMessage(chatId, userId, parentId, message, files);
    return ResponseEntity.ok(savedMessage);
  }


  /**
   * 채팅방 이전 메시지 조회 (페이징)
   * GET /api/chat/1/messages?userId=1&page=0&size=20
   */
  @GetMapping("/{chatId}/messages")
  public ResponseEntity<Page<MessageResponse>> getChatMessages(
      @PathVariable Long chatId,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    Long userId = userDetails.getUserId();

    Page<MessageResponse> messages = chatService.getChatMessages(chatId, userId, page, size);

    return ResponseEntity.ok(messages);
  }

  /**
   * 채팅방 정보 조회
   * GET /api/chat/1/info
   */
  @GetMapping("/{chatId}/info")
  public ResponseEntity<ChatRoomInfoResponse> getChatInfo(@PathVariable Long chatId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUserId();

    return ResponseEntity.ok(chatService.getChatRoomInfoByChatId(chatId, userId));
  }


  /**
   * 채팅방 참가
   * POST /api/chat/1/participants
   */
  @PostMapping("/{chatId}/participants")
  public ResponseEntity<ChatPartResponse> addParticipant(@PathVariable Long chatId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUserId();
    return ResponseEntity.ok(chatService.addParticipant(chatId, userId));
  }

  /**
   * 채팅방 나가기
   * PATCH /api/chat/1/leave
   */
  @PatchMapping("/{chatId}/leave")
  public ResponseEntity<List<ChatRoomInfoResponse>> leaveChat(@PathVariable Long chatId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUserId();
    chatService.leaveChat(chatId, userId);
    return ResponseEntity.ok(chatService.getChatRoomByUserId(userId));
  }

  /**
   * 나의 참가 채팅방 조회
   * GET /api/chat/1/my
   */
  @GetMapping("/my")
  public ResponseEntity<List<ChatRoomInfoResponse>> getMyChat(@AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUserId();
    return ResponseEntity.ok(chatService.getChatRoomByUserId(userId));
  }


}
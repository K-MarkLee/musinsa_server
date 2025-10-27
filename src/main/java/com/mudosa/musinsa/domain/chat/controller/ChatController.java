package com.mudosa.musinsa.domain.chat.controller;

import com.mudosa.musinsa.domain.chat.dto.ChatRoomInfoResponse;
import com.mudosa.musinsa.domain.chat.dto.MessageResponse;
import com.mudosa.musinsa.domain.chat.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 채팅 REST API 컨트롤러
 * WebSocket과 별도로 HTTP 요청으로 처리할 작업들
 * - 채팅방 목록 조회
 * - 메시지 히스토리 조회 (페이징)
 * - 채팅방 생성/삭제
 * - 파일 업로드 등
 */
@Tag(name = "Chat API", description = "채팅 API")
@RestController
@RequestMapping("/api/chat")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // CORS 설정 (프로덕션에서는 구체적으로 지정)
public class ChatController {

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
      @RequestParam("userId") Long userId,
      @RequestPart(value = "message", required = false) String message,
      @RequestPart(value = "files", required = false) List<MultipartFile> files
  ) {
    log.info("채팅 메시지 전송 요청: chatId={}, message={}, files={}",
        chatId, message, (files != null ? files.size() + "개" : "없음"));

    // 메시지와 파일이 모두 없는 경우: 요청 거부
    boolean noMessage = (message == null || message.trim().isEmpty());
    boolean noFiles = (files == null || files.isEmpty());
    if (noMessage && noFiles) {
      return ResponseEntity
          .badRequest()
          .build();
    }

    //service에서 메시지 전송 로직 구현
    MessageResponse savedMessage = chatService.saveMessage(chatId, userId, message, files);
    return ResponseEntity.ok(savedMessage);
  }

  /**
   * 채팅방 이전 메시지 조회 (페이징)
   * GET /api/chat/rooms/1/messages?userId=1&page=0&size=20
   */
  @GetMapping("/{chatId}/messages")
  public ResponseEntity<Page<MessageResponse>> getChatMessages(
      @PathVariable Long chatId,

      @RequestParam Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    log.info("메시지 목록 조회: chatId={}, userId={}, page={}, size={}",
        chatId, userId, page, size);

    Page<MessageResponse> messages = chatService.getChatMessages(chatId, userId, page, size);

    return ResponseEntity.ok(messages);
  }

  /**
   * 채팅방 이전 메시지 조회 (페이징)
   * GET /api/chat/rooms/1/messages?userId=1&page=0&size=20
   */
  @GetMapping("/{chatId}/info")
  public ResponseEntity<ChatRoomInfoResponse> getChatInfo(@PathVariable Long chatId) {
    return ResponseEntity.ok(chatService.getChatRoomInfoByChatId(chatId));
  }

//  /**
//   * 사용자의 채팅방 목록 조회
//   * GET /api/chat/rooms?userId=1
//   */
//  @GetMapping("/rooms")
//  public ResponseEntity<List<ChatRoomResponse>> getUserChatRooms(
//      @RequestParam Long userId
//  ) {
//    log.info("채팅방 목록 조회 요청: userId={}", userId);
//    List<ChatRoomResponse> rooms = chatService.getUserChatRooms(userId);
//    return ResponseEntity.ok(rooms);
//  }
//
//  /**
//   * 채팅방 생성
//   * POST /api/chat/rooms
//   * Body: { "brandId": 1, "type": "DM", "userIds": [1, 2] }
//   */
//  @PostMapping("/rooms")
//  public ResponseEntity<ChatRoomResponse> createChatRoom(
//      @RequestBody Map<String, Object> request
//  ) {
//    Long brandId = Long.parseLong(request.get("brandId").toString());
//    String type = request.get("type").toString();
//    List<Long> userIds = ((List<?>) request.get("userIds"))
//        .stream()
//        .map(id -> Long.parseLong(id.toString()))
//        .toList();
//
//    log.info("채팅방 생성 요청: brandId={}, type={}, userIds={}", brandId, type, userIds);
//
//    ChatRoomResponse room = chatService.createChatRoom(brandId, type, userIds);
//    return ResponseEntity.ok(room);
//  }
//


}
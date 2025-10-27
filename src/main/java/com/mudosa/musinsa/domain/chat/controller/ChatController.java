package com.mudosa.musinsa.domain.chat.controller;

import com.mudosa.musinsa.domain.chat.dto.ChatRoomResponse;
import com.mudosa.musinsa.domain.chat.dto.MessageResponse;
import com.mudosa.musinsa.domain.chat.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
   * 사용자의 채팅방 목록 조회
   * GET /api/chat/rooms?userId=1
   */
  @GetMapping("/rooms")
  public ResponseEntity<List<ChatRoomResponse>> getUserChatRooms(
      @RequestParam Long userId
  ) {
    log.info("채팅방 목록 조회 요청: userId={}", userId);
    List<ChatRoomResponse> rooms = chatService.getUserChatRooms(userId);
    return ResponseEntity.ok(rooms);
  }

  /**
   * 채팅방 생성
   * POST /api/chat/rooms
   * Body: { "brandId": 1, "type": "DM", "userIds": [1, 2] }
   */
  @PostMapping("/rooms")
  public ResponseEntity<ChatRoomResponse> createChatRoom(
      @RequestBody Map<String, Object> request
  ) {
    Long brandId = Long.parseLong(request.get("brandId").toString());
    String type = request.get("type").toString();
    List<Long> userIds = ((List<?>) request.get("userIds"))
        .stream()
        .map(id -> Long.parseLong(id.toString()))
        .toList();

    log.info("채팅방 생성 요청: brandId={}, type={}, userIds={}", brandId, type, userIds);

    ChatRoomResponse room = chatService.createChatRoom(brandId, type, userIds);
    return ResponseEntity.ok(room);
  }

  /**
   * 채팅방 메시지 목록 조회 (페이징)
   * GET /api/chat/rooms/1/messages?userId=1&page=0&size=20
   */
  @GetMapping("/rooms/{chatId}/messages")
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
   * 메시지 삭제
   * DELETE /api/chat/messages/1?userId=1
   */
  @DeleteMapping("/messages/{messageId}")
  public ResponseEntity<Void> deleteMessage(
      @PathVariable Long messageId,
      @RequestParam Long userId
  ) {
    log.info("메시지 삭제 요청: messageId={}, userId={}", messageId, userId);
    chatService.deleteMessage(messageId, userId);
    return ResponseEntity.noContent().build();
  }

  /**
   * 파일 업로드 엔드포인트 (예시)
   * POST /api/chat/upload
   * Content-Type: multipart/form-data
   */
  @PostMapping("/upload")
  public ResponseEntity<Map<String, String>> uploadFile(
      @RequestParam("file") org.springframework.web.multipart.MultipartFile file
  ) {
    try {
      // 파일 저장 로직 (S3, 로컬 스토리지 등)
      String fileName = file.getOriginalFilename();
      String fileUrl = "/uploads/" + fileName;

      log.info("파일 업로드: fileName={}", fileName);

      return ResponseEntity.ok(Map.of(
          "url", fileUrl,
          "fileName", fileName,
          "mimeType", file.getContentType(),
          "size", String.valueOf(file.getSize())
      ));

    } catch (Exception e) {
      log.error("파일 업로드 실패", e);
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * 채팅방 정보 조회
   * GET /api/chat/rooms/1
   */
  @GetMapping("/rooms/{chatId}")
  public ResponseEntity<ChatRoomResponse> getChatRoom(
      @PathVariable Long chatId,
      @RequestParam Long userId
  ) {
    log.info("채팅방 정보 조회: chatId={}, userId={}", chatId, userId);

    // 권한 검증 및 조회 로직
    List<ChatRoomResponse> rooms = chatService.getUserChatRooms(userId);
    ChatRoomResponse room = rooms.stream()
        .filter(r -> r.getChatId().equals(chatId))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

    return ResponseEntity.ok(room);
  }
}
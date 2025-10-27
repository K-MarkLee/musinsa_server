package com.mudosa.musinsa.domain.chat.controller;

import com.mudosa.musinsa.domain.chat.dto.MessageResponse;
import com.mudosa.musinsa.domain.chat.dto.MessageSendRequest;
import com.mudosa.musinsa.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket 메시지 처리 컨트롤러
 * 메시지 흐름:
 * 1. 클라이언트가 /app/chat/{chatId} 로 메시지 전송
 * 2. 컨트롤러에서 처리 후 DB 저장
 * 3. /brand/chat/{chatId} 를 구독한 모든 클라이언트에게 브로드캐스트
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class WebSocketChatController {

  private final ChatService chatService;
  private final SimpMessagingTemplate messagingTemplate;

  /**
   * 채팅 메시지 전송
   *
   * @MessageMapping: 클라이언트가 /brand/chat/{chatId} 로 메시지를 보내면 이 메서드가 처리
   * @SendTo: 처리 결과를 app/brand/chat/{chatId} 를 구독한 모든 클라이언트에게 전송
   */
  @MessageMapping("/brand/chat/{chatId}")
  @SendTo("/brand/chat/{chatId}")
  public MessageResponse sendMessage(
      @DestinationVariable Long chatId,
      @Payload MessageSendRequest request,
      SimpMessageHeaderAccessor headerAccessor
  ) {
    try {
      log.info("메시지 수신: chatId={}, content={}", chatId, request.getContent());

      // 메시지 DB 저장
      MessageResponse response = chatService.saveMessage(request);

      log.info("메시지 저장 완료: messageId={}", response.getMessageId());

      return response;

    } catch (Exception e) {
      log.error("메시지 전송 중 오류 발생", e);
      throw new RuntimeException("메시지 전송 실패: " + e.getMessage());
    }
  }

  /**
   * 특정 사용자에게 개인 메시지 전송 (DM)
   * /user/{userId}/qna/messages 를 구독한 특정 사용자에게만 전송
   */
  public void sendPrivateMessage(Long userId, MessageResponse message) {
    messagingTemplate.convertAndSendToUser(
        userId.toString(),
        "/qna/messages",
        message
    );
    log.info("개인 메시지 전송: userId={}, messageId={}", userId, message.getMessageId());
  }

  /**
   * 채팅방 전체에 시스템 알림 전송
   * 예: "사용자 A가 입장했습니다", "사용자 B가 퇴장했습니다"
   */
  public void sendSystemNotification(Long chatId, String notification) {
    messagingTemplate.convertAndSend(
        "/brand/chat/" + chatId + "/system",
        notification
    );
    log.info("시스템 알림 전송: chatId={}, notification={}", chatId, notification);
  }
}
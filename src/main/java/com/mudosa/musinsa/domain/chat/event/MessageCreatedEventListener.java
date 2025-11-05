package com.mudosa.musinsa.domain.chat.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageCreatedEventListener {

  private final SimpMessagingTemplate messagingTemplate;

  // 트랜잭션 커밋 후 실행 (DB 롤백 시 실행되지 않음)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(MessageCreatedEvent event) {
    //확인
    log.info("WebSocket broadcast -> chatId={}, message={}", event.messageResponse().getChatId(), event.messageResponse());
    //websocket으로 전송
    messagingTemplate.convertAndSend("/topic/chat/" + event.messageResponse().getChatId(), event.messageResponse());
  }
}
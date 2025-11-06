package com.mudosa.musinsa.domain.chat.event;

import com.mudosa.musinsa.domain.chat.dto.MessageResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 메시지 생성 완료 이벤트
 * - 트랜잭션 커밋 후에만 브로드캐스트하기 위한 도메인 이벤트
 * - 완성된 MessageResponse를 페이로드로 전달
 *
 * @param messageResponse 완성된 응답 DTO (첨부 포함)
 * @param timestamp       이벤트 발행 시간 (디버깅/모니터링용)
 */
@Slf4j
public record MessageCreatedEvent(MessageResponse messageResponse, long timestamp) {

  /**
   * 편의 생성자: 현재 시간으로 자동 설정
   */
  public MessageCreatedEvent(MessageResponse messageResponse) {
    this(messageResponse, System.currentTimeMillis());
    log.trace("[MessageCreatedEvent] 생성됨. chatId={}, messageId={}, timestamp={}",
        messageResponse.getChatId(), messageResponse.getMessageId(), this.timestamp);

  }

  /**
   * 빠른 접근을 위한 헬퍼 메서드
   */
  public Long getMessageId() {
    return messageResponse.getMessageId();
  }

  public Long getChatId() {
    return messageResponse.getChatId();
  }
}
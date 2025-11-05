package com.mudosa.musinsa.domain.chat.event;

import com.mudosa.musinsa.domain.chat.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

//기본으로 사용
@Primary
@Component("springPublisher")
@RequiredArgsConstructor
@Slf4j
public class SpringMessageEventPublisher implements MessageEventPublisher {
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public void publishMessageCreated(MessageResponse dto) {
    log.debug("[EventPublisher] 메시지 이벤트 발행 시작. chatId={}, messageId={}",
        dto.getChatId(), dto.getMessageId());

    eventPublisher.publishEvent(new MessageCreatedEvent(dto));

    log.info("[EventPublisher] 메시지 이벤트 발행 완료. chatId={}, messageId={}",
        dto.getChatId(), dto.getMessageId());

  }
}

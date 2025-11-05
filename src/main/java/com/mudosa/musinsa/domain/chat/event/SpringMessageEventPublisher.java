package com.mudosa.musinsa.domain.chat.event;

import com.mudosa.musinsa.domain.chat.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

//기본으로 사용
@Primary
@Component("springPublisher")
@RequiredArgsConstructor
public class SpringMessageEventPublisher implements MessageEventPublisher {
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public void publishMessageCreated(MessageResponse dto) {
    eventPublisher.publishEvent(new MessageCreatedEvent(dto));
  }
}

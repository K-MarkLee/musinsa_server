package com.mudosa.musinsa.domain.chat.event;

import com.mudosa.musinsa.domain.chat.dto.MessageResponse;

public interface MessageEventPublisher {
  void publishMessageCreated(MessageResponse dto);
}

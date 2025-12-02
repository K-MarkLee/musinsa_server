package com.mudosa.musinsa.domain.chat.event;

import java.util.List;

public interface ChatEventPublisher {
  void publishUploadEvent(Long messageId, List<TempUploadedFile> files, String clientMessageId);

  void publishBroadcastEvent(Long chatId, Object payload);
}

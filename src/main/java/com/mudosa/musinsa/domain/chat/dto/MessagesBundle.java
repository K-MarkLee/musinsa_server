package com.mudosa.musinsa.domain.chat.dto;

import com.mudosa.musinsa.domain.chat.entity.Message;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record MessagesBundle(
    List<Message> messages,
    boolean hasNext,
    Set<Long> managerUserIds,
    Map<Long, List<AttachmentResponse>> attachmentMap,
    Map<Long, Message> parentMap       // ← 새 필드 추가!
) {

  public static MessagesBundle empty(int size) {
    return new MessagesBundle(
        List.of(),
        false,
        Set.of(),
        Map.of(),
        Map.of()    // ← 빈 parentMap 추가
    );
  }
}
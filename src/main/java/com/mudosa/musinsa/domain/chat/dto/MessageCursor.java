package com.mudosa.musinsa.domain.chat.dto;

import java.time.LocalDateTime;

public record MessageCursor(LocalDateTime createdAt, Long messageId) {
  
}
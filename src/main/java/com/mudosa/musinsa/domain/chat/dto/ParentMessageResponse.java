package com.mudosa.musinsa.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParentMessageResponse {

  private Long messageId;
  private Long userId;
  private String userName;
  private String content;
  private List<AttachmentResponse> attachments; // 첨부파일 정보
  private LocalDateTime createdAt;
  private boolean isDeleted;
}

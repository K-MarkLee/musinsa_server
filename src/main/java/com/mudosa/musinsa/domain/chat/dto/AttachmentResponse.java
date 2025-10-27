package com.mudosa.musinsa.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 첨부파일 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
  private Long attachmentId;
  private String attachmentUrl;
  private String mimeType;
  private Long sizeBytes;
}
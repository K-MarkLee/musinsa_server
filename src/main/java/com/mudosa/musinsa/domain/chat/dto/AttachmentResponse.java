package com.mudosa.musinsa.domain.chat.dto;

import com.mudosa.musinsa.domain.chat.entity.MessageAttachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentResponse {

  private Long attachmentId;
  private String attachmentUrl;
  private String mimeType;
  private long sizeBytes;

  public static AttachmentResponse from(MessageAttachment entity) {
    return AttachmentResponse.builder()
        .attachmentId(entity.getAttachmentId())
        .attachmentUrl(entity.getAttachmentUrl())
        .mimeType(entity.getMimeType())
        .sizeBytes(entity.getSizeBytes())
        .build();
  }
}

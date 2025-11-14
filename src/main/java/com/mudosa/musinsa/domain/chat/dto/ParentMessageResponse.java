package com.mudosa.musinsa.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Parent Message Response Dto")
public class ParentMessageResponse {
  @Schema(description = "메시지 id", example = "1")
  private Long messageId;
  @Schema(description = "유저 id", example = "1")
  private Long userId;
  @Schema(description = "유저 이름", example = "홍길동")
  private String userName;
  @Schema(description = "메시지 내용", example = "안녕하세요?")
  private String content;
  @Schema(description = "메시지 내 첨부파일 리스트")
  private List<AttachmentResponse> attachments; // 첨부파일 정보
  @Schema(description = "보낸 시간", example = "2025-11-04T13:56:25.623Z")
  private LocalDateTime createdAt;
}

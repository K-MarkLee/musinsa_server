package com.mudosa.musinsa.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mudosa.musinsa.domain.chat.entity.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "FileUploadSuccess Response Dto")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WSFileUploadSuccessDTO {
  private String type = "ATTACHMENT";
  private Long messageId;
  private Long chatId;
  private List<AttachmentResponse> attachments;
  @Schema(description = "동일 메시지 여부 구분 id", example = "UUID-1111-1234")
  private String clientMessageId;

  public static WSFileUploadSuccessDTO of(Message message, String clientMessageId, List<AttachmentResponse> responses) {

    return WSFileUploadSuccessDTO.builder()
        .type("ATTACHMENT")
        .messageId(message.getMessageId())
        .chatId(message.getChatId())
        .attachments(responses)
        .clientMessageId(clientMessageId)
        .build();
  }
}

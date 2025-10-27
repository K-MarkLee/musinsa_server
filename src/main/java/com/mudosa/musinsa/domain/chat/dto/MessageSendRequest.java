package com.mudosa.musinsa.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 클라이언트가 메시지를 전송할 때 사용하는 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendRequest {
  private Long chatId;           // 채팅방 ID
  private Long chatPartId;       // 발신자 ChatPart ID
  private String content;        // 메시지 내용
  private Long parentId;         // 답장할 메시지 ID (스레드)
  private List<MultipartFile> attachments; // 첨부파일 정보
}

package com.mudosa.musinsa.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "message_attachment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MessageAttachment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "attachment_id")
  private Long attachmentId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "message_id", nullable = false)
  private Message message;

  @Column(name = "attachment_url", nullable = false, length = 1024)
  private String attachmentUrl;

  @Column(name = "mime_type", length = 100)
  private String mimeType;

  @Column(name = "size_bytes")
  private Long sizeBytes;
}

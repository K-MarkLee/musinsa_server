package com.mudosa.musinsa.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE message SET deleted_at = CURRENT_TIMESTAMP WHERE message_id = ?")
@Where(clause = "deleted_at IS NULL")
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "message_id")
  private Long messageId;

  // 발신자: NULL 허용
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_part_id")
  private ChatPart chatPart;

  // 답장(스레드) 자기참조
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Message parent;

  @OneToMany(mappedBy = "parent", orphanRemoval = false)
  @Builder.Default
  private List<Message> children = new ArrayList<>();

  @Column(name = "content", columnDefinition = "TEXT")
  private String content;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false,
      columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;

  @Column(name = "deleted_at")
  @Setter
  private LocalDateTime deletedAt;

  @Builder.Default
  @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MessageAttachment> attachments = new ArrayList<>();

  public boolean isSameRoom(Long chatId) {
    ChatPart cp = this.getChatPart();
    return cp != null
        && cp.getChatRoom() != null
        && cp.getChatRoom().getChatId().equals(chatId);
  }
}

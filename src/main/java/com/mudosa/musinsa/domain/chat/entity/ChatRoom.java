package com.mudosa.musinsa.domain.chat.entity;

import com.mudosa.musinsa.domain.chat.enums.ChatRoomType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "chat_id")
  private Long chatId;

  @Column(name = "brand_id", nullable = false)
  private Long brandId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 10)
  private ChatRoomType type; // GROUP, DM

  @Column(name = "last_message_at")
  private LocalDateTime lastMessageAt;

  // DB 기본값 사용 (CURRENT_TIMESTAMP / ON UPDATE)
  @Column(name = "created_at", nullable = false, insertable = false, updatable = false,
      columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false,
      columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;

  // ==== 연관관계 ====
  @OneToMany(mappedBy = "chatRoom", orphanRemoval = false)
  @Builder.Default
  private List<ChatPart> parts = new ArrayList<>();

  @OneToMany(mappedBy = "chatRoom", orphanRemoval = false)
  @Builder.Default
  private List<Message> messages = new ArrayList<>();
}

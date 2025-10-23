package com.mudosa.musinsa.chat.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅 참여자 엔티티
 * ChatRoom 애그리거트 내부
 */
@Entity
@Table(name = "chat_part")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatPart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_part_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatRoom chatRoom;
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // User 애그리거트 참조 (ID만)
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    /**
     * 채팅 참여자 생성
     */
    public static ChatPart create(Long userId) {
        ChatPart chatPart = new ChatPart();
        chatPart.userId = userId;
        chatPart.isActive = true;
        return chatPart;
    }
    
    /**
     * ChatRoom 할당 (Package Private)
     */
    void assignChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
    
    /**
     * 채팅방 나가기
     */
    public void leave() {
        this.isActive = false;
    }
}

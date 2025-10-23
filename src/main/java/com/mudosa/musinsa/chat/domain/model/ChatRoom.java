package com.mudosa.musinsa.chat.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 채팅방 애그리거트 루트
 */
@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long id;
    
    @Column(name = "brand_id", nullable = false)
    private Long brandId; // Brand 애그리거트 참조 (ID만)
    
    @Column(name = "room_name", length = 100)
    private String roomName;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // 채팅 참여자 (같은 애그리거트)
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatPart> chatParts = new ArrayList<>();
    
    // 메시지 (같은 애그리거트)
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();
    
    /**
     * 채팅방 생성
     */
    public static ChatRoom create(Long brandId, String roomName) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.brandId = brandId;
        chatRoom.roomName = roomName;
        chatRoom.isActive = true;
        return chatRoom;
    }
    
    /**
     * 참여자 추가
     */
    public void addParticipant(ChatPart chatPart) {
        this.chatParts.add(chatPart);
        chatPart.assignChatRoom(this);
    }
    
    /**
     * 메시지 추가
     */
    public void addMessage(Message message) {
        this.messages.add(message);
        message.assignChatRoom(this);
    }
}

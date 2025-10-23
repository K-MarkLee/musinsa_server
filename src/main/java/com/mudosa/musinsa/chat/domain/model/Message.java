package com.mudosa.musinsa.chat.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 메시지 엔티티
 * ChatRoom 애그리거트 내부
 */
@Entity
@Table(name = "message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatRoom chatRoom;
    
    @Column(name = "chat_part_id")
    private Long chatPartId; // ChatPart 참조 (같은 애그리거트이지만 ID로)
    
    @Column(name = "parent_id")
    private Long parentId; // 답장 메시지의 원본 메시지 ID
    
    @Column(name = "message_content", nullable = false, columnDefinition = "TEXT")
    private String messageContent;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    // 첨부파일 (같은 애그리거트)
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageAttachment> attachments = new ArrayList<>();
    
    /**
     * 메시지 생성
     */
    public static Message create(Long chatPartId, String content) {
        Message message = new Message();
        message.chatPartId = chatPartId;
        message.messageContent = content;
        message.isRead = false;
        return message;
    }
    
    /**
     * 답장 메시지 생성
     */
    public static Message createReply(Long chatPartId, String content, Long parentId) {
        Message message = create(chatPartId, content);
        message.parentId = parentId;
        return message;
    }
    
    /**
     * ChatRoom 할당 (Package Private)
     */
    void assignChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
    
    /**
     * 첨부파일 추가
     */
    public void addAttachment(MessageAttachment attachment) {
        this.attachments.add(attachment);
        attachment.assignMessage(this);
    }
    
    /**
     * 읽음 처리
     */
    public void markAsRead() {
        this.isRead = true;
    }
}

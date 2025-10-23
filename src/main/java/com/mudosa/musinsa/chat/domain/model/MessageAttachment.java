package com.mudosa.musinsa.chat.domain.model;

import com.mudosa.musinsa.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메시지 첨부파일 엔티티
 * ChatRoom 애그리거트 내부 (Message의 하위)
 */
@Entity
@Table(name = "message_attachment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageAttachment extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;
    
    @Column(name = "file_url", nullable = false, length = 2048)
    private String fileUrl;
    
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type")
    private FileType fileType;
    
    /**
     * 첨부파일 생성
     */
    public static MessageAttachment create(
        String fileUrl, 
        String fileName, 
        Long fileSize, 
        FileType fileType
    ) {
        MessageAttachment attachment = new MessageAttachment();
        attachment.fileUrl = fileUrl;
        attachment.fileName = fileName;
        attachment.fileSize = fileSize;
        attachment.fileType = fileType;
        return attachment;
    }
    
    /**
     * Message 할당 (Package Private)
     */
    void assignMessage(Message message) {
        this.message = message;
    }
}

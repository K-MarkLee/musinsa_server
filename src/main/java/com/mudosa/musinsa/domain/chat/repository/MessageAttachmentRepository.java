package com.mudosa.musinsa.domain.chat.repository;

import com.mudosa.musinsa.domain.chat.entity.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Long> {

  /**
   * 메시지의 모든 첨부파일 조회
   */
  List<MessageAttachment> findByMessage_MessageId(Long messageId);
}

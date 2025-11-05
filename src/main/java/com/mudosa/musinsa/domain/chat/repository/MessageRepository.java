package com.mudosa.musinsa.domain.chat.repository;

import com.mudosa.musinsa.domain.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

  /**
   * 채팅방의 메시지 페이징 조회 (최신순)
   */

  @Query(
      value = """
          select m
          from Message m
          join fetch m.chatRoom cr
          left join fetch m.chatPart cp
          left join fetch cp.user u
          left join fetch m.parent pm
          left join fetch pm.chatPart pmcp
          left join fetch pmcp.user pmu
          where cr.chatId = :chatId
          order by m.createdAt desc
          """,
      countQuery = """
          select count(m)
          from Message m
          where m.chatRoom.chatId = :chatId
          """
  )
  Page<Message> findPageWithRelationsByChatId(
      @Param("chatId") Long chatId,
      Pageable pageable
  );
}

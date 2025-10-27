package com.mudosa.musinsa.domain.chat.repository;

import com.mudosa.musinsa.domain.chat.entity.ChatRoom;
import com.mudosa.musinsa.domain.chat.enums.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  /**
   * 브랜드의 모든 채팅방 조회 (last_message_at DESC)
   * - ChatRoom.brand(연관필드) 의 PK 필드명 brandId 로 경로 지정
   */
  List<ChatRoom> findByBrand_BrandIdOrderByLastMessageAtDesc(Long brandId);

  /**
   * 특정 타입(GROUP/DM)만 정렬 조회
   */
  List<ChatRoom> findByBrand_BrandIdAndTypeOrderByLastMessageAtDesc(Long brandId, ChatRoomType type);

  /**
   * 브랜드의 첫 번째 GROUP 채팅방 (예: 브랜드 상세 진입 시 사용)
   */
  Optional<ChatRoom> findFirstByBrand_BrandIdAndTypeOrderByChatIdAsc(Long brandId, ChatRoomType type);

  /**
   * 채팅방 + 참여자(parts) 페치 조인 (N+1 방지)
   */
  @Query("""
      SELECT DISTINCT cr
      FROM ChatRoom cr
      LEFT JOIN FETCH cr.parts
      WHERE cr.chatId = :chatId
      """)
  Optional<ChatRoom> findByIdWithParts(@Param("chatId") Long chatId);

  /**
   * 필요 시 createdAt으로 대체 정렬( lastMessageAt 이 null 인 경우 )
   * COALESCE로 정렬 안정화
   */
  @Query("""
      SELECT cr
      FROM ChatRoom cr
      WHERE cr.brand.brandId = :brandId
      ORDER BY COALESCE(cr.lastMessageAt, cr.createdAt) DESC
      """)
  List<ChatRoom> findAllByBrandOrderStable(@Param("brandId") Long brandId);


}

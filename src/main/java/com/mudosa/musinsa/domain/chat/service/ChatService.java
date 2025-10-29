package com.mudosa.musinsa.domain.chat.service;

import com.mudosa.musinsa.domain.chat.dto.ChatPartResponse;
import com.mudosa.musinsa.domain.chat.dto.ChatRoomInfoResponse;
import com.mudosa.musinsa.domain.chat.dto.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 채팅 도메인 비즈니스 로직 인터페이스.
 * 구현체: {@code ChatServiceImpl}
 */
public interface ChatService {

  /**
   * 메시지 저장(답장/첨부 포함).
   *
   * @param chatId  채팅방 id
   * @param userId  발신자 유저 id
   * @param content 메시지 내용
   * @param files   첨부파일들(이미지)
   */
  MessageResponse saveMessage(Long chatId, Long userId, Long parentId, String content, List<MultipartFile> files);

  /**
   * 특정 채팅방의 메시지 페이지 조회(최신순).
   *
   * @param chatId 채팅방 ID
   * @param userId 조회 사용자 ID(권한 검증용)
   * @param page   페이지 번호(0-base)
   * @param size   페이지 크기
   * @return 메시지 응답 페이지
   * @throws RuntimeException 비참여자 접근 등 권한 오류
   */
  Page<MessageResponse> getChatMessages(Long chatId, Long userId, int page, int size);

  ChatRoomInfoResponse getChatRoomInfoByChatId(Long chatId, Long userId);

  ChatPartResponse addParticipant(Long chatId, Long userId);
}

package com.mudosa.musinsa.domain.chat.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.mudosa.musinsa.domain.chat.dto.ChatPartResponse;
import com.mudosa.musinsa.domain.chat.dto.ChatRoomInfoResponse;
import com.mudosa.musinsa.domain.chat.dto.MessageCursor;
import com.mudosa.musinsa.domain.chat.dto.MessageResponse;
import org.springframework.data.domain.Slice;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
   * @param now     현재 시간
   */
  MessageResponse saveMessage(Long chatId, Long userId, Long parentId, String content, List<MultipartFile> files, LocalDateTime now) throws FirebaseMessagingException;

  /**
   * 특정 채팅방의 메시지 페이지 조회(최신순).
   *
   * @param chatId 채팅방 ID
   * @param cursor 이전 마지막 메시지
   * @param size   페이지 크기
   * @return 메시지 응답 페이지
   * @throws RuntimeException 비참여자 접근 등 권한 오류
   */
  Slice<MessageResponse> getChatMessages(Long chatId, MessageCursor cursor, int size);

  /**
   * 특정 채팅방의 정보 조회
   *
   * @param chatId 채팅방 ID
   * @param userId 조회 사용자 ID(참여 여부 검증용)
   */
  ChatRoomInfoResponse getChatRoomInfoByChatId(Long chatId, Long userId);

  /**
   * 채팅방 참여
   *
   * @param chatId 채팅방 ID
   * @param userId 사용자 ID
   */
  ChatPartResponse addParticipant(Long chatId, Long userId);

  /**
   * 채팅방 떠나기
   *
   * @param chatId 채팅방 ID
   * @param userId 사용자 ID
   */
  void leaveChat(Long chatId, Long userId);

  /**
   * 유저 참여 채팅방 목록 조회
   *
   * @param userId 사용자 ID
   */
  List<ChatRoomInfoResponse> getChatRoomByUserId(Long userId);
}

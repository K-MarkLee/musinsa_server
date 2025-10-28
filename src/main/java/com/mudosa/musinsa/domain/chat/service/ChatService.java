package com.mudosa.musinsa.domain.chat.service;

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

  ChatRoomInfoResponse getChatRoomInfoByChatId(Long chatId);
//  /**
//   * 채팅방 생성 및 참여자 등록.
//   *
//   * @param brandId 브랜드 ID
//   * @param type    채팅방 타입 문자열 (e.g. "GROUP", "DM")
//   * @param userIds 초기 참여자 사용자 ID 목록
//   * @return 생성된 채팅방 정보
//   * @throws IllegalArgumentException 타입 미스매치 등 유효성 오류
//   */
//  ChatRoomResponse createChatRoom(Long brandId, String type, List<Long> userIds);
//
//


//
//  /**
//   * 사용자가 참여 중인 채팅방 목록 조회.
//   *
//   * @param userId 사용자 ID
//   * @return 채팅방 응답 목록
//   */
//  List<ChatRoomResponse> getUserChatRooms(Long userId);
//
//  /**
//   * 메시지 소프트 삭제.
//   *
//   * @param messageId 메시지 ID
//   * @param userId    삭제 수행 사용자 ID(본인 메시지 검증)
//   * @throws RuntimeException 권한 미일치/미존재 등
//   */
//  void deleteMessage(Long messageId, Long userId);
//
}

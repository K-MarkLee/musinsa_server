package com.mudosa.musinsa.domain.chat.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStore {

  /**
   * 채팅 메시지 첨부파일처럼 경로 패턴이 정해진 파일 저장
   *
   * @param chatId    채팅방 ID
   * @param messageId 메시지 ID
   * @param file      업로드 파일
   * @return 공개용(or 상대) URL
   */
  String storeMessageFile(Long chatId, Long messageId, MultipartFile file) throws IOException;

  /**
   * 저장된 파일을 삭제
   */
  boolean deleteMessageFile(String relativePath);

  /**
   * 메세지 전체 폴더 삭제
   */
  boolean deleteMessageFolder(Long chatId, Long messageId);
}

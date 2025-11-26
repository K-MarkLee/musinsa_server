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
   * 채팅 메시지 첨부파일처럼 경로 패턴이 정해진 파일 저장
   *
   * @param brandId 브랜드 ID
   * @param file    업로드 파일
   * @return 공개용(or 상대) URL
   */
  String storeBrandLogo(Long brandId, MultipartFile file) throws IOException;
}

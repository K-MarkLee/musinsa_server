package com.mudosa.musinsa.domain.chat.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
public class LocalFileStore implements FileStore {

  public String storeMessageFile(Long chatId, Long messageId, MultipartFile file) throws IOException {
    String baseDir = new ClassPathResource("static/").getFile().getAbsolutePath();
    String uploadDir = Paths.get(baseDir, "chat", String.valueOf(chatId), "message", String.valueOf(messageId)).toString();

    Files.createDirectories(Paths.get(uploadDir));

    String original = Objects.requireNonNullElse(file.getOriginalFilename(), "unknown");
    String safeName = UUID.randomUUID() + "_" + StringUtils.cleanPath(original);

    Path targetPath = Paths.get(uploadDir, safeName).toAbsolutePath().normalize();
    file.transferTo(targetPath.toFile());

    return "/chat/" + chatId + "/message/" + messageId + "/" + safeName;
  }

  /**
   * 저장된 메시지 파일 삭제
   *
   * @param relativePath "/chat/{chatId}/message/{messageId}/filename.ext" 형태
   */
  @Override
  public boolean deleteMessageFile(String relativePath) {
    try {
      // ClassPathResource로 실제 절대 경로 변환
      String baseDir = new ClassPathResource("static/").getFile().getAbsolutePath();
      Path filePath = Paths.get(baseDir + relativePath).normalize();

      return Files.deleteIfExists(filePath);
    } catch (IOException e) {
      log.error("Failed to delete file: {}", relativePath, e);
      return false;
    }
  }

  /**
   * 메시지 전체 폴더 삭제 (예: 메시지 삭제 시 첨부파일 폴더 제거)
   */
  @Override
  public boolean deleteMessageFolder(Long chatId, Long messageId) {
    try {
      String baseDir = new ClassPathResource("static/").getFile().getAbsolutePath();
      Path folderPath = Paths.get(baseDir + "/chat/" + chatId + "/message/" + messageId).normalize();

      if (Files.exists(folderPath)) {
        Files.walk(folderPath)
            .sorted((a, b) -> b.compareTo(a)) // 하위 파일 먼저 삭제
            .forEach(path -> {
              try {
                Files.deleteIfExists(path);
              } catch (IOException e) {
                log.error("Failed to delete folder: {}", folderPath, e);
              }
            });
        return true;
      }
      return false;
    } catch (IOException e) {
      log.error("Failed to delete folder: chat {} - message {}", chatId, messageId, e);
      return false;
    }
  }
}

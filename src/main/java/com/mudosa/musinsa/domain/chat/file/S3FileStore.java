package com.mudosa.musinsa.domain.chat.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3FileStore implements FileStore {

  private final AmazonS3 amazonS3;

  @Value("${aws.s3.bucket}")
  private String bucketName;

  @Override
  public String storeMessageFile(Long chatId, Long messageId, MultipartFile file) throws IOException {
    String original = Objects.requireNonNullElse(file.getOriginalFilename(), "unknown");
    String safeName = UUID.randomUUID() + "_" + StringUtils.cleanPath(original);

    // chat/{chatId}/message/{messageId}/파일명
    String key = String.format("chat/%d/message/%d/%s", chatId, messageId, safeName);

    return uploadToS3(key, file, original);
  }

  @Override
  public String storeBrandLogo(Long brandId, MultipartFile file) throws IOException {
    String original = Objects.requireNonNullElse(file.getOriginalFilename(), "unknown");
    String safeName = UUID.randomUUID() + "_" + StringUtils.cleanPath(original);

    // brand/{brandId}/logo/파일명
    String key = String.format("brand/%d/logo/%s", brandId, safeName);

    return uploadToS3(key, file, original);
  }

  /**
   * 실제 S3 업로드 공통 처리
   */
  private String uploadToS3(String key, MultipartFile file, String original) throws IOException {
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(file.getSize());
    metadata.setContentType(file.getContentType());

    try (InputStream inputStream = file.getInputStream()) {
      amazonS3.putObject(new PutObjectRequest(bucketName, key, inputStream, metadata));
    } catch (IOException e) {
      log.error("Failed to upload file to S3. key={}, filename={}", key, original, e);
      throw e;
    }

    // 전체 URL 반환 (현재 채팅과 동일한 방식)
    return amazonS3.getUrl(bucketName, key).toString();
  }
}

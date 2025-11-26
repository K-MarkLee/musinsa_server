package com.mudosa.musinsa.domain.chat.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3FileStore implements FileStore {

  private final S3Client s3Client;

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
   * 실제 S3 업로드 공통 처리 (v2 버전)
   */
  private String uploadToS3(String key, MultipartFile file, String original) throws IOException {
    try (InputStream inputStream = file.getInputStream()) {

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .contentType(file.getContentType())
          .contentLength(file.getSize())
          .build();

      // v2: RequestBody 사용
      s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

    } catch (IOException e) {
      log.error("Failed to upload file to S3. key={}, filename={}", key, original, e);
      throw e;
    }

    // v2에서 URL 생성: utilities().getUrl(...)
    return s3Client.utilities()
        .getUrl(GetUrlRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build())
        .toString();
  }
}

package com.mudosa.musinsa.domain.chat.file;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@Component
public class S3SyncFileStore extends AbstractS3Store implements FileStore {

  private final S3Client s3Client;

  public S3SyncFileStore(Tracer tracer, S3Client s3Client) {
    super(tracer);
    this.s3Client = s3Client;
  }

  @Override
  public CompletableFuture<String> storeMessageFile(Long chatId, Long messageId, MultipartFile file) {
    String key = generateMessageKey(chatId, messageId, file);
    String url = uploadInternal(key, file); // 기존 동기 업로드 로직 실행

    // ★ 핵심: 결과를 Future로 감싸서 즉시 반환
    return CompletableFuture.completedFuture(url);
  }

  @Override
  public CompletableFuture<String> storeBrandLogo(Long brandId, MultipartFile file) {
    String key = generateBrandKey(brandId, file);
    String url = uploadInternal(key, file);
    return CompletableFuture.completedFuture(url);
  }

  // 내부 실제 업로드 로직
  private String uploadInternal(String key, MultipartFile file) {
    Span span = tracer.nextSpan().name("s3.upload")
        .tag("type", "sync")
        .tag("file.size", String.valueOf(file.getSize()))
        .start();

    try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
      try (InputStream inputStream = file.getInputStream()) {
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(file.getContentType())
            .contentLength(file.getSize())
            .build();

        s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));

        return s3Client.utilities().getUrl(GetUrlRequest.builder()
            .bucket(bucketName).key(key).build()).toString();
      } catch (IOException e) {
        span.error(e);
        throw new RuntimeException(e);
      }
    } finally {
      span.end();
    }
  }
}
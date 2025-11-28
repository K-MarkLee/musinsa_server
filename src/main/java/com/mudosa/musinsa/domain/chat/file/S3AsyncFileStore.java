package com.mudosa.musinsa.domain.chat.file;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Component
public class S3AsyncFileStore extends AbstractS3Store implements FileStore {

  private final S3AsyncClient s3AsyncClient;

  public S3AsyncFileStore(Tracer tracer, S3AsyncClient s3AsyncClient) {
    super(tracer);
    this.s3AsyncClient = s3AsyncClient;
  }

  @Override
  public CompletableFuture<String> storeMessageFile(Long chatId, Long messageId, MultipartFile file) {
    String key = generateMessageKey(chatId, messageId, file);
    return uploadInternal(key, file);
  }

  // 2. 브랜드 로고 저장 (요청하신 부분)
  @Override
  public CompletableFuture<String> storeBrandLogo(Long brandId, MultipartFile file) {
    String key = generateBrandKey(brandId, file);
    return uploadInternal(key, file);
  }

  private CompletableFuture<String> uploadInternal(String key, MultipartFile file) {
    Span span = tracer.nextSpan().name("s3.upload")
        .tag("type", "async")
        .tag("file.size", String.valueOf(file.getSize()))
        .start();

    byte[] bytes;
    try {
      bytes = file.getBytes();
    } catch (IOException e) {
      span.error(e);
      span.end();
      return CompletableFuture.failedFuture(e);
    }

    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .contentType(file.getContentType())
        .contentLength(file.getSize())
        .build();

    return s3AsyncClient.putObject(request, AsyncRequestBody.fromBytes(bytes))
        .thenApply(resp -> s3AsyncClient.utilities().getUrl(GetUrlRequest.builder()
            .bucket(bucketName).key(key).build()).toString())
        .whenComplete((url, ex) -> {
          if (ex != null) span.error(ex);
          span.end();
        });
  }
}
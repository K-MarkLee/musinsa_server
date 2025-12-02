package com.mudosa.musinsa.domain.chat.event;

public record TempUploadedFile(
    String originalFilename,
    String contentType,
    byte[] bytes
) {
}

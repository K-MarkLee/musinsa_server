//package com.mudosa.musinsa.domain.chat.file;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Objects;
//import java.util.UUID;
//
//@Component
//@Slf4j
//public class LocalFileStore implements FileStore {
//
//  public String storeMessageFile(Long chatId, Long messageId, MultipartFile file) throws IOException {
//    String baseDir = new ClassPathResource("static/").getFile().getAbsolutePath();
//    String uploadDir = Paths.get(baseDir, "chat", String.valueOf(chatId), "message", String.valueOf(messageId)).toString();
//
//    Files.createDirectories(Paths.get(uploadDir));
//
//    String original = Objects.requireNonNullElse(file.getOriginalFilename(), "unknown");
//    String safeName = UUID.randomUUID() + "_" + StringUtils.cleanPath(original);
//
//    Path targetPath = Paths.get(uploadDir, safeName).toAbsolutePath().normalize();
//    file.transferTo(targetPath.toFile());
//
//    return "/chat/" + chatId + "/message/" + messageId + "/" + safeName;
//  }
//
//}

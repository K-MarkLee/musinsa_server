package com.mudosa.musinsa.domain.chat.mapper;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.domain.chat.dto.ChatRoomInfoResponse;
import com.mudosa.musinsa.domain.chat.entity.ChatRoom;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

//response 변환 mapper로 분리
@Component
public class ChatRoomMapper {

  public ChatRoomInfoResponse toChatRoomInfoResponse(@NotNull ChatRoom room) {
    Brand brand = room.getBrand();

    long activePartCount = room.getParts().stream()
        .filter(part -> part.getDeletedAt() == null)
        .count();

    return ChatRoomInfoResponse.builder()
        .chatId(room.getChatId())
        .brandId(brand != null ? brand.getBrandId() : null)
        .brandNameKo(brand != null ? brand.getNameKo() : null)
        .type(room.getType())
        .lastMessageAt(room.getLastMessageAt())
        .partNum(activePartCount)
        // isParticipate는 여기서 하드코딩하지 않음
        .logoUrl(brand != null ? brand.getLogoUrl() : null)
        .build();
  }

  public ChatRoomInfoResponse toChatRoomInfoResponse(@NotNull ChatRoom chatRoom,
                                                     boolean isParticipate,
                                                     long partNum) {
    return ChatRoomInfoResponse.builder()
        .brandId(chatRoom.getBrand().getBrandId())
        .brandNameKo(chatRoom.getBrand().getNameKo())
        .chatId(chatRoom.getChatId())
        .type(chatRoom.getType())
        .partNum(partNum)
        .lastMessageAt(chatRoom.getLastMessageAt())
        .isParticipate(isParticipate)
        .build();
  }
}


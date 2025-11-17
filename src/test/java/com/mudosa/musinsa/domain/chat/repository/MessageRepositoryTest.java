package com.mudosa.musinsa.domain.chat.repository;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.model.BrandStatus;
import com.mudosa.musinsa.domain.chat.entity.ChatPart;
import com.mudosa.musinsa.domain.chat.entity.ChatRoom;
import com.mudosa.musinsa.domain.chat.entity.Message;
import com.mudosa.musinsa.domain.chat.enums.ChatPartRole;
import com.mudosa.musinsa.domain.chat.enums.ChatRoomType;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.model.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DisplayName("MessageRepository 테스트")
@Transactional
class MessageRepositoryTest extends ServiceConfig {

  /* === Test Helper === */
  // 유저 생성 & 저장`
  private User saveUser(String userName) {
    User user = User.create(
        userName,
        "yong1234!",
        "test@test.com",
        UserRole.USER,
        "http://mudosa/uploads/avatar/avatar1.png",
        "010-0000-0000",
        "서울 강남구"
    );
    userRepository.save(user);
    return user;
  }

  // 브랜드 생성 & 저장
  private Brand saveBrand(String nameKo, String nameEn) {
    return brandRepository.save(Brand.builder()
        .nameKo(nameKo)
        .nameEn(nameEn)
        .commissionRate(BigDecimal.valueOf(10.00))
        .status(BrandStatus.ACTIVE)
        .build());
  }

  // 채팅방 생성 & 저장
  private ChatRoom saveChatRoom(Brand brand, ChatRoomType type) {
    return chatRoomRepository.save(
        ChatRoom.builder()
            .brand(brand)
            .type(type)
            .build()
    );
  }

  // 채팅방 참여 정보 생성 & 저장
  private ChatPart saveChatPart(ChatRoom chatRoom, User user) {
    return chatPartRepository.save(
        ChatPart.builder()
            .chatRoom(chatRoom)
            .user(user)
            .role(ChatPartRole.USER)
            .build()
    );
  }

  // 메시지 생성
  private Message saveMessage(ChatPart chatPart, String content, LocalDateTime timestamp) {
    Message message = Message.builder()
        .chatPart(chatPart)
        .chatId(chatPart.getChatRoom().getChatId())
        .content(content)
        .createdAt(timestamp)
        .build();

    messageRepository.save(message);
    return message;
  }

  private Message saveMessageWithParent(ChatPart chatPart, String content, LocalDateTime timestamp, Message parent) {
    Message message = Message.builder()
        .chatPart(chatPart)
        .chatId(chatPart.getChatRoom().getChatId())
        .content(content)
        .parent(parent)
        .createdAt(timestamp)
        .build();

    messageRepository.save(message);
    return message;
  }

  /* === findPageWithRelationsByChatId 메서드 테스트  === */
  @Nested
  @DisplayName("채팅방 메시지 페이징 조회")
  class findPageWithRelationsByChatId {

    /**
     * 첫 페이지이면서, 다음 페이지가 있는 경우 검증
     */
    private static void assertSlice_hasNext(Slice<Message> messages, int size, int totalCount, int page) {
      // 요청한 페이지/사이즈 그대로 들어왔는지
      assertThat(messages.getNumber()).isEqualTo(page);
      assertThat(messages.getSize()).isEqualTo(size);

      // 첫 페이지라 이전 페이지는 없음
      assertThat(messages.hasPrevious()).isFalse();

      // 이 테스트에서는 항상 "가득 찬 페이지 + 다음 페이지 존재" 시나리오
      assertThat(messages.getNumberOfElements()).isEqualTo(size);
      assertThat(messages.hasNext()).isTrue();

      // 전체 개수를 알고 있으니, 기대값도 계산해 볼 수 있음
      boolean expectedHasNext = totalCount > (page + 1) * size;
      assertThat(messages.hasNext()).isEqualTo(expectedHasNext);
    }

    /**
     * 마지막 페이지(또는 전체 개수가 size 이하인 케이스) 검증
     */
    private static void assertSlice_theEnd(Slice<Message> messages, int count, int page, int size) {
      // 현재 페이지의 실제 요소 수 = 총 메시지 수 (마지막 페이지라서)
      assertThat(messages.getNumberOfElements()).isEqualTo(count);

      assertThat(messages.getNumber()).isEqualTo(page);
      assertThat(messages.getSize()).isEqualTo(size);

      // 마지막 페이지이므로 다음 페이지 없음
      assertThat(messages.hasNext()).isFalse();
      // 테스트에서는 page=0인 케이스만 사용하므로 이전 페이지도 없음
      assertThat(messages.hasPrevious()).isFalse();
    }

    @DisplayName("메시지가 존재할 경우 최신순으로 정렬된 결과를 반환한다")
    @Test
    void findPageWithRelationsByChatId_Success() {
      // given
      //1. 유저 생성
      User user = saveUser("user");

      // 2. 브랜드 먼저 저장
      Brand brand = saveBrand("브랜드", "Brand");

      // 3. 이제 이 '영속된' 브랜드들을 채팅방에 달아준다
      ChatRoom chatRoom = saveChatRoom(brand, ChatRoomType.GROUP);

      // 4. 참가자 저장
      ChatPart p = saveChatPart(chatRoom, user);

      // 메시지 30건 생성
      int count = 30;
      LocalDateTime base = LocalDateTime.of(2000, 1, 1, 0, 0);
      for (int i = 1; i <= count; i++) {
        saveMessage(p, "안녕" + i, base.plusSeconds(i));
      }

      int page = 0;
      int size = 10;
      Pageable pageable = PageRequest.of(page, size);

      // when
      Slice<Message> messages = messageRepository.findSliceWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      // Slice 메타 검증
      assertSlice_hasNext(messages, size, count, page);

      // 최신순 정렬 검증
      assertThat(messages.getContent())
          .extracting(Message::getContent)
          .containsExactly(
              "안녕30", "안녕29", "안녕28", "안녕27", "안녕26",
              "안녕25", "안녕24", "안녕23", "안녕22", "안녕21"
          );
      assertThat(messages.getContent())
          .extracting(Message::getMessageId)
          .isSortedAccordingTo(Comparator.reverseOrder());
    }


    @DisplayName("같은 시간 메시지가 존재할 경우 messageId순으로 정렬된 결과를 반환한다")
    @Test
    void findPageWithRelationsByChatId_withSameTime() {
      // given
      //1. 유저 생성
      User user = saveUser("user");

      // 2. 브랜드 먼저 저장
      Brand brand = saveBrand("브랜드", "Brand");

      // 3. 이제 이 '영속된' 브랜드들을 채팅방에 달아준다
      ChatRoom chatRoom = saveChatRoom(brand, ChatRoomType.GROUP);

      // 4. 참가자 저장
      ChatPart p = saveChatPart(chatRoom, user);

      // 메시지 30건 생성
      int count = 30;
      LocalDateTime base = LocalDateTime.of(2000, 1, 1, 0, 0);
      for (int i = 1; i <= count; i++) {
        saveMessage(p, "안녕" + i, base);
      }

      int page = 0;
      int size = 10;
      Pageable pageable = PageRequest.of(page, size);

      // when
      Slice<Message> messages = messageRepository.findSliceWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      assertSlice_hasNext(messages, size, count, page);

      assertThat(messages.getContent())
          .extracting(Message::getContent)
          .containsExactly(
              "안녕30", "안녕29", "안녕28", "안녕27", "안녕26",
              "안녕25", "안녕24", "안녕23", "안녕22", "안녕21"
          );
      assertThat(messages.getContent())
          .extracting(Message::getMessageId)
          .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @DisplayName("메시지가 존재하지 않으면 빈 Slice를 반환한다")
    @Test
    void findPage_emptyResult() {
      // given
      User user = saveUser("user");
      Brand brand = saveBrand("브랜드", "Brand");
      ChatRoom chatRoom = saveChatRoom(brand, ChatRoomType.GROUP);
      saveChatPart(chatRoom, user);

      int count = 0;
      int page = 0;
      int size = 10;
      Pageable pageable = PageRequest.of(page, size);

      // when
      Slice<Message> messages = messageRepository.findSliceWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      assertSlice_theEnd(messages, count, page, size);
      assertThat(messages.getContent()).isEmpty();
    }

    @Test
    @DisplayName("메시지 개수가 페이지 크기보다 적을 경우, 모든 메시지를 반환한다")
    void findPage_lessMessageThanSize() {
      // given
      User user = saveUser("user");
      Brand brand = saveBrand("브랜드", "Brand");
      ChatRoom chatRoom = saveChatRoom(brand, ChatRoomType.GROUP);
      ChatPart p = saveChatPart(chatRoom, user);

      int count = 1;
      LocalDateTime base = LocalDateTime.of(2000, 1, 1, 0, 0);
      saveMessage(p, "안녕" + count, base);

      int page = 0;
      int size = 2;
      Pageable pageable = PageRequest.of(page, size);

      // when
      Slice<Message> messages = messageRepository.findSliceWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      assertSlice_theEnd(messages, count, page, size);

      assertThat(messages.getContent())
          .hasSize(count)
          .extracting(Message::getContent)
          .containsExactly("안녕1");
    }

    @DisplayName("여러 채팅방이 존재해도 조회한 채팅방의 메시지만 페이징된다")
    @Test
    void findPageWithRelationsByChatId_ignoreOtherChatRooms() {
      // given
      User user = saveUser("user");
      Brand brand = saveBrand("브랜드", "Brand");

      ChatRoom chatRoom1 = saveChatRoom(brand, ChatRoomType.GROUP);
      ChatPart p1 = saveChatPart(chatRoom1, user);

      ChatRoom chatRoom2 = saveChatRoom(brand, ChatRoomType.GROUP);
      ChatPart p2 = saveChatPart(chatRoom2, user);

      LocalDateTime base = LocalDateTime.of(2000, 1, 1, 0, 0);

      int chat1messageNum = 5;
      for (int i = 1; i <= chat1messageNum; i++) {
        saveMessage(p1, "room1-" + i, base.plusSeconds(i));
      }

      int chat2messageNum = 10;
      for (int i = 1; i <= chat2messageNum; i++) {
        saveMessage(p2, "room2-" + i, base.plusSeconds(i));
      }

      int page = 0;
      int size = 10;
      Pageable pageable = PageRequest.of(page, size);

      // when
      Slice<Message> messages = messageRepository.findSliceWithRelationsByChatId(chatRoom1.getChatId(), pageable);

      // then
      // 이 Slice에는 room1 메시지만 들어 있어야 하고, 개수는 chat1messageNum
      assertThat(messages.getNumberOfElements()).isEqualTo(chat1messageNum);
      assertThat(messages.hasNext()).isFalse();

      assertThat(messages.getContent())
          .extracting(Message::getContent)
          .allMatch(c -> c.startsWith("room1-"));
    }

    @DisplayName("첫 번째 페이지가 아닌 페이지를 조회할 때도 최신순이 유지된다")
    @Test
    void findPage_noFirstPage() {
      // given
      User user = saveUser("user");
      Brand brand = saveBrand("브랜드", "Brand");
      ChatRoom chatRoom = saveChatRoom(brand, ChatRoomType.GROUP);
      ChatPart p = saveChatPart(chatRoom, user);

      LocalDateTime base = LocalDateTime.of(2000, 1, 1, 0, 0);
      int count = 25;
      for (int i = 1; i <= count; i++) {
        saveMessage(p, "안녕" + i, base.plusSeconds(i));
      }

      Pageable pageable = PageRequest.of(1, 10);

      // when
      Slice<Message> messages = messageRepository.findSliceWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      assertThat(messages.getNumber()).isEqualTo(1);
      assertThat(messages.hasNext()).isTrue(); // 25건 → 0,1,2 페이지 존재

      assertThat(messages.getContent())
          .extracting(Message::getContent)
          .containsExactly(
              "안녕15", "안녕14", "안녕13", "안녕12", "안녕11",
              "안녕10", "안녕9", "안녕8", "안녕7", "안녕6"
          );
      assertThat(messages.getContent())
          .extracting(Message::getCreatedAt)
          .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @DisplayName("메시지와 함께 작성자 정보도 로딩된다")
    @Test
    void findPageWithUserFetched() {
      // given
      User user = saveUser("user");
      Brand brand = saveBrand("브랜드", "Brand");
      ChatRoom chatRoom = saveChatRoom(brand, ChatRoomType.GROUP);
      ChatPart p = saveChatPart(chatRoom, user);

      LocalDateTime base = LocalDateTime.of(2000, 1, 1, 0, 0);
      saveMessage(p, "안녕", base);

      Pageable pageable = PageRequest.of(0, 10);

      // when
      Slice<Message> messages = messageRepository.findSliceWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      assertThat(messages.getContent().getFirst().getChatPart().getUser().getUserName())
          .isEqualTo(user.getUserName());
      assertThat(messages.getContent().getFirst().getChatPart().getUser().getId())
          .isEqualTo(user.getId());
    }

    @DisplayName("존재하지 않는 채팅방 ID로 조회해도 빈 Slice를 반환한다")
    @Test
    void findPage_notExistsChatId() {
      // given
      User user = saveUser("user");
      Brand brand = saveBrand("브랜드", "Brand");
      ChatRoom chatRoom = saveChatRoom(brand, ChatRoomType.GROUP);
      ChatPart p = saveChatPart(chatRoom, user);

      LocalDateTime base = LocalDateTime.of(2000, 1, 1, 0, 0);
      saveMessage(p, "안녕", base);

      Pageable pageable = PageRequest.of(0, 10);

      // when
      Slice<Message> messages = messageRepository.findSliceWithRelationsByChatId(999999L, pageable);

      // then
      assertThat(messages.getContent()).isEmpty();
      assertThat(messages.getNumberOfElements()).isZero();
      assertThat(messages.hasNext()).isFalse();
    }

    @DisplayName("답장 메시지를 조회할 때 부모 메시지도 함께 조회된다")
    @Test
    void findPage_withParentMessage() {
      // given
      User user = saveUser("user");
      Brand brand = saveBrand("브랜드", "Brand");
      ChatRoom chatRoom = saveChatRoom(brand, ChatRoomType.GROUP);
      ChatPart p = saveChatPart(chatRoom, user);

      LocalDateTime base = LocalDateTime.of(2000, 1, 1, 0, 0);
      Message parent = saveMessage(p, "부모", base);
      saveMessageWithParent(p, "자식", base.plusSeconds(1), parent);

      Pageable pageable = PageRequest.of(0, 10);

      // when
      Slice<Message> messages = messageRepository.findSliceWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      assertThat(messages.getContent().getFirst().getParent()).isNotNull();
      assertThat(messages.getContent().getFirst().getParent().getContent()).isEqualTo("부모");
      assertThat(messages.getContent().getFirst().getContent()).isEqualTo("자식");
    }

    @DisplayName("삭제된 메시지는 조회 결과에 포함되지 않는다")
    @Test
    void findPage_excludeDeletedMessages() {
      // given
      User user = saveUser("user");
      Brand brand = saveBrand("브랜드", "Brand");
      ChatRoom chatRoom = saveChatRoom(brand, ChatRoomType.GROUP);
      ChatPart p = saveChatPart(chatRoom, user);

      LocalDateTime base = LocalDateTime.of(2000, 1, 1, 0, 0);
      Message alive = saveMessage(p, "alive", base.plusSeconds(1));
      Message deleted = saveMessage(p, "delete", base.plusSeconds(2));

      deleted.setDeletedAt(LocalDateTime.now());
      messageRepository.save(deleted);

      Pageable pageable = PageRequest.of(0, 10);

      // when
      Slice<Message> messages = messageRepository.findSliceWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      assertThat(messages.getNumberOfElements()).isEqualTo(1);
      assertThat(messages.getContent())
          .extracting(Message::getContent)
          .containsExactly("alive");
    }
  }
}
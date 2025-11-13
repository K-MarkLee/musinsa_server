package com.mudosa.musinsa.domain.chat.repository;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DisplayName("MessageRepository 테스트")
@Transactional
class MessageRepositoryTest extends JpaConfig {

  /* === Test Helper === */
  // 유저 생성 & 저장`
  private User saveUser(String userName) {
    User user = User.create(userName, "yong1234!", "test@test.com", UserRole.USER, "http://mudosa/uploads/avatar/avatar1.png", "010-0000-0000", "서울 강남구");
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

  //메시지 생성
  private Message saveMessage(ChatPart chatPart, String content, LocalDateTime timestamp) {
    // 1. Message 생성 및 저장
    Message message = Message.builder()
        .chatPart(chatPart)
        .content(content)
        .createdAt(timestamp)
        .build();

    messageRepository.save(message); // id 확보

    return message;
  }

  private Message saveMessageWithParent(ChatPart chatPart, String content, LocalDateTime timestamp, Message parent) {
    // 1. Message 생성 및 저장
    Message message = Message.builder()
        .chatPart(chatPart)
        .content(content)
        .parent(parent)
        .createdAt(timestamp)
        .build();

    messageRepository.save(message); // id 확보

    return message;
  }

  @Nested
  @DisplayName("채팅방 메시지 페이징 조회")
  class findPageWithRelationsByChatId {
    @DisplayName("메시지가 존재할 경우 최신순으로 정렬된 결과를 반환한다")
    @Test
    void findPageWithRelationsByChatId_Success() {
      // given
      //1. 유저 생성
      User user = saveUser("철수");

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
      Page<Message> messages = messageRepository.findPageWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      // 기본 페이징 정보 검증
      assertThat(messages).hasSize(size);
      assertThat(messages.getTotalElements()).isEqualTo(count);
      assertThat(messages.getNumber()).isEqualTo(page);
      assertThat(messages.getTotalPages()).isEqualTo((int) Math.ceil((double) count / size));
      assertThat(messages.hasNext()).isTrue();
      assertThat(messages.hasPrevious()).isFalse();

      // 최신순 정렬 검증
      assertThat(messages.getContent())
          .extracting(Message::getContent)
          .containsExactly(
              "안녕30", "안녕29", "안녕28", "안녕27", "안녕26",
              "안녕25", "안녕24", "안녕23", "안녕22", "안녕21"
          );
      assertThat(messages.getContent())
          .extracting(Message::getCreatedAt)
          .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @DisplayName("같은 시간 메시지가 존재할 경우 messageId순으로 정렬된 결과를 반환한다")
    @Test
    void findPageWithRelationsByChatId_withSameTime() {
      // given
      //1. 유저 생성
      User user = saveUser("철수");

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
      Page<Message> messages = messageRepository.findPageWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      // 기본 페이징 정보 검증
      assertThat(messages).hasSize(size);
      assertThat(messages.getTotalElements()).isEqualTo(count);
      assertThat(messages.getNumber()).isEqualTo(page);
      assertThat(messages.getTotalPages()).isEqualTo((int) Math.ceil((double) count / size));
      assertThat(messages.hasNext()).isTrue();
      assertThat(messages.hasPrevious()).isFalse();

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

    @DisplayName("메시지가 존재하지 않으면 빈 페이지를 반환한다")
    @Test
    void findPage_emptyResult() {
      // given
      User user = saveUser("철수");
      Brand brand = saveBrand("브랜드", "Brand");
      ChatRoom chatRoom = saveChatRoom(brand, ChatRoomType.GROUP);
      saveChatPart(chatRoom, user);

      // 메시지 0건 생성
      int count = 0;
      int page = 0;
      int size = 10;
      Pageable pageable = PageRequest.of(page, size);

      // when
      Page<Message> messages = messageRepository.findPageWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      // 기본 페이징 정보 검증
      assertThat(messages).hasSize(count);
      assertThat(messages.getTotalElements()).isEqualTo(count);
      assertThat(messages.getNumber()).isEqualTo(page);
      assertThat(messages.getTotalPages()).isEqualTo((int) Math.ceil((double) count / size));
      assertThat(messages.hasNext()).isFalse();
      assertThat(messages.hasPrevious()).isFalse();

      // 메시지 존재 X
      assertThat(messages.getContent())
          .isEmpty();
    }

    @Test
    @DisplayName("메시지가 한 건만 존재할 경우 올바르게 반환한다")
    void findPage_singleMessage() {
      // given
      //1. 유저 생성
      User user = saveUser("철수");

      // 2. 브랜드 먼저 저장
      Brand brand = saveBrand("브랜드", "Brand");

      // 3. 이제 이 '영속된' 브랜드들을 채팅방에 달아준다
      ChatRoom chatRoom = saveChatRoom(brand, ChatRoomType.GROUP);

      // 4. 참가자 저장
      ChatPart p = saveChatPart(chatRoom, user);

      // 메시지 1건 생성
      int count = 1;
      LocalDateTime base = LocalDateTime.of(2000, 1, 1, 0, 0);
      saveMessage(p, "안녕" + count, base);

      int page = 0;
      int size = 10;
      Pageable pageable = PageRequest.of(page, size);

      // when
      Page<Message> messages = messageRepository.findPageWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      // 기본 페이징 정보 검증
      assertThat(messages).hasSize(count);
      assertThat(messages.getTotalElements()).isEqualTo(count);
      assertThat(messages.getNumber()).isEqualTo(page);
      assertThat(messages.getTotalPages()).isEqualTo((int) Math.ceil((double) count / size));
      assertThat(messages.hasNext()).isFalse();
      assertThat(messages.hasPrevious()).isFalse();

      // 메시지 1건
      assertThat(messages.getContent()).extracting(Message::getContent).containsExactly("안녕1");
    }

    @DisplayName("여러 채팅방이 존재해도 조회한 채팅방의 메시지만 페이징된다")
    @Test
    void findPageWithRelationsByChatId_ignoreOtherChatRooms() {
      // given
      User user = saveUser("철수");
      Brand brand = saveBrand("브랜드", "Brand");
      ChatRoom chatRoom1 = saveChatRoom(brand, ChatRoomType.GROUP);
      ChatPart p1 = saveChatPart(chatRoom1, user);

      ChatRoom chatRoom2 = saveChatRoom(brand, ChatRoomType.GROUP);
      ChatPart p2 = saveChatPart(chatRoom2, user);

      LocalDateTime base = LocalDateTime.of(2000, 1, 1, 0, 0);

      // target room에 5건
      int chat1messageNum = 5;
      for (int i = 1; i <= chat1messageNum; i++) {
        saveMessage(p1, "room1-" + i, base.plusSeconds(i));
      }
      // 다른 room에 10건
      int chat2messageNum = 10;
      for (int i = 1; i <= chat2messageNum; i++) {
        saveMessage(p2, "room2-" + i, base.plusSeconds(i));
      }

      int page = 0;
      int size = 10;
      Pageable pageable = PageRequest.of(page, size);

      // when
      Page<Message> messages = messageRepository.findPageWithRelationsByChatId(chatRoom1.getChatId(), pageable);

      // then
      assertThat(messages.getTotalElements()).isEqualTo(chat1messageNum);
      assertThat(messages.getContent())
          .extracting(Message::getContent)
          .allMatch(c -> c.startsWith("room1-"));
    }

    @DisplayName("두 번째 페이지를 조회할 때도 최신순이 유지된다")
    @Test
    void findPage_secondPage() {
      // given
      User user = saveUser("철수");
      Brand brand = saveBrand("브랜드", "Brand");
      ChatRoom chatRoom = saveChatRoom(brand, ChatRoomType.GROUP);
      ChatPart p = saveChatPart(chatRoom, user);

      LocalDateTime base = LocalDateTime.of(2000, 1, 1, 0, 0);
      int count = 25;
      for (int i = 1; i <= count; i++) {
        saveMessage(p, "안녕" + i, base.plusSeconds(i));
      }

      // page 1 이면 11~20번째가 나와야 함 (내림차순이니까 실제로는 안녕15~안녕6 이런 식일 것)
      Pageable pageable = PageRequest.of(1, 10);

      // when
      Page<Message> messages = messageRepository.findPageWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      assertThat(messages.getNumber()).isEqualTo(1);
      assertThat(messages.hasNext()).isTrue(); // 25건이니까 0,1,2 페이지 존재
      // 가장 먼저 나와야 하는 건 전체 중 15번째(거꾸로 정렬이므로)
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
      User user = saveUser("철수");
      Brand brand = saveBrand("브랜드", "Brand");
      ChatRoom chatRoom = saveChatRoom(brand, ChatRoomType.GROUP);
      ChatPart p = saveChatPart(chatRoom, user);

      LocalDateTime base = LocalDateTime.of(2000, 1, 1, 0, 0);
      saveMessage(p, "안녕", base);

      Pageable pageable = PageRequest.of(0, 10);

      // when
      Page<Message> messages = messageRepository.findPageWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      // 실제 유저가 붙어있는지 확인
      assertThat(messages.getContent().getFirst().getChatPart().getUser().getUserName()).isEqualTo("철수");
    }

    @DisplayName("존재하지 않는 채팅방 ID로 조회해도 빈 페이지를 반환한다")
    @Test
    void findPage_notExistsChatId() {
      // given
      Pageable pageable = PageRequest.of(0, 10);

      // when
      Page<Message> messages = messageRepository.findPageWithRelationsByChatId(999999L, pageable);

      // then
      assertThat(messages).isEmpty();
      assertThat(messages.getTotalElements()).isZero();
    }

    @DisplayName("답장 메시지를 조회할 때 부모 메시지도 함께 조회된다")
    @Test
    void findPage_withParentMessage() {
      // given
      User user = saveUser("철수");
      Brand brand = saveBrand("브랜드", "Brand");
      ChatRoom chatRoom = saveChatRoom(brand, ChatRoomType.GROUP);
      ChatPart p = saveChatPart(chatRoom, user);

      LocalDateTime base = LocalDateTime.of(2000, 1, 1, 0, 0);
      Message parent = saveMessage(p, "부모", base);

      Message reply = saveMessageWithParent(p, "자식", base.plusSeconds(1), parent);

      Pageable pageable = PageRequest.of(0, 10);

      // when
      Page<Message> messages = messageRepository.findPageWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      assertThat(messages.getContent().getFirst().getParent()).isNotNull();
      assertThat(messages.getContent().getFirst().getParent().getContent()).isEqualTo("부모");
    }

    @DisplayName("삭제된 메시지는 조회 결과에 포함되지 않는다")
    @Test
    void findPage_excludeDeletedMessages() {
      // given
      User user = saveUser("철수");
      Brand brand = saveBrand("브랜드", "Brand");
      ChatRoom chatRoom = saveChatRoom(brand, ChatRoomType.GROUP);
      ChatPart p = saveChatPart(chatRoom, user);

      LocalDateTime base = LocalDateTime.of(2000, 1, 1, 0, 0);
      Message alive = saveMessage(p, "alive", base.plusSeconds(1));
      Message deleted = saveMessage(p, "delete", base.plusSeconds(2));

      // soft delete 시뮬레이션
      deleted.setDeletedAt(LocalDateTime.now());
      messageRepository.save(deleted);

      Pageable pageable = PageRequest.of(0, 10);

      // when
      Page<Message> messages = messageRepository.findPageWithRelationsByChatId(chatRoom.getChatId(), pageable);

      // then
      assertThat(messages.getTotalElements()).isEqualTo(1);
      assertThat(messages.getContent())
          .extracting(Message::getContent)
          .containsExactly("alive");
    }
  }
}
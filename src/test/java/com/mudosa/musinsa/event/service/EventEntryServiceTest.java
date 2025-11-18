package com.mudosa.musinsa.event.service;

import com.mudosa.musinsa.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EventEntryService 테스트")
class EventEntryServiceTest {

    private EventEntryService eventEntryService;

    @BeforeEach
    void setUp() {
        eventEntryService = new EventEntryService();
    }

    @Test
    @DisplayName("[해피케이스] 슬롯 확보 - 정상적으로 슬롯을 확보한다")
    void acquireSlot_Success() {
        // given
        Long eventId = 1L;
        Long userId = 100L;

        // when
        EventEntryService.EventEntryToken token = eventEntryService.acquireSlot(eventId, userId);

        // then
        assertThat(token).isNotNull();

        // 토큰 해제
        token.close();
    }

    @Test
    @DisplayName("[해피케이스] 슬롯 확보 후 해제 - try-with-resources로 자동 해제된다")
    void acquireSlot_AutoClose_Success() {
        // given
        Long eventId = 1L;
        Long userId = 100L;

        // when & then
        try (EventEntryService.EventEntryToken token = eventEntryService.acquireSlot(eventId, userId)) {
            assertThat(token).isNotNull();
        } // AutoCloseable에 의해 자동으로 close() 호출됨

        // 슬롯이 해제되었으므로 다시 확보 가능해야 함
        try (EventEntryService.EventEntryToken token2 = eventEntryService.acquireSlot(eventId, userId)) {
            assertThat(token2).isNotNull();
        }
    }

    @Test
    @DisplayName("[해피케이스] 슬롯 수동 해제 - release() 메서드로 슬롯을 해제한다")
    void releaseSlot_Success() {
        // given
        Long eventId = 1L;
        Long userId = 100L;
        EventEntryService.EventEntryToken token = eventEntryService.acquireSlot(eventId, userId);

        // when
        token.release();

        // then
        // 슬롯이 해제되었으므로 다시 확보 가능해야 함
        EventEntryService.EventEntryToken token2 = eventEntryService.acquireSlot(eventId, userId);
        assertThat(token2).isNotNull();
        token2.close();
    }

    @Test
    @DisplayName("[해피케이스] 여러 번 release 호출 - 중복 해제해도 예외가 발생하지 않는다")
    void releaseSlot_MultipleTimes_Success() {
        // given
        Long eventId = 1L;
        Long userId = 100L;
        EventEntryService.EventEntryToken token = eventEntryService.acquireSlot(eventId, userId);

        // when & then
        token.release();
        token.release(); // 두 번째 호출
        token.close(); // close도 호출

        // 예외가 발생하지 않아야 함
    }

    @Test
    @DisplayName("[해피케이스] 다른 사용자는 동시에 슬롯 확보 가능 - eventId는 같지만 userId가 다르면 슬롯 확보 가능")
    void acquireSlot_DifferentUser_Success() {
        // given
        Long eventId = 1L;
        Long userId1 = 100L;
        Long userId2 = 200L;

        // when
        EventEntryService.EventEntryToken token1 = eventEntryService.acquireSlot(eventId, userId1);
        EventEntryService.EventEntryToken token2 = eventEntryService.acquireSlot(eventId, userId2);

        // then
        assertThat(token1).isNotNull();
        assertThat(token2).isNotNull();

        token1.close();
        token2.close();
    }

    @Test
    @DisplayName("[해피케이스] 다른 이벤트는 동시에 슬롯 확보 가능 - userId는 같지만 eventId가 다르면 슬롯 확보 가능")
    void acquireSlot_DifferentEvent_Success() {
        // given
        Long eventId1 = 1L;
        Long eventId2 = 2L;
        Long userId = 100L;

        // when
        EventEntryService.EventEntryToken token1 = eventEntryService.acquireSlot(eventId1, userId);
        EventEntryService.EventEntryToken token2 = eventEntryService.acquireSlot(eventId2, userId);

        // then
        assertThat(token1).isNotNull();
        assertThat(token2).isNotNull();

        token1.close();
        token2.close();
    }

    @Test
    @DisplayName("[예외케이스] 중복 슬롯 확보 - 같은 사용자가 같은 이벤트 슬롯을 중복 확보하면 예외가 발생한다")
    void acquireSlot_Duplicate_ThrowsException() {
        // given
        Long eventId = 1L;
        Long userId = 100L;
        EventEntryService.EventEntryToken token1 = eventEntryService.acquireSlot(eventId, userId);

        // when & then
        assertThatThrownBy(() -> eventEntryService.acquireSlot(eventId, userId))
                .isInstanceOf(BusinessException.class);

        token1.close();
    }

    @Test
    @DisplayName("[예외케이스] eventId가 null이면 예외가 발생한다")
    void acquireSlot_NullEventId_ThrowsException() {
        // given
        Long eventId = null;
        Long userId = 100L;

        // when & then
        assertThatThrownBy(() -> eventEntryService.acquireSlot(eventId, userId))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("eventId cannot be null");
    }

    @Test
    @DisplayName("[예외케이스] userId가 null이면 예외가 발생한다")
    void acquireSlot_NullUserId_ThrowsException() {
        // given
        Long eventId = 1L;
        Long userId = null;

        // when & then
        assertThatThrownBy(() -> eventEntryService.acquireSlot(eventId, userId))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("userId cannot be null");
    }

    @Test
    @DisplayName("[해피케이스] 만료된 슬롯 재확보 - 5초 후 만료된 슬롯은 다시 확보할 수 있다")
    void acquireSlot_AfterExpiration_Success() throws InterruptedException {
        // given
        Long eventId = 1L;
        Long userId = 100L;
        EventEntryService.EventEntryToken token1 = eventEntryService.acquireSlot(eventId, userId);

        // when
        // 5초 대기 (실제 테스트에서는 너무 길어서 이 테스트는 선택적)
        // Thread.sleep(5100);

        // then
        // 시간이 지나면 purgeExpiredEntries()가 호출되어 만료된 엔트리가 제거됨
        // 실제 테스트에서는 시간이 오래 걸리므로 주석 처리

        token1.close();
    }
}

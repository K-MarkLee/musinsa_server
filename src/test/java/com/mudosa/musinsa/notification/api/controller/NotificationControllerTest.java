package com.mudosa.musinsa.notification.api.controller;


import com.mudosa.musinsa.notification.dto.NotificationDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("NotificationController 테스트")
public class NotificationControllerTest extends NotificationControllerTestSupport{

    @Nested
    @DisplayName("/api/notification/{userId} GET 메서드의 호출을 받는다.")
    class readNotification{
        @DisplayName("userId를 입력하면 사용자의 알림 내역을 조회한다.")
        @Test
        void readNotificationTest() throws Exception {
        // given
            NotificationDTO notificationDTO = createNotificationDTO();
            List<NotificationDTO> result = List.of(notificationDTO);
            Long userId = 1L;
            given(notificationService.readNotification(userId)).willReturn(result);
        // when & then
            mockMvc.perform(get("/api/notification/{userId}", userId))
                    .andDo(print())
                    .andExpect(status().isOk());
            verify(notificationService).readNotification(userId);
        }
    }

    @Nested
    @DisplayName("/api/notification/read PATCH 메서드의 호출을 받는다.")
    class updateNotification {
        @DisplayName("사용자 1이 자신에게 온 알림 1개를 읽음 처리한다.")
        @Test
        void updateNotificationTest() throws Exception {
        // given
        NotificationDTO notificationDTO = createNotificationDTO();
        // when & then
        mockMvc.perform(patch("/api/notification/read")
                        .content(objectMapper.writeValueAsString(notificationDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
        verify(notificationService).updateNotificationState(notificationDTO.getNotificationId());
        }
    }

    private NotificationDTO createNotificationDTO() {
        return NotificationDTO.builder()
                .userId(1L)
                .notificationTitle("asdasd")
                .notificationMessage("asdasd")
                .notificationUrl("asdasd")
                .build();
    }
}

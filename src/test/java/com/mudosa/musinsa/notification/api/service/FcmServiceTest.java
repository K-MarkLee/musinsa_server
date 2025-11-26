package com.mudosa.musinsa.notification.api.service;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.fbtoken.dto.FBTokenDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class FcmServiceTest extends ServiceConfig {

    @Nested
    @DisplayName("메세지 토큰 발송 테스트")
    class sendMessageByToken{
        @DisplayName("메세지 토큰 발송에 성공한다")
        @Test
        void Test(){
        // given
            String title = "test";
            String body = "test";
            String token = "test_token";

            FBTokenDTO fbTokenDTO = FBTokenDTO.builder()
                    .token(token)
                    .build();
            List<FBTokenDTO> fbTokenDTOList = List.of(fbTokenDTO);

            Mockito.when(fcmService.sendMessageByToken(anyString(), anyString(), any(List.class))).thenReturn(true);

        // when
            boolean success = fcmService.sendMessageByToken(title,body,fbTokenDTOList);
        // then
            assertThat(success).isTrue();
        }
    }

}

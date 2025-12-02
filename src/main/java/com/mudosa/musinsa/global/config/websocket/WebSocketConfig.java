package com.mudosa.musinsa.global.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("*")
        .withSockJS();

    registry.addEndpoint("/ws-native")
        .setAllowedOriginPatterns("*");
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic", "/qna");
    config.setApplicationDestinationPrefixes("/app");
    config.setUserDestinationPrefix("/user");
  }

  /**
   * 클라이언트 → 서버 방향 (SUBSCRIBE, SEND 등) 로그
   */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new StompLoggingInterceptor("IN"));
  }

  /**
   * 서버 → 클라이언트 방향 (MESSAGE 브로드캐스트 등) 로그
   */
  @Override
  public void configureClientOutboundChannel(ChannelRegistration registration) {
    registration.interceptors(new StompLoggingInterceptor("OUT"));
  }

  /**
   * STOMP 프레임 로깅용 인터셉터
   */
  static class StompLoggingInterceptor implements ChannelInterceptor {

    private final String direction; // IN / OUT

    StompLoggingInterceptor(String direction) {
      this.direction = direction;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
      StompHeaderAccessor accessor =
          MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

      if (accessor == null) {
        return message;
      }

      String sessionId = accessor.getSessionId();
      String command = accessor.getCommand() != null ? accessor.getCommand().name() : "NONE";
      String destination = accessor.getDestination();

      Object payload = message.getPayload();
      String payloadStr;

      if (payload instanceof byte[]) {
        payloadStr = new String((byte[]) payload, StandardCharsets.UTF_8);
      } else {
        payloadStr = String.valueOf(payload);
      }

      // 너무 길면 잘라서 로그
      if (payloadStr.length() > 300) {
        payloadStr = payloadStr.substring(0, 300) + "...(truncated)";
      }

      log.debug(
          "[WS {}] sessionId={}, command={}, destination={}, payload={}",
          direction, sessionId, command, destination, payloadStr
      );

      return message;
    }
  }
}

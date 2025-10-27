package com.mudosa.musinsa.global.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정 클래스
 * STOMP 프로토콜을 사용한 메시징 구성
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  /**
   * WebSocket 엔드포인트 등록
   * - /ws: WebSocket 연결 엔드포인트
   * - SockJS를 사용하여 브라우저 호환성 향상
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("*")
        .withSockJS();
  }

  /**
   * 메시지 브로커 설정
   * - /brand: 1:N 브로드캐스트 (그룹 채팅)
   * - /qna: 1:1 메시징 (개인 알림)
   * - /app: 클라이언트가 메시지를 보낼 prefix
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // Simple Broker 활성화 (개발용)
    // 프로덕션에서는 RabbitMQ, ActiveMQ 등 외부 브로커 사용 권장
    config.enableSimpleBroker("/brand", "/qna");

    // 클라이언트가 메시지를 보낼 때 사용할 prefix
    config.setApplicationDestinationPrefixes("/app");

    // 특정 사용자에게 메시지 보낼 때 사용할 prefix
    config.setUserDestinationPrefix("/user");
  }
}
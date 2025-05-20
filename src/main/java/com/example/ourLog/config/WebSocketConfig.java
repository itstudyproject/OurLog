package com.example.ourLog.config;// WebSocketConfig.java
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic"); // 구독 주소
    config.setApplicationDestinationPrefixes("/app"); // 클라이언트가 보낼 주소 prefix
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws-chat") // WebSocket 연결 주소
            .setAllowedOriginPatterns("*")
            .withSockJS(); // SockJS fallback
  }
}

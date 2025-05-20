package com.example.ourLog.config;// WebSocketConfig.java
import com.example.ourLog.security.util.JWTUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final JWTUtil jwtUtil;

  public WebSocketConfig(JWTUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws-chat")
            .setAllowedOriginPatterns("*")
            .addInterceptors(new JwtHandshakeInterceptor(jwtUtil))  // 여기 추가
            .withSockJS();
  }
}
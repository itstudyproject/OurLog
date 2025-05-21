package com.example.ourLog.config;// WebSocketConfig.java
import com.example.ourLog.repository.UserRepository;
import com.example.ourLog.security.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final JWTUtil jwtUtil;
  private final UserRepository userRepository;

  public WebSocketConfig(JWTUtil jwtUtil, UserRepository userRepository) {
    this.jwtUtil = jwtUtil;
    this.userRepository = userRepository;
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws-chat") // ★ 슬래시 붙이는 게 일반적
            .addInterceptors(jwtHandshakeInterceptor()) // JWT 인증 인터셉터
            .setAllowedOriginPatterns("*") // CORS 허용
            .withSockJS(); // SockJS fallback 허용
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // ★ /app 으로 시작하는 경로는 @MessageMapping 으로 라우팅
    registry.setApplicationDestinationPrefixes("/app");

    // ★ /topic (broadcast), /queue (1:1 등 private 메시지) 용 simple broker
    registry.enableSimpleBroker("/topic", "/queue");
  }

  @Bean
  public JwtHandshakeInterceptor jwtHandshakeInterceptor() {
    return new JwtHandshakeInterceptor(jwtUtil, userRepository);
  }
}

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

  @Override  // 수정된 부분
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("ourlog/ws-chat")
            .addInterceptors(jwtHandshakeInterceptor()) // 직접 생성
            .setAllowedOriginPatterns("*")
            .withSockJS();
  }

  @Override  // 수정된 부분
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic", "/queue");
    registry.setApplicationDestinationPrefixes("/app");
  }

  @Bean
  public JwtHandshakeInterceptor jwtHandshakeInterceptor() {
    return new JwtHandshakeInterceptor(jwtUtil, userRepository);
  }
}

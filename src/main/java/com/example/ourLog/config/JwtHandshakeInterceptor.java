package com.example.ourLog.config;

import com.example.ourLog.entity.User;
import com.example.ourLog.repository.UserRepository;
import com.example.ourLog.security.util.JWTUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

  private final JWTUtil jwtUtil;
  private final UserRepository userRepository;

  public JwtHandshakeInterceptor(JWTUtil jwtUtil, UserRepository userRepository) {
    this.jwtUtil = jwtUtil;
    this.userRepository = userRepository;
  }

  @Override
  public boolean beforeHandshake(ServerHttpRequest request,
                                 ServerHttpResponse response,
                                 WebSocketHandler wsHandler,
                                 Map<String, Object> attributes) {

    URI uri = request.getURI();
    String query = uri.getQuery(); // e.g. "token=abc.def.ghi&otherParam=123"

    System.out.println("[Handshake] Incoming URI: " + uri);

    if (query != null) {
      String[] params = query.split("&");

      for (String param : params) {
        if (param.startsWith("token=")) {
          String token = param.substring("token=".length());
          System.out.println("[Handshake] Extracted token: " + token);

          try {
            String email = jwtUtil.validateAndExtract(token);
            System.out.println("[Handshake] Extracted email: " + email);

            if (email != null) {
              Optional<User> userOpt = userRepository.findByEmail(email);
              if (userOpt.isPresent()) {
                attributes.put("user", userOpt.get());
                return true;
              }
            }

          } catch (Exception e) {
            System.out.println("[Handshake] Token validation failed: " + e.getMessage());
          }

          break;
        }
      }
    }

    System.out.println("[Handshake] Failed - No valid token");
    response.setStatusCode(HttpStatus.FORBIDDEN);
    return false;
  }

  @Override
  public void afterHandshake(ServerHttpRequest request,
                             ServerHttpResponse response,
                             WebSocketHandler wsHandler,
                             Exception exception) {
    // No-op
  }
}

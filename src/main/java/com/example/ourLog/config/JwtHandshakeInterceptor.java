package com.example.ourLog.config;

import com.example.ourLog.entity.User;
import com.example.ourLog.repository.UserRepository;
import com.example.ourLog.security.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

  @Autowired
  private JWTUtil jwtUtil;

  @Autowired
  private UserRepository userRepository;

  public JwtHandshakeInterceptor(JWTUtil jwtUtil, UserRepository userRepository) {
    this.jwtUtil = jwtUtil;
    this.userRepository = userRepository;
  }

  @Override
  public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

    URI uri = request.getURI();
    String query = uri.getQuery();

    System.out.println("WebSocket handshake attempt - URI: " + uri);
    System.out.println("Query string: " + query);

    if (query != null && query.startsWith("token=")) {
      String token = query.substring("token=".length());

     System.out.println("Extracted token: {}" + token);


      String email = jwtUtil.validateAndExtract(token);

      System.out.println("Extracted email from token: {}"+ email);


      if (email != null) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        userOpt.ifPresent(user -> attributes.put("user", user));
        return userOpt.isPresent();
      }
    }
    System.out.println("WebSocket handshake failed - Invalid token or missing token");

    response.setStatusCode(HttpStatus.FORBIDDEN);
    return false;
  }

  @Override
  public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                             WebSocketHandler wsHandler, Exception exception) {}
}
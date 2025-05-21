package com.example.ourLog.config;

import com.example.ourLog.security.util.JWTUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

  private final JWTUtil jwtUtil;

  public JwtHandshakeInterceptor(JWTUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

    List<String> authHeaders = request.getHeaders().get("Authorization");
    String token = null;

    if (authHeaders != null && !authHeaders.isEmpty()) {
      token = authHeaders.get(0).replace("Bearer ", "");
    }

    if (token == null) {
      String query = request.getURI().getQuery(); // 예: ?token=abcd...
      System.out.println("Query string: " + query);

      if (query != null && query.contains("token=")) {
        for (String param : query.split("&")) {
          if (param.startsWith("token=")) {
            token = param.substring("token=".length());
            break;
          }
        }
      }
    }

    System.out.println("WebSocket 요청으로부터 추출한 토큰: " + token);

    if (token != null) {
      String username = jwtUtil.validateAndExtract(token);
      System.out.println("토큰 검증 결과 username: " + username);

      if (username != null) {
        // 사용자 정보를 attributes에 넣을 수 있음 (선택)
        attributes.put("username", username);
        return true;
      }
    }

    System.out.println("WebSocket 핸드셰이크 실패 - 유효하지 않은 토큰");
    return false;
  }


  @Override
  public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                             WebSocketHandler wsHandler, Exception exception) {
  }
}

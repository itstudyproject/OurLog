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

    // 헤더에서 Authorization 토큰 가져오기 (Bearer 토큰 형식)
    List<String> authHeaders = request.getHeaders().get("Authorization");
    String token = null;
    if (authHeaders != null && !authHeaders.isEmpty()) {
      token = authHeaders.get(0).replace("Bearer ", "");
    }

    // 토큰 없으면 쿼리 파라미터에서 가져올 수도 있음 (SockJS 등 헤더 전달 불가 시 대비)
    if (token == null) {
      String query = request.getURI().getQuery(); // ?token=...
      if (query != null && query.contains("token=")) {
        for (String param : query.split("&")) {
          if (param.startsWith("token=")) {
            token = param.substring("token=".length());
            break;
          }
        }
      }
    }

    // 토큰 검증
    if (token != null && jwtUtil.validateAndExtract(token) != null) {
      // 필요시 사용자 정보 attributes에 넣기 가능
      return true; // 인증 성공
    }

    // 인증 실패하면 연결 차단
    return false;
  }

  @Override
  public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                             WebSocketHandler wsHandler, Exception exception) {
  }
}

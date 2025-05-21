package com.example.ourLog.websocket;

import com.example.ourLog.dto.ChatMessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ChatWebSocketHandler extends TextWebSocketHandler {

  // 유저별 세션 관리 (username -> WebSocketSession)
  private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    String username = (String) session.getAttributes().get("username");
    String toUser = (String) session.getAttributes().get("to");

    System.out.println("WebSocket 연결됨 - from: " + username + ", to: " + toUser);

    if (username != null) {
      sessions.put(username, session);
    }
  }

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    // 클라이언트로부터 받은 메시지 JSON 파싱
    ChatMessageDTO chatMessage = objectMapper.readValue(message.getPayload(), ChatMessageDTO.class);

    String fromUser = (String) session.getAttributes().get("username");
    String toUser = chatMessage.getReceiver();
    System.out.printf("받은 메시지 from=%s to=%s message=%s\n", fromUser, toUser, chatMessage.getContent());

    WebSocketSession receiverSession = sessions.get(toUser);

    if (receiverSession != null && receiverSession.isOpen()) {
      // 받는 사람에게 메시지 보내기
      String jsonMsg = objectMapper.writeValueAsString(chatMessage);
      receiverSession.sendMessage(new TextMessage(jsonMsg));
    } else {
      System.out.println("받는 사람 세션이 없거나 닫힘");
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
    String username = (String) session.getAttributes().get("username");
    if (username != null) {
      sessions.remove(username);
    }
    System.out.println("WebSocket 연결 종료 - 사용자: " + username);
  }
}

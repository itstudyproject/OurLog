package com.example.ourLog.controller;
import com.example.ourLog.dto.ChatMessageDTO;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

  private final SimpMessagingTemplate messagingTemplate;

  public ChatController(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @MessageMapping("/chat/send") // 클라이언트에서 /app/chat/send 로 보냄
  public void sendMessage(@Payload ChatMessageDTO message) {
    // 특정 사용자에게 메시지 전송
    messagingTemplate.convertAndSend("/topic/messages/" + message.getReceiver(), message);
  }
}

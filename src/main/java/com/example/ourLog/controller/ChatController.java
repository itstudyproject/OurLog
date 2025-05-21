package com.example.ourLog.controller;
import com.example.ourLog.dto.ChatMessageDTO;
import com.example.ourLog.service.UserService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ChatController {

  private UserService userService;

  private final SimpMessagingTemplate messagingTemplate;

  public ChatController(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @MessageMapping("/chat/send")
  public void sendMessage(@Payload ChatMessageDTO message,
                          @Header("simpSessionAttributes") java.util.Map<String, Object> sessionAttributes) {
    String authenticatedUsername = (String) sessionAttributes.get("username");

    if (authenticatedUsername == null) {
      System.out.println("❌ 인증된 사용자 없음. 메시지 무시.");
      return;
    }

    // 보낸 사람을 실제 인증된 사용자로 강제 설정
    message.setSender(authenticatedUsername);

    // 수신자에게 메시지 전달
    messagingTemplate.convertAndSend("/topic/messages/" + message.getReceiver(), message);
  }

}

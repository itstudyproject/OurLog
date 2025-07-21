package com.example.ourLog.controller;

import com.example.ourLog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chatUser")
@RequiredArgsConstructor
public class ChatUserController {

  private final UserService userService;

  @GetMapping("/users")
  public ResponseEntity<List<String>> getChatUsers() {
    List<String> usernames = userService.getAllUsernames();
    return ResponseEntity.ok(usernames);
  }
}

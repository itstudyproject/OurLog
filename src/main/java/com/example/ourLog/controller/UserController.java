package com.example.ourLog.controller;

import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
  private final UserService userService;


  @PostMapping(value = "/register")
  public ResponseEntity<Long> register(@RequestBody UserDTO userDTO) {
    log.info("register 메소드 호출됨.....................");
    log.info("받은 유저 정보: {}", userDTO);
    
    try {
      Long userId = userService.registerUser(userDTO);
      log.info("회원가입 성공, 생성된 userId: {}", userId);
      return new ResponseEntity<>(userId, HttpStatus.OK);
    } catch (Exception e) {
      log.error("회원가입 중 컨트롤러에서 오류 발생: ", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

//  @GetMapping(value = "/get/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<UserDTO> read(@PathVariable("userId") Long userId) {
//    return new ResponseEntity<>(userService.getUser(userId), HttpStatus.OK);
//  }
  @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserDTO> get(String email) {
    return new ResponseEntity<>(userService.getUserByEmail(email), HttpStatus.OK);
  }

  @Transactional
  @DeleteMapping(value = "/delete/{userId}")
  public ResponseEntity<Void> delete(@PathVariable("userId") Long userId) {
    log.info("delete user userId: {}", userId);
    userService.deleteUser(userId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}

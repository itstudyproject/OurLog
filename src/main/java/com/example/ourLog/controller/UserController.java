package com.example.ourLog.controller;

import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.service.UserService;
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
    log.info("register.....................");
    return new ResponseEntity<>(userService.registerUser(userDTO), HttpStatus.OK);
  }

//  @GetMapping(value = "/get/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<UserDTO> read(@PathVariable("userId") Long userId) {
//    return new ResponseEntity<>(userService.getUser(userId), HttpStatus.OK);
//  }
  @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserDTO> get(String email, boolean fromSocial) {
    return new ResponseEntity<>(userService.getUserByEmail(email, fromSocial), HttpStatus.OK);
  }

  @DeleteMapping(value = "/delete/{userId}")
  public ResponseEntity<Long> delete(@RequestBody UserDTO userDTO) {
    log.info("delete user..........");
    return new ResponseEntity<>(userService.removeUser(userDTO.getUserId()), HttpStatus.OK);
  }


}

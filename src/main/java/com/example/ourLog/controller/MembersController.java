package com.example.ourLog.controller;

import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.service.MembersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/members")
public class MembersController {
  private final MembersService membersService;

  @PostMapping(value = "/register")
  public ResponseEntity<Long> register(@RequestBody UserDTO membersDTO) {
    log.info("register.....................");
    return new ResponseEntity<>(membersService.registerMembers(membersDTO), HttpStatus.OK);
  }

  @GetMapping(value = "/get/{mid}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserDTO> read(@PathVariable("mid") Long mid) {
    return new ResponseEntity<>(membersService.getMembers(mid), HttpStatus.OK);
  }
  @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserDTO> get(String email) {
    return new ResponseEntity<>(membersService.getMembersByEmail(email), HttpStatus.OK);
  }

}

package com.example.ourLog.controller;

import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.entity.User;
import com.example.ourLog.service.UserProfileService;
import com.example.ourLog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/profile")
public class UserProfileController {
  private final UserService userService;
  private final UserProfileService userProfileService;

//  // 사용자 등록
//  @PostMapping(value = "/register")
//  public ResponseEntity<Long> register(@RequestBody UserDTO userDTO) {
//    log.info("register.....................");
//    return new ResponseEntity<>(userService.registerUser(userDTO), HttpStatus.OK);
//  }

  // ID로 사용자 조회
//  @GetMapping(value = "/get/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<UserDTO> read(@PathVariable("userId") Long userId) {
//    return new ResponseEntity<>(userService.getUser(userId), HttpStatus.OK);
//  }

  // 이메일 + 소셜 여부로 사용자 조회
//  @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity<UserDTO> get(String email, boolean fromSocial) {
//    return new ResponseEntity<>(userService.getUserByEmail(email, fromSocial), HttpStatus.OK);
//  }


  // ✅ 프로필 생성
  @PostMapping("/create")
  public ResponseEntity<UserProfileDTO> createProfile(@RequestBody UserProfileDTO profileDTO) {
    log.info("create profile for userId: {}", profileDTO.getUser());
    UserProfileDTO created = userProfileService.createProfile(profileDTO);
    return ResponseEntity.ok(created);
  }

  // ✅ 유저 ID로 프로필 조회
  @GetMapping("/get/{userId}")
  public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Long userId) {
    log.info("get profile for userId: {}", userId);

    UserProfileDTO profile = userProfileService.getProfileById(userId);
    return ResponseEntity.ok(profile);
  }


  // ✅ 모든 프로필 조회
  @GetMapping("/profiles")
  public ResponseEntity<List<UserProfileDTO>> getAllProfiles() {
    log.info("get all profiles");
    List<UserProfileDTO> profiles = userProfileService.getAllProfiles();
    return ResponseEntity.ok(profiles);
  }

  // ✅ 프로필 수정
  @PutMapping("/edit/{userId}")
  public ResponseEntity<UserProfileDTO> updateProfile(
      @PathVariable User user,
      @RequestBody UserProfileDTO profileDTO
  ) {
    log.info("update profile for userId: {}", user);
    UserProfileDTO updated = userProfileService.updateProfile(user, profileDTO);
    return ResponseEntity.ok(updated);
  }

  // ✅ 프로필 삭제
  @DeleteMapping("/delete/{userId}")
  public ResponseEntity<Void> deleteProfile(@PathVariable User user) {
    log.info("delete profile for userId: {}", user);
    userProfileService.deleteProfile(user);
    return ResponseEntity.noContent().build();
  }
}
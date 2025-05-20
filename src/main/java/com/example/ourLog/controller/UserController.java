package com.example.ourLog.controller;

import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.dto.UserRegisterDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.security.dto.UserAuthDTO;
import com.example.ourLog.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

import java.util.List;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
  private final UserService userService;


  @PostMapping(value = "/register")
  public ResponseEntity<?> register(@Valid @RequestBody UserRegisterDTO userRegisterDTO, BindingResult bindingResult) {
    log.info("register 메소드 호출됨.....................");
    log.info("받은 유저 정보: {}", userRegisterDTO);
    
    // 유효성 검사 오류가 있는 경우
    if (bindingResult.hasErrors()) {
      Map<String, String> errorMap = new HashMap<>();
      
      // 각 필드별 오류 메시지 수집
      for (FieldError error : bindingResult.getFieldErrors()) {
        errorMap.put(error.getField(), error.getDefaultMessage());
      }
      
      log.warn("유효성 검사 오류: {}", errorMap);
      return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
    }
    
    // 비밀번호와 비밀번호 확인이 일치하지 않는 경우
    if (!userRegisterDTO.isPasswordMatching()) {
      Map<String, String> errorMap = new HashMap<>();
      errorMap.put("passwordConfirm", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
      
      log.warn("비밀번호 불일치 오류");
      return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
    }
    
    try {
      // 중복 검사 (이메일, 닉네임, 전화번호)
      Map<String, String> duplicateResult = userService.checkDuplication(userRegisterDTO);
      if (!duplicateResult.isEmpty()) {
        log.warn("중복 검사 오류: {}", duplicateResult);
        return new ResponseEntity<>(duplicateResult, HttpStatus.BAD_REQUEST);
      }
      
      // 회원가입 진행
      Long userId = userService.registerUser(userRegisterDTO);
      log.info("회원가입 성공, 생성된 userId: {}", userId);
      return new ResponseEntity<>(userId, HttpStatus.OK);
    } catch (Exception e) {
      log.error("회원가입 중 컨트롤러에서 오류 발생: ", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
  // 이메일 중복 확인 API
  @GetMapping("/check/email")
  public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
    log.info("이메일 중복 확인: {}", email);
    
    boolean exists = userService.isEmailExists(email);
    log.info("이메일 '{}' 존재 여부: {}", email, exists);
    
    return ResponseEntity.ok(exists);
  }
  
  // 닉네임 중복 확인 API
  @GetMapping("/check/nickname") 
  public ResponseEntity<Boolean> checkNicknameExists(@RequestParam String nickname) {
    log.info("닉네임 중복 확인: {}", nickname);
    
    boolean exists = userService.isNicknameExists(nickname);
    log.info("닉네임 '{}' 존재 여부: {}", nickname, exists);
    
    return ResponseEntity.ok(exists);
  }
  
  // 전화번호 중복 확인 API
  @GetMapping("/check/mobile")
  public ResponseEntity<Boolean> checkMobileExists(@RequestParam String mobile) {
    log.info("전화번호 중복 확인: {}", mobile);
    
    boolean exists = userService.isMobileExists(mobile);
    log.info("전화번호 '{}' 존재 여부: {}", mobile, exists);
    
    return ResponseEntity.ok(exists);
  }

  @GetMapping(value = "/getUser/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserDTO> read(@PathVariable("userId") Long userId) {
    return new ResponseEntity<>(userService.getUser(userId), HttpStatus.OK);
  }


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

  @GetMapping("/check-admin")
  public ResponseEntity<?> checkAdminStatus(@AuthenticationPrincipal UserAuthDTO user) {
    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
    }

    log.info("현재 사용자 authorities: {}", user.getAuthorities());
    log.info("현재 사용자 isAdmin 여부: {}", user.isAdmin());

    return ResponseEntity.ok().body(Map.of("isAdmin", user.isAdmin()));
  }
}

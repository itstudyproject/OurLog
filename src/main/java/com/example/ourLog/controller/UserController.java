package com.example.ourLog.controller;

import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.dto.UserRegisterDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.security.dto.UserAuthDTO;
import com.example.ourLog.service.UserService;
import com.example.ourLog.service.social_login.OauthService;
import com.example.ourLog.service.social_login.SocialOauthService;
import com.example.ourLog.util.SocialLoginType;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.example.ourLog.security.util.JWTUtil;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
  private final UserService userService;
  private final OauthService oauthService;
  private final JWTUtil jwtUtil;


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
      Map<String, Object> result = new HashMap<>();
      result.put("userId", userId);
      return new ResponseEntity<>(result, HttpStatus.OK);

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

  @GetMapping(value = "/{socialLoginType}/callback")
  public ResponseEntity<?> callback(@PathVariable(name = "socialLoginType") SocialLoginType socialLoginType,
                                    @RequestParam(name = "code", required = false) String code,
                                    @RequestParam(name = "error", required = false) String error) {
    log.info(">> 소셜 로그인 API 서버로부터 받은 code :: {}", code);
    log.info(">> 소셜 로그인 API 서버로부터 받은 error :: {}", error);

    // 사용자가 로그인을 취소한 경우
    if (error != null && error.equals("access_denied")) {
      log.warn("소셜 로그인 취소됨");
      // 로그인 페이지로 리다이렉트
      HttpHeaders headers = new HttpHeaders();
      headers.setLocation(URI.create("http://localhost:5173/login"));
      return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    // 오류 발생 시 처리 (access_denied 외 다른 오류 포함)
    if (code == null || code.isEmpty()) {
       log.error("소셜 로그인 콜백에 code가 누락되었습니다.");
       // 오류 메시지와 함께 응답
       return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("소셜 로그인 코드 누락");
    }

    try {
      // 인증 코드로 액세스 토큰 및 사용자 정보 가져오기
      Map<String, Object> userInfo = oauthService.processSocialLogin(socialLoginType, code);

      if (userInfo != null) {
        // 사용자 정보 기반으로 회원가입 또는 로그인 처리
        UserDTO userDTO = userService.processSocialLoginUser(socialLoginType, userInfo);

        try {
          // JWT 토큰 발행
          String token = jwtUtil.generateToken(userDTO.getEmail());

          // 토큰을 쿼리 파라미터로 포함하여 메인 페이지로 리다이렉트
          URI location = UriComponentsBuilder.fromUriString("http://localhost:5173/social-login-handler")
                  .queryParam("token", token)
                  .queryParam("email", userDTO.getEmail())
                  .build().toUri();

          HttpHeaders headers = new HttpHeaders();
          headers.setLocation(location);
          // 302 Found 대신 303 See Other를 사용하여 GET 요청으로 리다이렉트되도록 권장
          return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);

        } catch (Exception e) {
          log.error("JWT 토큰 발행 및 리다이렉트 중 오류 발생: ", e);
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 처리 중 오류 발생");
        }

      } else {
        log.error("소셜 로그인 사용자 정보 가져오기 실패");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("소셜 로그인 처리 실패");
      }
    } catch (Exception e) {
      log.error("소셜 로그인 콜백 처리 중 오류 발생: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("소셜 로그인 처리 중 오류 발생");
    }
  }


}

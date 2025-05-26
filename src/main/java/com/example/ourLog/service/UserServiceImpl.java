package com.example.ourLog.service;


import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.dto.UserRegisterDTO;
import com.example.ourLog.entity.User;
import com.example.ourLog.entity.UserRole;
import com.example.ourLog.repository.UserRepository;
import com.example.ourLog.util.SocialLoginType;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.HashSet;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final HttpServletResponse response;

  @Override
  public UserDTO getUser(Long userId) {
    Optional<User> result = userRepository.findByUserId(userId);
    if (result.isPresent()) return entityToDTO(result.get());
    return null;
  }

  @Override
  public User findByUserId(Long userId) {
    Optional<User> result = userRepository.findByUserId(userId);
    return result.orElse(null);
  }

  @Override
  public UserDTO getUserByEmail(String email) {
    Optional<User> result = userRepository.findByEmail(email);
    if (result.isPresent()) return entityToDTO(result.get());
    return null;
  }

  @Override
  public User findByUserName(String username) {
    Optional<User> result = userRepository.findByName(username);
    return result.orElse(null);
  }

  @Override
  public void deleteUser(Long userId) {
    userRepository.deleteByUserId(userId); // 가급적 사용하지 말라.
  }

  @Override
  public Long updateUser(UserDTO userDTO) {
    User user = userRepository.findByUserId(userDTO.getUserId())
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id=" + userDTO.getUserId()));

    // 비밀번호 수정 요청이 들어온 경우
    if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
      // 비밀번호는 반드시 인코딩해서 저장
      String encoded = passwordEncoder.encode(userDTO.getPassword());
      user.setPassword(encoded);
    }

    // 전화번호 수정 요청이 들어온 경우
    if (userDTO.getMobile() != null && !userDTO.getMobile().isBlank()) {
      user.setMobile(userDTO.getMobile());
    }

    userRepository.save(user);
    return user.getUserId();
  }


  @Override
  public Map<String, String> checkDuplication(UserDTO userDTO) {
    Map<String, String> result = new HashMap<>();

    // 이메일 중복 체크
    if (userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) {
      Optional<User> emailCheck = userRepository.findByEmail(userDTO.getEmail());
      if (emailCheck.isPresent()) {
        result.put("email", "이미 사용 중인 이메일입니다.");
      }
    }

    // 닉네임 중복 체크
    if (userDTO.getNickname() != null && !userDTO.getNickname().isEmpty()) {
      Optional<User> nicknameCheck = userRepository.findByNickname(userDTO.getNickname());
      if (nicknameCheck.isPresent()) {
        result.put("nickname", "이미 사용 중인 닉네임입니다.");
      }
    }

    // 전화번호 중복 체크
    if (userDTO.getMobile() != null && !userDTO.getMobile().isEmpty()) {
      Optional<User> mobileCheck = userRepository.findByMobile(userDTO.getMobile());
      if (mobileCheck.isPresent()) {
        result.put("mobile", "이미 사용 중인 전화번호입니다.");
      }
    }

    return result;
  }

  @Override
  public boolean isEmailExists(String email) {
    log.info("이메일 중복 확인: {}", email);

    if (email == null || email.isEmpty()) {
      return false;
    }

    Optional<User> userOptional = userRepository.findByEmail(email);
    return userOptional.isPresent();
  }

  @Override
  public boolean isNicknameExists(String nickname) {
    log.info("닉네임 중복 확인: {}", nickname);

    if (nickname == null || nickname.isEmpty()) {
      return false;
    }

    Optional<User> userOptional = userRepository.findByNickname(nickname);
    return userOptional.isPresent();
  }

  @Override
  public boolean isMobileExists(String mobile) {
    log.info("전화번호 중복 확인: {}", mobile);

    if (mobile == null || mobile.isEmpty()) {
      return false;
    }

    Optional<User> userOptional = userRepository.findByMobile(mobile);
    return userOptional.isPresent();
  }

  @Override
  public Long registerUser(UserRegisterDTO userRegisterDTO) {
    log.info("UserRegisterDTO를 이용한 registerUser 시작: {}", userRegisterDTO);

    // UserDTO로 변환하여 기존 메서드 재사용
    UserDTO userDTO = new UserDTO();
    userDTO.setEmail(userRegisterDTO.getEmail());
    userDTO.setPassword(userRegisterDTO.getPassword());
    userDTO.setName(userRegisterDTO.getName());
    userDTO.setNickname(userRegisterDTO.getNickname());
    userDTO.setMobile(userRegisterDTO.getMobile());
    userDTO.setFromSocial(false);
    userDTO.setRoleSet(userRegisterDTO.getRoleSet());

    return registerUser(userDTO);
  }

  @Override
  public Long registerUser(UserDTO userDTO) {
    log.info("registerUser 시작: {}", userDTO);

    try {
      // NullPointerException 방지
      if (userDTO.getRoleSet() == null) {
        log.info("roleSet이 null입니다. 새 HashSet을 생성합니다.");
        userDTO.setRoleSet(new HashSet<>());
      }

      userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));

      // roleSet이 비어있으면 기본 USER 권한 추가
      if (userDTO.getRoleSet().isEmpty()) {
        log.info("roleSet이 비어있습니다. ROLE_USER 추가");
        userDTO.getRoleSet().add("ROLE_USER");
      }

      User user = dtoToEntity(userDTO);
      log.info("엔티티 변환 완료: {}", user);

      User savedUser = userRepository.save(user);
      log.info("사용자 저장 완료: {}", savedUser);

      return savedUser.getUserId();
    } catch (Exception e) {
      log.error("회원가입 중 오류 발생: ", e);
      throw e;
    }
  }

  @Override
  public List<String> getAllUsernames() {
    return userRepository.findAll()
            .stream()
            .map(User::getNickname) // 또는 getEmail 등 원하는 필드
            .collect(Collectors.toList());
  }

  @Override
  public UserDTO processSocialLoginUser(SocialLoginType socialLoginType, Map<String, Object> userInfo) {
    String email = (String) userInfo.get("email");
    String name = (String) userInfo.get("name");
    String socialId = (String) userInfo.get("sub"); // Google provides 'sub' as unique ID

    // 이메일로 기존 사용자 찾기
    Optional<User> result = userRepository.findByEmail(email);

    // 기존 사용자가 아닌 경우 회원가입
    if (result.isEmpty()) {
      User socialUser = User.builder()
              .email(email)
              .password(passwordEncoder.encode(UUID.randomUUID().toString())) // 소셜 로그인은 비밀번호 불필요, 임의 생성
              .nickname(socialLoginType.name() + "_" + socialId) // 예시: GOOGLE_12345...
              .name(name != null ? name : socialLoginType.name() + " User")
              // 전화번호 컬럼은 중복 불가이므로 유동적인 기본값 설정
              .mobile(socialLoginType.name() + "_010_" + socialId) // 예시: GOOGLE_12345..._mobile
              .fromSocial(true)
              .roleSet(new HashSet<>(Collections.singletonList(UserRole.USER)))
              .build();
      userRepository.save(socialUser);
      log.info("새 소셜 사용자 회원가입 완료: {}", email);
      return entityToDTO(socialUser);
    } else {
      // 기존 사용자인 경우 로그인 (여기서는 사용자 정보 반환)
      User existingUser = result.get();
      log.info("기존 소셜 사용자 로그인: {}", email);
      return entityToDTO(existingUser);
    }
  }
}
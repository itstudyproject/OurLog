package com.example.ourLog.service;


import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.dto.UserRegisterDTO;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.HashSet;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

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
  public void deleteUser(Long userId) {
    userRepository.deleteByUserId(userId); // 가급적 사용하지 말라.
  }

  @Override
  public Long updateUser(UserDTO userDTO) {
    Optional<User> result = userRepository.findByUserId(userDTO.getUserId());
    if (result.isPresent()) {
      User user = result.get();
      /* 변경할 내용은 user에 userDTO의 내용을 변경하시오 */
      return userRepository.save(user).getUserId();
    }
    return 0L;
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
}
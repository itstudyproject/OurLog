package com.example.ourLog.service;


import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
    return null;
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
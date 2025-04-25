package com.example.ourLog.service;


import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
 // private final PasswordEncoder passwordEncoder;

  @Override
  public UserDTO getUser(Long userId) {
    Optional<User> result = userRepository.findByUserId(userId);
    if (result.isPresent()) return entityToDTO(result.get());
    return null;
  }

  @Override
  public UserDTO getUserByEmail(String email, boolean fromSocial) {
    Optional<User> result = userRepository.findByEmail(email, fromSocial);
    if (result.isPresent()) return entityToDTO(result.get());
    return null;
  }

  @Override
  public void removeUser(Long userId) {
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

//  @Override
//  public Long registerUser(UserDTO userDTO) {
//    userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
//    return userRepository.save(dtoToEntity(userDTO)).getUserId();
//  }

}
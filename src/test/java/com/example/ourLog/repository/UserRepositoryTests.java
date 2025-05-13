package com.example.ourLog.repository;

import com.example.ourLog.entity.User;
import com.example.ourLog.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class UserRepositoryTests {

  @Autowired
  UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  public void insertUser() {
    IntStream.rangeClosed(1, 100).forEach(i -> {
      // 첫 번째 사용자만 ADMIN, 나머지 사용자들은 USER 역할
      UserRole role = (i == 1) ? UserRole.ADMIN : UserRole.USER;

      // User 객체 생성
      User user = User.builder()
              .email("r" + i + "@r.r")
              .password(passwordEncoder.encode("1"))  // 비밀번호 인코딩
              .name("name" + i)
              .mobile("010-1111-1" + return3Digit(i))
              .nickname("reviewer" + i)
              .build();

      // 역할 추가
      user.addMemberRole(role);  // UserRole을 roleSet에 추가

      // 사용자 저장
      userRepository.save(user);
    });
  }

  private String return3Digit(int i) {
    return (i < 10) ? "00" + i : (i < 100) ? "0" + i : "" + i;
  }


//  @Transactional
//  @Test
//  public void testFindByEmail() {
//    Optional<User> result = userRepository.findByEmail("r1@r.r", false);
//    if (result.isPresent()) {
//      System.out.println(result.get());
//    }
//
//  }

}
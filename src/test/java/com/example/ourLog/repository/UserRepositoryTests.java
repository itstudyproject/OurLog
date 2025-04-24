package com.example.ourLog.repository;

import com.example.ourLog.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class UserRepositoryTests {

  @Autowired
  UserRepository userRepository;

  @Test
  public void insertUser() {
    IntStream.rangeClosed(1, 100).forEach(i->{
      User user = User.builder()
              .email("r"+i+"@r.r")
              .password("1")
              .name("name" + i)
              .nickname("reviewer" + i)
              .build();
      userRepository.save(user);
    });
  }

}
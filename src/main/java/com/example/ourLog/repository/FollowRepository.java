package com.example.ourLog.repository;

import com.example.ourLog.entity.Follow;
import com.example.ourLog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

  boolean existsByFromUserAndToUser(User fromUser, User toUser);

  void deleteByFromUserAndToUser(User fromUser, User toUser);

  List<Follow> findAllByFromUser(User fromUser);

  List<Follow> findAllByToUser(User toUser);
}

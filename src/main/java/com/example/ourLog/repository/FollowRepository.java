package com.example.ourLog.repository;

import com.example.ourLog.entity.Follow;
import com.example.ourLog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

  @Query("select case when count(fo) > 0 " +
          "then true else false end from Follow fo " +
          "where fo.fromUser = :fromUser and fo.toUser = :toUser")
  boolean existsByFromUserAndToUser(User fromUser, User toUser);

  @Query("delete from Follow fo where fo.fromUser = :fromUser " +
          "and fo.toUser = :toUser")
  void deleteByFromUserAndToUser(User fromUser, User toUser);

  @Query("select fo from Follow fo where fo.fromUser = :fromUser")
  List<Follow> findAllByFromUser(User fromUser);

  @Query("select fo from Follow fo where fo.toUser = :toUser")
  List<Follow> findAllByToUser(User toUser);
}

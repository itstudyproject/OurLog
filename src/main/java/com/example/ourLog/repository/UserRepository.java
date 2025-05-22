package com.example.ourLog.repository;

import com.example.ourLog.entity.User;

import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User,Long> {
  @EntityGraph(attributePaths = {"roleSet"}, type = EntityGraph.EntityGraphType.LOAD)
  @Query("select u from User u where u.email = :email")
  Optional<User> findByEmail(@Param("email") String email);

//  @EntityGraph(attributePaths = {"roleSet"}, type = EntityGraph.EntityGraphType.LOAD)
//  @Query("select u from User u where u.email = :email")
//  Optional<User> findByEmail(@Param("email") String email);

  @EntityGraph(attributePaths = {"roleSet"}, type = EntityGraph.EntityGraphType.LOAD)
  @Query("select u from User u where u.userId = :userId")
  Optional<User> findByUserId(@Param("userId") Long userId);

  @Modifying
  @Query("delete from User u where u.userId = :userId")
  void deleteByUserId(@Param("userId") Long userId);

  Optional<User> findByNickname(String nickname);

  Optional<User> findByMobile(String mobile);

  Optional<User> findByName(String username);
}
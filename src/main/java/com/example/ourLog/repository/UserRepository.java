package com.example.ourLog.repository;

import com.example.ourLog.entity.User;

import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User,String> {
  @EntityGraph(attributePaths = {"roleSet"}, type = EntityGraph.EntityGraphType.LOAD)
  @Query("select u from User u where u.fromSocial = :fromSocial and u.email = :email")
  Optional<User> findByEmail(@Param("email") String email, @Param("fromSocial") boolean fromSocial);

  @EntityGraph(attributePaths = {"roleSet"}, type = EntityGraph.EntityGraphType.LOAD)
  @Query("select u from User u where u.email = :email")
  Optional<User> findByEmail(@Param("email") String email);

}

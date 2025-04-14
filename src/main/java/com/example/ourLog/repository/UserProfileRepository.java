package com.example.ourLog.repository;

import com.example.ourLog.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

  // User의 ID로 프로필을 찾기 위한 메서드
  @Query("select up from UserProfile up where up.userId = :userId and up.nickname = :nickname ")
  Optional<UserProfile> findByProfileId(Long userId); 
}

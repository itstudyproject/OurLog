package com.example.ourLog.repository;

import com.example.ourLog.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

  // User의 ID로 프로필을 찾기 위한 메서드
  Optional<UserProfile> findByProfileId_Id(Long userId);
}

package com.example.ourLog.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Table(name = "user")

public class User extends BaseEntity{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userId;

  private String email;
  private String password;
  private String name;
  private String nickname;
  private String mobile;
  private boolean fromSocial; // 구글 하나만 사용 할 예정

  @ElementCollection(fetch = FetchType.LAZY)
  @Builder.Default
  private Set<UserRole> roleSet = new HashSet<>();

  public void addMemberRole(UserRole userRole) {
    roleSet.add(userRole);
  }

  public boolean isAdmin() {
    return roleSet.contains(UserRole.ADMIN); // roleSet에 ADMIN 권한이 포함되어 있으면 true 반환
  }

}
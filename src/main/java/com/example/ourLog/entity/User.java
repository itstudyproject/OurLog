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

  @Column(unique = true)
  private String email;
  private String password;
  private String name;

  @Column(unique = true)
  private String nickname;

  @Column(unique = true)
  private String mobile;
  private boolean fromSocial; // 구글 하나만 사용 할 예정

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  private UserProfile userProfile;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  private Reply reply;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  private Post post;

  @ElementCollection(fetch = FetchType.LAZY)
  @Builder.Default
  private Set<UserRole> roleSet = new HashSet<>();

  public void addMemberRole(UserRole userRole) {
    roleSet.add(userRole);
  }

  public boolean isAdmin() {
    return roleSet.contains(UserRole.ADMIN); // roleSet에 ADMIN 권한이 포함되어 있으면 true 반환
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
    if (this.userProfile != null) {
      this.userProfile.setNickname(nickname);
    }
    if (this.post != null) {
      this.post.setNickname(nickname);
    }
  }

  public void setEmail(String email) {
    this.email = email;
    if (this.reply != null) {
      this.reply.setEmail(email);
    }
    if (this.userProfile != null) {
      this.userProfile.setEmail(email);
    }
  }
}
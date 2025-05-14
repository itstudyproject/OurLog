package com.example.ourLog.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

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

  private String password;
  private String name;

  @Column(unique = true)
  private String email;

  @Column(unique = true)
  private String nickname;

  @Column(unique = true)
  private String mobile;

  @OneToMany(mappedBy = "fromUser", fetch = FetchType.LAZY)
  @JsonIgnore
  private Set<Follow> following;

  @OneToMany(mappedBy = "toUser", fetch = FetchType.LAZY)
  @JsonIgnore
  private Set<Follow> followers;

  @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
  private UserProfile userProfile;


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
package com.example.ourLog.security.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
@Setter
@ToString
public class UserAuthDTO extends User implements OAuth2User {

  private Long userId;
  private String email;
  private String name;       // 이름 또는 닉네임
  private String nickname;   // ✅ 닉네임 추가
  private boolean fromSocial;
  private Map<String, Object> attr;

  // 일반 로그인용 생성자
  public UserAuthDTO(String username, String password,
                     Collection<? extends GrantedAuthority> authorities,
                     String email, String name, String nickname, boolean fromSocial, Long userId) {
    super(username, password, authorities);
    this.email = email;
    this.name = name;
    this.nickname = nickname;  // nickname 초기화
    this.fromSocial = fromSocial;
    this.userId = userId;
  }

  // 소셜 로그인용 생성자
  public UserAuthDTO(String username, String password,
                     boolean fromSocial,
                     Collection<? extends GrantedAuthority> authorities,
                     Map<String, Object> attr,
                     Long userId,
                     String nickname) {
    this(username, password, authorities, username, username, nickname, fromSocial, userId);
    this.attr = attr;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return this.attr;
  }

  @Override
  public String getName() {
    return this.name;
  }

  // ✅ 여기에 isAdmin() 추가
  public boolean isAdmin() {
    return getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
  }

}

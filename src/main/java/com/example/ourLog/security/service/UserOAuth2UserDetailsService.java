package com.example.ourLog.security.service;

import com.example.ourLog.entity.User;
import com.example.ourLog.entity.UserRole;
import com.example.ourLog.repository.UserRepository;
import com.example.ourLog.security.dto.UserAuthDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserOAuth2UserDetailsService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    OAuth2User oAuth2User = delegate.loadUser(userRequest);

    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    SocialType socialType = getSocialType(registrationId.trim());
    String userNameAttributeName = userRequest.getClientRegistration()
            .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

    log.info("userNameAttributeName >> " + userNameAttributeName);

    Map<String, Object> attributes = oAuth2User.getAttributes();
    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
      System.out.println(entry.getKey() + ":" + entry.getValue());
    }

    String email = null;
    if (socialType == SocialType.GOOGLE) {
      email = oAuth2User.getAttribute("email");
    }

    log.info("Email: " + email);

    User user = saveSocialMember(email);

    UserAuthDTO membersAuthDTO = new UserAuthDTO(
            user.getEmail(),                      // username
            user.getPassword(),                   // password
            user.isFromSocial(),                  // fromSocial
            user.getRoleSet().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                    .collect(Collectors.toList()),    // authorities
            attributes,                           // 소셜에서 받은 정보
            user.getUserId(),                     // userId
            user.getNickname()                    // nickname ✅ 추가됨
    );

    membersAuthDTO.setFromSocial(user.isFromSocial());
    membersAuthDTO.setName(user.getName());

    log.info("membersAuthDTO: " + membersAuthDTO);
    return membersAuthDTO;
  }

  /**
   * 이미 가입된 이메일이면 그대로 리턴,
   * 없으면 새로 소셜 계정으로 유저를 생성
   */
  private User saveSocialMember(String email) {
    Optional<User> result = userRepository.findByEmail(email);
    if (result.isPresent()) return result.get();

    // 소셜 로그인 유저 최초 등록
    User user = User.builder()
            .email(email)
            .password(passwordEncoder.encode("1")) // 더미 비밀번호
            .nickname("user_" + UUID.randomUUID().toString().substring(0, 8)) // 임시 닉네임
            .fromSocial(true)
            .build();
    user.addMemberRole(UserRole.USER);

    userRepository.save(user);
    return user;
  }

  private SocialType getSocialType(String registrationId) {
    if (SocialType.NAVER.name().equalsIgnoreCase(registrationId)) {
      return SocialType.NAVER;
    }
    if (SocialType.KAKAO.name().equalsIgnoreCase(registrationId)) {
      return SocialType.KAKAO;
    }
    return SocialType.GOOGLE;
  }

  enum SocialType {
    KAKAO, NAVER, GOOGLE
  }
}

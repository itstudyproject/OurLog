package com.example.ourLog.service.social_login;

import com.example.ourLog.util.SocialLoginType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OauthService {
  private final List<SocialOauthService> socialOauthList;
  private final HttpServletResponse response;

  public void request(SocialLoginType socialLoginType) {
    SocialOauthService socialOauth = this.findSocialOauthByType(socialLoginType);
    String redirectURL = socialOauth.getOauthRedirectURL();
    try {
      response.sendRedirect(redirectURL);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String requestAccessToken(SocialLoginType socialLoginType, String code) {
    SocialOauthService socialOauth = this.findSocialOauthByType(socialLoginType);
    
    return socialOauth.requestAccessToken(code);
  }

  public Map<String, Object> processSocialLogin(SocialLoginType socialLoginType, String code) {
    SocialOauthService socialOauth = this.findSocialOauthByType(socialLoginType);
    String accessToken = socialOauth.requestAccessToken(code);
    return socialOauth.getUserInfo(accessToken);
  }

  private SocialOauthService findSocialOauthByType(SocialLoginType socialLoginType) {
    return socialOauthList.stream()
            .filter(x -> x.type() == socialLoginType)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("알 수 없는 SocialLoginType 입니다."));
  }
}

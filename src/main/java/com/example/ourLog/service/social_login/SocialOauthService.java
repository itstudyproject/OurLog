package com.example.ourLog.service.social_login;

import com.example.ourLog.util.SocialLoginType;

import java.util.Map;

public interface SocialOauthService {
  String getOauthRedirectURL();
  String requestAccessToken(String code);
  Map<String, Object> getUserInfo(String accessToken);

  default SocialLoginType type() {
    if (this instanceof GoogleOauth) {
      return SocialLoginType.GOOGLE;
    } else if (this instanceof NaverOauth) {
      return SocialLoginType.NAVER;
    } else if (this instanceof KakaoOauth) {
      return SocialLoginType.KAKAO;
    } else {
      return null;
    }
  }
}
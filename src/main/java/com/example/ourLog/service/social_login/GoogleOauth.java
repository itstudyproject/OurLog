package com.example.ourLog.service.social_login;

import com.example.ourLog.util.SocialLoginType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GoogleOauth implements SocialOauthService {
  @Value("${sns.google.url}")
  private String GOOGLE_SNS_BASE_URL;
  @Value("${sns.google.client.id}")
  private String GOOGLE_SNS_CLIENT_ID;
  @Value("${sns.google.callback.url}")
  private String GOOGLE_SNS_CALLBACK_URL;
  @Value("${sns.google.client.secret}")
  private String GOOGLE_SNS_CLIENT_SECRET;
  @Value("${sns.google.token.url}")
  private String GOOGLE_SNS_TOKEN_BASE_URL;

  private final ObjectMapper objectMapper;

  @Override
  public String getOauthRedirectURL() {
    Map<String, Object> params = new HashMap<>();
    params.put("scope", "profile");
    params.put("response_type", "code");
    params.put("client_id", GOOGLE_SNS_CLIENT_ID);
    params.put("redirect_uri", GOOGLE_SNS_CALLBACK_URL);

    String parameterString = params.entrySet().stream()
            .map(x -> x.getKey() + "=" + x.getValue())
            .collect(Collectors.joining("&"));

    return GOOGLE_SNS_BASE_URL + "?" + parameterString;
  }

  @Override
  public String requestAccessToken(String code) {
    RestTemplate restTemplate = new RestTemplate();

    Map<String, Object> params = new HashMap<>();
    params.put("code", code);
    params.put("client_id", GOOGLE_SNS_CLIENT_ID);
    params.put("client_secret", GOOGLE_SNS_CLIENT_SECRET);
    params.put("redirect_uri", GOOGLE_SNS_CALLBACK_URL);
    params.put("grant_type", "authorization_code");

    ResponseEntity<String> responseEntity = restTemplate.postForEntity(GOOGLE_SNS_TOKEN_BASE_URL, params, String.class);

    if (responseEntity.getStatusCode() == HttpStatus.OK) {
      try {
        JsonNode root = objectMapper.readTree(responseEntity.getBody());
        return root.get("access_token").asText();
      } catch (Exception e) {
        e.printStackTrace();
        return "액세스 토큰 파싱 실패";
      }
    }
    return "구글 로그인 요청 처리 실패";
  }

  @Override
  public Map<String, Object> getUserInfo(String accessToken) {
    RestTemplate restTemplate = new RestTemplate();
    String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + accessToken);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<String> responseEntity = restTemplate.exchange(
              userInfoUrl, HttpMethod.GET, request, String.class);

      if (responseEntity.getStatusCode() == HttpStatus.OK) {
        return objectMapper.readValue(responseEntity.getBody(), Map.class);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}

// src/main/java/com/example/ourLog/service/SendbirdApiService.java
package com.example.ourLog.service;

import com.example.ourLog.entity.User; // 필요하다면 User 엔티티 임포트
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper; // ObjectMapper 임포트 필요

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SendBirdApiService {

  private final WebClient webClient;
  private final String sendbirdApiToken;
  private final String sendbirdAppId;
  private final ObjectMapper objectMapper = new ObjectMapper(); // ObjectMapper 인스턴스 생성

  public SendBirdApiService(
          @Value("${sendbird.app-id}") String sendbirdAppId,
          @Value("${sendbird.api-token}") String sendbirdApiToken) {
    this.sendbirdAppId = sendbirdAppId;
    this.sendbirdApiToken = sendbirdApiToken;
    this.webClient = WebClient.builder()
            .baseUrl("https://api-" + sendbirdAppId + ".sendbird.com/v3")
            .defaultHeader("Api-Token", sendbirdApiToken)
//            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
  }

  // Sendbird User 생성 또는 업데이트
  // https://sendbird.com/docs/chat/platform-api/v3/user/managing-users/create-a-user
  // https://sendbird.com/docs/chat/platform-api/v3/user/managing-users/update-a-user
  public Mono<Map> createOrUpdateUser(User user) { // 또는 UserDTO를 받을 수 있습니다.
    String userId = String.valueOf(user.getUserId()); // User ID를 String으로 변환 (Sendbird User ID는 String)
    // Sendbird User 정보 구성
    Map<String, Object> body = new HashMap<>();
    body.put("user_id", userId);
    body.put("nickname", user.getNickname());
    // TODO: 실제 프로필 이미지 URL을 UserProfile에서 가져와서 설정
    // User 엔티티에 UserProfile 정보가 포함되어 있지 않다면, UserProfileService를 주입받거나 인자로 받아야 합니다.
    // 임시로 기본 이미지 또는 UserProfileService를 통해 가져온 이미지 사용 예시
    // UserProfile userProfile = userProfileService.getProfileByUserId(user.getUserId());
    // body.put("profile_url", userProfile != null ? userProfile.getThumbnailImagePath() : "YOUR_DEFAULT_PROFILE_IMAGE_URL");
    body.put("profile_url", "YOUR_DEFAULT_PROFILE_IMAGE_URL"); // TODO: 실제 URL로 변경

    // Upsert (생성 또는 업데이트) 요청
    return webClient.put()
            .uri("/users/" + userId)
            .body(BodyInserters.fromValue(body))
            .retrieve()
            // 수정: HttpStatus::isError 대신 람다 표현식 사용
            .onStatus(status -> status.isError(), response ->
                    response.bodyToMono(String.class).flatMap(errorBody -> {
                      log.error("Sendbird API Error - Create/Update User {}: {}", userId, errorBody);
                      return Mono.error(new RuntimeException("Sendbird API Error: " + errorBody));
                    }))
            .bodyToMono(Map.class);
  }

  // Sendbird Access Token 생성
  // https://sendbird.com/docs/chat/platform-api/v3/user/managing-users/issue-access-token
  public Mono<Map<String, Object>> issueAccessToken(Long userId) {
    String sendbirdUserId = String.valueOf(userId);
    // Access Token 발급을 위한 요청 본문 생성
    Map<String, Boolean> body = new HashMap<>();
    body.put("issue_access_token", true); // Access Token 발급 요청 플래그

    try {
        // 전송될 JSON 본문 로깅
        String jsonBody = objectMapper.writeValueAsString(body);
        log.info("Sendbird API Request Body - Issue Access Token for {}: {}", sendbirdUserId, jsonBody);
    } catch (Exception e) {
        log.error("Error converting body to JSON", e);
    }

    return webClient.post()
            .uri("/users/" + sendbirdUserId + "/token")
            .body(BodyInserters.fromValue(body)) // 요청 본문 추가
            .retrieve()
            // 수정: HttpStatus::isError 대신 람다 표현식 사용
            .onStatus(status -> status.isError(), response ->
                    response.bodyToMono(String.class).flatMap(errorBody -> {
                        log.error("Sendbird API Error - Issue Access Token for {}: {}", sendbirdUserId, errorBody);
                        return Mono.error(new RuntimeException("Sendbird API Error: " + errorBody));
                    }))
            .bodyToMono(Map.class)
            .flatMap(responseMap -> {
                // 센드버드 API 응답에서 access_token (실제 키는 'token') 추출 및 유효성 검사
                if (responseMap != null && responseMap.containsKey("token") && responseMap.get("token") instanceof String) {
                    String accessToken = (String) responseMap.get("token");
                    if (accessToken != null && !accessToken.isEmpty()) {
                       log.info("Successfully issued Sendbird Access Token for {}: {}", sendbirdUserId, accessToken);
                        // Map을 Map<String, Object>로 캐스팅하여 반환 타입 일치
                        return Mono.just((Map<String, Object>) responseMap); // 유효한 토큰이 있으면 응답 맵 반환
                    }
                }
                // access_token (token)이 없거나 유효하지 않은 경우 오류 발생
                log.error("Sendbird API Error - Missing or invalid access_token (key 'token') in response for {}. Full response: {}", sendbirdUserId, responseMap);
                return Mono.error(new RuntimeException("Sendbird API did not return a valid access_token (key 'token'). Full response: " + responseMap));
            });
  }


  // TODO: 필요하다면 다른 Sendbird API 호출 메소드 추가 (예: 채널 생성, 메시지 전송 (서버에서 보낼 경우) 등)
}
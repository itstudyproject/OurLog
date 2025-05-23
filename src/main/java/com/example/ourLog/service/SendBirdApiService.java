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
  private final UserService userService; // User 엔티티 조회 및 Sendbird User ID를 가져오기 위해 필요

  public SendBirdApiService(
          @Value("${sendbird.app-id}") String sendbirdAppId,
          @Value("${sendbird.api-token}") String sendbirdApiToken,
          UserService userService) { // UserService 주입받도록 수정
    this.sendbirdAppId = sendbirdAppId;
    this.sendbirdApiToken = sendbirdApiToken;
    this.userService = userService; // UserService 할당
    this.webClient = WebClient.builder()
            .baseUrl("https://api-" + sendbirdAppId + ".sendbird.com/v3")
            .defaultHeader("Api-Token", sendbirdApiToken)
//            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
  }

  // Sendbird User 생성 (POST /users)
  // https://sendbird.com/docs/chat/platform-api/v3/user/managing-users/create-a-user
  public Mono<Map> createUser(User user) { // 또는 UserDTO를 받을 수 있습니다.
    String userId = String.valueOf(user.getUserId()); // User ID를 String으로 변환 (Sendbird User ID는 String)
    log.info("Creating Sendbird user with ID: {}", userId);

    Map<String, Object> body = new HashMap<>();
    body.put("user_id", userId);
    body.put("nickname", user.getNickname());
    // TODO: 실제 프로필 이미지 URL을 UserProfile에서 가져와서 설정
    // User 엔티티에 UserProfile 정보가 포함되어 있지 않다면, UserProfileService를 주입받거나 인자로 받아야 합니다.
    // 임시로 기본 이미지 또는 UserProfileService를 통해 가져온 이미지 사용 예시
    // UserProfile userProfile = userProfileService.getProfileByUserId(user.getUserId());
    // body.put("profile_url", userProfile != null ? userProfile.getThumbnailImagePath() : "YOUR_DEFAULT_PROFILE_IMAGE_URL");
    body.put("profile_url", "YOUR_DEFAULT_PROFILE_IMAGE_URL"); // TODO: 실제 URL로 변경

    return webClient.post() // POST 메서드 사용
            .uri("/users") // /users 엔드포인트 사용
            .body(BodyInserters.fromValue(body))
            .retrieve()
            .onStatus(status -> status.isError(), response ->
                    response.bodyToMono(String.class).flatMap(errorBody -> {
                      log.error("Sendbird API Error - Create User {}: {}", userId, errorBody);
                      return Mono.error(new RuntimeException("Sendbird API Error: " + errorBody));
                    }))
            .bodyToMono(Map.class);
  }

  // Sendbird User 업데이트 또는 생성 (PUT /users/{user_id}) - Upsert 기능
  // https://sendbird.com/docs/chat/platform-api/v3/user/managing-users/update-a-user
  // 이 메서드는 issueAccessToken에서 User가 존재하지 않을 때 생성하는 용도로 사용됩니다.
  public Mono<Map> updateUser(User user) { // 또는 UserDTO를 받을 수 있습니다.
    String userId = String.valueOf(user.getUserId()); // User ID를 String으로 변환 (Sendbird User ID는 String)
    log.info("Creating or updating Sendbird user with ID: {}", userId);
    // Sendbird User 정보 구성
    Map<String, Object> body = new HashMap<>();
    body.put("user_id", userId); // PUT 요청시 user_id는 URI에 포함되지만, body에도 포함하는 것이 좋습니다.
    body.put("nickname", user.getNickname());
    // TODO: 실제 프로필 이미지 URL을 UserProfile에서 가져와서 설정
    // User 엔티티에 UserProfile 정보가 포함되어 있지 않다면, UserProfileService를 주입받거나 인자로 받아야 합니다.
    // 임시로 기본 이미지 또는 UserProfileService를 통해 가져온 이미지 사용 예시
    // UserProfile userProfile = userProfileService.getProfileByUserId(user.getUserId());
    // body.put("profile_url", userProfile != null ? userProfile.getThumbnailImagePath() : "YOUR_DEFAULT_PROFILE_IMAGE_URL");
    body.put("profile_url", "YOUR_DEFAULT_PROFILE_IMAGE_URL"); // TODO: 실제 URL로 변경

    // Upsert (생성 또는 업데이트) 요청
    return webClient.put() // PUT 메서드 사용
            .uri("/users/" + userId) // /users/{user_id} 엔드포인트 사용
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

    // Sendbird User 존재 확인
    // https://sendbird.com/docs/chat/platform-api/v3/user/managing-users/view-a-user
    private Mono<Boolean> checkUserExists(String sendbirdUserId) {
        log.info("Checking if Sendbird user exists: {}", sendbirdUserId);
        return webClient.get()
                .uri("/users/" + sendbirdUserId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    if (response.statusCode() == HttpStatus.NOT_FOUND) {
                        // User not found (404) is not an error in this context, it means user doesn't exist.
                        log.info("Sendbird user not found: {}", sendbirdUserId);
                        return Mono.empty(); // Return empty to signal user does not exist
                    } else {
                        // Other 4xx errors are actual errors
                        return response.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("Sendbird API Error - Check User (4xx) {}: {}", sendbirdUserId, errorBody);
                            return Mono.error(new RuntimeException("Sendbird API Error checking user: " + errorBody));
                        });
                    }
                })
                .onStatus(status -> status.is5xxServerError(), response ->
                        response.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("Sendbird API Error - Check User (5xx) {}: {}", sendbirdUserId, errorBody);
                            return Mono.error(new RuntimeException("Sendbird API Error checking user: " + errorBody));
                        }))
                .bodyToMono(Map.class) // We only care if we get a response, not the content
                .hasElement(); // Returns Mono<Boolean> true if response body exists, false otherwise
    }


  // Sendbird Access Token 생성
  // https://sendbird.com/docs/chat/platform-api/v3/user/managing-users/issue-access-token
  // 수정: Long userId 대신 User user를 받도록 시그니처 변경
  public Mono<Map<String, Object>> issueAccessToken(Long userId) {
    String sendbirdUserId = String.valueOf(userId);
    log.info("Attempting to issue Sendbird Access Token for backend userId: {} (Sendbird userId: {})", userId, sendbirdUserId);
    User user = userService.findByUserId(userId);

    // 1. Sendbird에 User가 존재하는지 확인
    return checkUserExists(sendbirdUserId)
            .flatMap(exists -> {
                if (Boolean.FALSE.equals(exists)) {
                    // 2. User가 존재하지 않으면 생성 (createOrUpdateUser (PUT) 사용 - Sendbird upsert 기능 활용)
                    log.info("Sendbird user {} does not exist. Creating user first using upsert.", sendbirdUserId);
                    return createUser(user) // User 객체 전달 (PUT /users/{user_id})
                            .doOnSuccess(result -> log.info("Sendbird user {} created successfully via upsert.", sendbirdUserId))
                            .doOnError(e -> log.error("Failed to create Sendbird user {} via upsert.", sendbirdUserId, e));
                } else {
                     // 2. User가 이미 존재하면 바로 다음 단계로 진행
                    log.info("Sendbird user {} already exists.", sendbirdUserId);
                     return Mono.empty(); // User 생성 단계는 건너뛰고 다음 flatMap으로 진행하기 위해 empty() 반환
                }
            })
             .then(Mono.defer(() -> { // 이전 Mono의 결과와 상관없이 실행
                 // 3. Access Token 발급 요청 (User 생성 여부와 관계없이 실행)
                 log.info("Issuing Sendbird Access Token for Sendbird userId: {}", sendbirdUserId);
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
                         .bodyToMono(Map.class);
             }))
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
            })
            .doOnError(e -> {
                log.error("Exception during Sendbird API call for user {}:", sendbirdUserId, e);
            });
  }


  // TODO: 필요하다면 다른 Sendbird API 호출 메소드 추가 (예: 채널 생성, 메시지 전송 (서버에서 보낼 경우) 등)
}
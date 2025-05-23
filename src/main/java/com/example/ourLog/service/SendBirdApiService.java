package com.example.ourLog.service;

import com.example.ourLog.entity.User; // 필요하다면 User 엔티티 임포트
import com.example.ourLog.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder; // SecurityContextHolder import
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException; // WebClientResponseException 임포트
// import reactor.core.publisher.Mono; // Mono는 더 이상 필요하지 않으므로 주석 처리 또는 제거
// import reactor.util.context.Context; // Reactor Context는 더 이상 필요하지 않으므로 주석 처리 또는 제거
import com.fasterxml.jackson.databind.ObjectMapper; // ObjectMapper 임포트 필요
// import org.webjars.NotFoundException; // 웹JAR 라이브러리가 아닌 Spring/Java 표준 예외 사용을 고려

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class SendBirdApiService {

  private final WebClient webClient;
  private final String sendbirdApiToken;
  private final String sendbirdAppId;
  private final ObjectMapper objectMapper = new ObjectMapper(); // ObjectMapper 인스턴스 생성
  private final UserRepository userRepository; // User 조회용 UserRepository 주입

  public SendBirdApiService(
          @Value("${sendbird.app-id}") String sendbirdAppId,
          @Value("${sendbird.api-token}") String sendbirdApiToken,
          UserRepository userRepository) { // UserRepository 주입받도록 수정
    this.sendbirdAppId = sendbirdAppId;
    this.sendbirdApiToken = sendbirdApiToken;
    this.userRepository = userRepository; // 주입받은 UserRepository 할당
    this.webClient = WebClient.builder()
            .baseUrl("https://api-" + sendbirdAppId + ".sendbird.com/v3")
            .defaultHeader("Api-Token", sendbirdApiToken)
//            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
  }

  // Sendbird User 생성 (POST /users) -> 블록킹 방식으로 변경
  // https://sendbird.com/docs/chat/platform-api/v3/user/managing-users/create-a-user
  // requestId 파라미터 추가
  public Map<String, Object> createUser(User user, String requestId) { // 또는 UserDTO를 받을 수 있습니다.
    String userId = String.valueOf(user.getUserId()); // User ID를 String으로 변환 (Sendbird User ID는 String)
    log.info("[{}] Creating Sendbird user with ID (blocking): {}", requestId, userId); // requestId 로깅

    Map<String, Object> body = new HashMap<>();
    body.put("user_id", userId); // POST 요청시 user_id는 body에 포함
    body.put("nickname", user.getNickname());
    // TODO: 실제 프로필 이미지 URL을 UserProfile에서 가져와서 설정
    body.put("profile_url", "YOUR_DEFAULT_PROFILE_IMAGE_URL"); // TODO: 실제 URL로 변경

    try {
      // Sendbird API 호출 및 결과 블록킹
      Map<String, Object> userCreationResponse = webClient.post() // POST 메서드 사용
              .uri("/users") // /users 엔드포인트 사용
              .body(BodyInserters.fromValue(body))
              .retrieve()
              // onStatus 핸들러 제거 - 예외는 block() 호출 시 발생하여 하위 catch 블록에서 잡음
              // bodyToMono에 ParameterizedTypeReference 사용하여 명시적 타입 지정
              .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
              .block(); // 여기서 블록킹 발생

      log.info("[{}] Successfully created Sendbird user (blocking): {}", requestId, userCreationResponse.get("user_id")); // requestId 로깅
      return userCreationResponse; // 유저 생성 응답 자체를 반환

    } catch (WebClientResponseException e) {
      // WebClient 호출 중 발생한 HTTP 오류 처리
      log.error("[{}] Sendbird API Error - Create User (blocking) {}: HTTP Status {}, Body: {}", requestId, userId, e.getStatusCode(), e.getResponseBodyAsString(), e);
      throw new RuntimeException("Sendbird API Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
    } catch (Exception e) {
      // 그 외 오류 처리
      log.error("[{}] Error creating Sendbird user {} (blocking):", requestId, userId, e);
      throw new RuntimeException("Error creating Sendbird user: " + e.getMessage(), e);
    }
  }

  // Sendbird User 업데이트 또는 생성 (PUT /users/{user_id}) - Upsert 기능 -> 블록킹 방식으로 변경
  // https://sendbird.com/docs/chat/platform-api/v3/user/managing-users/update-a-user
  // 이 메서드는 issueAccessToken에서 User가 존재하지 않을 때 생성하는 용도로 사용됩니다.
  // requestId 파라미터 추가
  public Map<String, Object> updateUser(User user, String requestId) { // 또는 UserDTO를 받을 수 있습니다.
    String userId = String.valueOf(user.getUserId()); // User ID를 String으로 변환 (Sendbird User ID는 String)
    log.info("[{}] Creating or updating Sendbird user with ID (blocking): {}", requestId, userId); // requestId 로깅
    // Sendbird User 정보 구성
    Map<String, Object> body = new HashMap<>();
    body.put("user_id", userId); // PUT 요청시 user_id는 URI에 포함되지만, body에도 포함하는 것이 좋습니다.
    body.put("nickname", user.getNickname());
    // TODO: 실제 프로필 이미지 URL을 UserProfile에서 가져와서 설정
    body.put("profile_url", "YOUR_DEFAULT_PROFILE_IMAGE_URL"); // TODO: 실제 URL로 변경

    try {
      // Upsert (생성 또는 업데이트) 요청 (블록킹)
      Map<String, Object> updatedUserResponse = webClient.put() // PUT 메서드 사용
              .uri("/users/" + userId) // /users/{user_id} 엔드포인트 사용
              .body(BodyInserters.fromValue(body))
              .retrieve()
              // onStatus 핸들러 제거
               // bodyToMono에 ParameterizedTypeReference 사용하여 명시적 타입 지정
              .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
              .block(); // 여기서 블록킹 발생

      log.info("[{}] Successfully created or updated Sendbird user (blocking): {}", requestId, updatedUserResponse.get("user_id")); // requestId 로깅
      return updatedUserResponse;

    } catch (WebClientResponseException e) {
      // WebClient 호출 중 발생한 HTTP 오류 처리
      log.error("[{}] Sendbird API Error - Create/Update User (blocking) {}: HTTP Status {}, Body: {}", requestId, userId, e.getStatusCode(), e.getResponseBodyAsString(), e);
      throw new RuntimeException("Sendbird API Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
    } catch (Exception e) {
      // 그 외 오류 처리
      log.error("[{}] Error creating or updating Sendbird user {} (blocking):", requestId, userId, e);
      throw new RuntimeException("Error creating or updating Sendbird user: " + e.getMessage(), e);
    }
  }

    // Sendbird User 존재 확인 -> 블록킹 방식으로 변경
    private Map<String, Object> checkUserExistsBlocking(String sendbirdUserId, String requestId) {
        log.info("[{}] Checking if Sendbird user exists (blocking): {}", requestId, sendbirdUserId);

        try {
            // Sendbird API 호출 및 결과 블록킹
            Map<String, Object> userResponse = webClient.get()
                    .uri("/users/" + sendbirdUserId)
                    .retrieve()
                    // onStatus 핸들러 제거 - 404는 catch (WebClientResponseException.NotFound)에서 처리
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block(); // 여기서 블록킹 발생

            log.info("[{}] Sendbird user {} found (blocking).", requestId, sendbirdUserId);
            return userResponse; // 유저 정보 맵 반환

        } catch (WebClientResponseException.NotFound e) {
            // 404 Not Found 발생 시
            log.info("[{}] Sendbird user {} not found (blocking, caught 404).", requestId, sendbirdUserId);
            return null; // 유저 없음을 null로 반환
        } catch (WebClientResponseException e) {
             // 404 외 다른 HTTP 오류 발생 시
            log.error("[{}] Sendbird API Error - Check User (blocking) {}: HTTP Status {}, Body: {}", requestId, sendbirdUserId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Sendbird API Error checking user: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("[{}] Error checking Sendbird user existence {} (blocking):", requestId, sendbirdUserId, e);
            throw new RuntimeException("Error checking Sendbird user existence: " + e.getMessage(), e);
        }
    }

  // 기존 Sendbird 유저에게 토큰 발급 (private 헬퍼 메소드) -> 블록킹 방식으로 변경
  private Map<String, Object> issueTokenForExistingUser(String sendbirdUserId, String requestId) {
      // 메소드 시작 로그에 Request ID, 스레드, SecurityContext 상태 포함
      Authentication initialAuth = SecurityContextHolder.getContext().getAuthentication();
      log.info("[{}] issueTokenForExistingUser 진입 (블록킹) - 스레드: {}, sendbirdUserId: {}, 인증 상태: {}",
               requestId, Thread.currentThread().getName(), sendbirdUserId, initialAuth != null && initialAuth.isAuthenticated());

      Map<String, Boolean> body = new HashMap<>();
      body.put("issue_access_token", true);

      log.info("[{}] Issuing Sendbird Access Token for Sendbird userId (blocking): {}", requestId, sendbirdUserId); // requestId 로깅
      log.info("[{}] Sendbird API Request Body - Issue Access Token for {} (blocking): {}", requestId, sendbirdUserId, body); // requestId 로깅

      try {
          // Sendbird API 호출 및 결과 블록킹
          Map<String, Object> tokenResponse = webClient.post()
                  .uri("/users/{userId}/token", sendbirdUserId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .bodyValue(body)
                  .retrieve()
                  // onStatus 핸들러 제거
                   // bodyToMono에 ParameterizedTypeReference 사용하여 명시적 타입 지정
                  .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                  .block(); // 여기서 블록킹 발생

          log.info("[{}] Successfully issued Sendbird Access Token for {} (blocking): {}", requestId, sendbirdUserId, tokenResponse.get("token")); // requestId 로깅
          return tokenResponse;

      } catch (WebClientResponseException e) {
          // WebClient 호출 중 발생한 HTTP 오류 처리
          log.error("[{}] Error issuing Sendbird Access Token for user {} (blocking): HTTP Status {}, Body: {}", requestId, sendbirdUserId, e.getStatusCode(), e.getResponseBodyAsString(), e);
          throw new RuntimeException("Error issuing Sendbird Access Token: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
      } catch (Exception e) {
           log.error("[{}] Error issuing Sendbird Access Token for user {} (blocking):", requestId, sendbirdUserId, e); // requestId 로깅
           // 오류 발생 시 예외를 던져서 상위 호출자에게 알림
           throw new RuntimeException("Error issuing Sendbird Access Token: " + e.getMessage(), e);
      }
  }

  // Sendbird Access Token 생성 (기존 issueAccessToken 메소드 -> 블록킹으로 대체)
  public Map<String, Object> issueAccessToken(Long backendUserId, String requestId) {
    String sendbirdUserId = String.valueOf(backendUserId);

    // 서비스 메소드 시작 로그에 Request ID, 스레드, SecurityContext 상태 포함
    Authentication initialAuth = SecurityContextHolder.getContext().getAuthentication();
    log.info("[{}] SendBirdApiService issueAccessToken 진입 (블록킹) - 스레드: {}, backendUserId: {}, sendbirdUserId: {}, 인증 상태: {}",
             requestId, Thread.currentThread().getName(), backendUserId, sendbirdUserId, initialAuth != null && initialAuth.isAuthenticated());

    try {
        // Sendbird user 존재 여부 확인 (블록킹)
        log.info("[{}] Checking if Sendbird user exists (blocking): {}", requestId, sendbirdUserId);
        Map<String, Object> user = checkUserExistsBlocking(sendbirdUserId, requestId); // 블록킹 헬퍼 메소드 호출

        if (user != null) {
            // 사용자가 존재하면 토큰 발급 단계로 진행 (블록킹)
            log.info("[{}] Sendbird user {} already exists. Issuing token (blocking)...", requestId, sendbirdUserId);
            return issueTokenForExistingUser(sendbirdUserId, requestId); // 블록킹 헬퍼 메소드 호출
        } else {
            // Sendbird user가 없는 경우 (checkUserExistsBlocking에서 null 반환)
            log.info("[{}] Sendbird user {} not found (blocking). Attempting to create user...", requestId, sendbirdUserId);

            // 백엔드 DB에서 유저 정보 조회
            Optional<User> userOpt = userRepository.findByUserId(backendUserId);
            if (userOpt.isEmpty()) {
                log.error("[{}] Backend user with id {} not found for Sendbird user creation (blocking).", requestId, backendUserId);
                // 백엔드 유저 없으면 예외 발생
                throw new RuntimeException("Backend user not found for Sendbird creation"); // 예외를 던져서 ChatController에서 처리
            }
            User backendUser = userOpt.get();

            // 유저 생성 후 토큰 발급 (블록킹)
            createUser(backendUser, requestId); // 블록킹 헬퍼 메소드 호출

            // 유저 생성 성공 후 다시 토큰 발급 시도 (블록킹)
            log.info("[{}] Sendbird user {} created. Now issuing token (blocking)...", requestId, sendbirdUserId);
            return issueTokenForExistingUser(sendbirdUserId, requestId); // 블록킹 헬퍼 메소드 호출
        }

    } catch (RuntimeException e) {
        // Sendbird user 조회, 생성 또는 토큰 발급 중 오류 발생 시 (RuntimeException은 SendBirdApiService 내에서 발생시킨 예외를 잡음)
        log.error("[{}] Error during Sendbird user check/create/token issue (blocking): ", requestId, e.getMessage(), e);
        // 오류 발생 시 예외를 던져서 ChatController에서 처리
        throw e; // RuntimeException은 그대로 다시 던짐
    } catch (Exception e) {
         // 그 외 예상치 못한 오류 발생 시
         log.error("[{}] Unexpected error during Sendbird user check/create/token issue (blocking): ", requestId, e.getMessage(), e);
         throw new RuntimeException("An unexpected error occurred: " + e.getMessage(), e); // 새로운 RuntimeException으로 래핑하여 던짐
    }
  }

   // TODO: 필요하다면 다른 Sendbird API 호출 메소드 추가 (예: 채널 생성, 메시지 전송 (서버에서 보낼 경우) 등)
}
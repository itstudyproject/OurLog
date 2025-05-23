// src/main/java/com/example/ourLog/controller/SendbirdController.java
package com.example.ourLog.controller;

import com.example.ourLog.service.SendBirdApiService;
import com.example.ourLog.service.UserService;
import com.example.ourLog.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// import reactor.core.publisher.Mono; // Mono는 더 이상 필요하지 않으므로 주석 처리 또는 제거

import java.util.HashMap;
import java.util.Map;
import com.example.ourLog.security.dto.UserAuthDTO;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final SendBirdApiService sendbirdApiService;
    private final UserService userService; // 현재 로그인한 사용자의 ID를 가져오기 위해 필요

    // 인증된 사용자의 Sendbird Access Token을 발급하는 엔드포인트
    // 프론트엔드에서 이 API를 호출하여 토큰을 받아 Sendbird SDK connect 메소드에 사용합니다.
    // SecurityConfig에 이 경로에 대한 인증 설정이 필요합니다. (예: .requestMatchers("/sendbird/token").authenticated())
    // 수정: Mono<ResponseEntity<Map<String, String>>> 대신 ResponseEntity<Map<String, String>>를 반환하도록 시그니처 변경
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getSendbirdAccessToken(@RequestHeader("X-Request-ID") String requestId) {
        // 요청 시작 로그에 Request ID, 스레드, SecurityContext 상태 포함
        Authentication initialAuth = SecurityContextHolder.getContext().getAuthentication();
        log.info("[{}] ChatController /token 엔드포인트 진입 (블록킹) - 스레드: {}, 인증 상태: {}",
                 requestId, Thread.currentThread().getName(), initialAuth != null && initialAuth.isAuthenticated());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("[{}] 인증 정보 없음 (ChatController): {}", requestId, authentication);
            // SecurityContext가 비어있다면 401 Unauthorized 반환 (이전 필터에서 처리되어 여기까진 안 올 수 있음)
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        log.info("[{}] 인증 정보 확인됨 (ChatController): {}", requestId, authentication.getName());

        // UserDetails 객체 (UserAuthDTO)에서 userId를 추출
        // Principal이 UserDetails 타입인지 확인하고 캐스팅합니다.
        Object principal = authentication.getPrincipal();
        Long userId = null;
        if (principal instanceof UserAuthDTO) {
            UserAuthDTO userAuthDTO = (UserAuthDTO) principal;
            userId = userAuthDTO.getUserId();
            log.info("[{}] UserAuthDTO에서 userId 추출 성공: {}", requestId, userId);
        } else {
            // UserDetails 구현체가 UserAuthDTO가 아닌 경우 다른 방식으로 userId를 가져와야 합니다.
            // 예를 들어, principal.getName()을 Sendbird User ID로 사용하거나, 별도의 서비스 호출
            // 현재 구조에서는 UserAuthDTO 사용을 가정합니다.
            log.error("[{}] Principal is not UserAuthDTO. Cannot extract userId. Principal Type: {}", requestId, principal.getClass().getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Authentication principal format error"));
        }


        if (userId == null) {
             log.error("[{}] Could not extract userId from authentication principal.", requestId);
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User ID not available"));
        }

        log.info("[{}] Issuing Sendbird Access Token for backend userId (via service): {}", requestId, userId);

        try {
            // SendbirdApiService를 통해 토큰 발급 요청 (블록킹 메소드 호출)
            Map<String, Object> responseMap = sendbirdApiService.issueAccessToken(userId, requestId);

            // Sendbird API 응답에서 access_token (실제 키는 'token') 추출
            String accessToken = (String) responseMap.get("token");

            // 필요한 경우 expires_at도 함께 전달
            String expiresAt = null;
            if (responseMap.containsKey("expires_at") && responseMap.get("expires_at") != null) {
                expiresAt = String.valueOf(responseMap.get("expires_at"));
            }

            Map<String, String> tokenResponse = new HashMap<>();
            tokenResponse.put("accessToken", accessToken);
            if (expiresAt != null) {
                 tokenResponse.put("expiresAt", expiresAt);
            }

            log.info("[{}] tokenResponse (blocking): {}", requestId, tokenResponse);
            return ResponseEntity.ok(tokenResponse);

        } catch (RuntimeException e) {
            // SendbirdApiService에서 발생한 예외 처리
            log.error("[{}] Failed to get Sendbird Access Token (blocking): {}", requestId, e.getMessage(), e);
            // 오류 발생 시 500 Internal Server Error 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to get Sendbird access token", "details", e.getMessage()));
        }
    }

    // TODO: 필요하다면 Sendbird User 프로필 업데이트 등을 위한 API 엔드포인트 추가
}
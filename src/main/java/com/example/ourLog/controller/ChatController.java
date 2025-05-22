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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
    @GetMapping("/token")
    public Mono<ResponseEntity<Map<String, String>>> getSendbirdAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("인증 정보 없음 (ChatController): {}", authentication);
            return Mono.just(ResponseEntity.status(401).body(Map.of("error", "Unauthorized")));
        }
        log.info("인증 정보 확인됨 (ChatController): {}", authentication.getName());

        // UserDetails 객체 (UserAuthDTO)에서 userId를 추출
        // Principal이 UserDetails 타입인지 확인하고 캐스팅합니다.
        Object principal = authentication.getPrincipal();
        Long userId = null;
        if (principal instanceof UserAuthDTO) {
            UserAuthDTO userAuthDTO = (UserAuthDTO) principal;
            userId = userAuthDTO.getUserId();
        } else {
            // UserDetails 구현체가 UserAuthDTO가 아닌 경우 다른 방식으로 userId를 가져와야 합니다.
            // 예를 들어, principal.getName()을 Sendbird User ID로 사용하거나, 별도의 서비스 호출
            // 현재 구조에서는 UserAuthDTO 사용을 가정합니다.
            log.error("Principal is not UserAuthDTO. Cannot extract userId.");
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Authentication principal format error")));
        }


        if (userId == null) {
             log.error("Could not extract userId from authentication principal.");
             return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User ID not available")));
        }

        log.info("Issuing Sendbird Access Token for backend userId: {}", userId);

        // userService.findByUserId를 사용하여 User 엔티티 조회
        User user = userService.findByUserId(userId);

        if (user == null) {
             // userId로 사용자를 찾지 못한 경우 (데이터 불일치 등)
             log.warn("User not found in backend for userId: {}", userId);
             return Mono.just(ResponseEntity.status(404).body(Map.of("error", "User not found in backend")));
        }

        // SendbirdApiService를 통해 토큰 발급 요청
        return sendbirdApiService.issueAccessToken(userId)
                .map(responseMap -> {
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

                  log.info("tokenResponse:" + tokenResponse);
                    return ResponseEntity.ok(tokenResponse);
                })
                // 예외 발생 시 상세 정보를 로그로 출력
                .doOnError(e -> {
                    log.error("Exception during Sendbird token response processing:", e);
                })
                .onErrorResume(e -> {
                    log.error("Failed to get Sendbird Access Token", e);
                    // Sendbird API 호출 실패 시 500 Internal Server Error 반환
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to get Sendbird access token", "details", e.getMessage())));
                });
    }

    // TODO: 필요하다면 Sendbird User 프로필 업데이트 등을 위한 API 엔드포인트 추가
}
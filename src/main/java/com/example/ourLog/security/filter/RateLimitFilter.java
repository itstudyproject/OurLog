// src/main/java/com/example/ourLog/security/filter/RateLimitFilter.java
package com.example.ourLog.security.filter;

import com.example.ourLog.security.dto.UserAuthDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    // 사용자 ID별 마지막 요청 시간 저장 (밀리초 단위)
    private final Map<Long, Long> lastRequestTime = new ConcurrentHashMap<>();
    // 동일 사용자 요청 최소 간격 (밀리초 단위)
    private final long minRequestIntervalMillis;
    // 속도 제한을 적용할 경로 패턴
    private final String[] includePatterns;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public RateLimitFilter(long minRequestIntervalSeconds, String[] includePatterns) {
        this.minRequestIntervalMillis = TimeUnit.SECONDS.toMillis(minRequestIntervalSeconds);
        this.includePatterns = includePatterns;
        log.info("RateLimitFilter 초기화: 최소 요청 간격 {} ms, 포함 패턴 {}", this.minRequestIntervalMillis, includePatterns != null ? String.join(", ", includePatterns) : "모든 경로");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = extractPath(request);
        log.debug(">>>> RateLimitFilter 진입 - 최종 요청 경로: {}", requestURI);

        // Rate Limiting을 적용할 경로인지 확인
        boolean shouldApplyRateLimit = shouldApply(requestURI);
        log.debug("  ➡️ Rate Limiting 적용 여부 (URI {}): {}", requestURI, shouldApplyRateLimit);

        // 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Rate Limiting 적용 대상이고 인증된 사용자인 경우
        if (shouldApplyRateLimit && authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            Long userId = null;

            // UserAuthDTO에서 userId 추출
            if (principal instanceof UserAuthDTO) {
                userId = ((UserAuthDTO) principal).getUserId();
                log.debug("  인증된 UserId: {}", userId);
            } else {
                 log.warn("Principal is not UserAuthDTO. Cannot extract userId for rate limiting.");
            }

            if (userId != null) {
                long currentTime = System.currentTimeMillis();
                Long lastTime = lastRequestTime.get(userId);

                log.debug("  UserId: {}, 현재 시간: {}, 마지막 요청 시간: {}", userId, currentTime, lastTime);

                if (lastTime != null && (currentTime - lastTime) < minRequestIntervalMillis) {
                    log.warn("  Rate limit exceeded for userId: {} on URI: {}. Ignoring repeated request within interval.", userId, requestURI);
                    handleRateLimitExceeded(response, "Too many requests. Please wait and try again.");
                    return; // 요청 처리 중단
                }

                // 요청 처리 시작 전에 마지막 요청 시간 업데이트
                // TODO: 비동기 처리 (Mono)를 사용하는 컨트롤러 메서드의 경우,
                // 필터에서 시간을 업데이트하면 실제 비동기 작업 완료 전에 시간이 기록될 수 있음.
                // 더 정확한 Rate Limiting을 위해서는 비동기 작업의 시작 시점에 시간을 기록하고,
                // 완료 또는 에러 시점에 후처리 로직을 추가하는 방식 고려 필요.
                // 여기서는 필터 통과 시점을 요청 시작으로 간주합니다.
                lastRequestTime.put(userId, currentTime);
                log.debug("  UserId: {} 마지막 요청 시간 업데이트: {}", userId, currentTime);

            } else {
                 log.debug("  인증된 사용자이지만 userId를 가져올 수 없어 Rate Limiting 적용 안함.");
            }
        } else {
             log.debug("  Rate Limiting 적용 대상이 아니거나 (isApply:{}) 인증되지 않음 (isAuthenticated:{}).", shouldApplyRateLimit, (authentication != null ? authentication.isAuthenticated() : "null"));
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
        log.debug("<<<< RateLimitFilter 종료 - URI: {}", requestURI);
    }

    // HttpServletRequest에서 컨텍스트 패스를 제외한 실제 경로 추출
    private String extractPath(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && requestURI.startsWith(contextPath)) {
            return requestURI.substring(contextPath.length());
        }
        return requestURI;
    }

    // 속도 제한을 적용할 경로인지 판단하는 헬퍼 메서드 (AntPathMatcher 사용)
    private boolean shouldApply(String requestURI) {
        if (includePatterns == null || includePatterns.length == 0) {
            // 패턴이 없으면 모든 경로에 적용 (단, 필터가 인증 후에 실행되므로 인증된 요청에 한함)
            return true;
        }
        for (String pattern : includePatterns) {
             if (antPathMatcher.match(pattern, requestURI)) {
                 return true;
             }
        }
        return false;
    }

    // 속도 제한 초과 시 응답 처리
    private void handleRateLimitExceeded(HttpServletResponse response, String message) throws IOException {
        log.warn("➡️ Rate Limit 초과 응답 처리: {}", message);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429 Status Code
        response.setContentType("application/json;charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()));
        jsonObject.put("message", message);
        PrintWriter out = response.getWriter();
        out.print(jsonObject.toJSONString());
        out.flush();
    }
}
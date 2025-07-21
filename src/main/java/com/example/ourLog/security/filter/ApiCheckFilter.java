package com.example.ourLog.security.filter;

import com.example.ourLog.repository.UserRepository;
import com.example.ourLog.security.dto.UserAuthDTO;
import com.example.ourLog.security.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import net.minidev.json.JSONObject;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.ourLog.entity.User;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class ApiCheckFilter extends OncePerRequestFilter {
  private String[] pattern;
  private AntPathMatcher antPathMatcher;
  private JWTUtil jwtUtil;
  private UserDetailsService userDetailsService;
  private String[] authWhitelist;
  private UserRepository userRepository;


  public ApiCheckFilter(String[] pattern, JWTUtil jwtUtil, UserDetailsService userDetailsService, String[] authWhitelist, UserRepository userRepository) {
    this.pattern = pattern;
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
    this.authWhitelist = authWhitelist;
    this.userRepository = userRepository;
    antPathMatcher = new AntPathMatcher();
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String requestId = request.getHeader("X-Request-ID");
    if (requestId == null || requestId.isEmpty()) {
      requestId = "gen-" + UUID.randomUUID().toString().substring(0, 8);
    }
    log.info("requestId", requestId);
    MDC.put("requestId", requestId);

    try {
      log.info("[{}] >>>> ApiCheckFilter 진입 - 스레드: {}, URI: {}", requestId, Thread.currentThread().getName(), request.getRequestURI());
      log.info("[{}] ApiCheckFilter 실행: {} {}", requestId, request.getRequestURI(), request.getMethod());

      log.info("[{}] Authorization 헤더: {}", requestId, request.getHeader("Authorization"));

      String path = extractPath(request);

      log.info("[{}] 🔥 최종 요청 경로: {}", requestId, path);
      log.info("[{}] 🔥 isWhitelistedPath 결과: {}", requestId, isWhitelistedPath(path));


      if (isWhitelistedPath(path)) {
        log.info("[{}] !!!! ApiCheckFilter 화이트리스트 경로 통과 - 스레드: {}, URI: {}", requestId, Thread.currentThread().getName(), request.getRequestURI());
        filterChain.doFilter(request, response);
        log.info("[{}] <<<< ApiCheckFilter 종료 (화이트리스트) - 스레드: {}, URI: {}", requestId, Thread.currentThread().getName(), request.getRequestURI());
        return;
      }

      log.info("[{}] ➡️ 화이트리스트에 없는 경로. 인증 절차 시작.", requestId);

      try {
        log.info("[{}] 1. 토큰 추출 시도", requestId);

        String token = extractToken(request);
        log.info("[{}] 2. 추출된 토큰: {}", requestId, (token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "없음"));

        if (token == null) {
          log.warn("[{}] 3. 토큰이 없음. 인증 필수 경로 접근 거부.", requestId);
          handleAuthenticationFailure(response, "Authentication required");
          log.info("[{}] <<<< ApiCheckFilter 종료 (인증 실패: 토큰 없음) - 스레드: {}, URI: {}", requestId, Thread.currentThread().getName(), request.getRequestURI());
          return;
        }

        log.info("[{}] 4. 토큰 검증 시도", requestId);

        String email = jwtUtil.validateAndExtract(token);
        log.info("[{}] 5. 추출된 email: {}", requestId, email);

        if (email == null || email.isEmpty()) {
          log.warn("[{}] 6. 이메일이 없거나 유효하지 않은 토큰. 인증 실패.", requestId);
          handleAuthenticationFailure(response, "Invalid or expired token");
          log.info("[{}] <<<< ApiCheckFilter 종료 (인증 실패: 토큰 무효) - 스레드: {}, URI: {}", requestId, Thread.currentThread().getName(), request.getRequestURI());
          return;
        }

        log.info("[{}] 7. 유저 조회 시도 (Email: {})", requestId, email);

        Optional<User> userOpt = userRepository.findByEmail(email);

        log.info("[{}] 8. 유저 조회 결과: {}", requestId, (userOpt.isPresent() ? "User found" : "User not found"));

        if (userOpt.isEmpty()) {
          log.warn("[{}] 9. 해당 이메일({})을 가진 유저가 DB에 없음. 인증 실패.", requestId, email);
          handleAuthenticationFailure(response, "User associated with token not found");
          log.info("[{}] <<<< ApiCheckFilter 종료 (인증 실패: 유저 없음) - 스레드: {}, URI: {}", requestId, Thread.currentThread().getName(), request.getRequestURI());
          return;
        }

        User userEntity = userOpt.get();

        log.info("[{}] 10. 인증 처리할 User Entity: Email: {}, Roles: {}, UserId: {}",
                requestId, userEntity.getEmail(), userEntity.getRoleSet(), userEntity.getUserId());


        List<GrantedAuthority> authorities = userEntity.getRoleSet().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());

        log.info("[{}] 11. 부여된 권한: {}", requestId, authorities);

        UserAuthDTO userAuthDTO = new UserAuthDTO(
                userEntity.getEmail(),
                userEntity.getPassword(),
                authorities,
                userEntity.getEmail(),
                userEntity.getName(),
                userEntity.getNickname(),
                userEntity.isFromSocial(),
                userEntity.getUserId()
        );

        log.info("[{}] 12. UserAuthDTO 생성 완료. Principal Username: {}", requestId, userAuthDTO.getUsername());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userAuthDTO,
                null,
                userAuthDTO.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("[{}] 13. SecurityContextHolder에 인증 정보 저장 완료. Authentication Principal: {}",
                requestId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());


        log.info("[{}] 14. 인증 성공. 다음 필터 체인 진행.", requestId);
        filterChain.doFilter(request, response);

      } catch (Exception e) {
        log.error("[{}] 15. ApiCheckFilter 처리 중 예외 발생 (경로: {}): ", requestId, path, e);
        handleAuthenticationFailure(response, "Authentication error: " + e.getMessage());
      }

    } finally {
      MDC.remove("requestId");
      log.info("[{}] <<<< ApiCheckFilter 종료 - 스레드: {}, URI: {}", requestId, Thread.currentThread().getName(), request.getRequestURI());

    }
  }

  @Override
  protected boolean shouldNotFilterAsyncDispatch() {
    return true;
  }

  @Override
  protected boolean shouldNotFilterErrorDispatch() {
    return true;
  }

  private String extractPath(HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    String contextPath = request.getContextPath();
    if (StringUtils.hasText(contextPath) && requestURI.startsWith(contextPath)) {
      return requestURI.substring(contextPath.length());
    }
    return requestURI;
  }

  private boolean isWhitelistedPath(String path) {
    if (path.startsWith("/ws-chat")) {
      return true;
    }
    for (String white : authWhitelist) {
      if (antPathMatcher.match(white, path)) {
        return true;
      }
    }
    return false;
  }

  private String extractToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7).trim();
      if (StringUtils.hasText(token)) {
        log.debug("[{}] ✅ Authorization 헤더에서 추출된 토큰: {}", MDC.get("requestId"), token.substring(0, Math.min(token.length(), 20)) + "...");
        return token;
      }
    }

    String token = request.getParameter("token");
    if (StringUtils.hasText(token)) {
      log.debug("[{}] ✅ 쿼리 파라미터에서 추출된 토큰: {}", MDC.get("requestId"), token.substring(0, Math.min(token.length(), 20)) + "...");

      if (token.endsWith(";")) {
        token = token.substring(0, token.length() - 1);
        log.debug("[{}] ✂️ 세미콜론 제거된 토큰: {}", MDC.get("requestId"), token.substring(0, Math.min(token.length(), 20)) + "...");
      }
      return token;
    }

    return null;
  }

  private void handleAuthenticationFailure(HttpServletResponse response, String message) throws IOException {
    log.warn("[{}] ➡️ 인증 실패 처리: {}", MDC.get("requestId"), message);
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType("application/json;charset=utf-8");
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("code", "403");
    jsonObject.put("message", message);
    PrintWriter out = response.getWriter();
    out.print(jsonObject.toJSONString());
    out.flush();
  }
}
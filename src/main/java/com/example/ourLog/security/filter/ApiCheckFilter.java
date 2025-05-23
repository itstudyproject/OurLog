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
    log.info(">>>> ApiCheckFilter 진입 - 스레드: {}, URI: {}", Thread.currentThread().getName(), request.getRequestURI());
    log.info("ApiCheckFilter 실행: " + request.getRequestURI() + " " + request.getMethod());

    log.info("Authorization 헤더: " + request.getHeader("Authorization"));

    String path = extractPath(request);

    log.info("🔥 최종 요청 경로: {}", path);
    log.info("🔥 isWhitelistedPath 결과: {}", isWhitelistedPath(path));


    // 1. 요청 경로가 authWhitelist에 포함되는지 확인
    if (isWhitelistedPath(path)) {
      log.info("!!!! ApiCheckFilter 화이트리스트 경로 통과 - 스레드: {}, URI: {}", Thread.currentThread().getName(), request.getRequestURI());
      // 화이트리스트 경로는 토큰 없이도 접근 가능하므로 인증 절차 건너뛰고 다음 필터로 진행
      filterChain.doFilter(request, response);
      log.info("<<<< ApiCheckFilter 종료 (화이트리스트) - 스레드: {}, URI: {}", Thread.currentThread().getName(), request.getRequestURI());
      return;
    }

    // 2. 화이트리스트에 없으면 인증 필수 경로로 간주하고 토큰 검사 진행
    log.info("➡️ 화이트리스트에 없는 경로. 인증 절차 시작.");

    try {
      log.info("1. 토큰 추출 시도");

      String token = extractToken(request);
      log.info("2. 추출된 토큰: " + (token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "없음")); // 로그 보안 강화

      // 토큰이 없으면 403 Forbidden 응답
      if (token == null) {
        log.warn("3. 토큰이 없음. 인증 필수 경로 접근 거부.");
        handleAuthenticationFailure(response, "Authentication required");
        log.info("<<<< ApiCheckFilter 종료 (인증 실패: 토큰 없음) - 스레드: {}, URI: {}", Thread.currentThread().getName(), request.getRequestURI());
        return;
      }

      log.info("4. 토큰 검증 시도");

      String email = jwtUtil.validateAndExtract(token);
      log.info("5. 추출된 email: " + email);

      // 토큰은 있지만 유효하지 않아 이메일 추출 실패 시 403 응답
      if (email == null || email.isEmpty()) {
        log.warn("6. 이메일이 없거나 유효하지 않은 토큰. 인증 실패.");
        handleAuthenticationFailure(response, "Invalid or expired token");
        log.info("<<<< ApiCheckFilter 종료 (인증 실패: 토큰 무효) - 스레드: {}, URI: {}", Thread.currentThread().getName(), request.getRequestURI());
        return;
      }

      log.info("7. 유저 조회 시도 (Email: {})", email);

      Optional<User> userOpt = userRepository.findByEmail(email);

      log.info("8. 유저 조회 결과: " + (userOpt.isPresent() ? "User found" : "User not found"));

      // 유효한 토큰에서 이메일은 추출했지만 해당 이메일의 유저가 DB에 없는 경우 403 응답
      if (userOpt.isEmpty()) {
        log.warn("9. 해당 이메일({})을 가진 유저가 DB에 없음. 인증 실패.", email);
        handleAuthenticationFailure(response, "User associated with token not found");
        log.info("<<<< ApiCheckFilter 종료 (인증 실패: 유저 없음) - 스레드: {}, URI: {}", Thread.currentThread().getName(), request.getRequestURI());
        return;
      }

      User userEntity = userOpt.get();

      log.info("10. 인증 처리할 User Entity: Email: {}, Roles: {}, UserId: {}",
               userEntity.getEmail(), userEntity.getRoleSet(), userEntity.getUserId());


      List<GrantedAuthority> authorities = userEntity.getRoleSet().stream()
              .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
              .collect(Collectors.toList());

      log.info("11. 부여된 권한: " + authorities);

      // UserAuthDTO 생성자 순서에 맞춰서 수정 및 필드 이름 명확화
      UserAuthDTO userAuthDTO = new UserAuthDTO(
              userEntity.getEmail(),                      // username (Spring Security Principal 이름)
              userEntity.getPassword(),                   // password (인증 후에는 중요하지 않음)
              authorities,                                // 권한 목록
              userEntity.getEmail(),                      // email (추가 정보)
              userEntity.getName(),                       // name (추가 정보)
              userEntity.getNickname(),                   // nickname (추가 정보)
              userEntity.isFromSocial(),                  // fromSocial (추가 정보)
              userEntity.getUserId()                      // userId (추가 정보) - 컨트롤러/서비스에서 사용
      );

      log.info("12. UserAuthDTO 생성 완료. Principal Username: {}", userAuthDTO.getUsername());

      // Spring Security Authentication 객체 생성 및 SecurityContextHolder에 설정
      Authentication authentication = new UsernamePasswordAuthenticationToken(
              userAuthDTO, // Principal로 UserAuthDTO 객체 사용
              null, // Credentials (비밀번호)는 인증 후 제거
              userAuthDTO.getAuthorities()); // 부여된 권한

      SecurityContextHolder.getContext().setAuthentication(authentication);
      log.info("13. SecurityContextHolder에 인증 정보 저장 완료. Authentication Principal: {}",
               SecurityContextHolder.getContext().getAuthentication().getPrincipal());


      log.info("14. 인증 성공. 다음 필터 체인 진행.");
      filterChain.doFilter(request, response);
      log.info("<<<< ApiCheckFilter 종료 (인증 성공) - 스레드: {}, URI: {}", Thread.currentThread().getName(), request.getRequestURI());


    } catch (Exception e) {
      log.error("15. ApiCheckFilter 처리 중 예외 발생 (경로: {}): ", path, e);
      // 예외 발생 시 403 Forbidden 응답
      handleAuthenticationFailure(response, "Authentication error: " + e.getMessage());
      log.info("<<<< ApiCheckFilter 종료 (처리 예외) - 스레드: {}, URI: {}", Thread.currentThread().getName(), request.getRequestURI());
    }
    // finally 블록은 제거합니다. OncePerRequestFilter는 요청당 한 번만 실행되므로 context clear가 보통 필요 없습니다.
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
        log.debug("✅ Authorization 헤더에서 추출된 토큰: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
        return token;
      }
    }

    String token = request.getParameter("token");
    if (StringUtils.hasText(token)) {
      log.debug("✅ 쿼리 파라미터에서 추출된 토큰: {}", token.substring(0, Math.min(token.length(), 20)) + "...");

      if (token.endsWith(";")) {
        token = token.substring(0, token.length() - 1);
        log.debug("✂️ 세미콜론 제거된 토큰: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
      }
      return token;
    }

    return null;
  }

  private void handleAuthenticationFailure(HttpServletResponse response, String message) throws IOException {
    log.warn("➡️ 인증 실패 처리: {}", message);
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
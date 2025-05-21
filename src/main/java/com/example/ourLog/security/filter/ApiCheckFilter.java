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
    log.info("ApiCheckFilter 실행: " + request.getRequestURI() + " " + request.getMethod());

    log.info("Authorization 헤더: " + request.getHeader("Authorization"));

    String path = extractPath(request);

    log.info("🔥 최종 요청 경로: {}", path);
    log.info("🔥 isWhitelistedPath 결과: {}", isWhitelistedPath(path));


    if (isWhitelistedPath(path) || !requiresAuthentication(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      log.info("1. ");

      String token = extractToken(request);
      log.info("2. 추출된 토큰: " + token);

      if (token == null) {
        log.warn("3. 토큰이 없음");

        handleAuthenticationFailure(response, "Authentication required");
        return;
      }

      log.info("4. 토큰 검증 시도");

      String email = jwtUtil.validateAndExtract(token);
      log.info("5. 추출된 email: " + email);

      if (email == null || email.isEmpty()) {
        log.warn("6. 이메일이 없음");

        handleAuthenticationFailure(response, "Invalid token");
        return;
      }

      log.info("7. 유저 조회 시도");

      Optional<User> userOpt = userRepository.findByEmail(email);

      log.info("8. 유저 조회 결과: " + userOpt);

      if (userOpt.isEmpty()) {
        log.warn("9. 유저 없음");

        handleAuthenticationFailure(response, "User not found");
        return;
      }

      User userEntity = userOpt.get();

      log.info("10. userEntity: " + userEntity.getEmail() + ", roles: " + userEntity.getRoleSet());


      List<GrantedAuthority> authorities = userEntity.getRoleSet().stream()
              .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
              .collect(Collectors.toList());

      log.info("11. authorities: " + authorities);

      // UserAuthDTO 생성자 순서에 맞춰서 수정
      UserAuthDTO userAuthDTO = new UserAuthDTO(
              userEntity.getEmail(),                      // username
              userEntity.getPassword(),                   // password
              authorities,                                // 권한
              userEntity.getEmail(),                      // email
              userEntity.getName(),                       // name
              userEntity.getNickname(),                   // nickname
              userEntity.isFromSocial(),                  // fromSocial
              userEntity.getUserId()                      // userId
      );

      log.info("12. UserAuthDTO 생성 완료");

      Authentication authentication = new UsernamePasswordAuthenticationToken(
              userAuthDTO, null, userAuthDTO.getAuthorities());

      SecurityContextHolder.getContext().setAuthentication(authentication);
      log.info("13. SecurityContextHolder에 인증 정보 저장 완료");

      filterChain.doFilter(request, response);
      log.info("14. filterChain.doFilter 호출 완료");
      return;

    } catch (Exception e) {
      log.error("15. ApiCheckFilter Error: ", e);
      handleAuthenticationFailure(response, "Authentication error");
    }
  }

  private String extractPath(HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    String contextPath = "/ourlog";
    return requestURI.startsWith(contextPath)
            ? requestURI.substring(contextPath.length())
            : requestURI;
  }

  private boolean isWhitelistedPath(String path) {
    // 명시적으로 제외할 경로 (화이트리스트에 포함되어도 무시)
    if (path.startsWith("/ws-chat")) {
      log.info("⛔️ 제외 경로 매치됨 (화이트리스트 무시): {}", path);
      return false;
    }
    for (String white : authWhitelist) {
      log.info("⛳️ 화이트리스트 비교: {} <-> {}", white, path);
    }
    return Arrays.stream(authWhitelist)
            .anyMatch(pattern -> antPathMatcher.match(pattern, path));
  }

  private boolean requiresAuthentication(String path) {
    return Arrays.stream(pattern)
            .anyMatch(pattern -> antPathMatcher.match(pattern, path));
  }

  private String extractToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      log.info("✅ Authorization 헤더에서 추출된 토큰: {}", token);
      return token;
    }

    // 쿼리 파라미터에서 토큰 추출 시도
    String token = request.getParameter("token");
    if (token != null && !token.isEmpty()) {
      log.info("✅ 쿼리 파라미터에서 추출된 토큰: {}", token);

      // 세미콜론으로 끝나면 제거 (예: "abcde12345;")
      if (token.endsWith(";")) {
        token = token.substring(0, token.length() - 1);
        log.info("✂️ 세미콜론 제거된 토큰: {}", token);
      }

      return token;
    }

    log.warn("⚠️ 토큰 추출 실패: Authorization 헤더와 token 파라미터 모두 없음");
    return null;
  }

  private void handleAuthenticationFailure(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType("application/json;charset=utf-8");
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("code", "403");
    jsonObject.put("message", message);
    response.getWriter().println(jsonObject);
  }
}


//  @Override
//  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//    log.info("ApiCheckFilter................................");
//    log.info("REQUEST URI: " + request.getRequestURI());
//    log.info("REQUEST METHOD: " + request.getMethod());
//
//    String requestURI = request.getRequestURI();
//    String contextPath = "/ourlog";
//
//    String path = requestURI;
//    if (requestURI.startsWith(contextPath)) {
//      path = requestURI.substring(contextPath.length());
//      log.info("Context path 제거 후 URI: {}", path);
//    }
//
//    // AUTH_WHITELIST 경로는 인증 검사에서 제외
//    for (String whitelistPattern : authWhitelist) {
//      if (antPathMatcher.match(whitelistPattern, path)) {
//        log.info("인증 검사 제외 경로: {}", requestURI);
//        filterChain.doFilter(request, response);
//        return;
//      }
//    }
//
//    boolean requiresAuth = false;
//    for (String pattern : this.pattern) {
//      if (antPathMatcher.match(pattern, path)) {
//        requiresAuth = true;
//        break;
//      }
//    }
//
//    if (requiresAuth) {
//      String authHeader = request.getHeader("Authorization");
//
//      if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
//        try {
//          String token = authHeader.substring(7);
//          log.info("Attempting to validate token");
//
//          String email = jwtUtil.validateAndExtract(token);
//
//          if (email != null && !email.isEmpty()) {
//            log.info("Token validated successfully for email: {}", email);
//            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
//            UsernamePasswordAuthenticationToken authentication =
//                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            filterChain.doFilter(request, response);
//            return;
//          } else {
//            log.warn("Token validation returned null or empty email");
//          }
//        } catch (Exception e) {
//          log.error("Token validation failed", e);
//        }
//      }
//
//      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//      response.setContentType("application/json;charset=utf-8");
//      JSONObject jsonObject = new JSONObject();
//      jsonObject.put("code", "403");
//      jsonObject.put("message", "Authentication required");
//      PrintWriter printWriter = response.getWriter();
//      printWriter.println(jsonObject);
//      return;
//    }
//
//    filterChain.doFilter(request, response);
//  }
//}
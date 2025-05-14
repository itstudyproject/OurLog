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
    String path = extractPath(request);

    if (isWhitelistedPath(path) || !requiresAuthentication(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String token = extractToken(request);
      if (token == null) {
        handleAuthenticationFailure(response, "Authentication required");
        return;
      }

      String email = jwtUtil.validateAndExtract(token);
      if (email == null || email.isEmpty()) {
        handleAuthenticationFailure(response, "Invalid token");
        return;
      }

      Optional<User> userOpt = userRepository.findByEmail(email);
      if (userOpt.isEmpty()) {
        handleAuthenticationFailure(response, "User not found");
        return;
      }

      User userEntity = userOpt.get();
      List<GrantedAuthority> authorities = userEntity.getRoleSet().stream()
              .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
              .collect(Collectors.toList());

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

      Authentication authentication = new UsernamePasswordAuthenticationToken(
              userAuthDTO, null, userAuthDTO.getAuthorities());

      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);
      return;

    } catch (Exception e) {
      log.error("ApiCheckFilter Error: ", e);
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
    return Arrays.stream(authWhitelist)
        .anyMatch(pattern -> antPathMatcher.match(pattern, path));
  }

  private boolean requiresAuthentication(String path) {
    return Arrays.stream(pattern)
        .anyMatch(pattern -> antPathMatcher.match(pattern, path));
  }

  private String extractToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    return (authHeader != null && authHeader.startsWith("Bearer "))
        ? authHeader.substring(7)
        : null;
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
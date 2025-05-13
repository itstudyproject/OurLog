package com.example.ourLog.security.filter;

import com.example.ourLog.security.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import net.minidev.json.JSONObject;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

@Log4j2
public class ApiCheckFilter extends OncePerRequestFilter {
  private String[] pattern;
  private AntPathMatcher antPathMatcher;
  private JWTUtil jwtUtil;
  private UserDetailsService userDetailsService;
  private String[] authWhitelist;


  public ApiCheckFilter(String[] pattern, JWTUtil jwtUtil, UserDetailsService userDetailsService, String[] authWhitelist) {
    this.pattern = pattern;
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
    this.authWhitelist = authWhitelist;
    antPathMatcher = new AntPathMatcher();
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    log.info("ApiCheckFilter................................");
    log.info("REQUEST URI: " + request.getRequestURI());
    log.info("REQUEST METHOD: " + request.getMethod());

    String requestURI = request.getRequestURI();
    String contextPath = "/ourlog";

    String path = requestURI;
    if (requestURI.startsWith(contextPath)) {
      path = requestURI.substring(contextPath.length());
      log.info("Context path 제거 후 URI: {}", path);
    }

    // AUTH_WHITELIST 경로는 인증 검사에서 제외
    for (String whitelistPattern : authWhitelist) {
      if (antPathMatcher.match(whitelistPattern, path)) {
        log.info("인증 검사 제외 경로: {}", requestURI);
        filterChain.doFilter(request, response);
        return;
      }
    }

    boolean requiresAuth = false;
    for (String pattern : this.pattern) {
      if (antPathMatcher.match(pattern, path)) {
        requiresAuth = true;
        break;
      }
    }

    if (requiresAuth) {
      String authHeader = request.getHeader("Authorization");

      if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
        try {
          String token = authHeader.substring(7);
          log.info("Attempting to validate token");

          String email = jwtUtil.validateAndExtract(token);

          if (email != null && !email.isEmpty()) {
            log.info("Token validated successfully for email: {}", email);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
            return;
          } else {
            log.warn("Token validation returned null or empty email");
          }
        } catch (Exception e) {
          log.error("Token validation failed", e);
        }
      }

      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType("application/json;charset=utf-8");
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("code", "403");
      jsonObject.put("message", "Authentication required");
      PrintWriter printWriter = response.getWriter();
      printWriter.println(jsonObject);
      return;
    }

    filterChain.doFilter(request, response);
  }
}
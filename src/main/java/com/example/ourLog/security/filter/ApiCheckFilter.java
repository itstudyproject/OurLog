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


  public ApiCheckFilter(String[] pattern, JWTUtil jwtUtil, UserDetailsService userDetailsService) {
    this.pattern = pattern;
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
    antPathMatcher = new AntPathMatcher();
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    // client 요청 주소와 패턴이 같은지 비교 후 같으면 header에 Authorization에 값이 있는지 확인하는 메서드
    log.info("ApiCheckFilter................................");
    log.info("REQUEST URI: " + request.getRequestURI());

    boolean check = false;
    log.info("패턴 리스트: {}", Arrays.toString(pattern));
    for (int i = 0; i < pattern.length; i++) {
      log.info("패턴 매칭 시도: {} vs {}", pattern[i], request.getRequestURI());
      if (antPathMatcher.match(pattern[i], request.getRequestURI())) {
        log.info("패턴 매칭 성공: {}", pattern[i]);
        check = true;
        break;
      }
    }
    log.info("최종 check 값: {}", check);
    if (check) {  // 요청주소와 패턴이 일치한 경우
      log.info("check:" + check);
      String authHeader = request.getHeader("Authorization");
      if (checkAuthHeader(request)) {
        try {
          String email = jwtUtil.validateAndExtract(authHeader.substring(7));  // 예외가 발생할 경우 처리
          UserDetails userDetails = userDetailsService.loadUserByUsername(email);
          UsernamePasswordAuthenticationToken authentication =
                  new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
          SecurityContextHolder.getContext().setAuthentication(authentication);
          log.info("SecurityContext에 Authentication 저장: {}", SecurityContextHolder.getContext().getAuthentication());
          filterChain.doFilter(request, response);
          log.info("filterChain.doFilter 이후 Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
          return;
        } catch (Exception e) {
          log.error("Error extracting email from token", e);  // 예외가 발생한 경우 로그 출력
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          response.setContentType("application/json;charset=utf-8");
          JSONObject jsonObject = new JSONObject();
          jsonObject.put("code", "403");
          jsonObject.put("message", "Fail check API token");
          PrintWriter printWriter = response.getWriter();
          printWriter.println(jsonObject);
          return;
        }
      } else {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "403");
        jsonObject.put("message", "Fail check API token");
        PrintWriter printWriter = response.getWriter();
        printWriter.println(jsonObject);
        return;
      }
    }
    filterChain.doFilter(request, response);
  }

  private boolean checkAuthHeader(HttpServletRequest request) {
    boolean chkResult = false;
    String authHeader = request.getHeader("Authorization");

    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      log.info("Authorization exist: " + authHeader);
      try {
        String token = authHeader.substring(7); // 'Bearer ' 부분을 제외한 토큰만 추출
        String email = jwtUtil.validateAndExtract(token); // JWT 검증 및 이메일 추출
        if (email != null && !email.isEmpty()) {
          log.info("Validate result: " + email);
          chkResult = true;  // 이메일이 유효한 경우
        } else {
          log.warn("Invalid token: email is null or empty");
        }
      } catch (Exception e) {
        log.error("Error validating token", e);  // 예외 발생 시 에러 로그 출력
        // 예외 발생 시 인증 실패로 처리
      }
    } else {
      log.warn("Authorization header is missing or invalid");
    }
    return chkResult;
  }
}

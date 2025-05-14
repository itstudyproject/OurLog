package com.example.ourLog.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// CORS(Cross Origin Resource Sharing)
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) //필터의 우선순위가 높다를 표시
public class CORSFilter  extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String origin = request.getHeader("Origin");
    if ("http://localhost:5173".equals(origin)) {
      response.setHeader("Access-Control-Allow-Origin", origin);
    }
    // response.setHeader("Access-Control-Allow-Origin", "*"); // 이 줄은 삭제 또는 주석 처리
    response.setHeader("Access-Control-Allow-Credentials", "true");
    response.setHeader("Access-Control-Allow-Methods", "*");
    response.setHeader("Access-Control-Max-Age", "3600");
    response.setHeader("Access-Control-Allow-Headers",
            "Origin, X-Requested-with, Content-Type, Accept, Key, Authorization");
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      response.setStatus(HttpServletResponse.SC_OK);
    } else {
      filterChain.doFilter(request, response);
    }
  }
}
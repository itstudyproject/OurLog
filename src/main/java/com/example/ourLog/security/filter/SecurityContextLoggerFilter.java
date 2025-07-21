package com.example.ourLog.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class SecurityContextLoggerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info(">>>> SecurityContextLoggerFilter 진입 - 스레드: {}, URI: {}", Thread.currentThread().getName(), request.getRequestURI());
        try {
            // SecurityContextHolder의 현재 Authentication 객체 로깅
            log.info("Current SecurityContextHolder Authentication: {}", SecurityContextHolder.getContext().getAuthentication());

            filterChain.doFilter(request, response);
        } finally {
            // 요청 처리가 끝난 후 SecurityContextHolder의 상태를 다시 로깅 (선택 사항, 비동기 문제 추적에 도움)
            log.info("<<<< SecurityContextLoggerFilter 종료 - 스레드: {}, URI: {}, Final Authentication: {}", Thread.currentThread().getName(), request.getRequestURI(), SecurityContextHolder.getContext().getAuthentication());
        }
    }
}
//package com.example.ourLog.security.filter;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//// CORS(Cross Origin Resource Sharing)
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE) //필터의 우선순위가 높다를 표시
//public class CORSFilter  extends OncePerRequestFilter {
//  @Override
//  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//    System.out.println("CORSFilter 실행: " + request.getRequestURI() + " " + request.getMethod());
//
//    String origin = request.getHeader("Origin");
//    if ("http://localhost:5173".equals(origin) || "http://127.0.0.1:5173".equals(origin)) {
//      response.setHeader("Access-Control-Allow-Origin", origin);
//    } else {
//        // 허용되지 않은 출처의 요청에 대해서는 CORS 헤더를 설정하지 않거나 기본값 사용
//        // response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173"); // 특정 출처만 허용
//    }
//
//    // response.setHeader("Access-Control-Allow-Origin", "*"); // 이 줄은 삭제 또는 주석 처리
//    response.setHeader("Access-Control-Allow-Credentials", "true");
//    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH"); // 명시적으로 허용 메소드 지정
//    response.setHeader("Access-Control-Max-Age", "3600");
//    // Access-Control-Allow-Headers에 X-Request-ID 추가
//    response.setHeader("Access-Control-Allow-Headers",
//            "Origin, X-Requested-With, Content-Type, Accept, Key, Authorization, X-Request-ID");
//
//    // 클라이언트가 접근할 수 있도록 노출할 헤더 설정 (필요에 따라 추가)
//    response.setHeader("Access-Control-Expose-Headers", "X-Request-ID");
//
//
//    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
//      response.setStatus(HttpServletResponse.SC_OK);
//    } else {
//      filterChain.doFilter(request, response);
//    }
//  }
//}
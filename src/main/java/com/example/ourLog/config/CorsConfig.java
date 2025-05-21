package com.example.ourLog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 모든 출처 허용 (필요시 특정 도메인으로 제한 가능)
                .allowedMethods("*")         // 모든 HTTP 메서드 허용
                .allowCredentials(true);     // 쿠키/인증정보 허용
      }
    };
  }
}

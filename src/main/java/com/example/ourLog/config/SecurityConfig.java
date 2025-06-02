// src/main/java/com/example/ourLog/config/SecurityConfig.java
package com.example.ourLog.config;

import com.example.ourLog.repository.UserRepository;
import com.example.ourLog.security.filter.ApiCheckFilter;
import com.example.ourLog.security.filter.ApiLoginFilter;
import com.example.ourLog.security.filter.RateLimitFilter; // RateLimitFilter 임포트
import com.example.ourLog.security.handler.ApiLoginFailHandler;
import com.example.ourLog.security.service.UserUserDetailsService;
import com.example.ourLog.security.util.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Value 어노테이션 임포트
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration; // Import CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource; // Import CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // Import UrlBasedCorsConfigurationSource

import java.util.Arrays; // Import Arrays
import java.util.Collections; // Import Collections

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  // 인증 없이 접근 가능한 모든 경로를 여기에 정의
  private static final String[] AUTH_WHITELIST = {
          "/user/register",
          "/user/check/**",
          "/user/google/**",
          "/user/flutter/**",
          "/auth/login",
          "/post/list/**",
          "/post/posts/**",
          "/ranking/**",
          "/favorites/count/**",
          // 정적 리소스 경로 추가
          "/display/**",
          "/images/**",
          "/ourlog/picture/display/**",
          "/picture/display/**",
          "/profile/*/*/*/*", // 이미지 파일 경로
          "classpath:/static/images/**" // 기존 static 이미지 경로
  };

  // Rate Limit을 적용할 경로 패턴 정의
  private static final String[] RATE_LIMITED_PATHS = new String[]{
  };

  @Value("${app.rate-limit.interval-seconds:3}")
  private long rateLimitIntervalSeconds;

  private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

  @Bean
  protected SecurityFilterChain config(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
            .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // CORS 설정 추가
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));

    httpSecurity.authorizeHttpRequests(
            auth -> auth
                   .requestMatchers(new AntPathRequestMatcher("/chat/**")).authenticated()
                   // AUTH_WHITELIST에 정의된 모든 경로는 permitAll()
                   .requestMatchers(AUTH_WHITELIST).permitAll()

                   // AUTH_WHITELIST에 없는 나머지 경로는 authenticated() 처리
                   .requestMatchers(new AntPathRequestMatcher("/post/register/**")).authenticated()
                   .requestMatchers(new AntPathRequestMatcher("/post/modify/**")).authenticated()
                   .requestMatchers(new AntPathRequestMatcher("/post/remove/**")).authenticated()
                   .requestMatchers(new AntPathRequestMatcher("/post/read/**")).authenticated()
                   .requestMatchers("/reply/**").authenticated()
                   // /user/register, /user/check/** 는 AUTH_WHITELIST에 있음
                   .requestMatchers("/user/**").authenticated() // 그 외 user 경로는 authenticated() 유지
                   // /picture/display/**, /images/**, /profile/*/*/*/* 등은 AUTH_WHITELIST에 있음
                   .requestMatchers("/picture/upload").authenticated() // 이미지 업로드는 authenticated() 유지
                   .requestMatchers(new AntPathRequestMatcher("/uploadAjax")).authenticated() // uploadAjax도 authenticated() 유지
                   .requestMatchers(new AntPathRequestMatcher("/removeFile/**")).authenticated() // 파일 삭제도 authenticated() 유지
                   .requestMatchers("/question/**").authenticated()
                   .requestMatchers("/user/check-admin").authenticated()
                   .requestMatchers("/question-answer/**").authenticated()
                   .requestMatchers("/trades/**").authenticated()
                   .requestMatchers("/followers/**").authenticated()
                   .requestMatchers("/getPost/**").authenticated()
                   .requestMatchers("/favorites/{userId}").authenticated()
                   // AUTH_WHITELIST에 명시적으로 permitAll() 되지 않은 모든 경로는 authenticated() 적용
                   .anyRequest().authenticated()
    );

    // ApiCheckFilter는 여전히 AUTH_WHITELIST에 대해 토큰 검사를 스킵하도록 설정
    httpSecurity.addFilterBefore(
            apiCheckFilter(),
            UsernamePasswordAuthenticationFilter.class
    );

    // RateLimitFilter를 ApiCheckFilter 뒤 (UsernamePasswordAuthenticationFilter 앞)에 추가
    // RATE_LIMITED_PATHS 배열이 비어있지 않은 경우에만 필터 추가
    // if (RATE_LIMITED_PATHS != null && RATE_LIMITED_PATHS.length > 0) {
    //     httpSecurity.addFilterBefore(
    //             rateLimitFilter(),
    //             UsernamePasswordAuthenticationFilter.class
    //     );
    // } else {
    //     log.info("RateLimitFilter가 RATE_LIMITED_PATHS가 비어있어 필터 체인에 추가되지 않았습니다.");
    // }

    httpSecurity.addFilterBefore(
            apiLoginFilter(httpSecurity.getSharedObject(AuthenticationConfiguration.class)),
            UsernamePasswordAuthenticationFilter.class
    );

    return httpSecurity.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Autowired
  private UserUserDetailsService userDetailsService;

  @Autowired
  private UserRepository userRepository;

  @Bean
  public ApiCheckFilter apiCheckFilter() {
    // ApiCheckFilter는 AUTH_WHITELIST에 대해 토큰 검사를 스킵하도록 설정합니다.
    // pathsToSkip 배열 (첫 번째 인자)은 사용되지 않는 것으로 보이므로 null을 전달합니다.
    return new ApiCheckFilter(
            null, // 사용되지 않는 pattern 인자
            jwtUtil(),
            userDetailsService,
            AUTH_WHITELIST, // <-- AUTH_WHITELIST 전체를 전달하여 ApiCheckFilter에서 화이트리스트 체크
            userRepository
    );
  }

//   @Bean
//   public RateLimitFilter rateLimitFilter() {
//       return new RateLimitFilter(RATE_LIMITED_PATHS);
//   }

  @Bean
  public ApiLoginFilter apiLoginFilter(
          AuthenticationConfiguration authenticationConfiguration) throws Exception {
    ApiLoginFilter apiLoginFilter = new ApiLoginFilter("/auth/login", jwtUtil());
    apiLoginFilter.setAuthenticationManager(
            authenticationConfiguration.getAuthenticationManager()
    );
    apiLoginFilter.setAuthenticationFailureHandler(
            getApiLoginFailHandler()
    );
    return apiLoginFilter;
  }

  @Bean
  public ApiLoginFailHandler getApiLoginFailHandler() {
    return new ApiLoginFailHandler();
  }

  @Bean
  public JWTUtil jwtUtil() {
    return new JWTUtil();
  }

  // CORS 설정 Bean 추가
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
      CorsConfiguration configuration = new CorsConfiguration();
      configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173"));
      configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
      configuration.setAllowCredentials(true);
      configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Request-ID"));
      configuration.setExposedHeaders(Arrays.asList("X-Request-ID"));
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", configuration);
      return source;
  }
}
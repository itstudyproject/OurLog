package com.example.ourLog.config;

import com.example.ourLog.repository.UserRepository;
import com.example.ourLog.security.filter.ApiCheckFilter;
import com.example.ourLog.security.filter.ApiLoginFilter;
import com.example.ourLog.security.handler.ApiLoginFailHandler;
import com.example.ourLog.security.service.UserUserDetailsService;
import com.example.ourLog.security.util.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  private static final String[] AUTH_WHITELIST = {
          "/user/register",
          "/user/check/**",
          "/auth/login",
          "/display/**",   // 정적 리소스는 토큰 검사 제외
          "/images/**",
          "/post/list/**", "/post/posts/**",
          "/ourlog/picture/display/**",
          "/picture/display/**", "/ws-chat",
          "/ws-chat/info"
  };

  private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

  @Bean
  protected SecurityFilterChain config(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
            .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    httpSecurity.authorizeHttpRequests(
            auth -> auth
                    // AUTH_WHITELIST에 있는 경로는 모두 허용
                    .requestMatchers(AUTH_WHITELIST).permitAll() // AUTH_WHITELIST는 /user/register, /auth/login 등 포함
                    .requestMatchers("/profile/**").permitAll() // <-- 컨텍스트 패스 적용 후 /ourlog/profile/** 에 적용


                    // 조건부 허용::주소는 열어 줬지만, 토큰으로 필터 체크 (AUTH_WHITELIST에 없는 경로)
                    // /profile/** 패턴에 대한 authenticated() 규칙을 유지하되, 위의 이미지 경로보다 뒤에 오도록 합니다.
                    // 이렇게 하면 /ourlog/profile/** 중 이미지 파일 경로를 제외한 나머지(API)는 이 규칙에 의해 인증이 필요하게 됩니다.
                    .requestMatchers(new AntPathRequestMatcher("/post/register/**")).authenticated()
                    .requestMatchers(new AntPathRequestMatcher("/post/modify/**")).authenticated()
                    .requestMatchers(new AntPathRequestMatcher("/post/remove/**")).authenticated()
                    .requestMatchers(new AntPathRequestMatcher("/post/read/**")).authenticated()
                    .requestMatchers("/reply/**").permitAll()
                    .requestMatchers("/ourlog/picture/display/**").permitAll()
                    .requestMatchers("/user/**").permitAll()
                    .requestMatchers("/ranking/**").permitAll()
                    .requestMatchers("/picture/**").permitAll()
                    .requestMatchers("/picture/upload").authenticated()
                    .requestMatchers(new AntPathRequestMatcher("/uploadAjax")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/picture/display/**")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/removeFile/**")).permitAll()
                    .requestMatchers("/ws-chat/**").permitAll()  // WebSocket 연결 경로 (예: /ws)


                    // 여기에 추가! - 프로필 관련 API는 인증 필요
                    .requestMatchers("/question/**").authenticated()
                    .requestMatchers("/user/check-admin").authenticated()
                    .requestMatchers("/question-answer/**").authenticated()
                    // /profile/** authenticated() 규칙 유지 - 이미지 경로 permitAll 규칙보다 뒤에 위치해야 함

                    .requestMatchers("/trades/**").authenticated()
                    // 이미지 허용
                    .requestMatchers("/images/**").permitAll()
                    .requestMatchers("classpath:/static/images/**").permitAll()

                    // 팔로우
                    .requestMatchers("/followers/**").authenticated()
                    .requestMatchers("/getPost/**").authenticated()

                    // 그 외는 모두 막음.
                    .anyRequest().permitAll()
    );
//
//    httpSecurity.addFilterBefore(
//            apiCheckFilter(),
//            UsernamePasswordAuthenticationFilter.class //아이디,비번 기반 필터 실행 전 apiCheckFilter호출
//    );
//
//    httpSecurity.addFilterBefore(
//            apiLoginFilter(httpSecurity.getSharedObject(AuthenticationConfiguration.class)),
//            UsernamePasswordAuthenticationFilter.class
//    );

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
    return new ApiCheckFilter(
            new String[]{
                    "/reply/**",
                    "/post/read/**",
                    "/post/register/**",
                    "/post/modify/**",
                    "/post/remove/**",
                    "/user/**",
                    "/picture/**",
                    "/uploadAjax",
                    "/removeFile/**",
                    "/question/**",
                    "/question-answer/**",
                    "/profile/create", // <-- profile 하위 API는 명시적으로 추가하여 인증 필요하도록 설정
                    "/profile/get/**",
                    "/profile/profiles",
                    "/profile/edit/**",
                    "/profile/delete/**",
                    "/profile/purchases/**",
                    "/profile/sales/**",
                    // "/profile/upload-image/**", // 이미지 업로드도 인증 필요
                    "/profile/profileEdit/**",
                    "/profile/favorites/**",
                    "/profile/accountEdit/**",
                    "/trades/**",
                    "/followers/**",
                    "/followers/getPosts/**",
                    "/profile/**",
                    "/ws/**", "/ws-chat/**"

            },
            jwtUtil(),
            userDetailsService,
            AUTH_WHITELIST, // AUTH_WHITELIST는 그대로 유지 (일반적인 permitAll 경로)
            userRepository
    );
  }

  @Bean
  public ApiLoginFilter apiLoginFilter(
          // AuthenticationConfiguration :: Spring Security에서 모든 인증을 처리(UserDetailsService호출)
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
}






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
          "/post/list/**",
          "/post/posts/**",
          "/ourlog/picture/display/**",
          "/picture/display/**",
          "/profile/create",
          "/ranking/**"
  };

  // Rate Limit을 적용할 경로 패턴 정의 (null 또는 빈 배열이면 모든 인증된 경로에 적용)
  // 예: 모든 인증된 경로에 적용하려면 new String[]{} 또는 null
  // 예: /chat/token 경로에만 적용하려면 new String[]{"/chat/token"}
  // 예: /api 로 시작하는 모든 경로에 적용하려면 new String[]{"/api/**"}
  private static final String[] RATE_LIMITED_PATHS = new String[]{
          "/chat/token"
          // 여기에 속도 제한을 적용할 다른 경로 추가
          // 예: "/post/register", "/reply/**" 등
  };

  // application.properties 또는 application.yml 에서 값 주입
  // 기본값은 5초로 설정 (속성 없을 경우)
  @Value("${app.rate-limit.interval-seconds:3}")
  private long rateLimitIntervalSeconds; // 상수를 인스턴스 변수로 변경하고 @Value 주입

  private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

  @Bean
  protected SecurityFilterChain config(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
            .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    httpSecurity.authorizeHttpRequests(
            auth -> auth
                   // Rate Limit 필터가 적용되더라도 인가 설정은 필요합니다.
                   // permitAll()은 토큰 없이 접근 허용, authenticated()는 토큰 필요
                   // rate limit은 authenticated()된 경로에 대해 적용하는 것이 일반적입니다.
                   .requestMatchers(new AntPathRequestMatcher("/chat/**")).authenticated() // /chat/** 경로는 인증 필요
                   .requestMatchers(AUTH_WHITELIST).permitAll()
                   .requestMatchers("/profile/create").permitAll() // 프로필 생성은 인증 없이
                   .requestMatchers("/profile/**").authenticated() // 그 외 프로필 경로는 인증 필요
                   .requestMatchers(new AntPathRequestMatcher("/post/register/**")).authenticated()
                   .requestMatchers(new AntPathRequestMatcher("/post/modify/**")).authenticated()
                   .requestMatchers(new AntPathRequestMatcher("/post/remove/**")).authenticated()
                   .requestMatchers(new AntPathRequestMatcher("/post/read/**")).authenticated()
                   .requestMatchers("/reply/**").authenticated() // 댓글도 인증 필요
                   .requestMatchers("/ourlog/picture/display/**").permitAll()
                   .requestMatchers("/user/register").permitAll() // 회원가입은 인증 없이
                   .requestMatchers("/user/check/**").permitAll() // 중복 체크 등 인증 없이
                   .requestMatchers("/user/**").authenticated() // 그 외 user 경로는 인증 필요
                   .requestMatchers("/ranking/**").permitAll() // 랭킹은 인증 없이
                   .requestMatchers("/picture/display/**").permitAll() // 이미지 표시는 인증 없이
                   .requestMatchers("/picture/upload").authenticated() // 이미지 업로드는 인증 필요
                   .requestMatchers(new AntPathRequestMatcher("/uploadAjax")).authenticated() // uploadAjax도 인증 필요 (업로드 관련 기능이라면)
                   .requestMatchers(new AntPathRequestMatcher("/removeFile/**")).authenticated() // 파일 삭제도 인증 필요
                   .requestMatchers("/question/**").authenticated()
                   .requestMatchers("/user/check-admin").authenticated()
                   .requestMatchers("/question-answer/**").authenticated()
                   .requestMatchers("/trades/**").authenticated()
                   .requestMatchers("/images/**").permitAll()
                   .requestMatchers("classpath:/static/images/**").permitAll()
                   .requestMatchers("/followers/**").authenticated()
                   .requestMatchers("/getPost/**").authenticated()
                   .anyRequest().authenticated() // 위에 명시되지 않은 모든 경로는 인증 필요
    );

    // ApiCheckFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
    httpSecurity.addFilterBefore(
            apiCheckFilter(),
            UsernamePasswordAuthenticationFilter.class
    );

    // RateLimitFilter를 ApiCheckFilter 뒤 (UsernamePasswordAuthenticationFilter 앞)에 추가
    // ApiCheckFilter가 SecurityContext에 인증 정보를 설정한 후에 RateLimitFilter가 실행되도록 함.
     httpSecurity.addFilterBefore(
             rateLimitFilter(),
             UsernamePasswordAuthenticationFilter.class // UsernamePasswordAuthenticationFilter 앞에 오도록 설정
     );


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
    return new ApiCheckFilter(
            new String[]{
                    "/reply/**",
                    "/post/read/**",
                    "/post/register/**",
                    "/post/modify/**",
                    "/post/remove/**",
                    "/user/**", // 일부 경로는 화이트리스트에 포함됨
                    "/picture/**", // 일부 경로는 화이트리스트에 포함됨
                    "/uploadAjax",
                    "/removeFile/**",
                    "/question/**",
                    "/question-answer/**",
                    "/profile/get/**",
                    "/profile/profiles",
                    "/profile/edit/**",
                    "/profile/delete/**",
                    "/profile/purchases/**",
                    "/profile/sales/**",
                    "/profile/profileEdit/**",
                    "/profile/favorites/**",
                    "/profile/accountEdit/**",
                    "/trades/**",
                    "/followers/**",
                    "/followers/getPosts/**",
                    "/profile/**", // profile/create 제외
                    "/chat/**", // chat/token 포함
            },
            jwtUtil(),
            userDetailsService,
            AUTH_WHITELIST,
            userRepository
    );
  }

  // RateLimitFilter 빈 정의
  @Bean
  public RateLimitFilter rateLimitFilter() {
      // @Value로 주입받은 rateLimitIntervalSeconds 변수 사용
      return new RateLimitFilter(rateLimitIntervalSeconds, RATE_LIMITED_PATHS);
  }


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
}
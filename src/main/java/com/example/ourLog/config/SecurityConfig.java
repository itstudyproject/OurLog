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
          "/images/**"

  };

  private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

  @Bean
  protected SecurityFilterChain config(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
            .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    httpSecurity.authorizeHttpRequests(
            auth -> auth
                    // .anyRequest().permitAll() // 모든 주소 허용 :: 단독 사용

                    // 회원가입이기 때문에 무조건 수용(나중에 CORS로 지정하면 됨)
                    .requestMatchers(AUTH_WHITELIST).permitAll()

                    // 조건부 허용::주소는 열어 줬지만, 토큰으로 필터 체크
                    .requestMatchers("/post/list/**").permitAll()
                    .requestMatchers("/post/read/**").permitAll()
                    .requestMatchers("/post/posts/**").permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/post/**")).permitAll()
                    .requestMatchers("/reply/**").permitAll()
                    .requestMatchers("/user/**").permitAll()
                    .requestMatchers("/ranking/**").permitAll()
                    .requestMatchers("/picture/**").permitAll()
                    .requestMatchers("/picture/upload").authenticated()
                    .requestMatchers(new AntPathRequestMatcher("/uploadAjax")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/display/**")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/removeFile/**")).permitAll()

                    // 여기에 추가!
                    .requestMatchers("/question/**").authenticated()
                    .requestMatchers("/user/check-admin").authenticated()
                    .requestMatchers("/question-answer/**").authenticated()
                    .requestMatchers("/profile/**").authenticated()

                    // 이미지 허용
                    .requestMatchers("/images/**").permitAll()
                    .requestMatchers("classpath:/static/images/**").permitAll()



                    // 그 외는 모두 막음.
                    .anyRequest().denyAll()
    );

    httpSecurity.addFilterBefore(
            apiCheckFilter(),
            UsernamePasswordAuthenticationFilter.class //아이디,비번 기반 필터 실행 전 apiCheckFilter호출
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
            new String[]{"/reply/**", "/post/register/**","/post/modify/**", "/post/remove/**","/user/**", "/picture/**", "/uploadAjax", "/removeFile/**", "/question/**", "/question-answer/**", "/profile/**"},
            jwtUtil(),
            userDetailsService,
            AUTH_WHITELIST, // AUTH_WHITELIST 전달
            userRepository // 추가!
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






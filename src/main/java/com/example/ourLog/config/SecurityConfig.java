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
          "/post/list/**",
          "/post/posts/**",
          "/ourlog/picture/display/**",
          "/picture/display/**",
          "/profile/create",
  };

  private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

  @Bean
  protected SecurityFilterChain config(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
            .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    httpSecurity.authorizeHttpRequests(
            auth -> auth
                   .requestMatchers(new AntPathRequestMatcher("/chat/token")).permitAll()
                   .requestMatchers(AUTH_WHITELIST).permitAll()
                   .requestMatchers("/profile/**").permitAll()
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
                   .requestMatchers("/question/**").authenticated()
                   .requestMatchers("/user/check-admin").authenticated()
                   .requestMatchers("/question-answer/**").authenticated()
                   .requestMatchers("/trades/**").authenticated()
                   .requestMatchers("/images/**").permitAll()
                   .requestMatchers("classpath:/static/images/**").permitAll()
                   .requestMatchers("/followers/**").authenticated()
                   .requestMatchers("/getPost/**").authenticated()
                   .anyRequest().authenticated()
    );

    httpSecurity.addFilterBefore(
            apiCheckFilter(),
            UsernamePasswordAuthenticationFilter.class
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
                    "/user/**",
                    "/picture/**",
                    "/uploadAjax",
                    "/removeFile/**",
                    "/question/**",
                    "/question-answer/**",
                    "/profile/create",
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
                    "/profile/**",
                    "/chat/token",
            },
            jwtUtil(),
            userDetailsService,
            AUTH_WHITELIST,
            userRepository
    );
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






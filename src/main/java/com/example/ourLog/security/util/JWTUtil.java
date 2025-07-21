package com.example.ourLog.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;

@Log4j2
public class JWTUtil {
  private String secretKey = "1234567890abcdefghijklmnopqrstuvwxyz";
  private long expire = 60 * 24 * 30;

  // JWT 생성
  public String generateToken(String content) throws Exception {
    return Jwts.builder()
        .issuedAt(new Date())
        .expiration(Date.from(ZonedDateTime.now().plusMinutes(expire).toInstant()))
        .subject(content)  // "sub" 대신 subject() 사용
        .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
        .compact();
  }

  // JWT 검증 및 이메일 추출
  public String validateAndExtract(String tokenStr) {
    try {
      log.info("Validating token: {}", tokenStr);

      var jwtParser = Jwts.parser()
          .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
          .build();

      var claims = jwtParser.parseSignedClaims(tokenStr);
      String email = claims.getPayload().getSubject();

      log.info("Successfully extracted email: {}", email);
      return email;

    } catch (Exception e) {
      log.error("Token validation failed", e);
      return null;
    }
  }
}
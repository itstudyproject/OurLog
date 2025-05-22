package com.example.ourLog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final String uploadDir;

  public WebConfig(@Value("${com.example.upload.path}") String uploadDir) {
    this.uploadDir = uploadDir;
  }


  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {

    String fileStorageLocation = "file:///" + uploadDir.replace("\\", "/");

    registry.addResourceHandler("/images/**")
        .addResourceLocations("file:./upload/");
    // URL 패턴: /{context-path}/profile/**
    registry.addResourceHandler("/ourlog/profile/**")
            // 실제 파일 시스템 경로 (로컬 파일 경로 앞에 'file:///' 접두사 사용)
            // Windows 경로의 경우 역슬래시를 슬래시로 바꿔주거나 이중 역슬래시 사용
            // ensure the path ends with a slash to map correctly
            .addResourceLocations("file:///" + uploadDir.replace("\\", "/") + "/profile/");

    // 만약 컨텍스트 패스 없이 /profile/** 로 접근하고 싶다면 아래와 같이 추가
    registry.addResourceHandler("/profile/**")
            .addResourceLocations("file:///" + uploadDir.replace("\\", "/") + "/profile/");

    // 기본 정적 자원 핸들러도 유지 (static, public 등)
//    registry.addResourceHandler("/**")
//            .addResourceLocations("classpath:/static/", "classpath:/public/", "classpath:/resources/", "classpath:/META-INF/resources/");
  }
}

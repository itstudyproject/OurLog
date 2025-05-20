package com.example.ourLog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

// com.example.ourLog.dto.UploadResultDTO.java 수정 제안
// ... import 문 ...

@Data
@AllArgsConstructor
public class UploadResultDTO implements Serializable {
  private String fileName;
  private String uuid;
  private String folderPath; // 예: profile/2025/05/20
  private String contextPath;

  // URL 인코딩 제거
  public String getImageURL() {
    // folderPath는 이미 슬래시로 되어 있다고 가정
    return contextPath + "/" + folderPath + "/" + uuid + "_" + fileName;
  }

  // URL 인코딩 제거
  public String getThumbnailURL() {
    // folderPath는 이미 슬래시로 되어 있다고 가정
    return contextPath + "/" + folderPath + "/s_" + uuid + "_" + fileName;
  }
}
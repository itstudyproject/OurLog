package com.example.ourLog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PictureDTO {
  private String uuid;
<<<<<<< Updated upstream
  private String pictureName;
  private String path;
  public String getPictureURL() {
    try {
      return URLEncoder.encode(path + "/" + uuid + "_" + pictureName, "UTF-8");
=======
  private String picName;
  private String path;
  public String getPictureURL() {
    try {
      return URLEncoder.encode(path + "/" + uuid + "_" + picName, "UTF-8");
>>>>>>> Stashed changes
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return "";
  }
  public String getThumbnailURL() {
    try {
<<<<<<< Updated upstream
      return URLEncoder.encode(path + "/s_" + uuid + "_" + pictureName, "UTF-8");
=======
      return URLEncoder.encode(path + "/s_" + uuid + "_" + picName, "UTF-8");
>>>>>>> Stashed changes
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return "";
  }
}

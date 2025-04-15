package com.example.ourLog.service;

import com.example.ourLog.dto.PictureDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PictureService {
  List<PictureDTO> uploadFiles(List<MultipartFile> files);

  void assignPicturesToPost(Long postId); // postId 연결용
}

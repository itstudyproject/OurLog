package com.example.ourLog.controller;

import com.example.ourLog.dto.PictureDTO;
import com.example.ourLog.service.PictureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/picture")
@Log4j2
public class PictureController {

  private final PictureService pictureService;

  // ✅ 그림 파일 업로드 (MultipartFile 리스트)
  @PostMapping("/upload")
  public ResponseEntity<List<PictureDTO>> uploadPictures(@RequestPart("files") List<MultipartFile> files) {
    log.info("파일 업로드 요청됨, 개수: " + files.size());

    List<PictureDTO> uploadedPictures = pictureService.uploadFiles(files);

    return new ResponseEntity<>(uploadedPictures, HttpStatus.OK);
  }
}
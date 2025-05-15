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

  @PostMapping("/upload")
  public ResponseEntity<List<PictureDTO>> upload(@RequestPart("files") List<MultipartFile> files) {
    List<PictureDTO> result = pictureService.uploadFiles(files);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/get/{picId}")
  public ResponseEntity<PictureDTO> getPictureById(Long picId) {
    return new ResponseEntity<>(pictureService)
  }

}

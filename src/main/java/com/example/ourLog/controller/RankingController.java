package com.example.ourLog.controller;

import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ranking")
@RequiredArgsConstructor
public class RankingController {

  private final RankingService rankingService;

  @GetMapping
  public ResponseEntity<List<PostDTO>> getRanking(@RequestParam String type) {
    List<PostDTO> result = rankingService.getRankingBy(type);
    return ResponseEntity.ok(result);
  }

}

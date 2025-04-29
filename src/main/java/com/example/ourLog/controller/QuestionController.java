package com.example.ourLog.controller;

import com.example.ourLog.dto.PageRequestDTO;
import com.example.ourLog.dto.PageResultDTO;
import com.example.ourLog.dto.QuestionDTO;
import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.User;
import com.example.ourLog.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/question")
@Log4j2
@RequiredArgsConstructor
public class QuestionController {

  private final QuestionService questionService;

  @GetMapping({"", "/", "/list"})
  public ResponseEntity<PageResultDTO<QuestionDTO, Object[]>> list(PageRequestDTO pageRequestDTO) {
    PageResultDTO<QuestionDTO, Object[]> result = questionService.getList(pageRequestDTO);
    return ResponseEntity.ok(result);
  }

  // 질문 등록
  @PostMapping("/register")
  public ResponseEntity<?> registerQuestion(@RequestBody QuestionDTO questionDTO) {
    Long questionId = questionService.register(questionDTO);
    return ResponseEntity.ok("질문이 등록되었습니다: " + questionId);
  }

  // 질문 읽기
  @GetMapping({"/read", "/modify"})
  public ResponseEntity<QuestionDTO> read(@RequestParam Long questionId, @AuthenticationPrincipal User user) {
    QuestionDTO questionDTO = questionService.get(questionId, user);
    return ResponseEntity.ok(questionDTO);
  }

  // 질문 수정
  @PostMapping("/modify")
  public ResponseEntity<?> modify(@RequestBody QuestionDTO questionDTO) {
    questionService.modify(questionDTO);
    return ResponseEntity.ok("질문이 수정되었습니다: " + questionDTO.getQuestionId());
  }

  // 질문 삭제
  @PostMapping("/remove")
  public ResponseEntity<?> remove(@RequestBody QuestionDTO questionDTO) {
    questionService.removeWithAnswer(questionDTO.getQuestionId());
    return ResponseEntity.ok("질문이 삭제되었습니다: " + questionDTO.getQuestionId());
  }
}
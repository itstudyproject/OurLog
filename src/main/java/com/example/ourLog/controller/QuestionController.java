package com.example.ourLog.controller;

import com.example.ourLog.dto.PageRequestDTO;
import com.example.ourLog.dto.PageResultDTO;
import com.example.ourLog.dto.QuestionDTO;
import com.example.ourLog.dto.UserDTO;
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

   // 질문 등록
  @PostMapping("/inquiry")
  public ResponseEntity<?> inquiry(
          @RequestBody QuestionDTO questionDTO,
          @AuthenticationPrincipal User user
  ) {
    if (user == null) {
      return ResponseEntity.status(401).body("사용자가 인증되지 않았습니다.");
    }

    // User 엔티티 → UserDTO 변환
    UserDTO userDTO = UserDTO.builder()
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .email(user.getEmail())
            .build();

    // 질문 DTO에 사용자 정보 반영
    questionDTO.setUserDTO(userDTO);

    Long questionId = questionService.inquiry(questionDTO);
    return ResponseEntity.ok("질문이 등록되었습니다: " + questionId);
  }

  // 전체 질문 목록 (페이징)
  @GetMapping("/questionList")
  public ResponseEntity<PageResultDTO<QuestionDTO, ?>> getQuestionList(PageRequestDTO pageRequestDTO) {
    PageResultDTO<QuestionDTO, ?> resultDTO = questionService.getQuestionList(pageRequestDTO);
    return ResponseEntity.ok(resultDTO);
  }

  // 특정 유저의 질문 목록 가져오기
  @GetMapping("/my-questions")
  public ResponseEntity<?> getMyQuestions(@AuthenticationPrincipal User user) {
    if (user == null) {
      return ResponseEntity.status(401).body("사용자가 인증되지 않았습니다.");
    }

    try {
      String nickname = user.getNickname();
      return ResponseEntity.ok(questionService.getQuestionsByUserNickname(nickname));
    } catch (Exception e) {
      return ResponseEntity.status(500).body("질문 목록을 가져오는 중 오류 발생: " + e.getMessage());
    }
  }

  // 질문 읽기
  @GetMapping({"/readQuestion", "/editingInquiry"})
  public ResponseEntity<QuestionDTO> readQuestion(@RequestParam Long questionId, @AuthenticationPrincipal User user) {
    if (user == null) {
      return ResponseEntity.status(401).body(null);
    }

    QuestionDTO questionDTO = questionService.readQuestion(questionId, user); // user 검증 포함
    return ResponseEntity.ok(questionDTO);
  }

  // 질문 수정
  @PutMapping("/editingInquiry")
  public ResponseEntity<?> editingInquiry(@RequestBody QuestionDTO questionDTO, @AuthenticationPrincipal User user) {
    if (user == null) {
      return ResponseEntity.status(401).body("사용자가 인증되지 않았습니다.");
    }

    questionService.editingInquiry(questionDTO, user); // user 검증 포함
    return ResponseEntity.ok("질문이 수정되었습니다: " + questionDTO.getQuestionId());
  }

  // 질문 삭제
  @DeleteMapping("/deleteQuestion")
  public ResponseEntity<?> deleteQuestion(@RequestBody QuestionDTO questionDTO, @AuthenticationPrincipal User user) {
    if (user == null) {
      return ResponseEntity.status(401).body("사용자가 인증되지 않았습니다.");
    }

    questionService.deleteQuestion(questionDTO.getQuestionId(), user); // user 검증 포함
    return ResponseEntity.ok("질문이 삭제되었습니다: " + questionDTO.getQuestionId());
  }
}

package com.example.ourLog.controller;

import com.example.ourLog.dto.PageRequestDTO;
import com.example.ourLog.dto.PageResultDTO;
import com.example.ourLog.dto.QuestionDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.security.dto.UserAuthDTO;
import com.example.ourLog.service.QuestionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
          @AuthenticationPrincipal UserAuthDTO user
  ) {
    if (user == null) {
      return ResponseEntity.status(401).body("사용자가 인증되지 않았습니다.");
    }

    UserDTO userDTO = UserDTO.builder()
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .email(user.getEmail())
            .build();

    questionDTO.setUserDTO(userDTO);

    Long questionId = questionService.inquiry(questionDTO);
    return ResponseEntity.ok("질문이 등록되었습니다: " + questionId);
  }

  // 전체 질문 목록 (페이징)
  @GetMapping("/questionList")
  public ResponseEntity<PageResultDTO<QuestionDTO, ?>> getQuestionList(
          PageRequestDTO pageRequestDTO,
          @AuthenticationPrincipal UserAuthDTO user
  ) {
    log.info("🔥 /questionList 요청 도착");
    log.info("✅ isAdmin 여부: {}", user.isAdmin());


    if (user == null || !user.isAdmin()) {
      log.warn("⛔ 관리자가 아님: {}", user.getUsername());
      return ResponseEntity.status(403).body(null);
    }
    PageResultDTO<QuestionDTO, ?> resultDTO = questionService.getQuestionList(pageRequestDTO);
    log.info("📦 질문 리스트 응답 성공, 총 {}건", resultDTO.getDtoList().size());

    return ResponseEntity.ok(resultDTO);
  }

  // 특정 유저의 질문 목록 가져오기
  @GetMapping("/my-questions")
  public ResponseEntity<?> getMyQuestions() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(401).body("사용자가 인증되지 않았습니다.");
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof UserAuthDTO user)) {
      return ResponseEntity.status(401).body("인증된 사용자가 아닙니다.");
    }


    log.info("user: 확인 {}", user);
    if (user == null) {
      return ResponseEntity.status(401).body("사용자가 인증되지 않았습니다.");
    }
    try {
      String email = user.getEmail();
      return ResponseEntity.ok(questionService.getQuestionsByUserEmail(email));
    } catch (Exception e) {
      return ResponseEntity.status(500).body("질문 목록을 가져오는 중 오류 발생: " + e.getMessage());
    }
  }

  // 질문 읽기
  @GetMapping({"/readQuestion", "/editingInquiry"})
  public ResponseEntity<QuestionDTO> readQuestion(@RequestParam Long questionId, @AuthenticationPrincipal UserAuthDTO user) {
    if (user == null) {
      return ResponseEntity.status(401).body(null);
    }

    QuestionDTO questionDTO = questionService.readQuestion(questionId, user); // user 검증 포함
    return ResponseEntity.ok(questionDTO);
  }

  // 질문 수정
  @PutMapping("/editingInquiry")
  public ResponseEntity<?> editingInquiry(@RequestBody QuestionDTO questionDTO, @AuthenticationPrincipal UserAuthDTO user) {
    if (user == null) {
      return ResponseEntity.status(401).body("사용자가 인증되지 않았습니다.");
    }

    questionService.editingInquiry(questionDTO, user); // user 검증 포함
    return ResponseEntity.ok("질문이 수정되었습니다: " + questionDTO.getQuestionId());
  }

  // 질문 삭제
  @DeleteMapping("/deleteQuestion/{questionId}")
  public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId, @AuthenticationPrincipal UserAuthDTO user) {
    log.info("user: 확인 delete {}", user);

    try {
      if (user == null) {
        log.error("사용자 인증 실패");
        return ResponseEntity.status(401).body("사용자가 인증되지 않았습니다.");
      }
      questionService.deleteQuestion(questionId, user);
      log.info("질문 삭제 성공: questionId={}, userEmail={}", questionId, user.getEmail());
      return ResponseEntity.ok("질문이 삭제되었습니다: " + questionId);
    } catch (Exception e) {
      log.error("질문 삭제 중 에러 발생", e);
      return ResponseEntity.status(500).body("질문 삭제 중 에러 발생: " + e.getMessage());
    }
  }
}


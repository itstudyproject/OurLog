package com.example.ourLog.controller;

import com.example.ourLog.dto.AnswerDTO;
import com.example.ourLog.entity.Answer;
import com.example.ourLog.entity.User;
import com.example.ourLog.security.dto.UserAuthDTO;
import com.example.ourLog.service.AnswerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/question-answer")
@Log4j2

public class AnswerController {

  private final AnswerService answerService;

  @PostMapping("/{questionId}")
  public ResponseEntity<?> createAnswer(@PathVariable Long questionId,
                                        @RequestBody AnswerDTO answerDTO,
                                        @AuthenticationPrincipal UserAuthDTO user) {
    if (!user.isAdmin()) {  // 운영자가 아닌 경우 예외 처리
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body("운영자만 답변을 달 수 있습니다.");
    }

    Answer answer = answerService.writeAnswer(questionId, answerDTO.getContents(), user);
    return ResponseEntity.ok(AnswerDTO.builder()
            .answerId(answer.getAnswerId())
            .contents(answer.getContents())
            .regDate(answer.getRegDate())
            .modDate(answer.getModDate())
            .build());
  }

  @PutMapping("/{answerId}")
  public ResponseEntity<?> modifyAnswer(@PathVariable Long answerId,
                                        @RequestBody AnswerDTO answerDTO,
                                        @AuthenticationPrincipal UserAuthDTO user) {
    try {
      answerService.modifyAnswer(answerId, answerDTO.getContents(), user);
      return ResponseEntity.ok("답변이 수정되었습니다.");
    } catch (IllegalAccessException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("수정 실패");
    }
  }

  @DeleteMapping("/{answerId}")
  public ResponseEntity<?> deleteAnswer(@PathVariable Long answerId,
                                        @AuthenticationPrincipal UserAuthDTO user) {
    try {
      answerService.deleteAnswer(answerId, user);
      return ResponseEntity.ok("답변이 삭제되었습니다.");
    } catch (IllegalAccessException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("삭제 실패");
    }
  }

}

package com.example.ourLog.controller;

import com.example.ourLog.dto.QnaAnswerDTO;
import com.example.ourLog.entity.QnaAnswer;
import com.example.ourLog.entity.User;
import com.example.ourLog.service.QnaAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/qna-answer")
public class QnaAnswerController {

  private final QnaAnswerService qnaAnswerService;

  @PostMapping("/{qnaId}")
  public ResponseEntity<?> createAnswer(@PathVariable Long qnaId,
                                        @RequestBody QnaAnswerDTO qnaAnswerDTO,
                                        @AuthenticationPrincipal User loginUser) {
    if (!loginUser.isAdmin()) {  // 운영자가 아닌 경우 예외 처리
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body("운영자만 답변을 달 수 있습니다.");
    }

    QnaAnswer answer = qnaAnswerService.writeAnswer(qnaId, qnaAnswerDTO.getContents(), loginUser);
    return ResponseEntity.ok(answer);
  }

  @PutMapping("/{answerId}")
  public ResponseEntity<?> modifyAnswer(@PathVariable Long answerId,
                                        @RequestBody QnaAnswerDTO qnaAnswerDTO,
                                        @AuthenticationPrincipal User loginUser) {
    try {
      qnaAnswerService.modifyAnswer(answerId, qnaAnswerDTO.getContents(), loginUser);
      return ResponseEntity.ok("답변이 수정되었습니다.");
    } catch (IllegalAccessException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("수정 실패");
    }
  }

  @DeleteMapping("/{answerId}")
  public ResponseEntity<?> deleteAnswer(@PathVariable Long answerId,
                                        @AuthenticationPrincipal User loginUser) {
    try {
      qnaAnswerService.deleteAnswer(answerId, loginUser);
      return ResponseEntity.ok("답변이 삭제되었습니다.");
    } catch (IllegalAccessException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("삭제 실패");
    }
  }

}

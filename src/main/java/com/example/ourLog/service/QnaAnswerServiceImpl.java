package com.example.ourLog.service;

import com.example.ourLog.entity.QnA;
import com.example.ourLog.entity.QnaAnswer;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.QnARepository;
import com.example.ourLog.repository.QnaAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QnaAnswerServiceImpl implements QnaAnswerService{

  private final QnaAnswerRepository qnaAnswerRepository;
  private final QnARepository qnaRepository;

  public QnaAnswer writeAnswer(Long qnaId, String contents, User writer) {
    QnA question = qnaRepository.findById(qnaId)
            .orElseThrow(() -> new IllegalArgumentException("해당 QnA 없음"));

    // 이미 답변이 있는 경우 예외 처리
    qnaAnswerRepository.findByQuestion(question)
            .ifPresent(answer -> {
              throw new IllegalStateException("이미 답변이 등록된 문의입니다.");
            });

    QnaAnswer answer = new QnaAnswer(writer, question, contents);
    return qnaAnswerRepository.save(answer);
  }

  public void modifyAnswer(Long answerId, String newContents, User loginUser) throws IllegalAccessException {
    QnaAnswer answer = qnaAnswerRepository.findById(answerId)
            .orElseThrow(() -> new RuntimeException("답변이 존재하지 않습니다."));

    if (!answer.isSameWriter(loginUser)) {
      throw new IllegalAccessException("수정 권한이 없습니다.");
    }

    answer.updateContents(newContents);
    qnaAnswerRepository.save(answer);
  }

  public void deleteAnswer(Long answerId, User loginUser) throws IllegalAccessException {
    QnaAnswer answer = qnaAnswerRepository.findById(answerId)
            .orElseThrow(() -> new RuntimeException("답변이 존재하지 않습니다."));

    if (!answer.isSameWriter(loginUser)) {
      throw new IllegalAccessException("삭제 권한이 없습니다.");
    }

    qnaAnswerRepository.delete(answer);
  }
}
package com.example.ourLog.service;

import com.example.ourLog.entity.QnaAnswer;
import com.example.ourLog.entity.User;

public interface QnaAnswerService {
  QnaAnswer writeAnswer(Long qnaId, String contents, User writer);

  void modifyAnswer(Long answerId, String newContents, User loginUser) throws IllegalAccessException;

  void deleteAnswer(Long answerId, User loginUser) throws IllegalAccessException;
}

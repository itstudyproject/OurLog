package com.example.ourLog.service;

import com.example.ourLog.entity.Answer;
import com.example.ourLog.entity.User;

public interface AnswerService {
  Answer writeAnswer(Long questionId, String contents, User writer);

  void modifyAnswer(Long answerId, String newContents, User loginUser) throws IllegalAccessException;

  void deleteAnswer(Long answerId, User loginUser) throws IllegalAccessException;
}

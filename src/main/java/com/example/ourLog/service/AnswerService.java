package com.example.ourLog.service;

import com.example.ourLog.entity.Answer;
import com.example.ourLog.security.dto.UserAuthDTO;

public interface AnswerService {
  Answer writeAnswer(Long questionId, String contents, UserAuthDTO writer);

  void modifyAnswer(Long answerId, String newContents, UserAuthDTO loginUser) throws IllegalAccessException;

  void deleteAnswer(Long answerId, UserAuthDTO loginUser) throws IllegalAccessException;
}

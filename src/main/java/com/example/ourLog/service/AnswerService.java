package com.example.ourLog.service;

import com.example.ourLog.dto.AnswerDTO;
import com.example.ourLog.dto.QuestionDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.Answer;
import com.example.ourLog.entity.Question;
import com.example.ourLog.entity.User;
import com.example.ourLog.security.dto.UserAuthDTO;

public interface AnswerService {

  default Answer dtoToEntity(AnswerDTO answerDTO, User user, Question question) {
    if (answerDTO == null) return null;

    return Answer.builder()
            .answerId(answerDTO.getAnswerId())
            .user(user)
            .question(question)
            .contents(answerDTO.getContents())
            .build();
  }

  default AnswerDTO entityToDto(Answer answer) {
    if (answer == null) return null;

    return AnswerDTO.builder()
            .answerId(answer.getAnswerId())
            .contents(answer.getContents())
            .userDTO(convertUserToDTO(answer.getUser()))
            .regDate(answer.getRegDate())
            .modDate(answer.getModDate())
            .questionDTO(convertQuestionToDTO(answer.getQuestion()))
            .build();
  }

  default UserDTO convertUserToDTO(User user) {
    if (user == null) return null;

    return UserDTO.builder()
            .email(user.getEmail())
            .name(user.getName())
            // 필요한 필드 추가
            .build();
  }

  default QuestionDTO convertQuestionToDTO(Question question) {
    if (question == null) return null;

    return QuestionDTO.builder()
            .questionId(question.getQuestionId())
            .title(question.getTitle())
            // 필요한 필드 추가
            .build();
  }

  AnswerDTO writeAnswer(Long questionId, String contents, UserAuthDTO writer);

  void modifyAnswer(Long answerId, String newContents, UserAuthDTO user) throws IllegalAccessException;

  void deleteAnswer(Long answerId, UserAuthDTO user) throws IllegalAccessException;
}
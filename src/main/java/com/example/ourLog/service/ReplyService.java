package com.example.ourLog.service;

import com.example.ourLog.dto.ReplyDTO;
import com.example.ourLog.entity.Reply;

import java.util.List;

public interface ReplyService {
  Long register(ReplyDTO replyDTO);

  List<ReplyDTO> getList(Long mno);

  void modify(ReplyDTO replyDTO);

  void remove(Long replyId);

  default Reply dtoToEntity(ReplyDTO replyDTO) {
    Reply reply = Reply.builder()
        .replyId(replyDTO.getReplyId())
        .postId(replyDTO.getPostId())
        .user(replyDTO.getUserId())
        .content(replyDTO.getContent())
        .build();
    return reply;
  }


  default ReplyDTO entityToDto(Reply reply) {
    ReplyDTO replyDTO = ReplyDTO.builder()
        .replyId(reply.getReplyId())
        .postId(reply.getPostId())
        .userId(reply.getUser())
        .nickname(reply.getNickname())
        .email(reply.getEmail())
        .content(reply.getContent())
        .regDate(reply.getRegDate())
        .modDate(reply.getModDate())
        .build();
    return replyDTO;
  }
}
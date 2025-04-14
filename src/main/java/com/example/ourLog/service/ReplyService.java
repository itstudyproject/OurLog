package com.example.ourLog.service;

import com.example.ourLog.dto.ReplyDTO;
import com.example.ourLog.entity.Reply;
<<<<<<< Updated upstream
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
=======
>>>>>>> Stashed changes

import java.util.List;

public interface ReplyService {
  Long register(ReplyDTO replyDTO);

  List<ReplyDTO> getList(Long mno);

  void modify(ReplyDTO replyDTO);

  void remove(Long replyId);

  default Reply dtoToEntity(ReplyDTO replyDTO) {
    Reply reply = Reply.builder()
        .replyId(replyDTO.getReplyId())
<<<<<<< Updated upstream
        .post(Post.builder().postId(replyDTO.getPostId()).build())
        .user(User.builder().userId(replyDTO.getUserId()).build())
        .likes(replyDTO.getLikes())
=======
        .postId(replyDTO.getPostId())
        .userId(replyDTO.getUserId())
>>>>>>> Stashed changes
        .text(replyDTO.getText())
        .build();
    return reply;
  }


  default ReplyDTO entityToDto(Reply reply) {
    ReplyDTO replyDTO = ReplyDTO.builder()
        .replyId(reply.getReplyId())
<<<<<<< Updated upstream
        .postId(reply.getPost().getPostId())
        .userId(reply.getUser().getUserId())
        .nickname(reply.getUser().getNickname())
        .email(reply.getUser().getEmail())
=======
        .postId(reply.getPostId())
        .userId(reply.getUserId())
        .nickname(reply.getNickname())
        .email(reply.getEmail())
>>>>>>> Stashed changes
        .text(reply.getText())
        .regDate(reply.getRegDate())
        .modDate(reply.getModDate())
        .build();
    return replyDTO;
  }
}

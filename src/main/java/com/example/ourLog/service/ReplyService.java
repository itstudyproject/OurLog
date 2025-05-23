package com.example.ourLog.service;

import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.dto.ReplyDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.Reply;
import com.example.ourLog.entity.User;

import java.util.List;

public interface ReplyService {

    Long register(Long postId, ReplyDTO replyDTO);

    List<ReplyDTO> getList(Long postId);

    void modify(ReplyDTO replyDTO);

    void remove(Long replyId);

    default Reply dtoToEntity(ReplyDTO replyDTO) {
        Reply reply = Reply.builder()
                .replyId(replyDTO.getReplyId())
                .post(Post.builder()
                        .postId(replyDTO.getPostDTO().getPostId())
                        .build())
                .user(User.builder()
                        .userId(replyDTO.getUserDTO().getUserId())
                        .build())
                .content(replyDTO.getContent())
                .build();
        return reply;
    }

    default ReplyDTO entityToDto(Reply reply) {
        User replyUser = reply.getUser();

        UserDTO userDTO = null;
        if (replyUser != null) {
            userDTO = UserDTO.builder()
                    .userId(replyUser.getUserId())
                    .nickname(replyUser.getNickname())
                    .build();
        }

        ReplyDTO replyDTO = ReplyDTO.builder()
                .replyId(reply.getReplyId())
                .postDTO(PostDTO.builder()
                        .postId(reply.getPost().getPostId())
                        .build())
                .userDTO(userDTO)
                .content(reply.getContent())
                .regDate(reply.getRegDate())
                .modDate(reply.getModDate())
                .build();
        return replyDTO;
    }
}

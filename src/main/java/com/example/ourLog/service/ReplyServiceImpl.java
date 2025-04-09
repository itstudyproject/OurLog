package com.example.ourLog.service;

import com.example.ourLog.dto.ReplyDTO;
import com.example.ourLog.entity.Reply;
import com.example.ourLog.entity.Post;
import com.example.ourLog.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; 

@Service
@Log4j2
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {
  private final ReplyRepository replyRepository;

  @Override
  public Long register(ReplyDTO replyDTO) {
    Reply reply = dtoToEntity(replyDTO);
    replyRepository.save(reply);
    return reply.getReplyId();
  }

  @Override
  public List<ReplyDTO> getList(Long postId) {
    List<Reply> result = replyRepository.findByPostId(Post.builder().postId(postId).build());
    return result.stream().map(
        reply -> entityToDto(reply)).collect(Collectors.toList()
    );
  }

  @Override
  public void modify(ReplyDTO replyDTO) {
    Optional<Reply> result = replyRepository.findById(replyDTO.getReplyId());
    if (result.isPresent()) {
      Reply reply = result.get();
      reply.changeText(replyDTO.getText());
      replyRepository.save(reply);
    }
  }

  @Override
  public void remove(Long replyId) {
    replyRepository.deleteById(replyId);
  }
}

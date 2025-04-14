package com.example.ourLog.service;

import com.example.ourLog.dto.PageRequestDTO;
import com.example.ourLog.dto.PageResultDTO;
import com.example.ourLog.dto.QnADTO;
import com.example.ourLog.entity.QnA;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.QnARepository;
import com.example.ourLog.repository.ReplyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Log4j2
public class QnAServiceImpl implements QnAService {
  private final QnARepository qnARepository;
  private final ReplyRepository replyRepository;

  @Override
  public Long register(QnADTO qnADTO) {
    User writer = User.builder()
            .userId(qnADTO.getWriter().getUserId())
            .name(qnADTO.getWriter().getName())
            .build();

    QnA qna = dtoToEntity(qnADTO, writer);
    qnARepository.save(qna);
    return qna.getQnaId();
  }

  @Override
  public PageResultDTO<QnADTO, Object[]> getList(PageRequestDTO pageRequestDTO) {

    log.info(">>"+pageRequestDTO);
    
    Page<Object[]> result = qnARepository.searchPage(
            pageRequestDTO.getType(),
            pageRequestDTO.getKeyword(),
            pageRequestDTO.getPageable(Sort.by("qnaId").descending())
    );

    Function<Object[], QnADTO> fn = new Function<Object[], QnADTO>() {
      @Override
      public QnADTO apply(Object[] arr) {
        return entityToDto((QnA) arr[0], (User) arr[1], (Long) arr[2]);
      }
    };
    return new PageResultDTO<>(result, fn);
  }

  @Override
  public QnADTO get(Long qnaId) {
    Object[] result = qnARepository.getQnAWithAll(qnaId).get(0);

    QnA qna = (QnA) result[0];
    User user = (User) result[1];
    Long replyCount = (Long) result[2];

    return entityToDto(qna, user, replyCount);
  }

  @Override
  public void modify(QnADTO qnADTO) {
    Optional<QnA> result = qnARepository.findById(qnADTO.getQnaId());
    if (result.isPresent()) {
      QnA qna = result.get();
      qna.changeQnATitle(qnADTO.getTitle());
      qna.changeQnAContent(qnADTO.getContent());
      qnARepository.save(qna);
    }

  }

  @Transactional
  @Override
  public void removeWithReplies(Long qnaId) {
    replyRepository.deleteByQnaId(qnaId);  // 댓글 먼저 삭제
    qnARepository.deleteById(qnaId);
  }
}

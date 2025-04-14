package com.example.ourLog.service;

import com.example.ourLog.dto.PageRequestDTO;
import com.example.ourLog.dto.PageResultDTO;
import com.example.ourLog.dto.QnADTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.QnA;
import com.example.ourLog.entity.User;

public interface QnAService {

  // DTO → Entity 변환 메서드
  default QnA dtoToEntity(QnADTO qnADTO, User writer) {
    return QnA.builder()
            .qnaId(qnADTO.getQnaId())
            .title(qnADTO.getTitle())
            .content(qnADTO.getContent())
            .writer(writer)
            .build();
  }

  // Entity → DTO 변환 메서드
  default QnADTO entityToDto(QnA qnA, User writer, Long replyCount) {
    return QnADTO.builder()
            .qnaId(qnA.getQnaId())
            .title(qnA.getTitle())
            .content(qnA.getContent())
            .writer(UserDTO.builder()  // UserDTO로 변환
                    .userId(writer.getUserId())
                    .name(writer.getName())
                    .build())
            .regDate(qnA.getRegDate())
            .modDate(qnA.getModDate())
            .replyCount(replyCount.intValue())
            .build();
  }

  // QnA 등록
  Long register(QnADTO qnADTO);

  // 페이징된 QnA 목록 조회
  PageResultDTO<QnADTO, Object[]> getList(PageRequestDTO pageRequestDTO);

  // 단일 QnA 조회
  QnADTO get(Long qnaId);

  // QnA 수정
  void modify(QnADTO qnADTO);

  // QnA 및 관련 댓글 삭제
  void removeWithReplies(Long bno);
}

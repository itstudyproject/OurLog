package com.example.ourLog.controller;

import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.dto.ReplyDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.security.dto.UserAuthDTO;
import com.example.ourLog.service.ReplyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/reply")
public class ReplyController {
  private final ReplyService replyService;

  @GetMapping(value = "/all/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<ReplyDTO>> getList(@PathVariable("postId") Long postId) {
    List<ReplyDTO> replyDTOList = replyService.getList(postId);
    return new ResponseEntity<>(replyDTOList, HttpStatus.OK);
  }

    @PostMapping("/{postId}")
    public ResponseEntity<Long> register(
        @PathVariable("postId") Long postId,
        @RequestBody ReplyDTO replyDTO,
        @AuthenticationPrincipal UserAuthDTO user // 🔥 현재 로그인 사용자
    ) {
        if (user == null || user.getUserId() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // ✅ postId 설정
        replyDTO.setPostDTO(PostDTO.builder().postId(postId).build());

        // ✅ 현재 로그인 사용자 정보로 userDTO 설정
        replyDTO.setUserDTO(UserDTO.builder()
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .build());

        Long replyId = replyService.register(postId, replyDTO);
        return new ResponseEntity<>(replyId, HttpStatus.CREATED);
    }


    @PutMapping("/{postId}/{replyId}")
  public ResponseEntity<Long> modify(@RequestBody ReplyDTO replyDTO) {
    replyService.modify(replyDTO);
    return new ResponseEntity<>(replyDTO.getReplyId(), HttpStatus.OK);
  }

  @DeleteMapping("/{postId}/{replyId}")
  public ResponseEntity<Long> delete(@PathVariable Long replyId) {
    replyService.remove(replyId);
    return new ResponseEntity<>(replyId, HttpStatus.OK);
  }
}
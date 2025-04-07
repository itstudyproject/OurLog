package com.example.ourLog.controller;

import com.example.ourLog.dto.ReplyDTO;
import com.example.ourLog.service.ReplyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<Long> register(@RequestBody ReplyDTO replyDTO) {
    Long replyId = replyService.register(replyDTO);
    return new ResponseEntity<>(replyId, HttpStatus.OK);
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
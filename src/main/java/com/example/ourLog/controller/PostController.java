package com.example.ourLog.controller;

import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.dto.PageRequestDTO;
import com.example.ourLog.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {

  private final PostService postService;

  @Value("${com.example.upload.path}")
  private String uploadPath;

  // 🔄 검색/페이징 시 null 문자열 처리
  private void typeKeywordInit(PageRequestDTO pageRequestDTO) {
    if ("null".equals(pageRequestDTO.getType())) pageRequestDTO.setType("");
    if ("null".equals(pageRequestDTO.getKeyword())) pageRequestDTO.setKeyword("");
  }

  // ✅ 게시글 목록 (페이징 + 검색)
  @GetMapping("/list")
  public ResponseEntity<Map<String, Object>> list(PageRequestDTO pageRequestDTO) {
    Map<String, Object> result = new HashMap<>();
    result.put("pageResultDTO", postService.getList(pageRequestDTO));
    result.put("pageRequestDTO", pageRequestDTO);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  // ✅ 게시글 등록
  @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
  public ResponseEntity<Long> registerPost(@RequestBody PostDTO postDTO) {
    Long postId = postService.register(postDTO);
    return new ResponseEntity<>(postId, HttpStatus.CREATED);
  }

  // ✅ 게시글 상세 조회 (읽기 또는 수정용)
  @GetMapping({"/read/{postId}", "/modify/{postId}"})
  public ResponseEntity<Map<String, PostDTO>> getPost(@PathVariable("postId") Long postId) {
    PostDTO postDTO = postService.get(postId);
    Map<String, PostDTO> result = new HashMap<>();
    result.put("postDTO", postDTO);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  // ✅ 게시글 수정
  @PutMapping("/modify")
  public ResponseEntity<Map<String, String>> modify(@RequestBody PostDTO dto) {
    postService.modify(dto);
    Map<String, String> result = new HashMap<>();
    result.put("msg", dto.getPostId() + " 수정 완료");
    result.put("postId", String.valueOf(dto.getPostId()));
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  // ✅ 게시글 삭제
  @DeleteMapping("/remove/{postId}")
  public ResponseEntity<Map<String, String>> remove(
      @PathVariable Long postId,
      @RequestBody PageRequestDTO pageRequestDTO
  ) {
    Map<String, String> result = new HashMap<>();
    List<String> photoList = postService.removeWithReplyAndPicture(postId);

    photoList.forEach(fileName -> {
      try {
        String srcFileName = URLDecoder.decode(fileName, "UTF-8");
        File file = new File(uploadPath + File.separator + srcFileName);
        file.delete(); // 원본
        new File(file.getParent(), "s_" + file.getName()).delete(); // 썸네일
      } catch (Exception e) {
        log.warn("파일 삭제 실패: " + e.getMessage());
      }
    });

    if (postService.getList(pageRequestDTO).getDtoList().isEmpty() && pageRequestDTO.getPage() > 1) {
      pageRequestDTO.setPage(pageRequestDTO.getPage() - 1);
    }

    typeKeywordInit(pageRequestDTO);
    result.put("msg", postId + " 삭제 완료");
    result.put("page", String.valueOf(pageRequestDTO.getPage()));
    result.put("type", pageRequestDTO.getType());
    result.put("keyword", pageRequestDTO.getKeyword());
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}

package com.example.ourLog.controller;

import com.example.ourLog.dto.PageResultDTO;
import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.dto.PageRequestDTO;
import com.example.ourLog.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.ourLog.security.dto.UserAuthDTO;


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


  // 게시글 전체 조회
  @GetMapping("/posts")
  public ResponseEntity<List<PostDTO>> getAllPosts() {
    List<PostDTO> postList = postService.getAllPosts(); // 서비스 메서드는 아래에 정의됨
    return ResponseEntity.ok(postList);
  }

  // ✅ 게시글 목록 (페이징 + 검색)
  @GetMapping("/list")
  public ResponseEntity<Map<String, Object>> list(
      PageRequestDTO pageRequestDTO,
      @RequestParam(value = "boardNo", required = false) Long boardNo
  ) {
    Map<String, Object> result = new HashMap<>();
    result.put("pageResultDTO", postService.getList(pageRequestDTO, boardNo));
    result.put("pageRequestDTO", pageRequestDTO);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  //  // ✅ 인기순 게시글 목록
  @GetMapping("/list/popular")
  public ResponseEntity<PageResultDTO<PostDTO, Object[]>> getPopularArtList(
          @RequestParam(defaultValue = "1") int page,
          @RequestParam(defaultValue = "15") int size) {
    PageRequestDTO pageRequestDTO = new PageRequestDTO();
    pageRequestDTO.setPage(page);
    pageRequestDTO.setSize(size);
    return ResponseEntity.ok(postService.getPopularArtList(pageRequestDTO));
  }

  // ✅ 등록일시 기준으로 최신순 게시글 목록 (페이징 + 검색)
  @GetMapping("/list/latest")
  public ResponseEntity<Map<String, Object>> listLatest(
      PageRequestDTO pageRequestDTO,
      @RequestParam(value = "boardNo", required = false) Long boardNo
  ) {
    log.info("📨 최신순 게시글 목록 요청 - pageRequestDTO: {}, boardNo: {}", pageRequestDTO, boardNo);
    Map<String, Object> result = new HashMap<>();
    // 새로 추가된 getLatestList 서비스 메소드 호출
    result.put("pageResultDTO", postService.getLatestList(pageRequestDTO, boardNo));
    result.put("pageRequestDTO", pageRequestDTO);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  // ✅ 게시글 등록
  @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
  public ResponseEntity<Long> registerPost(@RequestBody PostDTO postDTO, @AuthenticationPrincipal UserAuthDTO user) {
    log.info("🔥 /register 요청 도착 by {}", user.getUsername());

    // 🔥 여기서 writerId 세팅
    postDTO.setUserId(user.getUserId());

    log.info("등록 요청: {}", postDTO);

    Long postId = postService.register(postDTO);
    log.info("📦 게시글 등록 완료, postId: {}", postId);
    return new ResponseEntity<>(postId, HttpStatus.CREATED);
  }

  // ✅ 게시글 상세 조회 (읽기 또는 수정용)
  @GetMapping({"/read/{postId}", "/modify/{postId}"})
  public ResponseEntity<Map<String, PostDTO>> getPost(
      @PathVariable("postId") Long postId,
      @AuthenticationPrincipal UserAuthDTO user
      ) {
    log.info("📨 게시글 조회 요청 - postId: {}, 요청자: {}", postId, user.getUsername());

    PostDTO postDTO = postService.get(postId);
    Map<String, PostDTO> result = new HashMap<>();
    result.put("postDTO", postDTO);

    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  // ✅ 게시글 수정
  @PutMapping("/modify")
  public ResponseEntity<Map<String, String>> modify(
      @RequestBody PostDTO dto,
      @AuthenticationPrincipal UserAuthDTO user) {
    log.info("✏️ 게시글 수정 요청 - postId: {}, 요청자: {}", dto.getPostId(), user.getUsername());
    postService.modify(dto);
    Map<String, String> result = new HashMap<>();
    result.put("msg", dto.getPostId() + " 수정 완료");
    result.put("postId", String.valueOf(dto.getPostId()));
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  // ✅ 조회수 증가 API
  @PostMapping("/increaseViews/{postId}")
  public ResponseEntity<String> increaseViews(@PathVariable Long postId) {
    log.info("📊 조회수 증가 요청 - postId: {}", postId);
    postService.increaseViews(postId);
    return ResponseEntity.ok("조회수 증가 성공");
  }

  // ✅ 게시글 삭제
  @DeleteMapping("/remove/{postId}")
  public ResponseEntity<Map<String, String>> remove(@PathVariable Long postId) {
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

    result.put("msg", postId + " 삭제 완료");
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}

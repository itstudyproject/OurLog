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
<<<<<<< Updated upstream
  private final PostService postService;
=======
  private final PostService PostService;
>>>>>>> Stashed changes

  @Value("${com.example.upload.path}")
  private String uploadPath;

  private void typeKeywordInit(PageRequestDTO pageRequestDTO) {
    if (pageRequestDTO.getType().equals("null")) pageRequestDTO.setType("");
    if (pageRequestDTO.getKeyword().equals("null")) pageRequestDTO.setKeyword("");
  }

  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> list(PageRequestDTO pageRequestDTO) {
    Map<String, Object> result = new HashMap<>();
<<<<<<< Updated upstream
    result.put("pageResultDTO", postService.getList(pageRequestDTO));
=======
    result.put("pageResultDTO", PostService.getList(pageRequestDTO));
>>>>>>> Stashed changes
    result.put("pageRequestDTO", pageRequestDTO);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
  public ResponseEntity<Long> registerPost(@RequestBody PostDTO postDTO) {
    System.out.println(">>>"+postDTO);

<<<<<<< Updated upstream
    Long postId = postService.register(postDTO);
=======
    Long postId = PostService.register(postDTO);
>>>>>>> Stashed changes
    return new ResponseEntity<>(postId, HttpStatus.OK);
  }

  @GetMapping(value = {"/read/{postId}", "/modify/{postId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, PostDTO>> getPost(@PathVariable("postId") Long postId) {
<<<<<<< Updated upstream
    PostDTO postDTO = postService.get(postId);
=======
    PostDTO postDTO = PostService.get(postId);
>>>>>>> Stashed changes
    Map<String, PostDTO> result = new HashMap<>();
    result.put("postDTO", postDTO);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @PutMapping(value = "/modify", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, String>> modify(@RequestBody PostDTO dto) {
    log.info("modify post... dto: " + dto);
<<<<<<< Updated upstream
    postService.modify(dto);
=======
    PostService.modify(dto);
>>>>>>> Stashed changes
    Map<String, String> result = new HashMap<>();
    result.put("msg", dto.getPostId() + " 수정");
    result.put("postId", dto.getPostId() + "");
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @DeleteMapping(value = "/remove/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, String>> remove(
      @PathVariable Long postId, @RequestBody PageRequestDTO pageRequestDTO) {

    Map<String, String> result = new HashMap<>();
<<<<<<< Updated upstream
    List<String> photoList = postService.removeWithReplyAndPicture(postId);
=======
    List<String> photoList = PostService.removeWithReplyAndPicture(postId);
>>>>>>> Stashed changes
    photoList.forEach(fileName -> {
      try {
        log.info("removeFile............" + fileName);
        String srcFileName = URLDecoder.decode(fileName, "UTF-8");
        File file = new File(uploadPath + File.separator + srcFileName);
        file.delete();
        File thumb = new File(file.getParent(), "s_" + file.getName());
        thumb.delete();
      } catch (Exception e) {
        log.info("remove file : " + e.getMessage());
      }
    });
<<<<<<< Updated upstream
    if (postService.getList(pageRequestDTO).getDtoList().size() == 0 && pageRequestDTO.getPage() != 1) {
=======
    if (PostService.getList(pageRequestDTO).getDtoList().size() == 0 && pageRequestDTO.getPage() != 1) {
>>>>>>> Stashed changes
      pageRequestDTO.setPage(pageRequestDTO.getPage() - 1);
    }
    typeKeywordInit(pageRequestDTO);
    result.put("msg", postId + " 삭제");
    result.put("page", pageRequestDTO.getPage() + "");
    result.put("type", pageRequestDTO.getType() + "");
    result.put("keyword", pageRequestDTO.getKeyword() + "");
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}

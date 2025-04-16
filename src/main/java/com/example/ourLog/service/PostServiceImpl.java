package com.example.ourLog.service;

import com.example.ourLog.dto.PictureDTO;
import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.dto.PageRequestDTO;
import com.example.ourLog.dto.PageResultDTO;
import com.example.ourLog.entity.Picture;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import com.example.ourLog.repository.PictureRepository;
import com.example.ourLog.repository.PostRepository;
import com.example.ourLog.repository.ReplyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URLDecoder;
import java.util.*;
import java.util.function.Function;

@Service
@Log4j2
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

  private final PostRepository postRepository;
  private final PictureRepository pictureRepository;
  private final ReplyRepository replyRepository;

  @Value("${com.example.upload.path}")
  private String uploadPath;

  @Override
  public PageResultDTO<PostDTO, Object[]> getList(PageRequestDTO pageRequestDTO) {
    Pageable pageable = pageRequestDTO.getPageable(Sort.by("postId").descending());

    Page<Object[]> result = postRepository.searchPage(
        pageRequestDTO.getType(),
        pageRequestDTO.getKeyword(),
        pageable
    );

    Function<Object[], PostDTO> fn = (arr -> entityToDTO(
        (Post) arr[0],
        List.of((Picture) arr[1]),
        (User) arr[2],
        (Long) arr[3]
    ));

    return new PageResultDTO<>(result, fn);
  }

  @Transactional
  @Override
  public Long register(PostDTO postDTO) {
    Map<String, Object> entityMap = dtoToEntity(postDTO);
    Post post = (Post) entityMap.get("post");

    postRepository.save(post);

    List<PictureDTO> pictureDTOList = postDTO.getPictureDTOList();
    if (pictureDTOList != null && !pictureDTOList.isEmpty()) {
      for (PictureDTO pictureDTO : pictureDTOList) {
        Picture picture = pictureRepository.findByUuid(pictureDTO.getUuid());
        if (picture != null && picture.getPostId() == null) {
          picture.setPostId(post);
          pictureRepository.save(picture);
        }
      }
    }

    return post.getPostId();
  }

  @Transactional
  @Override
  public void modify(PostDTO postDTO) {
    Optional<Post> result = postRepository.findById(postDTO.getPostId());
    if (result.isPresent()) {
      Post post = result.get();

      post.changeTitle(postDTO.getTitle());
      post.changeContent(postDTO.getContent());
      postRepository.save(post);

      List<Picture> oldPictures = pictureRepository.findByPostId(post.getPostId());
      List<String> newUUIDList = postDTO.getPictureDTOList()
          .stream()
          .map(PictureDTO::getUuid)
          .toList();

      for (Picture oldPicture : oldPictures) {
        if (!newUUIDList.contains(oldPicture.getUuid())) {
          pictureRepository.deleteByUuid(oldPicture.getUuid());
        }
      }

      for (String uuid : newUUIDList) {
        Picture picture = pictureRepository.findByUuid(uuid);
        if (picture != null && (picture.getPostId() == null || !picture.getPostId().equals(post))) {
          picture.setPostId(post);
          pictureRepository.save(picture);
        }
      }
    }
  }

  @Transactional
  @Override
  public List<String> removeWithReplyAndPicture(Long postId) {
    List<Picture> pictureList = pictureRepository.findByPostId(postId);
    List<String> removedFileNames = new ArrayList<>();

    for (Picture picture : pictureList) {
      removedFileNames.add(picture.getPath() + File.separator + picture.getUuid() + "_" + picture.getPicName());
      pictureRepository.deleteByUuid(picture.getUuid());
    }

    replyRepository.deleteByPostId(postId);
    postRepository.deleteById(postId);

    return removedFileNames;
  }

  @Override
  public void removePictureByUUID(String uuid) {
    pictureRepository.deleteByUuid(uuid);
  }

  // ✅ 최종 추가된 get() 구현
  @Override
  public PostDTO get(Long postId) {
    List<Object[]> result = postRepository.getPostWithAll(postId);
    if (result == null || result.isEmpty()) return null;

    Post post = (Post) result.get(0)[0];
    List<Picture> pictureList = new ArrayList<>();
    for (Object[] arr : result) {
      pictureList.add((Picture) arr[1]);
    }

    User user = (User) result.get(0)[2];
    Long replyCnt = (Long) result.get(0)[3];

    return entityToDTO(post, pictureList, user, replyCnt);
  }
}

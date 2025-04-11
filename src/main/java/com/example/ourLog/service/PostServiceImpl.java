package com.example.ourLog.service;

import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.dto.PageRequestDTO;
import com.example.ourLog.dto.PageResultDTO;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import com.example.ourLog.entity.Picture;
import com.example.ourLog.repository.ReplyRepository;
import com.example.ourLog.repository.PostRepository;
import com.example.ourLog.repository.PictureRepository;
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
import java.util.function.Consumer;
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
    Page<Object[]> result = postRepository.searchPage(pageRequestDTO.getType(),
        pageRequestDTO.getKeyword(),
        pageable);

    Function<Object[], PostDTO> fn = new Function<Object[], PostDTO>() {
      @Override
      public PostDTO apply(Object[] objects) {
        // post, picture, user, reply.count()
        return entityToDTO(
            (Post) objects[0],
            (List<Picture>) (Arrays.asList((Picture) objects[1])),
            (User) objects[2],
            (Long) objects[3]
        );
      }
    };
    return new PageResultDTO<>(result, fn);
  }

  @Override
  public Long register(PostDTO postDTO) {
    Map<String, Object> entityMap = dtoToEntity(postDTO);

    Post post = (Post) entityMap.get("post");
    postRepository.save(post);

    List<Picture> pictureList = (List<Picture>) entityMap.get("pictureList");
    pictureList.forEach(picture -> {
      pictureRepository.save(picture);
    });

    return post.getPostId();
  }

  @Override
  public PostDTO get(Long postId) {
    List<Object[]> result = postRepository.getPostWithAll(postId);
    Post post = (Post) result.get(0)[0];

    List<Picture> pictureList = new ArrayList<>();
    result.forEach(new Consumer<Object[]>() {
      @Override
      public void accept(Object[] objects) {
        pictureList.add((Picture) objects[1]);
      }
    });
    User user = (User) result.get(0)[2];

    Long replyCnt = (Long) result.get(0)[3];

    return entityToDTO(post, pictureList, user, replyCnt);
  }

  @Transactional
  @Override
  public void modify(PostDTO postDTO) {
    Optional<Post> result = postRepository.findById(postDTO.getPostId());
    if (result.isPresent()) {
      postDTO.setUserDTO(postDTO.getUserDTO());
      Map<String, Object> entityMap = dtoToEntity(postDTO);
      Post post = (Post) entityMap.get("post");
      post.changeTitle(postDTO.getTitle());
      post.changeContent(postDTO.getContent());
      postRepository.save(post);

      List<Picture> newPictureList =
          (List<Picture>) entityMap.get("pictureList");
      List<Picture> oldPictureList =
          pictureRepository.findByPostId(post.getPostId());

      if (newPictureList == null || newPictureList.size() == 0) {
        // 수정창에서 이미지 모두를 지웠을 때
        pictureRepository.deleteByPostId(post.getPostId());
        for (int i = 0; i < oldPictureList.size(); i++) {
          Picture oldPicture = oldPictureList.get(i);
          String fileName = oldPicture.getPath() + File.separator
              + oldPicture.getUuid() + "_" + oldPicture.getPicName();
          deleteFile(fileName);
        }
      } else { // newPictureList에 일부 변화 발생
        newPictureList.forEach(picture -> {
          boolean result1 = false;
          for (int i = 0; i < oldPictureList.size(); i++) {
            result1 = oldPictureList.get(i).getUuid().equals(picture.getUuid());
            if (result1) break;
          }
          if (!result1) pictureRepository.save(picture);
        });
        oldPictureList.forEach(oldPicture -> {
          boolean result1 = false;
          for (int i = 0; i < newPictureList.size(); i++) {
            result1 = newPictureList.get(i).getUuid().equals(oldPicture.getUuid());
            if (result1) break;
          }
          if (!result1) {
            pictureRepository.deleteByUuid(oldPicture.getUuid());
            String fileName = oldPicture.getPath() + File.separator
                + oldPicture.getUuid() + "_" + oldPicture.getPicName();
            deleteFile(fileName);
          }
        });
      }
    }
  }

  private void deleteFile(String fileName) {
    // 실제 파일도 지우기
    String searchFilename = null;
    try {
      searchFilename = URLDecoder.decode(fileName, "UTF-8");
      File file = new File(uploadPath + File.separator + searchFilename);
      file.delete();
      new File(file.getParent(), "s_" + file.getName()).delete();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  @Transactional
  @Override
  public List<String> removeWithReplyAndPicture(Long postId) {
    List<Picture> list = pictureRepository.findByPostId(postId);
    List<String> result = new ArrayList<>();
    list.forEach(new Consumer<Picture>() {
      @Override
      public void accept(Picture p) {
        result.add(p.getPath() + File.separator + p.getUuid() + "_" + p.getPicName());
      }
    });
    pictureRepository.deleteByPostId(postId);
    replyRepository.deleteByPostId(postId);
    postRepository.deleteById(postId);
    return result;
  }

  @Override
  public void removePictureByUUID(String uuid) {
    pictureRepository.deleteByUuid(uuid);
  }
}

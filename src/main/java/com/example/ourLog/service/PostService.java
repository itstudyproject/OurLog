package com.example.ourLog.service;

import com.example.ourLog.dto.*;
import com.example.ourLog.entity.*;

import java.util.*;
import java.util.stream.Collectors;

public interface PostService {

  // ✅ 이 entityToDTO 메소드를 기준으로 DTO 변환 로직을 구현합니다.
  // PictureDTO에 resizedImagePath와 originImagePath를 추가하고,
  // PostDTO의 단일 이미지 경로 필드(uuid, path, fileName 등) 설정은 PictureDTOList를 사용하도록 정리합니다.
  // PostService 인터페이스에 선언된 메소드를 오버라이드합니다.
  // ✨ 참고: PostService 인터페이스에 default로 선언된 entityToDTO 메소드는 제거하거나 주석 처리하여 혼동을 막는 것이 좋습니다.
  PostDTO entityToDTO(Post post, List<Picture> pictureList, User user, Trade trade);

  // PostService.java
  PageResultDTO<PostDTO, Object[]> getList(PageRequestDTO pageRequestDTO, Long boardNo);

  // 🔥 인기순 게시글 목록 조회 (조회수 기준)
//  PageResultDTO<PostDTO, Object[]> getPopularList(PageRequestDTO pageRequestDTO);


  Long register(PostDTO postDTO);

  PostDTO get(Long postId);

  void modify(PostDTO postDTO);

  List<String> removeWithReplyAndPicture(Long postId);

  void removePictureByUUID(String uuid);

  // ✅ 전체 게시글 가져오기 (페이징 없이)
  List<PostDTO> getAllPosts();

  List<PostDTO> getPostByUserId(Long userId);

  default Map<String, Object> dtoToEntity(PostDTO postDTO) {
    Map<String, Object> entityMap = new HashMap<>();


    Post post = Post.builder()
        .postId(postDTO.getPostId())
        .title(postDTO.getTitle())
        .content(postDTO.getContent())
        .tag(postDTO.getTag())
        .fileName(postDTO.getFileName())
        .boardNo(postDTO.getBoardNo())
        .user(User.builder()
            .userId(postDTO.getUserId())
            .nickname(postDTO.getNickname())
            .build())
        .build();

    entityMap.put("post", post);

    List<PictureDTO> pictureDTOList = postDTO.getPictureDTOList();
    if (pictureDTOList != null && !pictureDTOList.isEmpty()) {
      List<Picture> pictureList = pictureDTOList.stream()
          .map(dto -> Picture.builder()
              .uuid(dto.getUuid())
              .picName(dto.getPicName())
              .path(dto.getPath())
              .post(null)
              .build())
          .collect(Collectors.toList());
      entityMap.put("pictureList", pictureList);
    }

    return entityMap;
  }

  // ✨ Entity → DTO 변환

  default PostDTO entityToDTO(Post post, List<Picture> pictureList, User user, Trade trade, Long favoriteCount) {
    System.out.println("favoriteCount received: " + favoriteCount);
    // 유저 DTO 생성
    UserDTO userDTO = UserDTO.builder()
        .favoriteCnt(favoriteCount)
        .userId(user.getUserId())
        .nickname(user.getNickname())
        .build();

    // 🔥 유저 프로필 DTO 생성
    UserProfileDTO userProfileDTO = null;
    if (post.getUserProfile() != null) {
      User profileUser = post.getUserProfile().getUser();
      userProfileDTO = UserProfileDTO.builder()
          .profileId(post.getUserProfile().getProfileId())
          .userId(profileUser.getUserId()) // ✅ 수정된 부분
          .introduction(post.getUserProfile().getIntroduction())
          .originImagePath(post.getUserProfile().getOriginImagePath())
          .thumbnailImagePath(post.getUserProfile().getThumbnailImagePath())
          .build();
    }

    // ✅ Trade 엔티티가 null이 아니면 TradeDTO 생성하여 설정
    TradeDTO tradeDTO = null;
    if (trade != null) {
      // Trade 엔티티의 user 필드에서 판매자 ID와 닉네임 가져오기
      Long sellerId = trade.getUser() != null ? trade.getUser().getUserId() : null;
      // 판매자 닉네임 가져오기 (DTO에 sellerNickname이 있다면)
      // String sellerNickname = trade.getUser() != null ? trade.getUser().getNickname() : null;

      // 최신 입찰자 정보 가져오기 (bidHistory 컬렉션에서 최신 Bid 찾기)
      Long bidderId = null;
      String bidderNickname = null;
      // Trade 엔티티에 Bid 목록이 로딩되어 있다면 (fetch type 주의)
      if (trade.getBidHistory() != null && !trade.getBidHistory().isEmpty()) {
        // bidHistory는 정렬되어 있지 않을 수 있으므로 가장 최근 입찰을 찾아야 함
        // Bid 엔티티에 입찰 시간 필드가 있다면 그 기준으로 정렬
        // 여기서는 예시로 목록의 마지막 요소 (가장 최근에 추가된 것으로 가정) 사용 또는 별도의 로직 필요
        Bid latestBid = trade.getBidHistory().stream()
            .max(Comparator.comparing(Bid::getBidTime)) // Bid 엔티티에 getBidTime() 메서드가 있다고 가정
            .orElse(null);
        if (latestBid != null && latestBid.getUser() != null) {
          bidderId = latestBid.getUser().getUserId();
          bidderNickname = latestBid.getUser().getNickname();
        }
      }
      tradeDTO = TradeDTO.builder()
          .tradeId(trade.getTradeId())
          .postId(trade.getPost() != null ? trade.getPost().getPostId() : null) // Post 객체에서 postId 가져오기
          .sellerId(sellerId)
          .bidderId(bidderId)
          .bidderNickname(bidderNickname)
          .startPrice(trade.getStartPrice())
          .highestBid(trade.getHighestBid())
          .nowBuy(trade.getNowBuy())
          .tradeStatus(trade.isTradeStatus())
          .startBidTime(trade.getRegDate())
          .lastBidTime(trade.getEndTime())
          .build();
    }

    // PostDTO 생성
    PostDTO postDTO = PostDTO.builder()
        .postId(post.getPostId())
        .title(post.getTitle())
        .content(post.getContent())
        .tag(post.getTag())
        .fileName(post.getFileName())
        .boardNo(post.getBoardNo())
        .replyCnt(post.getReplyCnt())
        .views(post.getViews())
        .followers(post.getFollowers())
        .downloads(post.getDownloads())
        .userId(post.getUser().getUserId())
        .nickname(post.getUser().getNickname())
        .favoriteCnt(favoriteCount)
        .profileImage(post.getUserProfile() != null ? post.getUserProfile().getThumbnailImagePath() : null)
        // ✅ thumbnailImagePath 설정: pictureList에서 썸네일 경로 찾기
        .thumbnailImagePath(pictureList != null ?
            pictureList.stream()
                .filter(p -> p != null && p.getThumbnailImagePath() != null)
                .findFirst() // 첫 번째 썸네일 찾기
                .map(p -> {
                  return p.getThumbnailImagePath();
                })
                .orElse(null) // 썸네일 없으면 null
            : null)
        // 중간 크기 이미지 경로 설정
        .resizedImagePath(pictureList != null ?
            pictureList.stream()
                .filter(p -> p != null && p.getResizedImagePath() != null)
                .findFirst()
                .map(Picture::getResizedImagePath)
                .orElse(null)
            : null)
        // 원본 이미지 경로 설정
        .originImagePath(pictureList != null ?
            pictureList.stream()
                .filter(Objects::nonNull)
                .map(Picture::getOriginImagePath)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
            : null)
        .tradeDTO(tradeDTO)
        .regDate(post.getRegDate())
        .modDate(post.getModDate())
        .build();

    if (post.getUserProfile() != null) {
      System.out.println("== userProfile is not null ==");
      System.out.println("== nickname: " + post.getUserProfile().getUser().getNickname());
    }

    if (pictureList != null && !pictureList.isEmpty()) {
      List<PictureDTO> pictureDTOList = pictureList.stream()
          .filter(p -> p != null)
          .map(p -> PictureDTO.builder()
              .uuid(p.getUuid())
              .picName(p.getPicName())
              .path(p.getPath())
              .originImagePath(p.getOriginImagePath())
              .thumbnailImagePath(p.getThumbnailImagePath())
              .resizedImagePath(p.getResizedImagePath())
              .build())
          .collect(Collectors.toList());

      postDTO.setPictureDTOList(pictureDTOList);
    }

    return postDTO;
  }


  void increaseViews(Long postId);
}

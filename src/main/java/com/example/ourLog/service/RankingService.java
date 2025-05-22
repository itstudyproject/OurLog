package com.example.ourLog.service;

import com.example.ourLog.dto.PictureDTO;
import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.dto.TradeDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.entity.*;
import com.example.ourLog.repository.PictureRepository;
import com.example.ourLog.repository.PostRepository;
import com.example.ourLog.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

  private final PostRepository postRepository;
  private final PictureRepository pictureRepository;
  private final TradeRepository tradeRepository;

  public List<PostDTO> getRankingBy(String type) {
    try {
      List<Post> posts;

      switch (type.toLowerCase()) {
        case "followers":
        case "follow":
          posts = postRepository.findByFollowersDesc(5);
          break;
        case "downloads":
        case "download":
          posts = postRepository.findByDownloadsDesc(5);
          break;
        case "views":
        case "view":
        default:
          posts = postRepository.findByViewsDesc(5);
          break;
      }

      return posts.stream()
          .map(this::postToDTO)
          .collect(Collectors.toList());

    } catch (Exception e) {
      System.out.println("🔥 예외 발생: " + e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }



  private PostDTO postToDTO(Post post) {
    User user = post.getUser();
    Trade trade = post.getTrades().get(0);

    List<Picture> pictureList = pictureRepository.findByPostId(post.getPostId());

    // Picture 엔티티 목록을 PictureDTO 목록으로 변환합니다.
    List<PictureDTO> pictureDTOList = pictureList.stream()
        .map(picture -> PictureDTO.builder()
            .uuid(picture.getUuid())
            .picName(picture.getPicName())
            .path(picture.getPath())
            .originImagePath(picture.getOriginImagePath())
            .thumbnailImagePath(picture.getThumbnailImagePath())
            .resizedImagePath(picture.getResizedImagePath()) // resizedImagePath 포함
            .build())
        .collect(Collectors.toList());

    // 프론트엔드에서 사용하기 편리하도록 첫 번째 이미지의 경로를 PostDTO 최상위 필드에 설정합니다.
    // pictureList가 비어 있지 않은 경우에만 설정합니다.
    String firstThumbnailPath = null;
    String firstResizedPath = null;
    String firstOriginPathSingle = null;
    String firstFileName = null;

    if (!pictureList.isEmpty()) {
      Picture firstPicture = pictureList.get(0);
      firstThumbnailPath = firstPicture.getThumbnailImagePath();
      firstResizedPath = firstPicture.getResizedImagePath();
      firstOriginPathSingle = firstPicture.getOriginImagePath();
      firstFileName = firstPicture.getPicName(); // Picture 엔티티의 picName 필드가 파일 이름이라고 가정
    }

    // 모든 Picture들의 원본 이미지 경로 목록을 만듭니다.
    List<String> allOriginImagePaths = pictureList.stream()
        .map(Picture::getOriginImagePath)
        .filter(path -> path != null && !path.isEmpty())
        .collect(Collectors.toList());
    if (allOriginImagePaths.isEmpty()) {
      allOriginImagePaths = Collections.emptyList();
    }

    // ✅ 로딩된 Trade 엔티티로 TradeDTO를 생성합니다.
    TradeDTO tradeDTO = null;
    if (trade != null) { // Trade 엔티티가 존재하는 경우 (Fetch Join으로 로딩된 경우)
      // PostServiceImpl.entityToDTO 로직 참고하여 TradeDTO 생성
      // Trade 엔티티에 User가 Fetch Join되어 있지 않다면 여기서 N+1 발생 가능성
      Long sellerId = trade.getUser() != null ? trade.getUser().getUserId() : null;

      // BidHistory는 Fetch Join하지 않았다면 여기서 null이거나 지연 로딩될 수 있습니다.
      // Ranking에서는 최고 입찰가만 필요하다면 BidHistory Fetch Join은 필수는 아닐 수 있습니다.
      Long bidderId = null;
      String bidderNickname = null;
      // 필요시 BidHistory 로딩 및 최신 Bid information extraction logic


      tradeDTO = TradeDTO.builder()
          .tradeId(trade.getTradeId())
          .postId(trade.getPost() != null ? trade.getPost().getPostId() : null) // Post 객체에서 postId 가져오기
          .sellerId(sellerId)
          .bidderId(bidderId) // 최신 입찰자 ID (필요시)
          .bidderNickname(bidderNickname) // 최신 입찰자 닉네임 (필요시)
          .startPrice(trade.getStartPrice())
          .highestBid(trade.getHighestBid()) // ✅ highestBid 설정
          .nowBuy(trade.getNowBuy()) // 즉시 구매가 (필요시)
          .tradeStatus(trade.isTradeStatus()) // 거래 상태 (필요시)
          .startBidTime(trade.getRegDate()) // 경매 시작 시간 (필요시)
          .lastBidTime(trade.getEndTime()) // 경매 종료 시간 (필요시)
          .build();
    }

    // PostDTO를 빌드하여 반환합니다.
    return PostDTO.builder()
        .postId(post.getPostId())
        .title(post.getTitle())
        .content(post.getContent())
        .views(post.getViews())
        .downloads(post.getDownloads())
        .followers(post.getFollowers())
        .tag(post.getTag())
        .fileName(firstFileName) // 첫 번째 파일 이름 설정
        .boardNo(post.getBoardNo())
        .replyCnt(post.getReplyCnt()) // replyCnt 필드가 Post 엔티티에 있다고 가정
        .regDate(post.getRegDate())
        .modDate(post.getModDate())
        .nickname(user.getNickname()) // User 엔티티에서 닉네임 가져옴
        .thumbnailImagePath(firstThumbnailPath) // 첫 번째 썸네일 경로 설정
        .resizedImagePath(firstResizedPath) // 첫 번째 중간 크기 이미지 경로 설정 (추가)
        .originImagePath(allOriginImagePaths) // 첫 번째 원본 이미지 경로 설정 (추가)
        .pictureDTOList(pictureDTOList) // PictureDTO 목록 설정 (추가)
        // tradeDTO, userProfileDTO 등 RankingService에서 필요 없는 필드는 설정하지 않습니다.
        .tradeDTO(tradeDTO)
        .build();
  }
}

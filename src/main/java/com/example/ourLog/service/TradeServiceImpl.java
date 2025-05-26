package com.example.ourLog.service;

import com.example.ourLog.dto.PictureDTO;
import com.example.ourLog.dto.PostDTO;
import com.example.ourLog.dto.TradeDTO;
import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.entity.*;
import com.example.ourLog.repository.*;
import com.example.ourLog.security.dto.UserAuthDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

  private final TradeRepository tradeRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final BidRepository bidRepository;
  private final PictureRepository pictureRepository;

  // 경매 조회
  @Override
  @Transactional
  public TradeDTO getTradeByPost(Post post) {
    Trade trade = tradeRepository.findByPost(post)
        .orElseThrow(() -> new RuntimeException("관련된 거래가 존재하지 않습니다."));

    return TradeDTO.builder()
        .tradeId(trade.getTradeId())
        .postId(trade.getPost().getPostId())
        .startPrice(trade.getStartPrice())
        .highestBid(trade.getHighestBid())
        .nowBuy(trade.getNowBuy())
        .tradeStatus(trade.isTradeStatus())
        .regDate(trade.getRegDate())
        .startBidTime(trade.getRegDate())
        .lastBidTime(trade.getEndTime())
        .build();
  }

  @Override
  public List<TradeDTO> getTradeByUserId(Long userId) {
    List<Trade> trades = tradeRepository.findByUser_UserId(userId);

    if (trades.isEmpty()) {
      throw new RuntimeException("해당 사용자의 거래 내역이 없습니다");
    }

    return trades.stream()
        .map(trade -> TradeDTO.builder()
            .tradeId(trade.getTradeId())
            .postId(trade.getPost().getPostId())
            .sellerId(trade.getUser().getUserId())
            .startPrice(trade.getStartPrice())
            .highestBid(trade.getHighestBid())
            .nowBuy(trade.getNowBuy())
            .tradeStatus(trade.isTradeStatus())
            .build())
        .collect(Collectors.toList());
  }

  // 경매 등록
  @Override
  @Transactional
  public Trade bidRegist(TradeDTO dto) {
    Post post = postRepository.findById(dto.getPostId())
        .orElseThrow(() -> new RuntimeException("그림이 존재하지 않습니다."));
    User seller = userRepository.findById(dto.getSellerId())
        .orElseThrow(() -> new RuntimeException("판매자가 존재하지 않습니다."));

    LocalDateTime adjustedEndTime = null;
    if (dto.getLastBidTime() != null) {
      // dto.getLastBidTime()은 이미 LocalDateTime 타입입니다.
      // 여기에 9시간을 더해줍니다.
      adjustedEndTime = dto.getLastBidTime().plusHours(9);
    } else {
      // 종료 시간이 DTO에 없으면 현재 시간에 9시간 더한 값 사용 (또는 오류 처리)
      adjustedEndTime = LocalDateTime.now().plusHours(9);
      // 또는 throw new RuntimeException("경매 종료 시간이 지정되지 않았습니다.");
    }


    Trade trade = Trade.builder()
        .post(post)
        .user(seller)
        .startPrice(dto.getStartPrice())
        .highestBid(dto.getStartPrice()) // 시작가는 최고입찰가로 초기화
        .nowBuy(dto.getNowBuy())
        .endTime(adjustedEndTime)
        .tradeStatus(false)
        .build();

    return tradeRepository.save(trade);
  }

  // 입찰 갱신
  @Override
  @Transactional
  public String bidUpdate(Long tradeId, TradeDTO dto, UserAuthDTO currentBidder) {
    Trade trade = tradeRepository.findById(tradeId)
        .orElseThrow(() -> new RuntimeException("거래가 존재하지 않습니다."));

    if (trade.isTradeStatus()) {
      throw new RuntimeException("종료된 경매입니다.");
    }

    // 입찰가 기본 검증
    if (dto.getBidAmount() == null || dto.getBidAmount() <= 0) {
      throw new RuntimeException("올바른 입찰 금액을 입력해주세요.");
    }

    if (dto.getBidAmount() % 1000 != 0) {
      throw new RuntimeException("입찰가는 1000원 단위로 입력해야 합니다.");
    }

    // 최소 입찰가 검증
    Long minBidAmount = trade.getHighestBid() + 1000; // 최소 1000원 이상 높게
    if (dto.getBidAmount() < minBidAmount) {
      throw new RuntimeException("입찰가는 현재 최고가보다 1000원 이상 높아야 합니다.");
    }

    // 즉시 구매가와 입찰금액이 동일할 경우
    if (trade.getNowBuy() != null && dto.getBidAmount() != null && dto.getBidAmount().equals(trade.getNowBuy())) {
      // ✅ 추가: 즉시 구매가와 같은 경우 특정 문자열 반환하여 프론트엔드에 알림
      return "EQUALS_NOW_BUY";
    }

    // 입찰자 정보
    User bidder = userRepository.findById(currentBidder.getUserId())
        .orElseThrow(() -> new RuntimeException("입찰자가 존재하지 않습니다."));

    // 판매자 본인 입찰 방지
    if (bidder.getUserId().equals(trade.getUser().getUserId())) {
      throw new RuntimeException("판매자는 본인의 경매에 입찰할 수 없습니다.");
    }

    // 최고 입찰가 갱신
    trade.setHighestBid(dto.getBidAmount());

    Bid bid = Bid.builder()
        .amount(dto.getBidAmount())
        .trade(trade)
        .user(bidder)
        .bidTime(LocalDateTime.now())
        .build();

    bidRepository.save(bid);
    tradeRepository.save(trade);

    return "입찰이 등록되었습니다.";
  }

  // 즉시 구매
  @Override
  @Transactional
  public String nowBuy(Long tradeId, Long userId) {
    Trade trade = tradeRepository.findById(tradeId)
        .orElseThrow(() -> new RuntimeException("거래가 존재하지 않습니다."));

    if (trade.isTradeStatus()) {
      throw new RuntimeException("종료된 거래입니다.");
    }

    if (trade.getNowBuy() == null) {
      throw new RuntimeException("즉시 구매가가 설정되지 않은 상품입니다.");
    }

    User buyer = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자 정보가 존재하지 않습니다."));

    Bid bid = Bid.builder()
        .amount(trade.getNowBuy())
        .trade(trade)
        .user(buyer)
        .bidTime(LocalDateTime.now())
        .build();
    bidRepository.save(bid);

    incrementDownloadCount(trade.getPost().getPostId(), userId);

    trade.setHighestBid(trade.getNowBuy());
    trade.setTradeStatus(true);
    tradeRepository.save(trade);


    return "즉시구매가 완료되었습니다.";
  }


  // 경매 종료
  @Override
  @Transactional
  public String bidClose(Long tradeId) {
    Trade trade = tradeRepository.findById(tradeId)
        .orElseThrow(() -> new RuntimeException("거래가 존재하지 않습니다."));

    if (trade.isTradeStatus()) {
      throw new RuntimeException("이미 종료된 거래입니다.");
    }

    // 거래 종료
    trade.setTradeStatus(true);
    trade.setEndTime(LocalDateTime.now());
    tradeRepository.save(trade);

    if (trade.getHighestBid() != null && trade.getHighestBid() > 0) { // highestBid가 0보다 큰지도 확인 필요할 수 있습니다.
      // 최고 입찰자 (낙찰자) 찾기 - bid 테이블에서 해당 trade의 최고 입찰가와 동일한 bid 기록을 찾습니다.
      Optional<Bid> winningBidOptional = bidRepository.findTopByTradeOrderByBidTimeDesc(trade)
          .filter(bid -> bid.getAmount() != null && bid.getAmount().equals(trade.getHighestBid()));

      if (winningBidOptional.isPresent()) {
        User winner = winningBidOptional.get().getUser();
        Long postId = trade.getPost().getPostId();
        Long winnerId = winner.getUserId();

        // 다운로드 수 증가 (사용자당 1회 체크는 incrementDownloadCount 내부에서 처리)
        incrementDownloadCount(postId, winnerId);
        return "경매가 낙찰 종료되었습니다. 낙찰자: " + winner.getNickname();
      } else {
        // 최고 입찰가는 있지만 해당 Bid 기록을 못 찾은 경우 (비정상 상황 또는 낙찰 기준 불일치)
        System.err.println("Error: Trade " + tradeId + " has highest bid but no matching Bid record found for winner.");
        return "경매가 종료되었으나 낙찰자 정보를 찾는데 문제가 발생했습니다.";
      }

    } else {
      // highestBid가 null이거나 0 이하면 유찰
      return "경매가 유찰 종료되었습니다.";
    }
  }


  // 구매 목록 조회 (현재 입찰 중, 낙찰받은 목록)
  @Override
  public Map<String, List<TradeDTO>> getPurchaseList(Long userId) {
    // 현재 입찰 중인 경매 조회
    List<Trade> currentBidTrades = bidRepository.findCurrentBidTradesByUserId(userId);

    // 낙찰받은 경매 조회
    List<Trade> wonTrades = bidRepository.findWonTradesByUserId(userId);

    // TradeDTO로 변환
    Function<Trade, TradeDTO> tradeToDtoMapper = trade -> {
      // 해당 Trade와 연관된 Post를 가져옵니다.
      Post post = trade.getPost(); // Trade 엔티티에 Post 연관 관계가 있으므로 직접 접근 가능

      // Post와 연관된 Picture 목록을 가져옵니다. (N+1 문제가 발생할 수 있으므로 효율적인 쿼리 필요)
      // 여기서는 간단히 findByPostId를 사용하지만, 실제 운영 환경에서는 Fetch Join 등을 고려해야 합니다.
      List<Picture> pictureList = pictureRepository.findByPostId(post.getPostId());

      String imageUrl = null;
      if (pictureList != null && !pictureList.isEmpty()) {
        Picture firstPicture = pictureList.get(0);
        // resizedImagePath -> thumbnailImagePath -> originImagePath 순으로 사용
        if (firstPicture.getResizedImagePath() != null) {
          imageUrl = "/ourlog/picture/display/" + firstPicture.getResizedImagePath();
        } else if (firstPicture.getThumbnailImagePath() != null) {
          imageUrl = "/ourlog/picture/display/" + firstPicture.getThumbnailImagePath();
        } else if (firstPicture.getOriginImagePath() != null) {
          imageUrl = "/ourlog/picture/display/" + firstPicture.getOriginImagePath();
        }
      }

      // 최근 입찰 정보 조회
      Optional<Bid> lastBid = bidRepository.findTopByTradeOrderByBidTimeDesc(trade);

      return TradeDTO.builder()
          .tradeId(trade.getTradeId())
          .postId(trade.getPost().getPostId())
          .startPrice(trade.getStartPrice())
          .highestBid(trade.getHighestBid())
          .nowBuy(trade.getNowBuy())
          .tradeStatus(trade.isTradeStatus())
          .postTitle(trade.getPost().getTitle())
          .sellerId(trade.getUser().getUserId())
          .startBidTime(trade.getRegDate())
          .lastBidTime(trade.getEndTime())
          .bidderId(lastBid.map(bid -> bid.getUser().getUserId()).orElse(null))
          .bidderNickname(lastBid.map(bid -> bid.getUser().getNickname()).orElse(null))
          .postImage(imageUrl)
          .build();
    };

    // 결과 맵 생성
    Map<String, List<TradeDTO>> purchaseList = new HashMap<>();
    purchaseList.put("currentBids", currentBidTrades.stream()
        .map(tradeToDtoMapper)
        .collect(Collectors.toList()));
    purchaseList.put("wonTrades", wonTrades.stream()
        .map(tradeToDtoMapper)
        .collect(Collectors.toList()));

    return purchaseList;
  }

  // 랭킹(다운로드수)
  @Override
  public List<Map<String, Object>> getTradeRanking() {
    List<Post> rankingPosts = postRepository.findRankingByDownloads(); // List<Post> 반환

    // RankingPage에서 필요한 형식(List<Map<String, Object>>)으로 변환
    return rankingPosts.stream().map(post -> {
      Map<String, Object> map = new HashMap<>();
      map.put("id", post.getPostId()); // RankingPage에서 'id'로 사용
      map.put("title", post.getTitle());
      // User 정보는 Post 엔티티의 연관 관계를 통해 가져옵니다. (N+1 주의 필요)
      map.put("author", post.getUser() != null ? post.getUser().getNickname() : "익명");
      map.put("downloads", post.getDownloads() != null ? post.getDownloads() : 0);
      map.put("views", post.getViews() != null ? post.getViews() : 0);
      // map.put("followers", ?); // Post 엔티티에 followers 필드가 있다면 추가

      // 이미지 URL 생성 (Post 엔티티에 Picture 목록이 로딩되어 있어야 함)
      String imageUrl = null;
      // Post 엔티티의 pictureDTOList 필드가 @OneToMany 관계이고 로딩 가능하다면 사용
      if (post.getPictureList() != null && !post.getPictureList().isEmpty()) {
        Picture firstPic = post.getPictureList().get(0);
        // Picture 엔티티의 경로 필드명을 확인하여 적절히 조합
        if (firstPic.getResizedImagePath() != null) {
          imageUrl = "/ourlog/picture/display/" + firstPic.getResizedImagePath();
        } else if (firstPic.getThumbnailImagePath() != null) {
          imageUrl = "/ourlog/picture/display/" + firstPic.getThumbnailImagePath();
        } else if (firstPic.getOriginImagePath() != null) {
          imageUrl = "/ourlog/picture/display/" + firstPic.getOriginImagePath();
        }
      }
      map.put("imageSrc", imageUrl);

      // 아바타 URL 생성 (Post 엔티티의 User 연관 관계를 통해 User의 profileImage 필드를 가져옴)
      String avatarUrl = null;
      User author = post.getUser(); // 작가(User) 엔티티 가져오기
      if (author != null && author.getUserProfile() != null) {
        UserProfile userProfile = author.getUserProfile();
        // ✅ UserProfile 엔티티의 프로필 이미지 경로 필드에 맞는 getter 호출
        // UserProfile 엔티티에 getProfileImagePath() 메소드가 있다고 가정합니다.
        if (userProfile.getThumbnailImagePath() != null) { // 예시: UserProfile에 profileImagePath 필드가 있다고 가정
          avatarUrl = "/ourlog/picture/display/" + userProfile.getThumbnailImagePath(); // 경로 조합 확인 (예: /ourlog/picture/display/path/to/image.jpg)
        } else {
          // UserProfile은 있지만 프로필 이미지가 설정되지 않은 경우의 처리
          avatarUrl = "/images/default-avatar.png"; // 기본 아바타 경로
        }
      } else {
        // User 연관 관계가 없거나 UserProfile 연관 관계가 없는 경우
        avatarUrl = "/images/default-avatar.png"; // 기본 아바타 경로
      }
      map.put("avatar", avatarUrl);

      // RankingPage에서 pictureDTOList를 직접 사용하는 경우를 위해 Post 엔티티에서 가져와 포함
      map.put("pictureDTOList", post.getPictureList()); // Post 엔티티에 @OneToMany List<Picture> pictureDTOList 필드가 있어야 함


      return map;
    }).collect(Collectors.toList());
  }

  // ✅ 다운로드 수 증가를 위한 내부 도우미 메소드 (BidRepository 활용)
  @Transactional // 필요에 따라 트랜잭션 관리
  private void incrementDownloadCount(Long postId, Long userId) {
    // 특정 사용자가 특정 게시글을 이미 낙찰/즉시구매했는지 확인 (Bid 테이블 활용)
    boolean alreadyAcquired = bidRepository.existsSuccessfulBidForPostAndUser(postId, userId);

    if (!alreadyAcquired) {
      // 처음으로 획득한 경우에만 다운로드 수 증가
      Optional<Post> postOptional = postRepository.findById(postId);
      if (postOptional.isPresent()) {
        Post post = postOptional.get();
        // downloads 필드 증가 (null 체크하여 초기값 설정)
        post.setDownloads(post.getDownloads() != null ? post.getDownloads() + 1 : 1);
        postRepository.save(post);
        System.out.println("Downloads increased for Post ID " + postId + " by User ID " + userId);
      } else {
        System.err.println("Error: Post with ID " + postId + " not found when trying to increment downloads.");
      }
    } else {
      System.out.println("Downloads not increased: User ID " + userId + " already acquired Post ID " + postId);
    }
  }

  // 판매 목록 조회 (진행 중, 종료된 경매 포함)
  @Override
  public List<TradeDTO> getSalesList(Long userId) {
    List<Trade> salesList = tradeRepository.findByUser_UserIdOrderByRegDateDesc(userId);

//    if (salesList.isEmpty()) {
//      throw new RuntimeException("판매 내역이 없습니다");
//    }

    return salesList.stream()
        .map(trade -> {
          // 최근 입찰 정보 조회
          Post post = trade.getPost(); // Trade 엔티티에 Post 연관 관계가 있다고 가정

          // Post와 연관된 Picture 목록을 가져옵니다.
          // (N+1 문제가 발생할 수 있으므로 효율적인 쿼리나 Fetch Join 고려)
          List<Picture> pictureList = post != null ? post.getPictureList() : null; // Post 엔티티에 getPictureList()가 있다고 가정

          String imageUrl = null;
          if (pictureList != null && !pictureList.isEmpty()) {
            Picture firstPicture = pictureList.get(0);
            // Picture 엔티티의 경로 필드명을 확인하여 적절히 조합
            if (firstPicture.getResizedImagePath() != null) {
              imageUrl = "/ourlog/picture/display/" + firstPicture.getResizedImagePath();
            } else if (firstPicture.getThumbnailImagePath() != null) {
              imageUrl = "/ourlog/picture/display/" + firstPicture.getThumbnailImagePath();
            } else if (firstPicture.getOriginImagePath() != null) {
              imageUrl = "/ourlog/picture/display/" + firstPicture.getOriginImagePath();
            }
          }


          // 최근 입찰 정보 조회 (필요하다면)
          Optional<Bid> lastBid = bidRepository.findTopByTradeOrderByBidTimeDesc(trade);


          return TradeDTO.builder()
              .tradeId(trade.getTradeId())
              .postId(trade.getPost().getPostId())
              .sellerId(trade.getUser().getUserId())
              .startPrice(trade.getStartPrice())
              .highestBid(trade.getHighestBid()) // 판매 완료 시에는 이게 판매가가 됩니다.
              .nowBuy(trade.getNowBuy())
              .tradeStatus(trade.isTradeStatus())
              .lastBidTime(trade.getEndTime()) // 판매 목록에서는 경매 종료 시간을 표시하는 것이 적절할 수 있습니다.
              // 입찰자 정보는 판매 목록에서 중요하지 않을 수 있지만, 필요하다면 추가
              .bidderId(lastBid.map(bid -> bid.getUser().getUserId()).orElse(null))
              .bidderNickname(lastBid.map(bid -> bid.getUser().getNickname()).orElse(null))
              // ✅ postTitle과 postImage 필드 추가
              .postTitle(post != null ? post.getTitle() : "제목 없음")
              .postImage(imageUrl) // 위에서 생성한 이미지 URL 사용
              .build();
        })
        .collect(Collectors.toList());
  }
}

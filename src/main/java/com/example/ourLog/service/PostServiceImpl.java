package com.example.ourLog.service;

import com.example.ourLog.dto.*;
import com.example.ourLog.entity.*;
import com.example.ourLog.repository.PictureRepository;
import com.example.ourLog.repository.PostRepository;
import com.example.ourLog.repository.ReplyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2 // Log4j2 사용 시 필요
@RequiredArgsConstructor // final 필드 생성자 자동 생성
@Transactional // 기본 트랜잭션 처리
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final ReplyService replyService;
    private final PictureRepository pictureRepository;
    private final ReplyRepository replyRepository;

    @Value("${com.example.upload.path}")
    private String uploadPath; // 파일 업로드 경로 추가 (코드에 이미 있음)

    //================================================================================================================
    // ⚡️ DTO 변환 로직 수정
    // ================================================================================================================
    // ✅ 이 entityToDTO 메소드를 기준으로 DTO 변환 로직을 구현합니다.
    // PictureDTO에 resizedImagePath와 originImagePath를 추가하고,
    // PostDTO의 단일 이미지 경로 필드(uuid, path, fileName 등) 설정은 PictureDTOList를 사용하도록 정리합니다.
    @Override // PostService 인터페이스에 선언된 메소드를 오버라이드합니다.
    // ✨ 참고: PostService 인터페이스에 default로 선언된 entityToDTO 메소드는 제거하거나 주석 처리하여 혼동을 막는 것이 좋습니다.
    public PostDTO entityToDTO(Post post, List<Picture> pictureList, User user, Trade trade, UserProfile userProfile) {
        // PostDTO 기본 정보 빌드
        PostDTO dto = PostDTO.builder()
                .postId(post.getPostId())
                .boardNo(post.getBoardNo())
                .title(post.getTitle())
                .content(post.getContent())
                .regDate(post.getRegDate())
                .modDate(post.getModDate())
                .views(post.getViews())
                .tag(post.getTag())
                .userId(user.getUserId()) // Post 엔티티의 user 필드에서 userId 가져와 설정
                .nickname(user.getNickname()) // Post 엔티티의 user 필드에서 nickname 가져와 설정
                // ✅ favoriteCnt는 Favorite 엔티티와의 연관 관계 및 집계 로직에 따라 달라집니다. 현재 Favorite.builder().build().getFavoriteCnt()는 0을 반환할 것입니다. 실제 좋아요 수를 가져오는 로직으로 수정 필요합니다.
                .userProfile(userProfile != null ? UserProfileDTO.builder()
                        .profileId(userProfile.getProfileId())
                        .introduction(userProfile.getIntroduction())
                        .originImagePath(userProfile.getOriginImagePath())
                        .thumbnailImagePath(userProfile.getThumbnailImagePath())
                        .followCnt(userProfile.getFollowCnt())
                        .followingCnt(userProfile.getFollowingCnt())
                        .build() : null)

                // ✅ 프로필 이미지 경로는 UserProfile 엔티티에서 가져옵니다.
                .profileImage(user != null && user.getUserProfile() != null ?
                        user.getUserProfile().getThumbnailImagePath() : null)
                // ❌ PostDTO 자체의 단일 이미지 경로 필드는 PictureDTOList를 사용하므로 제거하거나 주석 처리합니다.
                // .uuid(thumbnail.getUuid())
                // .path(thumbnail.getPath())
                // .fileName(thumbnail.getPicName())
                .build();

        // ✅ PictureList를 PictureDTOList로 변환하여 설정
        if (pictureList != null && !pictureList.isEmpty()) {
            List<PictureDTO> pictureDTOs = pictureList.stream()
                    .filter(Objects::nonNull) // null Picture 객체 필터링
                    .map(pic -> PictureDTO.builder()
                    .picId(pic.getPicId()) // Picture 엔티티의 picId 매핑
                    .uuid(pic.getUuid())
                    .path(pic.getPath())
                    .picName(pic.getPicName())
                    // ✅ resizedImagePath와 originImagePath 필드를 Picture 엔티티에서 가져와 매핑합니다.
                    .resizedImagePath(pic.getResizedImagePath())
                    .originImagePath(pic.getOriginImagePath())
                    // 필요하다면 다른 Picture 필드(downloads 등)도 추가 매핑
                    // .downloads(pic.getDownloads())
                    .build())
                    .collect(Collectors.toList());
            dto.setPictureDTOList(pictureDTOs);

            // ✅ PostDTO의 fileName 필드는 대표 썸네일의 파일명으로 설정할 수 있습니다.
            // pictureList에서 post.getFileName()과 일치하는 Picture를 찾거나 첫 번째 Picture의 파일명을 설정합니다.
            Picture representativePicture = pictureList.stream()
                    .filter(pic -> pic != null && pic.getPicName() != null && pic.getPicName().equals(post.getFileName()))
                    .findFirst()
                    .orElse(!pictureList.isEmpty() ? pictureList.get(0) : null); // 파일명이 일치하는 것이 없으면 첫 번째 Picture 선택

            if (representativePicture != null) {
                dto.setFileName(representativePicture.getUuid()); // 대표 파일명 설정
                // 필요하다면 대표 이미지의 uuid, path도 여기에 설정 가능 (하지만 pictureDTOList에서 찾아서 사용하는게 더 일관적일 수 있습니다)
                // dto.setUuid(representativePicture.getUuid());
                // dto.setPath(representativePicture.getPath());
            } else {
                dto.setFileName(null); // 대표 이미지가 없으면 null
            }

        } else {
            dto.setPictureDTOList(Collections.emptyList()); // Picture List가 null이면 빈 리스트 설정
            dto.setFileName(null); // Picture List가 없으면 파일명도 null
        }

        // ✅ Trade 엔티티가 null이 아니면 TradeDTO 생성하여 설정합니다.
        // 이 로직은 기존 코드에서 잘 되어 있으나, bidderId/bidderNickname을 BidHistory에서 찾는 로직은 Bid 엔티티 및 연관 설정에 따라 구현이 필요합니다.
        // 현재는 매개변수로 받은 단일 Trade 객체(대표 Trade)를 매핑합니다.
        TradeDTO tradeDTO = null;
        if (trade != null) {
            // Trade 엔티티의 user 필드에서 판매자 ID 가져오기
            Long sellerId = trade.getUser() != null ? trade.getUser().getUserId() : null;
            Long bidderId = null; // TradeDTO 필드에 이미 있는 bidderId 사용
            String bidderNickname = null; // TradeDTO 필드에 이미 있는 bidderNickname 사용

            if (trade.getBidHistory() != null && !trade.getBidHistory().isEmpty()) {
                // bidHistory 목록에서 bidTime 기준으로 가장 최신 Bid를 찾습니다.
                Optional<Bid> latestBid = trade.getBidHistory().stream()
                        .filter(bid -> bid != null && bid.getBidTime() != null) // null 필터링 및 bidTime 존재 확인
                        .max(Comparator.comparing(Bid::getBidTime)); // Bid 엔티티에 getBidTime() 메소드가 있다고 가정 (Bid 엔티티 코드에서 확인됨)

                // 최신 Bid가 존재하면 해당 Bid의 User(입찰자) 정보를 가져옵니다.
                if (latestBid.isPresent() && latestBid.get().getUser() != null) {
                    User latestBidder = latestBid.get().getUser();
                    bidderId = latestBidder.getUserId(); // User 엔티티에서 userId 가져옴
                    bidderNickname = latestBidder.getNickname(); // User 엔티티에서 nickname 가져옴 (User 엔티티에 getter 필요)
                }
            }

            tradeDTO = TradeDTO.builder()
                    .tradeId(trade.getTradeId())
                    .postId(trade.getPost() != null ? trade.getPost().getPostId() : null) // Post 객체에서 postId 가져오기
                    .sellerId(sellerId) // Trade 엔티티의 user 필드에서 가져온 sellerId
                    .bidderId(bidderId)
                    .bidderNickname(bidderNickname)
                    .startPrice(trade.getStartPrice())
                    .highestBid(trade.getHighestBid())
                    .nowBuy(trade.getNowBuy())
                    .tradeStatus(trade.isTradeStatus()) // boolean 타입 필드는 is로 시작
                    .startBidTime(trade.getRegDate()) // 경매 시작 시간 -> 보통 Trade의 생성일(regDate) 또는 별도 필드
                    .lastBidTime(trade.getEndTime()) // 마지막 입찰 시간 -> 보통 Trade의 종료 시간(endTime) 또는 별도 필드
                    // Trade 엔티티에 lastBidTime 필드가 있다면 그걸 사용하는게 더 정확합니다. TradeDTO에는 lastBidTime 필드가 있습니다.
                    // .lastBidTime(trade.getLastBidTime()) // Trade 엔티티에 lastBidTime 필드가 있다면 이 필드를 사용하세요.
                    .build();
        }
        dto.setTradeDTO(tradeDTO); // 생성된 tradeDTO 또는 null을 PostDTO에 설정
        return dto;
    }

    @Override
    public PageResultDTO<PostDTO, Object[]> getList(PageRequestDTO pageRequestDTO, Long boardNo) {
        Pageable pageable = pageRequestDTO.getPageable(Sort.by("postId").descending());
        Page<Object[]> result;

        // ✅ 1. 통합 검색일 경우: 제목 + 내용 + 태그 + 작성자 (기존 로직 유지)
        if ("all".equalsIgnoreCase(pageRequestDTO.getType())) {
            result = postRepository.searchAllFields(boardNo, pageRequestDTO.getKeyword(), pageable);

            // ✅ 2. 기본: 제목만 검색 (기존 로직 유지)
        } else {
            result = postRepository.searchPage(boardNo, pageRequestDTO.getKeyword(), pageable);
        }

        List<Post> postList = result.getContent().stream()
                .map(arr -> (Post) arr[0])
                .collect(Collectors.toList());

        List<Long> postIds = postList.stream().map(Post::getPostId).collect(Collectors.toList());

        // ✅ Picture와 Trade 엔티티를 Post ID별로 미리 조회하고 맵으로 그룹화합니다. (기존 로직 유지)
        // Picture 엔티티에 Post와의 연관 관계 (@ManyToOne Post post)가 올바르게 설정되어 있어야 합니다.
        List<Picture> pictures = postIds.isEmpty() ? Collections.emptyList() : postRepository.findPicturesByPostIds(postIds);
        Map<Long, List<Picture>> picturesByPostId = pictures.stream()
                .filter(pic -> pic != null && pic.getPost() != null) // null 필터링 및 Post 연관 관계 확인
                .collect(Collectors.groupingBy(picture -> picture.getPost().getPostId()));

        // ✅ Trade 엔티티를 Post ID별로 미리 조회합니다. (기존 로직 유지)
        // Trade 엔티티에 Post와의 연관 관계 (@ManyToOne Post post)가 올바르게 설정되어 있어야 합니다.
        List<Trade> trades = postIds.isEmpty() ? Collections.emptyList() : postRepository.findTradesByPostIds(postIds);

        // ✅⚡️ 수정 부분: 각 Post에 대해 '대표 Trade' 하나를 선정하여 맵으로 만듭니다.
        Map<Long, Trade> representativeTradesByPostId = new HashMap<>();
        Map<Long, List<Trade>> tradesGroupedByPostId = trades.stream()
                .filter(trade -> trade != null && trade.getPost() != null) // null 필터링 및 Post 연관 관계 확인
                .collect(Collectors.groupingBy(trade -> trade.getPost().getPostId())); // Post ID별로 Trade 목록 그룹화

        tradesGroupedByPostId.forEach((postId, tradeListForPost) -> {
            Trade representativeTrade = null;

            // 1. 해당 Post에 대해 현재 진행 중인 경매를 찾습니다.
            // tradeStatus가 false(진행 중)이고, endTime이 현재 시간보다 미래인 Trade를 찾습니다.
            Optional<Trade> activeTrade = tradeListForPost.stream()
                    .filter(trade -> !trade.isTradeStatus() && trade.getEndTime() != null && trade.getEndTime().isAfter(java.time.LocalDateTime.now()))
                    // 활성 경매가 여러 개일 경우를 대비하여 가장 최근에 등록된 경매를 선택 (필요에 따라 정렬 기준 변경 가능)
                    .max(Comparator.comparing(Trade::getRegDate));

            if (activeTrade.isPresent()) {
                representativeTrade = activeTrade.get();
            } else {
                // 2. 진행 중인 경매가 없으면 가장 최근에 종료된 경매를 찾습니다.
                // tradeStatus가 true(종료)이거나, endTime이 현재 시간보다 같거나 이전인 Trade를 찾습니다.
                Optional<Trade> latestEndedTrade = tradeListForPost.stream()
                        .filter(trade -> trade.isTradeStatus() || (trade.getEndTime() != null && !trade.getEndTime().isAfter(java.time.LocalDateTime.now())))
                        // 종료 시간(endTime) 또는 등록일(regDate) 기준으로 가장 최신 Trade를 선택
                        .max(Comparator.comparing(t -> t.getEndTime() != null ? t.getEndTime() : t.getRegDate()));

                if (latestEndedTrade.isPresent()) {
                    representativeTrade = latestEndedTrade.get();
                }
            }
            // 대표 Trade가 선택되었다면 맵에 추가합니다. (Trade가 전혀 없는 Post는 맵에 포함되지 않아 해당 Post의 TradeDTO는 entityToDTO에서 null로 설정됩니다)
            if (representativeTrade != null) {
                representativeTradesByPostId.put(postId, representativeTrade);
            }
        });

        // ✅ PostDTO 목록으로 변환합니다.
        List<PostDTO> postDTOList = postList.stream()
                .map(post -> {
                    // 해당 Post의 Picture 리스트와 '대표 Trade'를 맵에서 가져옵니다.
                    List<Picture> postPictures = picturesByPostId.getOrDefault(post.getPostId(), Collections.emptyList());
                    // 수정된 부분: 대표 Trade 맵에서 가져오기
                    Trade postTrade = representativeTradesByPostId.get(post.getPostId());
                    User user = post.getUser(); // Post 엔티티에 User 연관 관계가 제대로 로딩되어야 합니다.
                    UserProfile userProfile = post.getUserProfile(); // Post 엔티티에 UserProfile 연관 관계가 제대로 로딩되어야 합니다.
                    return entityToDTO(post, postPictures, user, postTrade, userProfile);
                })
                .collect(Collectors.toList());

        // ✅ PageResultDTO를 생성하여 반환합니다.
        return new PageResultDTO<>(result, postDTOList);
    }

    @Override
    public PageResultDTO<PostDTO, Object[]> getPopularArtList(PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable(Sort.by("favoriteCnt").descending());

        Page<Object[]> result = postRepository.getPopularArtList(pageable);

        List<Object[]> content = result.getContent();

        // Picture, Trade, User 등 필요시 미리 조회/매핑 (생략 가능)
        // postIds 추출
        List<Long> postIds = content.stream()
            .map(arr -> ((Post) arr[0]).getPostId())
            .collect(Collectors.toList());

        List<Picture> pictures = postIds.isEmpty() ? Collections.emptyList() : postRepository.findPicturesByPostIds(postIds);
        Map<Long, List<Picture>> picturesByPostId = pictures.stream()
            .filter(pic -> pic != null && pic.getPost() != null)
            .collect(Collectors.groupingBy(picture -> picture.getPost().getPostId()));

        List<Trade> trades = postIds.isEmpty() ? Collections.emptyList() : postRepository.findTradesByPostIds(postIds);
        Map<Long, Trade> representativeTradesByPostId = new HashMap<>();
        Map<Long, List<Trade>> tradesGroupedByPostId = trades.stream()
            .filter(trade -> trade != null && trade.getPost() != null)
            .collect(Collectors.groupingBy(trade -> trade.getPost().getPostId()));

        // 대표 Trade 선정 로직 (생략, 기존과 동일)

        tradesGroupedByPostId.forEach((postId, tradeListForPost) -> {
            Trade representativeTrade = null;
            Optional<Trade> activeTrade = tradeListForPost.stream()
                .filter(trade -> !trade.isTradeStatus() && trade.getEndTime() != null && trade.getEndTime().isAfter(java.time.LocalDateTime.now()))
                .max(Comparator.comparing(Trade::getRegDate));

            if (activeTrade.isPresent()) {
                representativeTrade = activeTrade.get();
            } else {
                Optional<Trade> latestEndedTrade = tradeListForPost.stream()
                    .filter(trade -> trade.isTradeStatus() || (trade.getEndTime() != null && !trade.getEndTime().isAfter(java.time.LocalDateTime.now())))
                    .max(Comparator.comparing(t -> t.getEndTime() != null ? t.getEndTime() : t.getRegDate()));

                if (latestEndedTrade.isPresent()) {
                    representativeTrade = latestEndedTrade.get();
                }
            }
            if (representativeTrade != null) {
                representativeTradesByPostId.put(postId, representativeTrade);
            }
        });

        // PostDTO 목록으로 변환 (여기서 arr[3]의 favoriteCnt를 반드시 할당!)
        List<PostDTO> postDTOList = content.stream()
            .map(arr -> {
                Post post = (Post) arr[0];
                Long favoriteCnt = (Long) arr[3];
                List<Picture> postPictures = picturesByPostId.getOrDefault(post.getPostId(), Collections.emptyList());
                Trade postTrade = representativeTradesByPostId.get(post.getPostId());
                User user = post.getUser();
                UserProfile userProfile = post.getUserProfile();
                PostDTO dto = entityToDTO(post, postPictures, user, postTrade, userProfile);
                dto.setFavoriteCnt(favoriteCnt != null ? favoriteCnt : 0L);
                return dto;
            })
            .collect(Collectors.toList());

        return new PageResultDTO<>(result, postDTOList);
    }

    //================================================================================================================
    // ⚡️ get 메소드 수정: 단일 Post에 대해 '대표 Trade' 선정 로직 추가
    // ================================================================================================================
    @Transactional // 트랜잭션 필요 (readOnly = true도 고려 가능)
    @Override
    public PostDTO get(Long postId) {
        // ✅ postRepository.getPostWithAll 쿼리가 Post, Picture, User, ReplyCount, Trade를 함께 가져옵니다.
        // 이 쿼리가 특정 postId에 대해 여러 Trade 결과를 반환할 수 있으므로 결과를 가공해야 합니다.
        List<Object[]> result = postRepository.getPostWithAll(postId);
        if (result == null || result.isEmpty()) {
            log.warn("❌ 게시글 조회 결과 없음: postId = {}", postId);
            return null;
        }

        // ✅ 쿼리 결과에서 Post, User, ReplyCount는 첫 번째 행에서 가져옵니다.
        Post post = (Post) result.get(0)[0];
        Picture pic = (Picture) result.get(0)[1];
        User user = (User) result.get(0)[2];
        Long replyCnt = (Long) result.get(0)[3];
        UserProfile userProfile = (UserProfile) result.get(0)[4];
        Trade trade = (Trade) result.get(0)[5];

        // ✅ 쿼리 결과에서 해당 Post의 모든 Picture 엔티티를 추출합니다.
        List<Picture> pictureList = result.stream()
                .map(arr -> (Picture) arr[1]) // 각 Object[]의 두 번째 요소가 Picture 엔티티입니다.
                .filter(Objects::nonNull) // null Picture 객체 필터링
                .distinct() // 중복 Picture 엔티티 제거
                .collect(Collectors.toList());

        // ✅ 쿼리 결과에서 해당 Post의 모든 Trade 엔티티를 추출합니다.
        // getPostWithAll 쿼리가 여러 Trade 결과를 반환한다면, result 리스트의 각 행에 Trade가 있을 수 있습니다.
        // 모든 Trade 엔티티를 수집합니다.
        List<Trade> tradeListForPost = result.stream()
                .map(arr -> (Trade) arr[5]) // 각 Object[]의 여섯 번째 요소가 Trade 엔티티입니다.
                .filter(Objects::nonNull) // null Trade 객체 필터링
                .distinct() // 중복 Trade 엔티티 제거
                .collect(Collectors.toList());

        // ✅⚡️ 수정 부분: 추출한 Trade 목록에서 '대표 Trade' 하나를 선정합니다.
        Trade representativeTrade = null;
        if (!tradeListForPost.isEmpty()) {
            // 1. 현재 진행 중인 경매 찾기 (tradeStatus가 false, endTime이 현재보다 미래)
            Optional<Trade> activeTrade = tradeListForPost.stream()
                    .filter(t -> !t.isTradeStatus() && t.getEndTime() != null && t.getEndTime().isAfter(java.time.LocalDateTime.now()))
                    .max(Comparator.comparing(Trade::getRegDate));

            if (activeTrade.isPresent()) {
                representativeTrade = activeTrade.get();
            } else {
                // 2. 진행 중인 경매가 없으면 가장 최근에 종료된 경매 찾기
                Optional<Trade> latestEndedTrade = tradeListForPost.stream()
                        .filter(t -> t.isTradeStatus() || (t.getEndTime() != null && !t.getEndTime().isAfter(java.time.LocalDateTime.now())))
                        // 종료 시간(endTime) 또는 등록일(regDate) 기준으로 가장 최신 Trade를 선택
                        .max(Comparator.comparing(t -> t.getEndTime() != null ? t.getEndTime() : t.getRegDate()));

                if (latestEndedTrade.isPresent()) {
                    representativeTrade = latestEndedTrade.get();
                }
            }
            // representativeTrade는 선택된 Trade 객체 또는 null이 됩니다.
        }
        List<ReplyDTO> replyDTOList = replyService.getList(postId);
        PostDTO dto = entityToDTO(post, pictureList, user, representativeTrade, userProfile);
        dto.setReplyDTOList(replyDTOList);
        // ✅ '대표 Trade'(representativeTrade)를 entityToDTO 메소드에 전달하여 DTO를 생성합니다.
        return dto;
    }

    //================================================================================================================
    // 기존 메소드들 (필요에 따라 수정 검토)
    // ================================================================================================================
    @Transactional // 트랜잭션 필요
    @Override
    public Long register(PostDTO postDTO) {
        log.info("➡️ PostService register 호출: {}", postDTO);
        log.info("✅ 백엔드 PostService register에서 받은 postDTO의 fileName: {}", postDTO.getFileName()); // 디버깅 로그 추가

        Map<String, Object> entityMap = dtoToEntity(postDTO); // dtoToEntity는 PictureDTO의 origin/resized/thumbnail 경로를 사용하지 않음 -> 필요시 수정
        Post post = (Post) entityMap.get("post");

        // ✅ User 엔티티가 Post 엔티티 내부에 제대로 설정되었는지 확인하고 저장
        // dtoToEntity에서 User 객체를 만들어서 설정했지만, 실제 User 엔티티 객체를 DB에서 가져와 설정하는 것이 안전합니다.
        // PostDTO의 userId를 사용하여 User 엔티티를 조회하고 post.setUser()로 설정하는 로직 추가 고려
        // post.setUser(userRepository.findById(postDTO.getUserId()).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.")));
        postRepository.save(post); // Post 저장 (PostId 자동 생성)
        log.info("➡️ Post 엔티티 저장 완료, postId: {}", post.getPostId());

        // ✅ Picture 엔티티에 Post 연결 및 저장
        // postDTO.getPictureDTOList()에 DTO 변환 전의 파일 업로드 정보(uuid, path, picName)가 담겨 있어야 합니다.
        // 이 로직은 Picture 테이블에 이미 저장된 Picture 엔티티를 찾아 Post와의 연관 관계를 설정하는 것으로 보입니다.
        List<PictureDTO> pictureDTOList = postDTO.getPictureDTOList();
        if (pictureDTOList != null && !pictureDTOList.isEmpty()) {
            for (PictureDTO pictureDTO : pictureDTOList) {
                // uuid로 Picture 엔티티를 찾습니다.
                Picture picture = pictureRepository.findByUuid(pictureDTO.getUuid());
                // Picture 엔티티가 존재하고 아직 어떤 Post와도 연결되지 않았다면 현재 Post와 연결합니다.
                if (picture != null && picture.getPost() == null) {
                    picture.setPost(post); // Picture 엔티티에 Post 엔티티 설정
                    pictureRepository.save(picture); // Picture 엔티티 저장/업데이트
                    log.info("➡️ Picture {} 엔티티에 Post {} 연결 및 저장 완료", picture.getUuid(), post.getPostId());
                } else if (picture == null) {
                    log.warn("⚠️ UUID {} 에 해당하는 Picture 엔티티를 찾을 수 없습니다.", pictureDTO.getUuid());
                    // Picture 엔티티가 없으면 등록 실패 또는 관련 Picture 정보 누락 처리 필요
                } else if (picture.getPost() != null) {
                    log.warn("⚠️ UUID {} 에 해당하는 Picture 엔티티는 이미 다른 Post {} 에 연결되어 있습니다.", pictureDTO.getUuid(), picture.getPost().getPostId());
                    // 이미 다른 Post에 연결된 Picture는 무시하거나 에러 처리
                }
            }
        } else {
            log.info("➡️ 등록할 PictureDTOList가 비어있거나 null입니다.");
        }

        // ✅ Trade 정보 등록 로직 추가 (필요시)
        // 현재 dtoToEntity 에는 TradeDTO -> Trade 엔티티 변환 로직이 없습니다.
        // 만약 게시글 등록 시 경매 정보도 함께 등록한다면 이 부분에 로직을 추가해야 합니다.
        // PostDTO에 TradeDTO가 있고, 이를 Trade 엔티티로 변환하여 저장하는 로직이 필요합니다.
        log.info("✅ PostService register 완료, postId: {}", post.getPostId());
        return post.getPostId();
    }

    @Transactional // 트랜잭션 필요
    @Override
    public void modify(PostDTO postDTO) {
        Optional<Post> result = postRepository.findById(postDTO.getPostId());
        if (result.isPresent()) {
            Post post = result.get();
            // ✅ 게시글 기본 정보 수정 (기존 로직 유지)
            post.changeTitle(postDTO.getTitle());
            post.changeContent(postDTO.getContent());
            post.setFileName(postDTO.getFileName()); // 대표 파일명 업데이트
            post.setTag(postDTO.getTag());
            //postRepository.save(post); // 변경 감지 기능으로 인해 명시적 save 없어도 될 수 있으나, 안전을 위해 유지 또는 플러시

            // ✅ Picture 연관 관계 수정 (기존 로직 유지)
            // 수정된 PictureDTOList를 기반으로 기존 Picture 연관 관계를 업데이트하고 불필요한 Picture 삭제
            List<Picture> oldPictures = pictureRepository.findByPostId(post.getPostId());
            List<String> newUUIDList = postDTO.getPictureDTOList() != null
                    ? postDTO.getPictureDTOList().stream().map(PictureDTO::getUuid).filter(Objects::nonNull).toList() : Collections.emptyList();

            // 삭제할 Picture 찾기: 기존 Picture 중 새 목록에 없는 것
            List<Picture> picturesToDelete = oldPictures.stream()
                    .filter(pic -> pic != null && pic.getUuid() != null && !newUUIDList.contains(pic.getUuid()))
                    .collect(Collectors.toList());

            // Picture 및 물리적 파일 삭제
            for (Picture picture : picturesToDelete) {
                // ✅ 물리적 파일 삭제 로직 추가 (removeWithReplyAndPicture 참조)
                try {
                    String filePath = uploadPath + File.separator + picture.getPath() + File.separator + picture.getUuid() + "_" + picture.getPicName();
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.delete(); // 원본 파일 삭제
                    }
                    // 썸네일 파일 경로도 있다면 삭제 (예: 's_' 접두사 사용 시)
                    String thumbnailFilePath = uploadPath + File.separator + picture.getPath() + File.separator + "s_" + picture.getUuid() + "_" + picture.getPicName();
                    File thumbnailFile = new File(thumbnailFilePath);
                    if (thumbnailFile.exists()) {
                        thumbnailFile.delete(); // 썸네일 파일 삭제
                    }
                    log.info("➡️ 물리적 파일 삭제 완료: {}", filePath);
                } catch (Exception e) {
                    log.warn("⚠️ 물리적 파일 삭제 실패: {} - {}", picture.getUuid(), e.getMessage());
                }
                pictureRepository.delete(picture); // Picture 엔티티 삭제
                log.info("➡️ Picture 엔티티 삭제 완료: {}", picture.getUuid());
            }

            // 새로 추가되거나 기존에 연결되지 않았던 Picture 연결
            for (String uuid : newUUIDList) {
                Picture picture = pictureRepository.findByUuid(uuid);
                // Picture 엔티티가 존재하고, 현재 Post와 연결되어 있지 않다면 연결합니다.
                if (picture != null && (picture.getPost() == null || !picture.getPost().getPostId().equals(post.getPostId()))) {
                    picture.setPost(post);
                    pictureRepository.save(picture); // Picture 엔티티 업데이트
                    log.info("➡️ Picture {} 엔티티에 Post {} 연결 및 저장 완료", picture.getUuid(), post.getPostId());
                } else if (picture == null) {
                    log.warn("⚠️ 수정 중 UUID {} 에 해당하는 Picture 엔티티를 찾을 수 없습니다.", uuid);
                }
            }

            postRepository.save(post); // 최종 Post 엔티티 저장
            log.info("✅ PostService modify 완료, postId: {}", postDTO.getPostId());

        } else {
            log.warn("❌ 수정할 게시글을 찾을 수 없습니다: postId = {}", postDTO.getPostId());
            throw new RuntimeException("수정할 게시글이 존재하지 않습니다."); // 또는 적절한 예외 처리
        }
    }

    @Transactional // 트랜잭션 필요
    @Override
    public List<String> removeWithReplyAndPicture(Long postId) {
        // ✅ 게시글과 관련된 댓글, Picture, Trade 정보를 먼저 삭제하고 게시글을 삭제합니다.
        // Picture 삭제 시 물리적 파일도 삭제합니다.

        // Picture 삭제 및 물리적 파일 목록 수집
        List<Picture> pictureList = pictureRepository.findByPostId(postId);
        List<String> removedFilePaths = new ArrayList<>();

        if (pictureList != null) {
            for (Picture picture : pictureList) {
                // ✅ 물리적 파일 경로 생성 및 목록 추가 (uploadPath 사용)
                String originalFilePath = uploadPath + File.separator + picture.getPath() + File.separator + picture.getUuid() + "_" + picture.getPicName();
                removedFilePaths.add(originalFilePath);
                // 썸네일 파일 경로도 있다면 추가 (예: 's_' 접두사 사용 시)
                String thumbnailFilePath = uploadPath + File.separator + picture.getPath() + File.separator + "s_" + picture.getUuid() + "_" + picture.getPicName();
                removedFilePaths.add(thumbnailFilePath);

                // Picture 엔티티 삭제 (orphanRemoval 설정에 따라 Post 삭제 시 자동 삭제될 수도 있지만 명시적으로 삭제)
                pictureRepository.delete(picture);
                log.info("➡️ Picture 엔티티 삭제 요청: {}", picture.getUuid());
            }
            log.info("➡️ 총 {} 개의 Picture 엔티티 삭제 요청 완료", pictureList.size());
        } else {
            log.info("➡️ 삭제할 Picture 엔티티가 없습니다. postId: {}", postId);
        }

        // ✅ 댓글 삭제 (ReplyRepository에 deleteByPostId 메소드 필요)
        try {
            replyRepository.deleteByPostId(postId); // deleteByPostId 메소드가 삭제된 개수를 반환한다고 가정
            log.info("➡️ Post {} 와 관련된 댓글 {} 개 삭제 완료", postId);
        } catch (Exception e) {
            log.error("⚠️ Post {} 와 관련된 댓글 삭제 중 오류 발생: {}", postId, e.getMessage());
            // 오류 발생 시 예외 처리 또는 롤백 고려
        }

        // ✅ Trade 삭제 (Trade 엔티티에 Post와의 연관 관계 및 CascadeType.ALL 또는 orphanRemoval 설정 필요)
        // Post 엔티티의 @OneToMany List<Trade> trades 필드에 CascadeType.ALL 또는 orphanRemoval = true 설정이 되어 있다면
        // Post 삭제 시 연관된 Trade 엔티티들이 자동으로 삭제됩니다.
        // 만약 자동 삭제가 안된다면 여기에서 명시적으로 TradeRepository를 사용하여 삭제해야 합니다.
        // 예: tradeRepository.deleteByPostId(postId); // TradeRepository에 해당 메소드 필요
        // ✅ 최종적으로 Post 삭제
        try {
            postRepository.deleteById(postId);
            log.info("✅ Post {} 삭제 완료", postId);
        } catch (Exception e) {
            log.error("⚠️ Post {} 삭제 중 오류 발생: {}", postId, e.getMessage());
            throw new RuntimeException("게시글 삭제 중 오류가 발생했습니다.", e); // 예외 발생 시 트랜잭션 롤백
        }

        // ✅ 물리적 파일 삭제는 컨트롤러에서 removedFilePaths 리스트를 받아서 처리하도록 기존 로직 유지합니다.
        log.info("✅ PostService removeWithReplyAndPicture 완료, postId: {}", postId);
        return removedFilePaths; // 물리적으로 삭제할 파일 경로 목록 반환
    }

    @Override
    public void removePictureByUUID(String uuid) {
        // ✅ UUID로 Picture 엔티티 삭제 (물리적 파일 삭제 로직 추가 필요)
        Picture picture = pictureRepository.findByUuid(uuid);
        if (picture != null) {
            // 물리적 파일 삭제 로직 (removeWithReplyAndPicture 참조)
            try {
                String originalFilePath = uploadPath + File.separator + picture.getPath() + File.separator + picture.getUuid() + "_" + picture.getPicName();
                File originalFile = new File(originalFilePath);
                if (originalFile.exists()) {
                    originalFile.delete();
                }
                String thumbnailFilePath = uploadPath + File.separator + picture.getPath() + File.separator + "s_" + picture.getUuid() + "_" + picture.getPicName();
                File thumbnailFile = new File(thumbnailFilePath);
                if (thumbnailFile.exists()) {
                    thumbnailFile.delete();
                }
                log.info("➡️ 물리적 파일 삭제 완료 (by UUID): {}", originalFilePath);
            } catch (Exception e) {
                log.warn("⚠️ 물리적 파일 삭제 실패 (by UUID): {} - {}", uuid, e.getMessage());
            }
            pictureRepository.deleteByUuid(uuid); // Picture 엔티티 삭제
            log.info("✅ Picture 엔티티 삭제 완료 (by UUID): {}", uuid);
        } else {
            log.warn("❌ UUID {} 에 해당하는 Picture 엔티티를 찾을 수 없습니다.", uuid);
        }
    }

  // ✅ 전체 게시글 가져오기 (페이징 없이) - Trade 정보는 entityToDTO에서 null로 처리될 수 있습니다.
  @Override
  public List<PostDTO> getAllPosts() {
    List<Post> posts = postRepository.findAll(); // 모든 Post 엔티티 조회
    // PictureList와 User 엔티티는 Post 엔티티 로딩 시 Fetch 전략에 따라 달라집니다.
    // N+1 문제가 발생하지 않도록 Repository 쿼리에서 Fetch Join을 사용하는 것이 좋습니다.

    return posts.stream()
        .map(post -> {
          // 각 Post에 대해 Picture와 User를 가져와 entityToDTO 호출 (Trade는 null 전달)
          // N+1 문제 방지를 위해 여기서 PictureRepository.findByPostId 호출보다는 초기 쿼리에서 Fetch Join 고려
          List<Picture> pictureList = pictureRepository.findByPostId(post.getPostId()); // ⚠️ N+1 문제 발생 가능!
          User user = post.getUser(); // FetchType.LAZY 이면 여기서 N+1 문제 발생 가능!
          return entityToDTO(post, pictureList, user, null, null); // Trade는 null 전달
        })
        .collect(Collectors.toList());
  }

  @Transactional
  @Override
  public List<PostDTO> getPostByUserId(Long userId) {
    log.info("➡️ PostService getPostByUserId 호출: userId={}", userId);

    // ✅ 수정 부분: Repository에서 Post와 Picture를 Fetch Join하여 함께 조회합니다.
    // User 엔티티도 Fetch Join 하려면 PostRepository에 해당 쿼리 추가 후 사용하세요.
    List<Post> postList = postRepository.findPostsWithPicturesByUserId(userId);
    // List<Post> postList = postRepository.findPostsWithPicturesAndUserByUserId(userId); // User도 Fetch Join 시

    if (postList == null || postList.isEmpty()) {
      log.warn("❌ 사용자 ID {} 에 해당하는 게시글 없음", userId);
      return Collections.emptyList(); // 결과가 없으면 빈 리스트 반환
    }

    // ✅ 수정 부분: 조회된 Post 목록을 entityToDTO 헬퍼 메서드를 사용하여 PostDTO 목록으로 변환합니다.
    // entityToDTO 메서드는 Post 엔티티의 pictureList, user 정보를 사용하여 DTO를 구성합니다.
    // 이 경우 Trade 정보는 필요 없으므로 null을 전달합니다.
    return postList.stream()
        .map(post -> {
          // entityToDTO 메서드가 Post 엔티티의 pictureList와 user 정보를 자동으로 사용합니다.
          // 만약 User 정보가 Fetch Join되지 않았다면 post.getUser() 호출 시 N+1 문제가 발생할 수 있습니다.
          // User도 Fetch Join 하는 쿼리를 사용하거나, UserRepository에서 User를 별도로 조회하여 전달하는 것을 고려하세요.
          User user = post.getUser(); // User 엔티티가 로딩되어 있다고 가정
          List<Picture> pictureList = post.getPictureList(); // Picture 엔티티 목록이 로딩되어 있다고 가정
          Trade trade = null; // 이 API에서는 Trade 정보가 필요 없으므로 null
          UserProfile userProfile = post.getUserProfile(); // Post 엔티티에서 UserProfile 연관 관계가 제대로 로딩되어야 합니다.

          return entityToDTO(post, pictureList, user, trade, userProfile);
        })
        .collect(Collectors.toList());
  }

  // ✅ 조회수 증가 메소드 (기존 로직 유지)
  @Override
  public void increaseViews(Long postId) {
      postRepository.increaseViews(postId);
      log.info("✅ postId {} 조회수 증가 처리 요청 완료", postId);
  }
  }


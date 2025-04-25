//package com.example.ourLog.repository;
//
//import com.example.ourLog.entity.Follow;
//import com.example.ourLog.entity.User;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Commit;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.IntStream;
//
//@SpringBootTest
//class FollowRepositoryTests {
//
//  @Autowired
//  FollowRepository followRepository;
//
//  @Autowired
//  UserRepository userRepository;
//
//  @Test
//  @Transactional
//  @Commit
//  public void insertFollowTest() {
//    IntStream.rangeClosed(1, 100).forEach(i -> {
//      Long fromUserId = (long) ((Math.random() * 100) + 1); // 1~100
//      Long toUserId = (long) ((Math.random() * 100) + 1);   // 1~100
//
//      // 자기 자신을 팔로우하지 않도록 필터링
//      if (fromUserId.equals(toUserId)) return;
//
//      Follow follow = Follow.builder()
//              .followCnt(0L)
//              .followingCnt(0L)
//              .fromUser(User.builder().userId(fromUserId).build())
//              .toUser(User.builder().userId(toUserId).build())
//              .build();
//
//      followRepository.save(follow);
//    });
//  }
//
//  @Test
//  @Transactional
//  public void getFollowingListTest() {
//    Long fromUserId = 1L;
//
//    List<Follow> followingList = followRepository.findAllByFromUser(
//            User.builder().userId(fromUserId).build()
//    );
//
//    System.out.println("=== [" + fromUserId + "]번 사용자가 팔로우한 목록 ===");
//    followingList.forEach(follow ->
//            System.out.println("→ " + follow.getToUser().getUserId()));
//  }
//
//  @Test
//  @Transactional
//  public void getFollowerListTest() {
//    Long toUserId = 2L;
//
//    List<Follow> followerList = followRepository.findAllByToUser(
//            User.builder().userId(toUserId).build()
//    );
//
//    System.out.println("=== [" + toUserId + "]번 사용자를 팔로우한 사람 목록 ===");
//    followerList.forEach(follow ->
//            System.out.println("← " + follow.getFromUser().getUserId()));
//  }
//
//
//}

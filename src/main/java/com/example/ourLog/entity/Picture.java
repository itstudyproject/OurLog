  package com.example.ourLog.entity;

  import com.fasterxml.jackson.annotation.JsonProperty;

  import jakarta.persistence.*;
  import lombok.*;

  @Entity
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  @ToString
  @Table(name = "picture") // 이 클래스는 DB의 picture 테이블과 매핑됨
  public class Picture extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // MySQL의 AUTO_INCREMENT처럼 자동 증가 방식 사용
    private Long picId; // 그림 고유 번호 (primary key)

    private String uuid; // 파일 구분을 위한 UUID (중복 방지용, 파일명과 함께 저장됨)

    private String picName; // 원래 업로드된 파일명 (ex. "image.jpg")

    private String path; // 업로드된 파일이 저장된 상대 경로 (ex. "2024/04/20")


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id") // 또는 "owner_nickname"이 진짜 외래키라면 그걸로
    @JsonProperty
    private User user;

    // 이 그림이 연결된 게시글 (Post와 다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id") // DB에서 외래키 컬럼 이름
    @JsonProperty
    private Post post;

    private Long downloads; // 다운로드 수 (해당 그림을 다운로드한 횟수)

    private String tag; // 태그 (검색 필터용)

    private String originImagePath; // 원본 이미지의 전체 경로 (ex. 2024/04/20/uuid_filename.jpg)

    private String thumbnailImagePath; // 썸네일 이미지의 경로 (ex. 2024/04/20/s_uuid_filename.jpg)

    private String resizedImagePath; // 리사이즈된 이미지가 있을 경우 그 경로

  }


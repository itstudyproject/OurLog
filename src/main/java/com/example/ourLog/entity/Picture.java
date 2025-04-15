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

  // 그림의 작성자 닉네임 - 사실상 User의 nickname을 받아올 용도
  @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계, 성능을 위해 지연 로딩 사용
  @JoinColumn(name = "owner_nickname") // 실제 DB에서 외래키 컬럼 이름 지정
  @JsonProperty // JSON 직렬화 시 이 값 포함
  private User userNickname;

  // 그림의 작성자 (userId 기준으로 연관 관계 설정)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  @JsonProperty
  private User userId;

  // 이 그림이 연결된 게시글 (Post와 다대일 관계)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id") // DB에서 외래키 컬럼 이름
  @JsonProperty
  private Post postId;

  private String describe; // 그림 설명 (캡션 같은 역할)

  private Long views; // 조회수 (해당 그림을 본 횟수, 초기값 0L)

  private Long downloads; // 다운로드 수 (해당 그림을 다운로드한 횟수)

  private String tag; // 태그 (검색 필터용)

  private String originImagePath; // 원본 이미지의 전체 경로 (ex. 2024/04/20/uuid_filename.jpg)

  private String thumbnailImagePath; // 썸네일 이미지의 경로 (ex. 2024/04/20/s_uuid_filename.jpg)

  private String resizedImagePath; // 리사이즈된 이미지가 있을 경우 그 경로
}


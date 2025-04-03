package com.example.ourLog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Table(name = "user")

public class User extends BaseEntity{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userId;

  private String email;
  private String password;
  private String name;
  private String nickname;
  private String phoneNo;

}

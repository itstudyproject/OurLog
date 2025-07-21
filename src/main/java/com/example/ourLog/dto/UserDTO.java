package com.example.ourLog.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.example.ourLog.entity.UserRole;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDTO {
  private Long userId;
  
  @NotBlank(message = "이메일은 필수 입력 항목입니다.")
  @Email(message = "유효한 이메일 형식이 아닙니다.")
  private String email;
  
  @NotBlank(message = "이름은 필수 입력 항목입니다.")
  @Size(min = 2, max = 20, message = "이름은 2~20자 사이어야 합니다.")
  private String name;
  
  @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
  @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이어야 합니다.")
  @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다.")
  private String nickname;
  
  @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
  @Size(min = 1, max = 20, message = "비밀번호는 8~20자 사이어야 합니다.")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", 
          message = "비밀번호는 최소 2자 이상이어야 하며, 영문자, 숫자, 특수문자를 포함해야 합니다.")
  private String password;
  
  @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
  @Pattern(regexp = "^01([0|1|6|7|8|9])-?([0-9]{3,4})-?([0-9]{4})$", message = "유효한 전화번호 형식이 아닙니다.")
  private String mobile;
  
  private boolean fromSocial;

  private Long favoriteCnt;
  
  private LocalDateTime regDate;
  private LocalDateTime modDate;
  
  @Builder.Default
  private Set<String> roleSet = new HashSet<>();
}
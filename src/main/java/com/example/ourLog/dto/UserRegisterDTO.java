package com.example.ourLog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class UserRegisterDTO extends UserDTO {
    
    @NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다.")
    private String passwordConfirm;
    
    // 비밀번호 확인 일치 여부 검사 메서드
    public boolean isPasswordMatching() {
        return getPassword() != null && getPassword().equals(passwordConfirm);
    }
} 
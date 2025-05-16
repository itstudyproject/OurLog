package com.example.ourLog.service;

import com.example.ourLog.dto.UserDTO;
import com.example.ourLog.dto.UserProfileDTO;
import com.example.ourLog.entity.User;
import com.example.ourLog.entity.UserRole;

import java.util.function.Function;
import java.util.stream.Collectors;

public interface UserService {
  Long registerUser(UserDTO userDTO);
  Long updateUser(UserDTO userDTO);
  void deleteUser(Long userId);
  UserDTO getUser(Long userId);

    User findByUserId(Long userId);
  UserDTO getUserByEmail(String email);

  default User dtoToEntity(UserDTO userDTO) {
    User user = User.builder()
        .userId(userDTO.getUserId())
        .email(userDTO.getEmail())
        .password(userDTO.getPassword())
        .nickname(userDTO.getNickname())
        .name(userDTO.getName())
        .mobile(userDTO.getMobile())
        .fromSocial(userDTO.isFromSocial())
        .roleSet(userDTO.getRoleSet().stream().map(new Function<String, UserRole>() {
          @Override
          public UserRole apply(String str) {
            if (str.equals("ROLE_GUEST")) return UserRole.GUEST;
            else if (str.equals("ROLE_USER")) return UserRole.USER;
            else if (str.equals("ROLE_ADMIN")) return UserRole.ADMIN;
            else return UserRole.USER;
          }
        }).collect(Collectors.toSet()))
        .build();
    return user;
  }

  default UserDTO entityToDTO(User user) {
    UserDTO userDTO = UserDTO.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .password(user.getPassword())
        .nickname(user.getNickname())
        .name(user.getName())
        .fromSocial(user.isFromSocial())
        .regDate(user.getRegDate())
        .modDate(user.getModDate())
        .roleSet(user.getRoleSet().stream().map(new Function<UserRole, String >() {
          @Override
          public String apply(UserRole userRole) {
            return new String("ROLE_" + userRole.name());
          }
        }).collect(Collectors.toSet()))
        .build();
    return userDTO;
  }


}
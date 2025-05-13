package com.example.ourLog.security.service;

import com.example.ourLog.entity.User;
import com.example.ourLog.repository.UserRepository;
import com.example.ourLog.security.dto.UserAuthDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<User> result = userRepository.findByEmail(username);
    if (!result.isPresent()) throw new UsernameNotFoundException("Check Email or Social");

    User user = result.get();

    List<SimpleGrantedAuthority> authorities = user.getRoleSet().stream()
            .map(membersRole -> new SimpleGrantedAuthority("ROLE_" + membersRole.name()))
            .collect(Collectors.toList());

    log.info("생성되는 권한 리스트: {}", authorities);

    // ✅ userId 및 nickname을 포함하여 생성자 호출
    return new UserAuthDTO(
            user.getEmail(),              // username
            user.getPassword(),          // password
            authorities,                 // roles
            user.getEmail(),             // email
            user.getName(),              // name
            user.getNickname(),          // nickname 추가
            user.isFromSocial(),         // fromSocial
            user.getUserId()             // userId 추가
    );
  }
}

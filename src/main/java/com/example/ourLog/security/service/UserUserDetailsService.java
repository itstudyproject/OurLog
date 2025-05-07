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

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<com.example.ourLog.entity.User> result = userRepository.findByEmail(username, false);
    if (!result.isPresent()) throw new UsernameNotFoundException("Check Email or Social");
    User user = result.get();

    UserAuthDTO userAuthDTO = new UserAuthDTO(
        user.getEmail(), user.getPassword(),
            user.getRoleSet().stream().map(
            membersRole -> new SimpleGrantedAuthority(
                "ROLE_" + membersRole.name()
            )
        ).collect(Collectors.toList()),
            user.getEmail(),
            user.getName(), user.isFromSocial()
    );
    return userAuthDTO;
  }
}

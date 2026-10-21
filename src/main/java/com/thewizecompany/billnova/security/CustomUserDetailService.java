package com.thewizecompany.billnova.security;


import com.thewizecompany.billnova.entity.Users;
import com.thewizecompany.billnova.repository.UserRepository;
import lombok.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @NullMarked
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{

        Users user = userRepository.findByUsernameAndIsDeletedFalseAndIsActiveTrue(
                username.toLowerCase().trim()
        )
                .orElseThrow(() -> {
                    log.debug("User not found with username: {}", username);
                    return new UsernameNotFoundException("Invalid Credentials");
                });

        if (user.isAccountLocked()){
            log.warn("login attempt on locked account: {}", username);
            throw new UsernameNotFoundException("Account is locked");
        }

        return User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name()
                        )
                ))
                .accountLocked(user.isAccountLocked())
                .disabled(!user.isActive())
                .build();
    }

}

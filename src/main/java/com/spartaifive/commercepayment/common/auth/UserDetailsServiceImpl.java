package com.spartaifive.commercepayment.common.auth;

import com.spartaifive.commercepayment.domain.user.entity.User;
import com.spartaifive.commercepayment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetailsImpl loadUserByUsername(String userId) throws  RuntimeException{
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDetailsImpl(user);
    }
    public UserDetailsImpl loadUserByEmail(String email) throws  RuntimeException{
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 못찾음"));

        return new UserDetailsImpl(user);
    }
}

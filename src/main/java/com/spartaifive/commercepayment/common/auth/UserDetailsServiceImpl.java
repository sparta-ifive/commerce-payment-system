package com.spartaifive.commercepayment.common.auth;

import com.spartaifive.commercepayment.common.exception.ServiceErrorException;
import com.spartaifive.commercepayment.domain.user.entity.User;
import com.spartaifive.commercepayment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import static com.spartaifive.commercepayment.common.exception.ErrorCode.ERR_USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetailsImpl loadUserByUsername(String userId) throws  RuntimeException{
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ServiceErrorException(ERR_USER_NOT_FOUND));
        return new UserDetailsImpl(user);
    }
    public UserDetailsImpl loadUserByEmail(String email) throws  RuntimeException{
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceErrorException(ERR_USER_NOT_FOUND));

        return new UserDetailsImpl(user);
    }
}

package com.spartaifive.commercepayment.common.auth;

import com.spartaifive.commercepayment.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails, CredentialsContainer {

    // private final User user;
    private Long userId;
    private String email;
    private String name;
    private String password;

    public UserDetailsImpl(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.password = user.getPassword();
    }

    public UserDetailsImpl(
        Long userId,
        String email,
        String name,
        String password
    ) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.password = password;
    }

    /**
     * 사용자 ID 반환
     * Controller, Service에서 활용
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * 사용자 이메일 반환
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * 사용자 이름 반환
     */
    public String getName() {
        return this.name;
    }

    // 현재는 권한 없음 (필요 시 추가)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email; // 이메일이 아이디로 사용
    }

    //계정 만료 여부
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    //계정 잠금 여부
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    //비밀번호 만료 여부
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    //계정 비활성화 여부
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}

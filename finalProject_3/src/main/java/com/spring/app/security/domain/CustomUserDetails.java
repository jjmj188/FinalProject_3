package com.spring.app.security.domain;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;
    private MemberDTO memberDto;

    public CustomUserDetails(MemberDTO memberDto) {
        this.memberDto = memberDto;
    }

    // 권한 부여 (임시로 모두 일반 유저 권한 부여, 관리자일 경우 분기 가능)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return authorities;
    }

    @Override
    public String getPassword() {
        return memberDto.getPassword();
    }

    @Override
    public String getUsername() {
        return memberDto.getEmail(); // 아이디 대신 이메일을 시큐리티 Username으로 사용!
    }

    // 계정 만료 여부
    @Override
    public boolean isAccountNonExpired() { return true; }

    // 계정 잠김(휴면) 여부 (필요시 DB의 IS_DORMANT 컬럼 활용)
    @Override
    public boolean isAccountNonLocked() { return true; }

    // 비밀번호 만료 여부
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // 계정 활성화(탈퇴) 여부 (필요시 DB의 IS_WITHDRAWN 컬럼 활용)
    @Override
    public boolean isEnabled() { return true; }

    // *** 추가 정보 접근용 getter ***
    public String getMemberName() { return memberDto.getUserName(); }
    public String getNickname() { return memberDto.getNickname(); }
}
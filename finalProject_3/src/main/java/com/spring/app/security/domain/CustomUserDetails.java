package com.spring.app.security.domain;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

    private MemberDTO member;

    public CustomUserDetails(MemberDTO member) {
        this.member = member;
    }

    // MemberDTO의 원본 데이터에 접근하기 위한 getter들
    public MemberDTO getMember() { return member; }
    public String getMemberName() { return member.getUserName(); }
    public String getNickname() { return member.getNickname(); }

    // 시큐리티 필수 구현 메서드들
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        // status 나 권한 컬럼에 따라 분기 (임시로 ROLE_USER 부여)
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); 
        return authorities;
    }

    @Override
    public String getPassword() {
        return member.getPassword(); // DB에 암호화되어 저장된 비밀번호 리턴
    }

    @Override
    public String getUsername() {
        return member.getEmail(); // 아이디로 이메일을 사용하므로 이메일 리턴
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { 
        return member.getStatus() == 0; // 예: 0이면 정상, 1이면 탈퇴 등
    }
}
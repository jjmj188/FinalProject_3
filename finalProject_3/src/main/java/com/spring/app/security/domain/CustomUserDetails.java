package com.spring.app.security.domain;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/*
    Spring Security 의 UserDetails 인터페이스를 구현한 클래스.
    MemberDTO 를 감싸서 인증 정보를 제공한다.
    jwt_jpa_board 의 CustomUserDetails 구조를 따른다.
    - 권한(authorities)은 MemberDTO.getAuthorities() (List<String>) 에서 로드한다.
*/
public class CustomUserDetails implements UserDetails {

    private MemberDTO member;

    public CustomUserDetails(MemberDTO member) {
        this.member = member;
    }

    // MemberDTO 원본 데이터 접근용 getter
    public MemberDTO getMember()      { return member; }
    public String getMemberName()     { return member.getUserName(); }
    public String getNickname()       { return member.getNickname(); }

    /* 권한 목록 — MemberDTO.getAuthorities() (List<String>) 에서 변환 */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<String> authList = member.getAuthorities();
        if (authList == null || authList.isEmpty()) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return authList.stream()
                       .map(SimpleGrantedAuthority::new)
                       .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() {
        return member.getStatus() == 0;
    }
}

package com.spring.app.security.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.spring.app.security.domain.CustomUserDetails;
import com.spring.app.security.domain.MemberDTO;
import com.spring.app.security.model.MemberDAO;

/*
    Spring Security 가 로그인 시 호출하는 UserDetailsService 구현체.
    jwt_jpa_board 패턴: MemberDTO 에 authorities(List<String>) 를 세팅한 뒤
    CustomUserDetails(memberDTO) 단일 생성자로 전달한다.
*/
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private MemberDAO dao;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. 이메일로 회원 조회
        MemberDTO member = dao.findByEmail(email);
        if (member == null) {
            throw new UsernameNotFoundException("해당 이메일을 가진 사용자가 없습니다.");
        }

        // 2. AUTHORITIES 테이블에서 권한 목록 조회
        List<String> authorities = dao.findAuthoritiesByEmail(email);

        // 3. 권한이 없으면 기본 ROLE_USER 부여
        if (authorities.isEmpty()) {
            authorities.add("ROLE_USER");
        }

        // 4. MemberDTO 에 권한 목록 세팅 (jwt_jpa_board 방식)
        member.setAuthorities(authorities);

        System.out.println("====== [loadUserByUsername] ======");
        System.out.println("### email      : " + email);
        System.out.println("### authorities: " + authorities);
        System.out.println("==================================");

        // 5. CustomUserDetails 에 담아서 리턴
        return new CustomUserDetails(member);
    }
}

package com.spring.app.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.spring.app.security.domain.CustomUserDetails;
import com.spring.app.security.domain.MemberDTO;
import com.spring.app.security.model.MemberDAO;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private MemberDAO dao;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. 폼에서 입력한 이메일로 DB 조회 (DAO에 만들어두신 findByEmail 사용!)
        MemberDTO member = dao.findByEmail(email);

        // 2. 일치하는 회원이 없으면 예외 발생 (로그인 실패 처리됨)
        if (member == null) {
            throw new UsernameNotFoundException("해당 이메일을 가진 사용자가 없습니다.");
        }

        // 3. 회원이 있으면 시큐리티 전용 객체인 CustomUserDetails 에 담아서 리턴
        return new CustomUserDetails(member);
    }
}
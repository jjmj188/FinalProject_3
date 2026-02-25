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
    private MemberDAO memberDao;

    // 로그인 처리 메서드
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        // 아이디(username 파라미터)로 이메일이 들어오므로 email로 DB를 조회합니다.
        MemberDTO memberDto = memberDao.findByEmail(email); 
        
        System.out.println("~~ 확인용 로그인 시도 계정: " + email);

        if(memberDto != null) {
            // 사용자가 존재하면 시큐리티 규격에 맞는 CustomUserDetails 객체로 반환
            return new CustomUserDetails(memberDto);
        } else {
            // 사용자가 없으면 예외 발생 (시큐리티가 받아서 로그인 실패 처리)
            throw new UsernameNotFoundException("해당 이메일로 가입된 회원이 없습니다.");
        }
    }
}
package com.spring.app.security.model;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spring.app.security.domain.MemberDTO;

@Repository
public class MemberDAO_imple implements MemberDAO {

    @Autowired
    private SqlSessionTemplate sqlsession;

    @Override
    public int checkEmail(String email) {
        return sqlsession.selectOne("member.checkEmail", email);
    }

    @Override
    public int checkNickname(String nickname) {
        return sqlsession.selectOne("member.checkNickname", nickname);
    }

    @Override
    public int checkPhone(String phone) {
        return sqlsession.selectOne("member.checkPhone", phone);
    }

    @Override
    public int insertMember(MemberDTO member) {
        return sqlsession.insert("member.insertMember", member);
    }

    // 로그인용 데이터 조회
    @Override
    public MemberDTO findByEmail(String email) {
        return sqlsession.selectOne("member.findByEmail", email);
    }
    
 // 이메일 찾기
    @Override
    public String findEmailByPhone(String phone) {
        // "member.findEmailByPhone" 에서 "member" 부분은 XML의 namespace 이름입니다.
        return sqlsession.selectOne("member.findEmailByPhone", phone); 
    }

    // 비밀번호 재설정 (업데이트)
    @Override
    public void updatePasswordByPhone(Map<String, String> paramMap) {
        sqlsession.update("member.updatePasswordByPhone", paramMap);
    }

    // AUTHORITIES 테이블에서 권한 목록 조회
    @Override
    public List<String> findAuthoritiesByEmail(String email) {
        return sqlsession.selectList("member.findAuthoritiesByEmail", email);
    }

    // RefreshToken 저장/업데이트
    @Override
    public void saveRefreshToken(String email, String rtValue) {
        java.util.Map<String, String> paramMap = new java.util.HashMap<>();
        paramMap.put("email", email);
        paramMap.put("rtValue", rtValue);
        sqlsession.insert("member.saveRefreshToken", paramMap);
    }

    // RefreshToken 조회
    @Override
    public String findRefreshTokenByEmail(String email) {
        return sqlsession.selectOne("member.findRefreshTokenByEmail", email);
    }

    // RefreshToken 삭제
    @Override
    public void deleteRefreshToken(String email) {
        sqlsession.delete("member.deleteRefreshToken", email);
    }
}
package com.spring.app.security.model;

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
}
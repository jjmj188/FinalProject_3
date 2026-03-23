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
 
    // 마지막 로그인 일자 갱신
    @Override
    public void updateLastLoginDate(String email) {
        sqlsession.update("member.updateLastLoginDate", email);
    }
 
    // 휴면 처리 배치
    @Override
    public void moveMembersToUserDormant() {
        sqlsession.insert("member.moveMembersToUserDormant");
    }
 
    @Override
    public void setIdleForDormantMembers() {
        sqlsession.update("member.setIdleForDormantMembers");
    }
 
    // 휴면 계정 전화번호 확인
    @Override
    public int checkIdlePhone(String phone) {
        return sqlsession.selectOne("member.checkIdlePhone", phone);
    }
 
    // 휴면 해제
    @Override
    public void reactivateMember(String email) {
        sqlsession.update("member.reactivateMember", email);
    }
 
    @Override
    public void deleteFromUserDormant(String email) {
        sqlsession.delete("member.deleteFromUserDormant", email);
    }
 
    @Override
    public void updateProfile(MemberDTO member) {
        sqlsession.update("member.updateProfile", member);
    }
 
    @Override
    public void withdrawMember(String email) {
        sqlsession.delete("member.withdrawMember", email);
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
 
    // 소셜 로그인 전용 회원 조회
    @Override
    public MemberDTO findByEmailForSocial(String email) {
        return sqlsession.selectOne("member.findByEmailForSocial", email);
    }
 
    // 소셜 로그인 자동 회원가입
    @Override
    public int insertSocialMember(MemberDTO member) {
        return sqlsession.insert("member.insertSocialMember", member);
    }
 
    @Override
    public void insertAuthority(String email) {
        sqlsession.insert("member.insertAuthority", email);
    }
 
	@Override
	public void updateCashBalance(Map<String, Object> paraMap) {
		sqlsession.update("member.updateCashBalance", paraMap);
	}
}
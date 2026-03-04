package com.spring.app.admin.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.spring.app.admin.ad.domain.AdDTO;
import com.spring.app.product.domain.ProductDTO;
import com.spring.app.security.domain.MemberDTO;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminDAO_imple implements AdminDAO {
  
    @Qualifier("sqlsession")
    private final SqlSessionTemplate sqlsession;

    private static final String admin = "admin";

    @Override
    public int insertAd(AdDTO adDto) {
        return sqlsession.insert(admin + ".insertAd", adDto);
    }

    @Override
    public List<MemberDTO> selectMemberList() {
        return sqlsession.selectList(admin + ".selectMemberList");
    }

    @Override
    public int countNewMembers() {
        return sqlsession.selectOne(admin + ".countNewMembers");
    }

    @Override
    public int countWithdrawals() {
        return sqlsession.selectOne(admin + ".countWithdrawals");
    }

    @Override
    public int countTotalMembers() {
        return sqlsession.selectOne(admin + ".countTotalMembers");
    }

	@Override
	public MemberDTO getMemberByNo(int userNo) {
		return sqlsession.selectOne(admin + ".detailMember", userNo);
	}

	@Override
	public List<MemberDTO> selectMemberListPaged(Map<String, Integer> params) {
		return sqlsession.selectList(admin + ".selectMemberListPaged", params);
	}

	
	//상품 리스트 보여주기
	@Override
	public List<ProductDTO> selectProductList(int page, int size) {
		
		int offset=(page-1)*size;
		
		Map<String,Object>params =new HashMap<>();
		params.put("offset", offset);
		params.put("size", size);
		
		return sqlsession.selectList(admin+".selectProduct",params);
	}
	
	//상품카운트
	@Override
	public int selectProductCount() {
		return sqlsession.selectOne(admin+".selectCount");
	}
}







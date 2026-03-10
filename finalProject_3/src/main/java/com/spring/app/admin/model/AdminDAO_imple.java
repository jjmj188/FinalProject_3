package com.spring.app.admin.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.spring.app.admin.domain.AdDTO;
import com.spring.app.admin.domain.InquiryDTO;
import com.spring.app.admin.domain.SearchDTO;
import com.spring.app.admin.domain.StatDTO;
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
	
	@Override
	public int getOnsaleProductCount() {
		return sqlsession.selectOne(admin+".getOnsaleProductCount");
	}
	//===========================================================
	//상품리스트 가져오기
	@Override
	public List<AdDTO> selectAdList() {

		return sqlsession.selectList(admin+".selectAdList");
	}

	//===========================================================
	//광고 상세보기
	@Override
	public AdDTO getAd(Long adId) {
		 return sqlsession.selectOne(admin + ".getAd", adId);
	}
	//광고 승인하기
	@Override
    public void approveAd(Long adId) {
        sqlsession.update(admin + ".approveAd", adId);
		
	}
	//광고 반려하기
	@Override
	public void rejectAd(Long adId, String reason) {
		Map<String,Object>map=new HashMap<>();
		map.put("adId", adId);
		map.put("reason", reason);
		 sqlsession.update(admin + ".rejectAd", map);
		
	}

	@Override
	public List<AdDTO> getConflictAds(Map<String,Object> map) {
	    return sqlsession.selectList("admin.getConflictAds", map);
	}

	@Override
	public List<StatDTO> getUserStats(String type) {
		return sqlsession.selectList("admin.getUserStats", type);
	}

	
	@Override
	public List<SearchDTO> getPopularKeywords() {
		return sqlsession.selectList("admin.getPopularKeywords");
	}
	
	//-----------------------------------------------------------------
	//문의내역 상단고정
	// AdminDAO_imple.java 139라인 근처 수정
	public List<InquiryDTO> getTop3FAQ() {
	   
	    return sqlsession.selectList("admin.getTop3FAQ"); 
	}

	public List<InquiryDTO> getAllInquiries() {
	   
	    return sqlsession.selectList("admin.getAllInquiries");
	}


    @Override
    public int getReportedProductCount() {
        // 신고된 상품의 총 개수(숫자 하나)를 가져옵니다.
        return sqlsession.selectOne("admin.getReportedProductCount");
    }

    @Override
    public List<Map<String, Object>> getDailyProductStats() {
        // 최근 7일간의 날짜별 등록수를 List<Map> 형태로 가져옵니다.
        // 결과 예시: [{REG_DATE: '03-08', CNT: 5}, {REG_DATE: '03-09', CNT: 12}]
        return sqlsession.selectList("admin.getDailyProductStats");
    }

    @Override
    public List<Map<String, Object>> getCategoryProductStats() {
        // 카테고리별 상품 비중을 List<Map> 형태로 가져옵니다.
        // 결과 예시: [{CATEGORY_NAME: '패션', CNT: 20}, {CATEGORY_NAME: '가전', CNT: 15}]
        return sqlsession.selectList("admin.getCategoryProductStats");
    }

    @Override
    public MemberDTO getMemberById(String loginid) {
        return sqlsession.selectOne(admin + ".getMemberById", loginid);
    }

	@Override
	public List<Map<String, Object>> getCategoryProdusctStats() {
		// TODO Auto-generated method stub
		return null;
	}

}







package com.spring.app.admin.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.spring.app.admin.domain.AdDTO;
import com.spring.app.admin.domain.InquiryDTO;
import com.spring.app.admin.domain.SearchDTO;
import com.spring.app.admin.domain.StatDTO;
import com.spring.app.product.domain.ProductDTO;
import com.spring.app.security.domain.MemberDTO;
public interface AdminDAO {

	
	
	int insertAd(AdDTO adDto); //광고 신청 insert하기 
	  //-------------------------------------------------------------------------
	List<MemberDTO> selectMemberList(); //멤버리스트 보기

	MemberDTO getMemberByNo(int userNo); //멤버 한명 보기
	
    int countNewMembers(); //신규회원수
 
    int countWithdrawals(); //최근탈퇴수

    int countTotalMembers();//총멤버

    List<MemberDTO> selectMemberListPaged(Map<String, Integer> params);
	
    //-------------------------------------------------------------------------
    List<ProductDTO> selectProductList(int offset, int size); //상품리스트 보기

    int selectProductCount ();//상품 카운트

	List<AdDTO> selectAdList();//상품리스트 가져오기
	
	int getOnsaleProductCount(); //판매중인 상품만 가져오기
	
	List<Map<String, Object>> getCategoryProductStats();
	  //-------------------------------------------------------------------------
	
	AdDTO getAd(Long adId);

	void approveAd(Long adId);

	void rejectAd(Long adId, String reason);
	 
	List<AdDTO> getConflictAds(Map<String, Object> map); //예정 광고 있는지확인
	 //-------------------------------------------------------------------------
	List<StatDTO> getUserStats(String type);
	List<SearchDTO> getPopularKeywords();//인기검색어 가져오기
	
	//------------------------------------------------------------------
	List<InquiryDTO> getTop3FAQ(); //상단고정 자주묻는질문 가져오기
	List<InquiryDTO> getAllInquiries(); //모든 질문 
	int getReportedProductCount();
	List<Map<String, Object>> getDailyProductStats();
	List<Map<String, Object>> getCategoryProdusctStats();
	MemberDTO getMemberById(String loginId);
	

	

}

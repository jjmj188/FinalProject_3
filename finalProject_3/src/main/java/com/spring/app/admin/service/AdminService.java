package com.spring.app.admin.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.spring.app.admin.domain.AdDTO;
import com.spring.app.admin.domain.InquiryDTO;
import com.spring.app.admin.domain.ProductDetailDTO;
import com.spring.app.admin.domain.SearchDTO;
import com.spring.app.product.domain.ProductDTO;
import com.spring.app.security.domain.MemberDTO;

public interface AdminService {

	int registerAd(AdDTO adDto); //광고 신청 insert하기

    MemberDTO getMemberById(String loginId);// 회원 정보 조회 (아이디로 조회)

    //----------------------------------------------------------------------------------
	 List<MemberDTO> getMemberList();//

	 int getNewMembersCount();  //신규가입자 가져오기

	 int getWithdrawalsCount(); //탈퇴수 가져오기

	 int getTotalMembersCount(); //모든 회원 수

	 int getSuspendedMembersCount(); //정지 회원 수

	 MemberDTO getMemberByNo(int userNo);

	 List<MemberDTO> getMemberList(int page, int size); //회원 보여주기

	 List<MemberDTO> getMemberList(int page, int size, String status, String keyword); //검색 포함 회원 목록

	 int getMemberCount(String status, String keyword); //검색 조건 포함 회원 수

	 void suspendMember(int userNo); //일시정지

	 void unsuspendMember(int userNo); //정지 해제

	 void permanentBanMember(int userNo); //영구정지

	 List<ProductDTO> getMemberActiveProducts(int userNo); //회원의 거래중인 상품

	 List<ProductDTO> getProductList(int page, int size); //상품 리스트 보여주기
	//--------------------------------------------------------------------------------
	 int getTotalProductsCount(); //총상품개수 

	 int getOnsaleProductCount(); //판매중인 상품
	 
	 int getReportedProductCount();

	 Map<String, Object> getDailyProductStats();

	 Map<String, Object> getCategoryProductStats();
	 
	//--------------------------------------------------------------------------------
	 List<AdDTO> getAdList();//광고리스트 가져오기

	 AdDTO getAd(Long adId);//광고 상세보여주기

	 void approvedAd(Long adId); //광고 승인하기

	 void rejectAd(Long adId, String reason);//광고반려하기



	 List<AdDTO> getConflictAds(LocalDate startDate, LocalDate endDate, Long adId);

	 Map<String, Object> getUserRegistrationStats(String type); //신규가입자 

	 List<SearchDTO> getPopularKeywords();//인기검색어 가져오기 

	 List<InquiryDTO> getTop3FAQ();   // 상단고정

	 List<InquiryDTO> getAllInquiries(); //문의 모든 리스트

	 int countPendingReportsAndInquiries();
	 int countPendingAds();
	 int countTodayProducts();
	 long getDailyTradeAmount();
	 Map<String, Object> getWithdrawReasonStats();
	


	 
	



	 



	 // 리뷰 관리
	 Map<String, Object> getReviewListPaged(int page, int size, String keyword);
	 void deleteReview(int reviewNo);

	 // 거래 관리
	 Map<String, Object> getTransactionListPaged(int page, int size);

	 // 신고 관리
	 Map<String, Object> getReportListPaged(int page, int size, String type);
	 void updateReportStatus(int reportId, String status);
	 Map<String, Object> getReportStats();

	 // 문의 관리
	 Map<String, Object> getAdminInquiryListPaged(int page, int size, String status);
	 void saveInquiryAnswer(int inquiryId, String adminAnswer);

	 // 상품 상세
	 ProductDetailDTO getProductDetail(int productNo);

	 // 회계관리
	 Map<String, Object> getAccountingStats();
	 Map<String, Object> getAccountingList(int page, int size);

}

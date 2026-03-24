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

	 void suspendMember(int userNo); //일시정지 (즉시)

	 void unsuspendMember(int userNo); //정지 해제

	 void permanentBanMember(int userNo); //영구정지 (즉시)

	 void scheduleSuspend(int userNo, String email); // 3일 후 일시정지 예약 + 알림

	 void scheduleBan(int userNo, String email); // 3일 후 영구정지 예약 + 알림

	 void processScheduledSuspensions(); // 스케줄러 호출용

	 int hasPendingSuspension(int userNo); // 예약 중 여부

	 List<ProductDTO> getMemberActiveProducts(int userNo); //회원의 거래중인 상품

	 List<ProductDTO> getProductList(int page, int size, String status, String filter); //상품 리스트 보여주기
	 int getProductCount(String status, String filter); //필터 포함 상품 수
	 
	 int getMonthNewMembersCount(); //이달 신규 가입자수 
	 
	 int getIdleMembersCount();//휴면 회원
	 
	 List<Integer> getMemberAgeStats();//연령대별 
	  
	 List<Map<String,Object>> getMemberRegionStats();//지역별 
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

	 void withdrawAd(Long adId, String reason);//광고 조기 철회

	 String getBanner();
	 void updateBanner(String text);

	 List<AdDTO> getActiveAds();

	 List<AdDTO> getConflictAds(LocalDate startDate, LocalDate endDate, Long adId);

	 Map<String, Object> getUserRegistrationStats(String type); //신규가입자 

	 List<SearchDTO> getPopularKeywords();//인기검색어 가져오기 

	 List<InquiryDTO> getTop3FAQ();   // 상단고정

	 List<InquiryDTO> getAllInquiries(); //문의 모든 리스트

	 Map<String, Object> getAdMonthlyStats();
	 int countPendingReportsAndInquiries();
	 int countPendingAds();
	 int countTodayProducts();
	 long getDailyTradeAmount();
	 Map<String, Object> getWithdrawReasonStats();
	


	 
	



	 



	 // 리뷰 관리
	 Map<String, Object> getReviewListPaged(int page, int size, String keyword, String filter, String rating);
	 Map<String, Object> getReviewStats();
	 Map<String, Object> getReviewChartData();
	 void deleteReview(int reviewNo);

	 // 거래 관리
	 Map<String, Object> getTransactionListPaged(int page, int size, String status);
	 Map<String, Object> getTransactionStatusStats();

	 // 신고 관리
	 Map<String, Object> getReportListPaged(int page, int size, String type);
	 void updateReportStatus(int reportId, String status);
	 void sendAdminNotification(String email, String title, String message);
	 com.spring.app.admin.domain.ReportAdminDTO getReportDetail(long reportId);
	 Map<String, Object> getReportStats();

	 // 문의 관리
	 Map<String, Object> getAdminInquiryListPaged(int page, int size, String status);
	 void saveInquiryAnswer(int inquiryId, String adminAnswer);
	 int countPendingInquiries();
	 int countAnsweredInquiries();
	 List<String> getFaqKeywords();

	 // 상품 상세
	 ProductDetailDTO getProductDetail(int productNo);
	 void deleteProduct(int productNo, String sellerEmail, String sellerMsg, String buyerEmail, String buyerMsg);

	 // 회계관리
	 Map<String, Object> getAccountingStats();
	 Map<String, Object> getAccountingList(int page, int size);

	



}

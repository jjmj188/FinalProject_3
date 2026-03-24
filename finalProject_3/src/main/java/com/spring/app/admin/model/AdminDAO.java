package com.spring.app.admin.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.spring.app.admin.domain.AdDTO;
import com.spring.app.admin.domain.InquiryDTO;
import com.spring.app.admin.domain.ProductDetailDTO;
import com.spring.app.admin.domain.ReportAdminDTO;
import com.spring.app.admin.domain.ReviewAdminDTO;
import com.spring.app.admin.domain.SearchDTO;
import com.spring.app.admin.domain.StatDTO;
import com.spring.app.admin.domain.TransactionAdminDTO;
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

    List<MemberDTO> selectMemberListPagedSearch(Map<String, Object> params);

    int countSearchMembers(Map<String, Object> params);

    int countSuspendedMembers();

    void suspendMember(int userNo);

    void unsuspendMember(int userNo);

    void permanentBanMember(int userNo);

    // 정지 예약
    void insertSuspendSchedule(Map<String, Object> params);
    List<Map<String, Object>> getDueSuspensions();
    void deleteSuspendSchedule(int scheduleId);
    int hasPendingSuspension(int userNo);
    void deleteUserSuspendSchedule(int userNo);

    List<ProductDTO> getMemberActiveProducts(int userNo);
    
    int countMonthNewMembers(); //이번달 신규
    int countIdleMembers();     //휴면회원
    Map<String,Object> countByAge();  //연려대별
    List<Map<String,Object>> countByRegion(); //지역별 

    //-------------------------------------------------------------------------
    List<ProductDTO> selectProductList(Map<String, Object> params); //상품리스트 보기

    int selectProductCount(Map<String, Object> params);//상품 카운트

	List<AdDTO> selectAdList();//상품리스트 가져오기
	
	int getOnsaleProductCount(); //판매중인 상품만 가져오기
	
	List<Map<String, Object>> getCategoryProductStats();
	  //-------------------------------------------------------------------------
	
	AdDTO getAd(Long adId);

	void approveAd(Long adId);

	void rejectAd(Long adId, String reason);

	void withdrawAd(Long adId, String reason);

	String getBanner();
	void updateBanner(String text);

	List<AdDTO> getActiveAds();

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

	List<Map<String, Object>> getAdMonthlyStats();
	int countPendingReportsAndInquiries();
	int countPendingAds();
	int countTodayProducts();
	long getDailyTradeAmount();
	List<Map<String, Object>> getWithdrawReasonStats();

	// 리뷰 관리
	List<ReviewAdminDTO> getReviewList(Map<String, Object> params);
	int countReviews();
	int countReviewsFiltered(Map<String, Object> params);
	int countSuspectReviews();
	int countLowRatingReviews();
	int countRecentReviews();
	List<Map<String, Object>> getDailyReviewCounts();
	List<Map<String, Object>> getRatingDistribution();
	void deleteReview(int reviewNo);

	// 거래 관리
	List<TransactionAdminDTO> getTransactionList(Map<String, Object> params);
	int countTransactions(Map<String, Object> params);
	List<Map<String, Object>> countTransactionsByStatus();

	// 신고 관리
	List<ReportAdminDTO> getReportList(Map<String, Object> params);
	int countReports(Map<String, Object> params);
	void updateReportStatus(Map<String, Object> params);
	void insertAdminNotification(Map<String, Object> params);
	ReportAdminDTO getReportDetail(long reportId);

	// 신고 유형별 통계
	int getProductReportCount();
	int getChatReportCount();
	int getPendingReportCount();

	// 문의 관리 (관리자)
	List<InquiryDTO> getAdminInquiryList(Map<String, Object> params);
	int countAdminInquiries(Map<String, Object> params);
	void saveInquiryAnswer(Map<String, Object> params);
	int countPendingInquiries();
	int countAnsweredInquiries();
	List<String> getFaqKeywords();

	// 상품 상세
	ProductDetailDTO getProductDetail(int productNo);
	List<String> getProductImages(int productNo);
	Map<String,Object> getBuyerForProduct(int productNo);
	void deleteProduct(int productNo);

	// 회계관리
	long getThisMonthAdRevenue();
	long getTotalAdRevenue();
	long getThisMonthTradeVolume();
	long getThisMonthRefundAmount();
	List<Map<String, Object>> getMonthlyAdRevenue();
	List<Map<String, Object>> getAdRevenueList(Map<String, Object> params);
	int countAdRevenue();

}

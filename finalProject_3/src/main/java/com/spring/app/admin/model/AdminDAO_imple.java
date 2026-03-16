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
import com.spring.app.admin.domain.ProductDetailDTO;
import com.spring.app.admin.domain.ReportAdminDTO;
import com.spring.app.admin.domain.ReviewAdminDTO;
import com.spring.app.admin.domain.SearchDTO;
import com.spring.app.admin.domain.StatDTO;
import com.spring.app.admin.domain.TransactionAdminDTO;
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

	@Override
	public List<MemberDTO> selectMemberListPagedSearch(Map<String, Object> params) {
		return sqlsession.selectList(admin + ".selectMemberListPagedSearch", params);
	}

	@Override
	public int countSearchMembers(Map<String, Object> params) {
		return sqlsession.selectOne(admin + ".countSearchMembers", params);
	}

	@Override
	public int countSuspendedMembers() {
		return sqlsession.selectOne(admin + ".countSuspendedMembers");
	}

	@Override
	public void suspendMember(int userNo) {
		sqlsession.update(admin + ".suspendMember", userNo);
	}

	@Override
	public void unsuspendMember(int userNo) {
		sqlsession.update(admin + ".unsuspendMember", userNo);
	}

	@Override
	public void permanentBanMember(int userNo) {
		sqlsession.update(admin + ".permanentBanMember", userNo);
	}

	@Override
	public void insertSuspendSchedule(Map<String, Object> params) {
		sqlsession.insert(admin + ".insertSuspendSchedule", params);
	}

	@Override
	public List<Map<String, Object>> getDueSuspensions() {
		return sqlsession.selectList(admin + ".getDueSuspensions");
	}

	@Override
	public void deleteSuspendSchedule(int scheduleId) {
		sqlsession.delete(admin + ".deleteSuspendSchedule", scheduleId);
	}

	@Override
	public int hasPendingSuspension(int userNo) {
		return sqlsession.selectOne(admin + ".hasPendingSuspension", userNo);
	}

	@Override
	public List<ProductDTO> getMemberActiveProducts(int userNo) {
		return sqlsession.selectList(admin + ".getMemberActiveProducts", userNo);
	}

	
	//상품 리스트 보여주기
	@Override
	public List<ProductDTO> selectProductList(Map<String, Object> params) {
		return sqlsession.selectList(admin+".selectProduct", params);
	}

	//상품카운트
	@Override
	public int selectProductCount(Map<String, Object> params) {
		return sqlsession.selectOne(admin+".selectCount", params);
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
	public String getBanner() {
	    return sqlsession.selectOne(admin + ".getBanner");
	}

	@Override
	public void updateBanner(String text) {
	    sqlsession.update(admin + ".updateBanner", text);
	}

	//광고 조기 철회
	@Override
	public void withdrawAd(Long adId, String reason) {
		Map<String,Object> map = new HashMap<>();
		map.put("adId", adId);
		map.put("reason", reason);
		sqlsession.update(admin + ".withdrawAd", map);
	}

	@Override
	public List<AdDTO> getActiveAds() {
	    return sqlsession.selectList(admin + ".getActiveAds");
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
        return sqlsession.selectList("admin.getDailyProductStats");
    }

    @Override
    public List<Map<String, Object>> getAdMonthlyStats() {
        return sqlsession.selectList("admin.getAdMonthlyStats");
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
    public int countPendingReportsAndInquiries() {
        return sqlsession.selectOne(admin + ".countPendingReportsAndInquiries");
    }

    @Override
    public int countPendingAds() {
        return sqlsession.selectOne(admin + ".countPendingAds");
    }

    @Override
    public int countTodayProducts() {
        return sqlsession.selectOne(admin + ".countTodayProducts");
    }

    @Override
    public long getDailyTradeAmount() {
        return sqlsession.selectOne(admin + ".getDailyTradeAmount");
    }

    @Override
    public List<Map<String, Object>> getWithdrawReasonStats() {
        return sqlsession.selectList(admin + ".getWithdrawReasonStats");
    }

	@Override
	public List<Map<String, Object>> getCategoryProdusctStats() {
		return null;
	}

	@Override
	public List<ReviewAdminDTO> getReviewList(Map<String, Object> params) {
		return sqlsession.selectList(admin + ".getReviewList", params);
	}

	@Override
	public int countReviews() {
		return sqlsession.selectOne(admin + ".countReviews");
	}

	@Override
	public void deleteReview(int reviewNo) {
		sqlsession.delete(admin + ".deleteReview", reviewNo);
	}

	@Override
	public List<TransactionAdminDTO> getTransactionList(Map<String, Object> params) {
		return sqlsession.selectList(admin + ".getTransactionList", params);
	}

	@Override
	public int countTransactions(Map<String, Object> params) {
		return sqlsession.selectOne(admin + ".countTransactions", params);
	}

	@Override
	public List<Map<String, Object>> countTransactionsByStatus() {
		return sqlsession.selectList(admin + ".countTransactionsByStatus");
	}

	@Override
	public List<ReportAdminDTO> getReportList(Map<String, Object> params) {
		return sqlsession.selectList(admin + ".getReportList", params);
	}

	@Override
	public int countReports(Map<String, Object> params) {
		return sqlsession.selectOne(admin + ".countReports", params);
	}

	@Override
	public void updateReportStatus(Map<String, Object> params) {
		sqlsession.update(admin + ".updateReportStatus", params);
	}

	@Override
	public void insertAdminNotification(Map<String, Object> params) {
		sqlsession.insert(admin + ".insertAdminNotification", params);
	}

	@Override
	public ReportAdminDTO getReportDetail(long reportId) {
		return sqlsession.selectOne(admin + ".getReportDetail", reportId);
	}

	@Override
	public List<InquiryDTO> getAdminInquiryList(Map<String, Object> params) {
		return sqlsession.selectList(admin + ".getAdminInquiryList", params);
	}

	@Override
	public int countAdminInquiries(Map<String, Object> params) {
		return sqlsession.selectOne(admin + ".countAdminInquiries", params);
	}

	@Override
	public void saveInquiryAnswer(Map<String, Object> params) {
		sqlsession.update(admin + ".saveInquiryAnswer", params);
	}

	@Override
	public ProductDetailDTO getProductDetail(int productNo) {
		return sqlsession.selectOne(admin + ".getProductDetail", productNo);
	}

	@Override
	public List<String> getProductImages(int productNo) {
		return sqlsession.selectList(admin + ".getProductImages", productNo);
	}

	// 회계관리
	@Override
	public long getThisMonthAdRevenue() {
		return sqlsession.selectOne(admin + ".getThisMonthAdRevenue");
	}

	@Override
	public long getTotalAdRevenue() {
		return sqlsession.selectOne(admin + ".getTotalAdRevenue");
	}

	@Override
	public long getThisMonthTradeVolume() {
		return sqlsession.selectOne(admin + ".getThisMonthTradeVolume");
	}

	@Override
	public long getThisMonthRefundAmount() {
		return sqlsession.selectOne(admin + ".getThisMonthRefundAmount");
	}

	@Override
	public List<Map<String, Object>> getMonthlyAdRevenue() {
		return sqlsession.selectList(admin + ".getMonthlyAdRevenue");
	}

	@Override
	public List<Map<String, Object>> getAdRevenueList(Map<String, Object> params) {
		return sqlsession.selectList(admin + ".getAdRevenueList", params);
	}

	@Override
	public int countAdRevenue() {
		return sqlsession.selectOne(admin + ".countAdRevenue");
	}

	@Override
	public int getProductReportCount() {
		return sqlsession.selectOne(admin + ".getProductReportCount");
	}

	@Override
	public int getChatReportCount() {
		return sqlsession.selectOne(admin + ".getChatReportCount");
	}

	@Override
	public int getPendingReportCount() {
		return sqlsession.selectOne(admin + ".getPendingReportCount");
	}

	@Override
	public int countPendingInquiries() {
		return sqlsession.selectOne(admin + ".countPendingInquiries");
	}

	@Override
	public int countAnsweredInquiries() {
		return sqlsession.selectOne(admin + ".countAnsweredInquiries");
	}

	@Override
	public List<String> getFaqKeywords() {
		return sqlsession.selectList(admin + ".getFaqKeywords");
	}

}







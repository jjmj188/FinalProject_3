package com.spring.app.admin.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.spring.app.admin.domain.AdDTO;
import com.spring.app.admin.domain.InquiryDTO;
import com.spring.app.admin.domain.ProductDetailDTO;
import com.spring.app.admin.domain.SearchDTO;
import com.spring.app.admin.domain.StatDTO;
import com.spring.app.admin.model.AdminDAO;
import com.spring.app.product.domain.ProductDTO;
import com.spring.app.security.domain.MemberDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService_imple implements AdminService {
	
	private final AdminDAO dao;
	private final SimpMessagingTemplate messagingTemplate;

	//광고 페이지 회원정보 가져오기
		@Override
		public MemberDTO getMemberById(String loginId) {
		
		return dao.getMemberById(loginId);
		}
		
    //광고 insert하기 
	@Value("${file.adminupload-dir}")
	private String adminuploadDir;

	@Override
	public int registerAd(AdDTO adDto) {

	    MultipartFile attachment = adDto.getAttachment();

	    if (attachment != null && !attachment.isEmpty()) {

	        try {

			        	Path uploadPath = Paths.get(adminuploadDir);
		
			        	if (!Files.exists(uploadPath)) {
			        	    Files.createDirectories(uploadPath);
			        	}
			        	String savedName =
			        	        UUID.randomUUID() + "_" + attachment.getOriginalFilename();

			        	Files.copy(
			        	        attachment.getInputStream(),
			        	        uploadPath.resolve(savedName),
			        	        StandardCopyOption.REPLACE_EXISTING
			        	);

			        	adDto.setFilePath("/adminupload/" + savedName);

	        } catch (IOException e) {
	            throw new RuntimeException("파일 저장 실패: " + e.getMessage(), e);
	        }
	    }

	    return dao.insertAd(adDto);
	
	}
	
	
  //====================================================================================//
        //회원 전체 리스트 
	    @Override
	    public List<MemberDTO> getMemberList() {
	        return dao.selectMemberList();
	    }
	    //회원 신규 카운트
	    @Override
	    public int getNewMembersCount() {
	        return dao.countNewMembers();
	    }
	    //회원 탈퇴 카운트
	    @Override
	    public int getWithdrawalsCount() {
	        return dao.countWithdrawals();
	    }
	    //회원 전체 카운트
	    @Override
	    public int getTotalMembersCount() {
	        return dao.countTotalMembers();
	    }


		@Override
		public MemberDTO getMemberByNo(int userNo) {
			return dao.getMemberByNo(userNo);
		}

		@Override
		public List<MemberDTO> getMemberList(int page, int size) {
			Map<String, Integer> params = new HashMap<>();
			params.put("start", (page - 1) * size);
			params.put("end", page * size);
			return dao.selectMemberListPaged(params);
		}

		//====================================================================================//
		//상품 리스트 가져오기
		@Override
		public List<ProductDTO> getProductList(int page, int size, String status, String filter) {
			int offset = (page - 1) * size;
			Map<String, Object> params = new HashMap<>();
			params.put("offset", offset);
			params.put("size", size);
			params.put("status", status != null ? status : "");
			params.put("filter", filter != null ? filter : "");
			return dao.selectProductList(params);
		}

		@Override
		public int getProductCount(String status, String filter) {
			Map<String, Object> params = new HashMap<>();
			params.put("status", status != null ? status : "");
			params.put("filter", filter != null ? filter : "");
			params.put("offset", 0);
			params.put("size", 0);
			return dao.selectProductCount(params);
		}

		//총 상품 개수
		@Override
		public int getTotalProductsCount() {
			Map<String, Object> params = new HashMap<>();
			params.put("status", "");
			params.put("filter", "");
			params.put("offset", 0);
			params.put("size", 0);
			return dao.selectProductCount(params);
		}
		//판매중인 상품만 가져오기
		@Override
		public int getOnsaleProductCount() {
		    return dao.getOnsaleProductCount(); // 매퍼 호출
		}
		
		//====================================================================================//
		// 컨텐츠 관리
		@Override
		public List<AdDTO> getAdList() {
			return dao.selectAdList();
		}
		//광고 상세보기
		@Override
		public AdDTO getAd(Long adId) {
			return dao.getAd(adId);
		}
		
		//광고 승인하기
		@Override
		public void approvedAd(Long adId) {
		dao.approveAd(adId);
			
		}
		
		//광고 반려하기
		@Override
		public void rejectAd(Long adId, String reason) {
		dao.rejectAd(adId, reason);

		}

		@Override
		public String getBanner() {
		    return dao.getBanner();
		}

		@Override
		public void updateBanner(String text) {
		    dao.updateBanner(text);
		}

		//광고 조기 철회
		@Override
		public void withdrawAd(Long adId, String reason) {
		dao.withdrawAd(adId, reason);
		}

		@Override
		public List<AdDTO> getActiveAds() {
		    return dao.getActiveAds();
		}

		//예정광고 있는지 확인
		@Override
		public List<AdDTO> getConflictAds(LocalDate startDate, LocalDate endDate, Long adId) {

		    Map<String,Object> map = new HashMap<>();

		    map.put("startDate", startDate);
		    map.put("endDate", endDate);
		    map.put("adId", adId);

		    return dao.getConflictAds(map);
		}
		

		@Override
		public Map<String, Object> getUserRegistrationStats(String type) {
		    List<StatDTO> stats = dao.getUserStats(type);
		    
		    Map<String, Object> response = new HashMap<>();
		    response.put("labels", stats.stream().map(StatDTO::getLabel).collect(Collectors.toList()));
		    response.put("data", stats.stream().map(StatDTO::getCount).collect(Collectors.toList()));
		    return response;
		}
		
		//인기검색어 가져오기
		@Override
		public List<SearchDTO> getPopularKeywords() {
		    return dao.getPopularKeywords();
		}
		//===================================================================
		//문의내역
		@Override
	    public List<InquiryDTO> getTop3FAQ() {
	        return dao.getTop3FAQ();
	    }

	    @Override
	    public List<InquiryDTO> getAllInquiries() {
	        return dao.getAllInquiries();
	    }
	    
	    //==============================================================================
	    @Override
	    public int getReportedProductCount() {
	        // 신고 테이블(예: REPORTS)의 전체 건수를 조회해옵니다.
	        return dao.getReportedProductCount();
	    }

	    @Override
	    public Map<String, Object> getDailyProductStats() {
	        List<Map<String, Object>> rawData = dao.getDailyProductStats();
	        List<String> labels = new ArrayList<>();
	        List<Long> data = new ArrayList<>();
	        for (Map<String, Object> row : rawData) {
	            labels.add(String.valueOf(row.get("REG_DATE")));
	            data.add(((Number) row.get("CNT")).longValue());
	        }
	        Map<String, Object> resultMap = new HashMap<>();
	        resultMap.put("labels", labels);
	        resultMap.put("data", data);
	        return resultMap;
	    }

	    @Override
	    public Map<String, Object> getAdMonthlyStats() {
	        List<Map<String, Object>> rawData = dao.getAdMonthlyStats();
	        List<String> labels = new ArrayList<>();
	        List<Long> data = new ArrayList<>();
	        for (Map<String, Object> row : rawData) {
	            labels.add(String.valueOf(row.get("MONTH_LABEL")));
	            data.add(((Number) row.get("CNT")).longValue());
	        }
	        Map<String, Object> resultMap = new HashMap<>();
	        resultMap.put("labels", labels);
	        resultMap.put("data", data);
	        return resultMap;
	    }

	    @Override
	    public Map<String, Object> getCategoryProductStats() {
	        // DB에서 [{CATEGORY_NAME: "패션", CNT: 35}, ...] 형태의 리스트를 가져옵니다.
	        List<Map<String, Object>> rawData = dao.getCategoryProductStats();
	        
	        List<String> labels = new ArrayList<>();
	        List<Long> data = new ArrayList<>();
	        
	        // 도넛 차트용 데이터 가공
	        for (Map<String, Object> row : rawData) {
	            labels.add(String.valueOf(row.get("CATEGORY_NAME"))); // 카테고리명
	            data.add(((Number) row.get("CNT")).longValue());      // 비중 수치
	        }
	        
	        Map<String, Object> resultMap = new HashMap<>();
	        resultMap.put("labels", labels);
	        resultMap.put("data", data);
	        
	        return resultMap;
	    }
	   
	




	@Override
	public Map<String, Object> getReviewListPaged(int page, int size, String keyword) {
		Map<String, Object> params = new HashMap<>();
		params.put("start", (page - 1) * size);
		params.put("end", page * size);
		params.put("keyword", keyword);
		int total = dao.countReviews();
		int totalPages = (int) Math.ceil((double) total / size);
		if (totalPages == 0) totalPages = 1;
		Map<String, Object> result = new HashMap<>();
		result.put("list", dao.getReviewList(params));
		result.put("total", total);
		result.put("totalPages", totalPages);
		return result;
	}

	@Override
	public void deleteReview(int reviewNo) {
		dao.deleteReview(reviewNo);
	}

	@Override
	public Map<String, Object> getTransactionListPaged(int page, int size, String status) {
		Map<String, Object> params = new HashMap<>();
		params.put("start", (page - 1) * size);
		params.put("end", page * size);
		params.put("status", status);
		int total = dao.countTransactions(params);
		int totalPages = (int) Math.ceil((double) total / size);
		if (totalPages == 0) totalPages = 1;
		Map<String, Object> result = new HashMap<>();
		result.put("list", dao.getTransactionList(params));
		result.put("total", total);
		result.put("totalPages", totalPages);
		return result;
	}

	@Override
	public Map<String, Object> getTransactionStatusStats() {
		List<Map<String, Object>> rows = dao.countTransactionsByStatus();
		Map<String, Object> stats = new HashMap<>();
		for (Map<String, Object> row : rows) {
			String st = String.valueOf(row.get("TRADE_STATUS"));
			long cnt = ((Number) row.get("CNT")).longValue();
			stats.put(st, cnt);
		}
		return stats;
	}

	@Override
	public Map<String, Object> getReportListPaged(int page, int size, String type) {
		Map<String, Object> params = new HashMap<>();
		params.put("start", (page - 1) * size);
		params.put("end", page * size);
		params.put("type", type);
		int total = dao.countReports(params);
		int totalPages = (int) Math.ceil((double) total / size);
		if (totalPages == 0) totalPages = 1;
		Map<String, Object> result = new HashMap<>();
		result.put("list", dao.getReportList(params));
		result.put("total", total);
		result.put("totalPages", totalPages);
		return result;
	}

	@Override
	public void updateReportStatus(int reportId, String status) {
		Map<String, Object> params = new HashMap<>();
		params.put("reportId", reportId);
		params.put("status", status);
		dao.updateReportStatus(params);
	}

	@Override
	public Map<String, Object> getReportStats() {
		Map<String, Object> stats = new HashMap<>();
		stats.put("productReportCount", dao.getProductReportCount());
		stats.put("chatReportCount", dao.getChatReportCount());
		stats.put("pendingReportCount", dao.getPendingReportCount());
		return stats;
	}

	@Override
	public void sendAdminNotification(String email, String title, String message) {
		Map<String, Object> params = new HashMap<>();
		params.put("email", email);
		params.put("title", title);
		params.put("message", message);
		dao.insertAdminNotification(params);
		Map<String, Object> payload = new HashMap<>();
		payload.put("title", title);
		payload.put("message", message);
		messagingTemplate.convertAndSend("/topic/noti/" + email, payload);
	}

	@Override
	public com.spring.app.admin.domain.ReportAdminDTO getReportDetail(long reportId) {
		return dao.getReportDetail(reportId);
	}

	@Override
	public int countPendingInquiries() { return dao.countPendingInquiries(); }

	@Override
	public int countAnsweredInquiries() { return dao.countAnsweredInquiries(); }

	@Override
	public List<String> getFaqKeywords() { return dao.getFaqKeywords(); }

	@Override
	public Map<String, Object> getAdminInquiryListPaged(int page, int size, String status) {
		Map<String, Object> params = new HashMap<>();
		params.put("start", (page - 1) * size);
		params.put("end", page * size);
		params.put("status", status);
		int total = dao.countAdminInquiries(params);
		int totalPages = (int) Math.ceil((double) total / size);
		if (totalPages == 0) totalPages = 1;
		Map<String, Object> result = new HashMap<>();
		result.put("list", dao.getAdminInquiryList(params));
		result.put("total", total);
		result.put("totalPages", totalPages);
		return result;
	}

	@Override
	public void saveInquiryAnswer(int inquiryId, String adminAnswer) {
		Map<String, Object> params = new HashMap<>();
		params.put("inquiryId", inquiryId);
		params.put("adminAnswer", adminAnswer);
		dao.saveInquiryAnswer(params);
	}

	@Override
	public ProductDetailDTO getProductDetail(int productNo) {
		ProductDetailDTO dto = dao.getProductDetail(productNo);
		if (dto != null) {
			dto.setImages(dao.getProductImages(productNo));
		}
		return dto;
	}


	@Override
	public int getSuspendedMembersCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public List<MemberDTO> getMemberList(int page, int size, String status, String keyword) {
		Map<String, Object> params = new HashMap<>();
		params.put("start", (page - 1) * size);
		params.put("end", page * size);
		params.put("status", (status != null && !status.isEmpty()) ? status : null);
		params.put("keyword", (keyword != null && !keyword.isEmpty()) ? keyword : null);
		return dao.selectMemberListPagedSearch(params);
	}


	@Override
	public int getMemberCount(String status, String keyword) {
		Map<String, Object> params = new HashMap<>();
		params.put("status", (status != null && !status.isEmpty()) ? status : null);
		params.put("keyword", (keyword != null && !keyword.isEmpty()) ? keyword : null);
		return dao.countSearchMembers(params);
	}


	@Override
	public void suspendMember(int userNo) { dao.suspendMember(userNo); }

	@Override
	public void unsuspendMember(int userNo) { dao.unsuspendMember(userNo); }

	@Override
	public void permanentBanMember(int userNo) { dao.permanentBanMember(userNo); }

	@Override
	public void scheduleSuspend(int userNo, String email) {
		Map<String, Object> params = new HashMap<>();
		params.put("userNo", userNo);
		params.put("suspendType", "SUSPEND");
		dao.insertSuspendSchedule(params);
		// 알림 전송
		sendAdminNotification(email,
			"[관리자] 계정 일시정지 예정 안내",
			"회원님의 계정이 관리자 검토에 의해 3일 후 일시정지 처리될 예정입니다.\n" +
			"이의가 있으시면 고객센터로 문의해주세요.");
	}

	@Override
	public void scheduleBan(int userNo, String email) {
		Map<String, Object> params = new HashMap<>();
		params.put("userNo", userNo);
		params.put("suspendType", "BAN");
		dao.insertSuspendSchedule(params);
		// 알림 전송
		sendAdminNotification(email,
			"[관리자] 계정 영구정지 예정 안내",
			"회원님의 계정이 관리자 검토에 의해 3일 후 영구정지 처리될 예정입니다.\n" +
			"이의가 있으시면 고객센터로 문의해주세요.");
	}

	@Override
	public void processScheduledSuspensions() {
		List<Map<String, Object>> list = dao.getDueSuspensions();
		for (Map<String, Object> row : list) {
			int scheduleId = ((Number) row.get("scheduleId")).intValue();
			int userNo     = ((Number) row.get("userNo")).intValue();
			String type    = (String) row.get("suspendType");
			try {
				if ("BAN".equals(type)) {
					dao.permanentBanMember(userNo);
				} else {
					dao.suspendMember(userNo);
				}
			} finally {
				dao.deleteSuspendSchedule(scheduleId);
			}
		}
	}

	@Override
	public int hasPendingSuspension(int userNo) {
		return dao.hasPendingSuspension(userNo);
	}

	@Override
	public List<ProductDTO> getMemberActiveProducts(int userNo) {
		return dao.getMemberActiveProducts(userNo);
	}


	@Override
	public int countPendingReportsAndInquiries() {
		return dao.countPendingReportsAndInquiries();
	}

	@Override
	public int countPendingAds() {
		return dao.countPendingAds();
	}

	@Override
	public int countTodayProducts() {
		return dao.countTodayProducts();
	}

	@Override
	public long getDailyTradeAmount() {
		return dao.getDailyTradeAmount();
	}


	@Override
	public Map<String, Object> getWithdrawReasonStats() {
		List<Map<String, Object>> rows = dao.getWithdrawReasonStats();
		List<String> labels = new ArrayList<>();
		List<Long> data = new ArrayList<>();
		for (Map<String, Object> row : rows) {
			labels.add(String.valueOf(row.get("REASON")));
			data.add(((Number) row.get("CNT")).longValue());
		}
		Map<String, Object> result = new HashMap<>();
		result.put("labels", labels);
		result.put("data", data);
		return result;
	}

	@Override
	public Map<String, Object> getAccountingStats() {
		Map<String, Object> stats = new HashMap<>();
		stats.put("thisMonthAdRevenue", dao.getThisMonthAdRevenue());
		stats.put("totalAdRevenue", dao.getTotalAdRevenue());
		stats.put("thisMonthTradeVolume", dao.getThisMonthTradeVolume());
		stats.put("thisMonthRefundAmount", dao.getThisMonthRefundAmount());

		List<Map<String, Object>> monthlyRaw = dao.getMonthlyAdRevenue();
		List<String> labels = new ArrayList<>();
		List<Long> revenues = new ArrayList<>();
		for (Map<String, Object> row : monthlyRaw) {
			labels.add(String.valueOf(row.get("MONTH_LABEL")) + "월");
			revenues.add(((Number) row.get("REVENUE")).longValue());
		}
		stats.put("monthlyLabels", labels);
		stats.put("monthlyRevenues", revenues);
		return stats;
	}

	@Override
	public Map<String, Object> getAccountingList(int page, int size) {
		Map<String, Object> params = new HashMap<>();
		params.put("start", (page - 1) * size);
		params.put("end", page * size);
		int total = dao.countAdRevenue();
		int totalPages = (int) Math.ceil((double) total / size);
		if (totalPages == 0) totalPages = 1;
		Map<String, Object> result = new HashMap<>();
		result.put("list", dao.getAdRevenueList(params));
		result.put("total", total);
		result.put("totalPages", totalPages);
		return result;
	}
}
		
		
		







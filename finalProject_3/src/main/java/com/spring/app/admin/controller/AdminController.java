package com.spring.app.admin.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.app.admin.domain.AdDTO;
import com.spring.app.admin.domain.InquiryDTO;
import com.spring.app.admin.domain.SearchDTO;
import com.spring.app.admin.service.AdminService;
import com.spring.app.common.AES256;
import com.spring.app.common.FileManager;
import com.spring.app.product.domain.ProductDTO;
import com.spring.app.security.domain.MemberDTO;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final FileManager fileManager;
    private final AES256 aes256;

    @Value("${file.upload-dir}")
    private String uploadDir;
    
    //관리자 메인페이지
    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("popularKeywords", adminService.getPopularKeywords());
        model.addAttribute("reportsAndInquiries", adminService.countPendingReportsAndInquiries());
        model.addAttribute("adInquiries", adminService.countPendingAds());
        model.addAttribute("todayProducts", adminService.countTodayProducts());
        model.addAttribute("dailyTradeAmount", adminService.getDailyTradeAmount());
        model.addAttribute("withdrawStats", adminService.getWithdrawReasonStats());
        return "admin/index";
    }
    		
    //index페이지 신규가입자
    @GetMapping("/user-stats")
    @ResponseBody
    // (value = "type") 을 추가해서 이름을 명시해줍니다.
    public Map<String, Object> getUserStats(@RequestParam(value = "type", defaultValue = "week") String type) {
        return adminService.getUserRegistrationStats(type);
    }
    
    //=====================================================================================//
     //회원 관리 페이지
    @GetMapping("/member")
    public String memebrPage(Model model,
                             @RequestParam(value = "page", defaultValue = "1") int page,
                             @RequestParam(value = "status", defaultValue = "") String status,
                             @RequestParam(value = "keyword", defaultValue = "") String keyword) {
        int size = 20;
        int totalMembers = adminService.getMemberCount(status, keyword);
        int totalPages = (int) Math.ceil((double) totalMembers / size);
        if (totalPages == 0) totalPages = 1;

        model.addAttribute("members", adminService.getMemberList(page, size, status, keyword));
        model.addAttribute("newMembers", adminService.getNewMembersCount());
        model.addAttribute("suspendedMembers", adminService.getSuspendedMembersCount());
        model.addAttribute("totalMembers", adminService.getTotalMembersCount());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("searchStatus", status);
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("searchCount", totalMembers);

        return "admin/member";
    }

    //회원 상세 보여주기 (멤버정보 + 거래중인 상품)
    @GetMapping("/member/detail")
    @ResponseBody
    public Map<String, Object> getMemberDetail(@RequestParam("userNo") int userNo) {
        MemberDTO member = adminService.getMemberByNo(userNo);
        Map<String, Object> result = new HashMap<>();
        if (member == null) return result;
        result.put("userNo", member.getUserNo());
        result.put("email", member.getEmail());
        result.put("userName", member.getUserName());
        result.put("nickname", member.getNickname());
        String decryptedPhone = member.getPhone();
        if (decryptedPhone != null && !decryptedPhone.isEmpty()) {
            try { decryptedPhone = aes256.decrypt(decryptedPhone); } catch (Exception ignored) {}
        }
        result.put("phone", decryptedPhone);
        result.put("regDate", member.getRegDate());
        result.put("status", member.getStatus());
        result.put("suspended", member.getSuspended());
        result.put("idle", member.getIdle());
        result.put("mannerTemp", member.getMannerTemp());
        result.put("profileImg", member.getProfileImg());
        result.put("products", adminService.getMemberActiveProducts(userNo));
        try {
            result.put("pendingSuspension", adminService.hasPendingSuspension(userNo) > 0);
        } catch (Exception e) {
            result.put("pendingSuspension", false);
        }
        return result;
    }

    //일시정지 예약 (3일 후 적용, 즉시 알림 발송)
    @PostMapping("/member/suspend")
    @ResponseBody
    public Map<String, Object> suspendMember(@RequestParam("userNo") int userNo,
                                              @RequestParam(value="email", required=false) String email) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (email == null || email.isEmpty()) {
                MemberDTO m = adminService.getMemberByNo(userNo);
                email = m != null ? m.getEmail() : "";
            }
            adminService.scheduleSuspend(userNo, email);
            result.put("success", true);
            result.put("message", "3일 후 일시정지가 예약되었습니다. 회원에게 알림을 발송했습니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    //정지 해제
    @PostMapping("/member/unsuspend")
    @ResponseBody
    public Map<String, Object> unsuspendMember(@RequestParam("userNo") int userNo) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.unsuspendMember(userNo);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }

    //영구정지 예약 (3일 후 적용, 즉시 알림 발송)
    @PostMapping("/member/ban")
    @ResponseBody
    public Map<String, Object> permanentBanMember(@RequestParam("userNo") int userNo,
                                                   @RequestParam(value="email", required=false) String email) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (email == null || email.isEmpty()) {
                MemberDTO m = adminService.getMemberByNo(userNo);
                email = m != null ? m.getEmail() : "";
            }
            adminService.scheduleBan(userNo, email);
            result.put("success", true);
            result.put("message", "3일 후 영구정지가 예약되었습니다. 회원에게 알림을 발송했습니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
    
    //=====================================================================================//
    
   //상품리스트 가져오기
    @GetMapping("/product")
    public String productPage(Model model,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "status", defaultValue = "") String status,
            @RequestParam(value = "filter", defaultValue = "") String filter) {

        // 1. 기본 데이터 및 페이징
        List<ProductDTO> productList = adminService.getProductList(page, size, status, filter);
        int filteredTotal = adminService.getProductCount(status, filter);
        int totalProducts = adminService.getTotalProductsCount();
        int onsaleCount = adminService.getOnsaleProductCount();
        int reportCount = adminService.getReportedProductCount();

        // 2. 카테고리 매핑
        Map<Integer, String> categoryMap = new HashMap<>();
        categoryMap.put(1, "패션"); categoryMap.put(2, "육아"); categoryMap.put(3, "가전");
        categoryMap.put(4, "홈·인테리어"); categoryMap.put(5, "취미");
        categoryMap.put(6, "여행"); categoryMap.put(7, "공구/산업용품");
        for(ProductDTO p : productList) {
            p.setCategoryName(categoryMap.getOrDefault(p.getCategoryNo(), "기타"));
        }

        // 3. 차트 데이터
        Map<String, Object> dailyStats = adminService.getDailyProductStats();
        model.addAttribute("dailyLabels", dailyStats.get("labels"));
        model.addAttribute("dailyData", dailyStats.get("data"));
        Map<String, Object> categoryStats = adminService.getCategoryProductStats();
        model.addAttribute("categoryLabels", categoryStats.get("labels"));
        model.addAttribute("categoryData", categoryStats.get("data"));

        // 4. 모델 담기
        model.addAttribute("productList", productList);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("filteredTotal", filteredTotal);
        model.addAttribute("onsaleCount", onsaleCount);
        model.addAttribute("reportCount", reportCount);
        model.addAttribute("size", size);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) filteredTotal / size));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedFilter", filter);

        return "admin/product";
    }
     
    
    
    //=====================================================================================//
    
    //=====================================================================================//  
    //광고 페이지 보여주기
    @GetMapping("/ad")
    public String adPage(Model model, Principal principal) {
        if (principal != null) {
            MemberDTO loginUser = adminService.getMemberById(principal.getName());
            model.addAttribute("loginUser", loginUser);
        }
    	return "admin/ad";
    }
    
 	// 현재 로그인한 사용자의 ID(username) 가져오기
    @GetMapping("ad/select")
    public String memberSelect(Model model, Principal principal){
   
        String loginId = principal.getName(); 
        
     
        MemberDTO loginUser = adminService.getMemberById(loginId);
        
        //  뷰 레이어로 전달
        model.addAttribute("loginUser", loginUser);
        
        return "ad/register"; 
    }
    
    
    //광고 등록하기
    @PostMapping("/ad/register")
    @ResponseBody
    public Map<String,Object> registerAd(AdDTO adDto, Principal principal) {

        Map<String,Object> result = new HashMap<>();

        try {

            MemberDTO loginUser = adminService.getMemberById(principal.getName());

            adDto.setUserNo(loginUser.getUserNo());

            int n = adminService.registerAd(adDto);

            result.put("success", n > 0);

        } catch(Exception e) {

            result.put("success", false);
        }

        return result;
    }

    //=====================================================================================//
    	
    //컨텐츠 관리페이지  

    @GetMapping("/contents")
    public String getDashboard(
            @RequestParam(value="year", defaultValue="0") Integer year,
            @RequestParam(value="month", defaultValue="0") Integer month,
            Model model) {

        LocalDate now = LocalDate.now();

        if(year == 0) year = now.getYear();
        if(month == 0) month = now.getMonthValue();

        LocalDate firstDay = LocalDate.of(year, month, 1);

        List<List<LocalDate>> calendarWeeks = generateCalendar(firstDay);

        List<AdDTO> adList = adminService.getAdList();
        Map<String, Object> adStats = adminService.getAdMonthlyStats();

        model.addAttribute("calendarWeeks", calendarWeeks);
        model.addAttribute("adList", adList);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("chartLabels", adStats.get("labels"));
        model.addAttribute("chartData", adStats.get("data"));

        return "admin/contents";
    }
    
    //광고 상세보기
    
    @GetMapping("/ad/detail")
    @ResponseBody
    public Map<String,Object> getAdDetail(@RequestParam("adId") Long adId) {

        AdDTO ad = adminService.getAd(adId);

        Map<String,Object> map = new HashMap<>();

        if(ad == null){
            map.put("ad", null);
            map.put("conflictAds", new ArrayList<>());
            return map;
        }

        // 기간 겹치는 광고 목록
        List<AdDTO> conflictAds =
                adminService.getConflictAds(ad.getStartDate(), ad.getEndDate(), adId);

        map.put("ad", ad);
        map.put("conflictAds", conflictAds);

        return map;
    }
    
    @GetMapping("/ad/download")
    public void downloadAdFile(@RequestParam("adId") Long adId,
                               HttpServletResponse response) throws Exception {
        AdDTO ad = adminService.getAd(adId);
        if (ad == null || ad.getFilePath() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "파일을 찾을 수 없습니다.");
            return;
        }
        String filePath = ad.getFilePath();
        int lastSlash = filePath.lastIndexOf('/');
        String subDir   = filePath.substring(0, lastSlash);
        String fileName = filePath.substring(lastSlash + 1);
        int underscoreIdx = fileName.indexOf('_');
        String originalName = (underscoreIdx >= 0) ? fileName.substring(underscoreIdx + 1) : fileName;

        String dirPath = uploadDir + File.separator + subDir;
        fileManager.doFileDownload(fileName, originalName, dirPath, response);
    }

    @PostMapping("/ad/approve")
    @ResponseBody
    public String approvedAd(@RequestParam("adId") Long adId) {
    	adminService.approvedAd(adId);
    	return "ok";
    }
    
    
    @PostMapping("/ad/reject")
    @ResponseBody
    public String rejectAd(@RequestParam("adId") Long adId,
            @RequestParam("reason") String reason) {

    	adminService.rejectAd(adId, reason);

        return "ok";
    }

    @PostMapping("/ad/withdraw")
    @ResponseBody
    public String withdrawAd(@RequestParam("adId") Long adId,
            @RequestParam("reason") String reason) {
        adminService.withdrawAd(adId, reason);
        return "ok";
    }

    @PostMapping("/banner/update")
    @ResponseBody
    public String updateBanner(@RequestParam("text") String text) {
        adminService.updateBanner(text);
        return "ok";
    }

 // ---------------------------------------------------------
    // [추가할 부분] 달력 데이터를 만드는 로직 (에러 해결용)
    // ---------------------------------------------------------
    private List<List<LocalDate>> generateCalendar(LocalDate date) {
        List<List<LocalDate>> weeks = new ArrayList<>();
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        LocalDate lastDayOfMonth = date.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

        // 시작 요일 (월=1, ..., 일=7) -> 일요일 시작으로 맞추려면 조정 필요
        int startDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue(); 
        // 월요일 시작 기준일 때 1일 이전의 월요일로 이동
        LocalDate current = firstDayOfMonth.minusDays(startDayOfWeek - 1);

        // 최소 5주는 출력하도록 설정
        for (int i = 0; i < 6; i++) { // 최대 6주까지
            List<LocalDate> week = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                week.add(current);
                current = current.plusDays(1);
            }
            weeks.add(week);
            // 마지막 날짜가 지났고, 주가 꽉 찼다면 중단
            if (current.isAfter(lastDayOfMonth)) break;
        }
        return weeks;
    }
    // 리뷰 관리 페이지
    @GetMapping("/review")
    public String review(Model model,
                         @RequestParam(value = "page", defaultValue = "1") int page,
                         @RequestParam(value = "size", defaultValue = "20") int size,
                         @RequestParam(value = "keyword", defaultValue = "") String keyword) {
        Map<String, Object> data = adminService.getReviewListPaged(page, size, keyword);
        model.addAttribute("reviewList", data.get("list"));
        model.addAttribute("totalCount", data.get("total"));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", data.get("totalPages"));
        model.addAttribute("searchKeyword", keyword);
        return "admin/review";
    }

    @PostMapping("/review/delete")
    @ResponseBody
    public Map<String, Object> deleteReview(@RequestParam("reviewNo") int reviewNo) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.deleteReview(reviewNo);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }

    // 거래 관리 페이지
    @GetMapping("/transaction")
    public String transaction(Model model,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "size", defaultValue = "20") int size,
                              @RequestParam(value = "status", defaultValue = "ALL") String status) {
        Map<String, Object> data = adminService.getTransactionListPaged(page, size, status);
        Map<String, Object> statusStats = adminService.getTransactionStatusStats();
        model.addAttribute("tradeList", data.get("list"));
        model.addAttribute("totalCount", data.get("total"));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", data.get("totalPages"));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statusStats", statusStats);
        return "admin/transaction";
    }

    // 신고 관리 페이지
    @GetMapping("/complaint")
    public String complaint(Model model,
                            @RequestParam(value = "page", defaultValue = "1") int page,
                            @RequestParam(value = "size", defaultValue = "20") int size,
                            @RequestParam(value = "type", defaultValue = "ALL") String type) {
        Map<String, Object> data = adminService.getReportListPaged(page, size, type);
        model.addAttribute("reportList", data.get("list"));
        model.addAttribute("totalCount", data.get("total"));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", data.get("totalPages"));
        model.addAttribute("selectedType", type);
        model.addAttribute("reportStats", adminService.getReportStats());
        return "admin/complaint";
    }

    @GetMapping("/complaint/detail")
    public String complaintDetail(@RequestParam("reportId") long reportId, Model model) {
        model.addAttribute("report", adminService.getReportDetail(reportId));
        return "admin/complaint_detail";
    }

    @PostMapping("/complaint/memberAction")
    @ResponseBody
    public Map<String, Object> memberAction(@RequestParam("userNo") int userNo,
                                             @RequestParam("action") String action) {
        Map<String, Object> result = new HashMap<>();
        try {
            switch (action) {
                case "suspend":   adminService.suspendMember(userNo);      break;
                case "ban":       adminService.permanentBanMember(userNo); break;
                case "unsuspend": adminService.unsuspendMember(userNo);    break;
            }
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PostMapping("/complaint/notify")
    @ResponseBody
    public Map<String, Object> sendComplaintNotification(@RequestParam("email") String email,
                                                          @RequestParam("title") String title,
                                                          @RequestParam("message") String message) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.sendAdminNotification(email, title, message);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }

    @PostMapping("/api/sendMessage")
    @ResponseBody
    public Map<String, Object> sendMessage(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.sendAdminNotification(
                (String) body.get("targetEmail"),
                (String) body.get("title"),
                (String) body.get("message")
            );
            result.put("success", true);
            result.put("message", "메시지가 성공적으로 전송되었습니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "전송 실패: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/complaint/process")
    @ResponseBody
    public Map<String, Object> processReport(@RequestParam("reportId") int reportId,
                                              @RequestParam("status") String status) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.updateReportStatus(reportId, status);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }

    // 문의 관리 페이지
    @GetMapping("/inquiry")
    public String inquiry(Model model,
                          @RequestParam(value = "page", defaultValue = "1") int page,
                          @RequestParam(value = "size", defaultValue = "20") int size,
                          @RequestParam(value = "status", defaultValue = "ALL") String status) {
        Map<String, Object> data = adminService.getAdminInquiryListPaged(page, size, status);
        model.addAttribute("inquiryList", data.get("list"));
        model.addAttribute("totalCount", data.get("total"));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", data.get("totalPages"));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("pendingCount", adminService.countPendingInquiries());
        model.addAttribute("answeredCount", adminService.countAnsweredInquiries());
        model.addAttribute("faqKeywords", adminService.getFaqKeywords());
        return "admin/inquiry";
    }

    @PostMapping("/inquiry/answer")
    @ResponseBody
    public Map<String, Object> saveInquiryAnswer(@RequestParam("inquiryId") int inquiryId,
                                                  @RequestParam("adminAnswer") String adminAnswer) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.saveInquiryAnswer(inquiryId, adminAnswer);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }

    // 상품 상세 페이지
    @GetMapping("/productDetail")
    public String productDetail(@RequestParam("productNo") int productNo, Model model) {
        model.addAttribute("product", adminService.getProductDetail(productNo));
        return "admin/product_detail";
    }
    
    
  //=========================================================================================================================
	 // 사용자 문의내역 게시판 
	
	 @GetMapping("/user_inquiry") 
	 public String inquiryList(Model model) {
	     
	     // 1. 데이터 가져오기
	 List<InquiryDTO> faqList = adminService.getTop3FAQ();
	 List<InquiryDTO> inquiryList = adminService.getAllInquiries();
	
	 // 2. 데이터 전달 (Null 방지)
	 model.addAttribute("faqList", faqList != null ? faqList : new ArrayList<>());
	 model.addAttribute("inquiryList", inquiryList != null ? inquiryList : new ArrayList<>());
	
	 // 3. 화면 리턴 (templates/admin/user_inquiry.html)
	 return "admin/user_inquiry";
	 }
	    
	    //문의하기 글쓰기 페이지
	 @GetMapping("/inquiry_write")
	 public String write() {
		 return "admin/inquiry_write";
	 }

	// 회계관리
	@GetMapping("/accounting")
	public String getAccounting(
	        @RequestParam(value="page", defaultValue="1") int page,
	        Model model) {
	    int size = 10;
	    Map<String, Object> stats = adminService.getAccountingStats();
	    Map<String, Object> listData = adminService.getAccountingList(page, size);
	    model.addAttribute("stats", stats);
	    model.addAttribute("list", listData.get("list"));
	    model.addAttribute("total", listData.get("total"));
	    model.addAttribute("totalPages", listData.get("totalPages"));
	    model.addAttribute("currentPage", page);
	    return "admin/accounting";
	}

}
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

    @Value("${file.upload-dir}")
    private String uploadDir;
    
    //관리자 메인페이지
    @GetMapping("/index")
    public String index(Model model) {
        // 인기 검색어 상위 10개 가져오기
        List<SearchDTO> popularKeywords = adminService.getPopularKeywords();
        model.addAttribute("popularKeywords", popularKeywords);
        
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
                             @RequestParam(value = "page", defaultValue = "1") int page)  {
        int size = 20;
        int totalMembers = adminService.getTotalMembersCount();
        int totalPages = (int) Math.ceil((double) totalMembers / size);
        if (totalPages == 0) totalPages = 1;

        model.addAttribute("members", adminService.getMemberList(page, size));
        model.addAttribute("newMembers", adminService.getNewMembersCount());
        model.addAttribute("withdrawals", adminService.getWithdrawalsCount());
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "admin/member";
    }
    
    //회원 한명 보여주기
    @GetMapping("/member/detail")
    @ResponseBody
    public MemberDTO getMemberDetail(@RequestParam("userNo")int userNo) {
    return adminService.getMemberByNo(userNo);	
    }
    
    //=====================================================================================//
    
   //상품리스트 가져오기
    @GetMapping("/product")
    public String productPage(Model model,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        // 1. 기본 데이터 및 페이징
        List<ProductDTO> productList = adminService.getProductList(page, size);
        int totalProducts = adminService.getTotalProductsCount();
        int onsaleCount = adminService.getOnsaleProductCount();
        int reportCount = adminService.getReportedProductCount(); // 신고 건수 서비스 추가 권장

        // 2. 카테고리 매핑 (DB에서 가져오거나 상수로 관리)
        Map<Integer, String> categoryMap = new HashMap<>();
        categoryMap.put(1, "패션");
        categoryMap.put(2, "육아");
        categoryMap.put(3, "가전");
        categoryMap.put(4, "홈·인테리어");
        categoryMap.put(5, "취미");
        categoryMap.put(6, "여행");
        categoryMap.put(7, "공구/산업용품");

        for(ProductDTO p : productList) {
            p.setCategoryName(categoryMap.getOrDefault(p.getCategoryNo(), "기타")); 
        }

        // 3. [핵심] 차트 데이터 준비
        // A. 일별 등록 현황 (최근 7일) -> 서비스에서 List<Map<String, Object>> 형태로 받아온다고 가정
        // 데이터 예시: Labels: ["03-03", "03-04"...], Data: [12, 5, 8...]
        Map<String, Object> dailyStats = adminService.getDailyProductStats(); 
        model.addAttribute("dailyLabels", dailyStats.get("labels")); // 날짜 리스트
        model.addAttribute("dailyData", dailyStats.get("data"));     // 건수 리스트

        // B. 카테고리별 비중 (도넛 차트용)
        // 데이터 예시: Labels: ["패션", "가전"...], Data: [45, 20...]
        Map<String, Object> categoryStats = adminService.getCategoryProductStats();
        model.addAttribute("categoryLabels", categoryStats.get("labels"));
        model.addAttribute("categoryData", categoryStats.get("data"));

        // 4. 모델 담기
        model.addAttribute("productList", productList);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("onsaleCount", onsaleCount);
        model.addAttribute("reportCount", reportCount);
        model.addAttribute("size", size);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) totalProducts / size));

        return "admin/product"; 
    }
     
    
    
    //=====================================================================================//
    
    //=====================================================================================//  
    //광고 페이지 보여주기
    @GetMapping("/ad")
    public String adPage() {
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
    public Map<String,Object> registerAd(AdDTO adDto,
                                         HttpSession session) {

        Map<String,Object> result = new HashMap<>();

        try {

            MemberDTO loginUser = (MemberDTO) session.getAttribute("loginUser");

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

        model.addAttribute("calendarWeeks", calendarWeeks);
        model.addAttribute("adList", adList);
        model.addAttribute("year", year);
        model.addAttribute("month", month);

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
    //문의 페이지 보여주기
    @GetMapping("/inquiry")
    public String inquiry() {
    	return "admin/inquiry";
    }
    
    
    //신고 페이지 보여주기
    @GetMapping("/complaint")
    public String complaint() {
    	return "admin/complaint";
    }
    
    //신고 페이지 보여주기
    @GetMapping("/transaction")
    public String transaction() {
    	return "admin/transaction";
    }
    
    //신고 페이지 보여주기
    @GetMapping("/review")
    public String review() {
    	return "admin/review";
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
		    
    
    
    
}
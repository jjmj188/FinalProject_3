package com.spring.app.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.app.admin.ad.domain.AdDTO;
import com.spring.app.admin.service.AdminService;
import com.spring.app.product.domain.ProductDTO;
import com.spring.app.security.domain.MemberDTO;

import lombok.RequiredArgsConstructor;

import java.util.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
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
    @GetMapping("/product")
    public String productPage(Model model,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        // 1. 데이터 가져오기
        List<ProductDTO> productList = adminService.getProductList(page, size);
        int totalProducts = adminService.getTotalProductsCount();
        
        // 2. 카테고리 매핑 (번호 -> 이름)
        Map<Integer, String> categoryMap = new HashMap<>();
        categoryMap.put(1, "패션");
        categoryMap.put(2, "육아");
        categoryMap.put(3, "가전");
        categoryMap.put(4, "홈·인테리어");
        categoryMap.put(5, "취미");
        categoryMap.put(6, "여행");
        categoryMap.put(7, "공구/산업용품");

        for(ProductDTO p : productList) {
            p.setAreaGu(categoryMap.getOrDefault(p.getCategoryNo(), "기타")); 
        }

        // 3. 모델 담기
        model.addAttribute("productList", productList);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("size", size);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) totalProducts / size));
        
        // 차트 및 통계용 (에러 방지용 임시값)
        model.addAttribute("reportCount", 0);
        model.addAttribute("onsaleCount", 12); // DB 연결 필요
        model.addAttribute("chartData", Arrays.asList(5, 10, 8, 15, 20, 12, 7)); // 월~일 데이터

        return "admin/product"; 
    }
    
    
    
    //=====================================================================================//
    
    //=====================================================================================//  
    //광고 페이지 보여주기
    @GetMapping("/ad")
    public String adPage() {
    	return "admin/ad";
    }
     //광고 등록하기
    @PostMapping("/ad/register")
    @ResponseBody
    public Map<String,Object>registerAd(AdDTO adDto){
    	Map<String,Object>map=new HashMap<>();
    	try {
    		adminService.registerAd(adDto); 
            map.put("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            map.put("success", false);
            map.put("error", e.getMessage());
        }
        return map;
    }
    //=====================================================================================//
    	
    

    
}
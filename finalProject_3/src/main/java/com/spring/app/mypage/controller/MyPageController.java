package com.spring.app.mypage.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.app.mypage.domain.AccountDTO;
import com.spring.app.mypage.domain.DeliveryAddressDTO;
import com.spring.app.mypage.domain.MyPurchaseDTO;
import com.spring.app.mypage.domain.MyReportDTO;
import com.spring.app.product.domain.ProductDTO;
import com.spring.app.mypage.service.MyPageService;
import com.spring.app.security.domain.MemberDTO;
import com.spring.app.security.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/mypage")
public class MyPageController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MyPageService myPageService;

    @GetMapping("/main")
    public String myPageMain(Model model, Principal principal, HttpServletRequest request) {
        if (principal != null) {
            MemberDTO member = memberService.getMemberByEmail(principal.getName());
            if (member != null) {
                model.addAttribute("member", member);

                String ctxPath = request.getContextPath();
                String profileImg = member.getProfileImg();
                String profileImgUrl;
                if (profileImg == null || profileImg.isEmpty()) {
                    profileImgUrl = ctxPath + "/images/default_profile.png";
                } else if (profileImg.startsWith("http")) {
                    profileImgUrl = profileImg;
                } else {
                    profileImgUrl = ctxPath + "/resources/profile_images/" + profileImg;
                }
                model.addAttribute("profileImgUrl", profileImgUrl);
            }
        }
        return "mypage/mypage_main";
    }

    // ===== 찜 목록 =====

    @GetMapping("/wishlist")
    @ResponseBody
    public List<ProductDTO> getWishlist(Principal principal) {
        if (principal == null) return List.of();
        return myPageService.getWishlist(principal.getName());
    }

    // ===== 내 판매상품 =====

    @GetMapping("/my-products")
    @ResponseBody
    public List<ProductDTO> getMyProducts(Principal principal) {
        if (principal == null) return List.of();
        return myPageService.getMyProducts(principal.getName());
    }

    @PostMapping("/product/update")
    @ResponseBody
    public Map<String, Object> updateMyProduct(@RequestBody Map<String, Object> params, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) { result.put("success", false); return result; }
        params.put("email", principal.getName());
        myPageService.updateMyProduct(params);
        result.put("success", true);
        return result;
    }

    @PostMapping("/product/delete")
    @ResponseBody
    public Map<String, Object> deleteMyProduct(@RequestParam("productNo") int productNo, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) { result.put("success", false); return result; }

        int txnCount = myPageService.getProductTransactionCount(productNo);
        if (txnCount > 0) {
            result.put("success", false);
            result.put("message", "거래 내역이 있는 상품은 삭제할 수 없습니다.");
            return result;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("productNo", productNo);
        params.put("email", principal.getName());
        myPageService.deleteMyProduct(params);
        result.put("success", true);
        return result;
    }

    // ===== 내 구매상품 =====

    @GetMapping("/my-purchases")
    @ResponseBody
    public List<MyPurchaseDTO> getMyPurchases(Principal principal) {
        if (principal == null) return List.of();
        return myPageService.getMyPurchases(principal.getName());
    }

    // ===== 계좌 관리 =====

    @GetMapping("/accounts")
    @ResponseBody
    public List<AccountDTO> getAccounts(Principal principal) {
        if (principal == null) return List.of();
        return myPageService.getAccountList(principal.getName());
    }

    @PostMapping("/account/add")
    @ResponseBody
    public Map<String, Object> addAccount(@RequestBody AccountDTO account, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) { result.put("success", false); return result; }

        String email = principal.getName();
        int count = myPageService.getAccountCount(email);
        if (count >= 5) {
            result.put("success", false);
            result.put("message", "계좌는 최대 5개까지 등록 가능합니다.");
            return result;
        }

        account.setEmail(email);
        account.setIsPrimary(count == 0 ? "Y" : "N");
        myPageService.insertAccount(account);
        result.put("success", true);
        return result;
    }

    @PostMapping("/account/update")
    @ResponseBody
    public Map<String, Object> updateAccount(@RequestBody AccountDTO account, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) { result.put("success", false); return result; }

        account.setEmail(principal.getName());
        myPageService.updateAccount(account);
        result.put("success", true);
        return result;
    }

    @PostMapping("/account/delete")
    @ResponseBody
    public Map<String, Object> deleteAccount(@RequestParam("accountId") int accountId, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) { result.put("success", false); return result; }

        String email = principal.getName();
        List<AccountDTO> accounts = myPageService.getAccountList(email);
        AccountDTO target = accounts.stream()
                .filter(a -> a.getAccountId() == accountId).findFirst().orElse(null);

        if (target == null) {
            result.put("success", false);
            result.put("message", "계좌를 찾을 수 없습니다.");
            return result;
        }
        if ("Y".equals(target.getIsPrimary()) && accounts.size() > 1) {
            result.put("success", false);
            result.put("message", "대표 계좌는 삭제할 수 없습니다. 먼저 다른 계좌를 대표로 설정해주세요.");
            return result;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("accountId", accountId);
        params.put("email", email);
        myPageService.deleteAccount(params);
        result.put("success", true);
        return result;
    }

    @PostMapping("/account/setPrimary")
    @ResponseBody
    public Map<String, Object> setPrimaryAccount(@RequestParam("accountId") int accountId, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) { result.put("success", false); return result; }

        Map<String, Object> params = new HashMap<>();
        params.put("accountId", accountId);
        params.put("email", principal.getName());
        myPageService.setPrimaryAccount(params);
        result.put("success", true);
        return result;
    }

    // ===== 배송지 관리 =====

    @GetMapping("/deliveries")
    @ResponseBody
    public List<DeliveryAddressDTO> getDeliveries(Principal principal) {
        if (principal == null) return List.of();
        return myPageService.getDeliveryList(principal.getName());
    }

    @PostMapping("/delivery/add")
    @ResponseBody
    public Map<String, Object> addDelivery(@RequestBody DeliveryAddressDTO delivery, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) { result.put("success", false); return result; }

        String email = principal.getName();
        int count = myPageService.getDeliveryCount(email);
        if (count >= 5) {
            result.put("success", false);
            result.put("message", "배송지는 최대 5개까지 등록 가능합니다.");
            return result;
        }

        delivery.setMemberEmail(email);
        delivery.setIsPrimary(count == 0 ? "Y" : "N");
        myPageService.insertDelivery(delivery);
        result.put("success", true);
        return result;
    }

    @PostMapping("/delivery/update")
    @ResponseBody
    public Map<String, Object> updateDelivery(@RequestBody DeliveryAddressDTO delivery, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) { result.put("success", false); return result; }

        delivery.setMemberEmail(principal.getName());
        myPageService.updateDelivery(delivery);
        result.put("success", true);
        return result;
    }

    @PostMapping("/delivery/delete")
    @ResponseBody
    public Map<String, Object> deleteDelivery(@RequestParam("deliveryNo") int deliveryNo, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) { result.put("success", false); return result; }

        String email = principal.getName();
        List<DeliveryAddressDTO> deliveries = myPageService.getDeliveryList(email);
        DeliveryAddressDTO target = deliveries.stream()
                .filter(d -> d.getDeliveryNo() == deliveryNo).findFirst().orElse(null);

        if (target == null) {
            result.put("success", false);
            result.put("message", "배송지를 찾을 수 없습니다.");
            return result;
        }
        if ("Y".equals(target.getIsPrimary()) && deliveries.size() > 1) {
            result.put("success", false);
            result.put("message", "대표 배송지는 삭제할 수 없습니다. 먼저 다른 배송지를 대표로 설정해주세요.");
            return result;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("deliveryNo", deliveryNo);
        params.put("email", email);
        myPageService.deleteDelivery(params);
        result.put("success", true);
        return result;
    }

    @PostMapping("/delivery/setPrimary")
    @ResponseBody
    public Map<String, Object> setPrimaryDelivery(@RequestParam("deliveryNo") int deliveryNo, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) { result.put("success", false); return result; }

        Map<String, Object> params = new HashMap<>();
        params.put("deliveryNo", deliveryNo);
        params.put("email", principal.getName());
        myPageService.setPrimaryDelivery(params);
        result.put("success", true);
        return result;
    }

    // ===== 신고관리 =====

    @GetMapping("/reports/sent")
    @ResponseBody
    public List<MyReportDTO> getMyReportsSent(Principal principal) {
        if (principal == null) return List.of();
        return myPageService.getMyReportsSent(principal.getName());
    }

    @GetMapping("/reports/received")
    @ResponseBody
    public List<MyReportDTO> getMyReportsReceived(Principal principal) {
        if (principal == null) return List.of();
        return myPageService.getMyReportsReceived(principal.getName());
    }
}

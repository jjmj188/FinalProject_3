package com.spring.app.mypage.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.app.common.FileManager;
import com.spring.app.mypage.domain.AccountDTO;
import com.spring.app.mypage.domain.DeliveryAddressDTO;
import com.spring.app.mypage.domain.MyPurchaseDTO;
import com.spring.app.mypage.domain.MyReportDTO;
import com.spring.app.product.domain.ProductDTO;
import com.spring.app.product.domain.ProductImageDTO;
import com.spring.app.product.domain.ProductMeetLocationDTO;
import com.spring.app.product.domain.ProductShippingOptionDTO;
import com.spring.app.product.service.ProductService;
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

    @Autowired
    private ProductService productService;

    @Autowired
    private FileManager fileManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${file.images-dir}")
    private String imagesDir;

    @Value("${sweettracker.api-key}")
    private String sweetTrackerApiKey;

    private static final String IMAGE_WEB_PREFIX = "/images/";

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
                model.addAttribute("safePayCount", myPageService.getMySafePayCount(principal.getName()));
                model.addAttribute("tradeCount", myPageService.getMyTradeCount(principal.getName()));
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

    @GetMapping("/product/edit/{productNo}")
    public String editProductPage(@PathVariable("productNo") int productNo, Model model, Principal principal) throws Exception {
        if (principal == null) return "redirect:/security/login";

        // 본인 상품 여부 확인
        Map<String, Object> checkParams = new HashMap<>();
        checkParams.put("productNo", productNo);
        checkParams.put("email", principal.getName());
        if (myPageService.getMyProductByNo(checkParams) == null) return "redirect:/mypage/main";

        // 전체 상품 상세 조회 (이미지/배송/위치 포함)
        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("productNo", productNo);
        paraMap.put("memberEmail", principal.getName());
        ProductDTO product = productService.getProductDetailFull(paraMap);
        if (product == null) return "redirect:/mypage/main";

        String shippingOptionsJson = "[]";
        String meetLocationsJson   = "[]";
        if (product.getShippingOptionList() != null)
            shippingOptionsJson = objectMapper.writeValueAsString(product.getShippingOptionList());
        if (product.getMeetLocationList() != null)
            meetLocationsJson = objectMapper.writeValueAsString(product.getMeetLocationList());

        model.addAttribute("product", product);
        model.addAttribute("shippingOptionsJson", shippingOptionsJson);
        model.addAttribute("meetLocationsJson", meetLocationsJson);
        return "mypage/product_edit";
    }

    @PostMapping("/product/editSave")
    @ResponseBody
    public Map<String, Object> editProductSave(
            @RequestParam("productNo") int productNo,
            @RequestParam("productName") String productName,
            @RequestParam(value = "productPrice", required = false) Integer productPrice,
            @RequestParam("tradeStatus") String tradeStatus,
            @RequestParam(value = "productDesc", required = false) String productDesc,
            @RequestParam(value = "categoryNo", required = false) Integer categoryNo,
            @RequestParam(value = "productCondition", required = false) String productCondition,
            @RequestParam(value = "tradeMethod", required = false) String tradeMethod,
            @RequestParam(value = "shippingOptionsJson", required = false) String shippingOptionsJson,
            @RequestParam(value = "meetLocationsJson", required = false) String meetLocationsJson,
            @RequestParam(value = "deletedImageNos", required = false) String deletedImageNos,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            Principal principal) {

        Map<String, Object> result = new HashMap<>();
        if (principal == null) { result.put("success", false); return result; }
        String email = principal.getName();

        // 본인 상품 여부 확인
        Map<String, Object> checkParams = new HashMap<>();
        checkParams.put("productNo", productNo);
        checkParams.put("email", email);
        if (myPageService.getMyProductByNo(checkParams) == null) {
            result.put("success", false); result.put("message", "상품을 찾을 수 없습니다."); return result;
        }

        // 1. PRODUCTS 테이블 업데이트
        Map<String, Object> params = new HashMap<>();
        params.put("productNo", productNo);
        params.put("email", email);
        params.put("productName", productName);
        params.put("productPrice", productPrice);
        params.put("tradeStatus", tradeStatus);
        params.put("productDesc", productDesc);
        params.put("categoryNo", categoryNo);
        params.put("productCondition", productCondition);
        params.put("tradeMethod", tradeMethod);
        myPageService.updateMyProduct(params);

        // 2. 삭제 요청 이미지 처리
        if (deletedImageNos != null && !deletedImageNos.trim().isEmpty()) {
            for (String idStr : deletedImageNos.split(",")) {
                String trimmed = idStr.trim();
                if (trimmed.isEmpty()) continue;
                int prdImgNo = Integer.parseInt(trimmed);
                ProductImageDTO img = myPageService.getProductImageByNo(prdImgNo);
                if (img != null && img.getFilename() != null) {
                    try { fileManager.doFileDelete(img.getFilename(), imagesDir); } catch (Exception ignore) {}
                }
                myPageService.deleteProductImageByNo(prdImgNo);
            }
        }

        // 3. 새 이미지 업로드
        if (images != null) {
            for (MultipartFile mf : images) {
                if (mf == null || mf.isEmpty()) continue;
                try {
                    String savedFileName = fileManager.doFileUpload(mf.getBytes(), mf.getOriginalFilename(), imagesDir);
                    if (savedFileName == null) continue;
                    ProductImageDTO imgDto = new ProductImageDTO();
                    imgDto.setProductNo(productNo);
                    imgDto.setOrgfilename(mf.getOriginalFilename());
                    imgDto.setFilename(savedFileName);
                    imgDto.setImgUrl(IMAGE_WEB_PREFIX + savedFileName);
                    imgDto.setSortNo(99);
                    imgDto.setIsMain("N");
                    myPageService.insertProductImageEdit(imgDto);
                } catch (Exception ignore) {}
            }
        }

        // 4. 대표 이미지 재설정
        myPageService.resetMainImages(productNo);
        myPageService.setFirstImageAsMain(productNo);

        // 5. 배송 옵션 교체
        myPageService.deleteProductShippingOptions(productNo);
        if (shippingOptionsJson != null && !shippingOptionsJson.trim().isEmpty() && !"[]".equals(shippingOptionsJson.trim())) {
            try {
                List<ProductShippingOptionDTO> shippingList = objectMapper.readValue(shippingOptionsJson, new TypeReference<List<ProductShippingOptionDTO>>() {});
                for (ProductShippingOptionDTO opt : shippingList) {
                    opt.setProductNo(productNo);
                    myPageService.insertShippingOptionEdit(opt);
                }
            } catch (Exception ignore) {}
        }

        // 6. 직거래 위치 교체
        myPageService.deleteProductMeetLocations(productNo);
        if (meetLocationsJson != null && !meetLocationsJson.trim().isEmpty() && !"[]".equals(meetLocationsJson.trim())) {
            try {
                List<ProductMeetLocationDTO> locationList = objectMapper.readValue(meetLocationsJson, new TypeReference<List<ProductMeetLocationDTO>>() {});
                int sortNo = 1;
                for (ProductMeetLocationDTO loc : locationList) {
                    loc.setProductNo(productNo);
                    loc.setSortNo(sortNo++);
                    myPageService.insertMeetLocationEdit(loc);
                }
            } catch (Exception ignore) {}
        }

        result.put("success", true);
        return result;
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

        int reportCount = myPageService.getProductReportCount(productNo);
        if (reportCount > 0) {
            result.put("success", false);
            result.put("message", "신고가 접수된 상품은 삭제할 수 없습니다.");
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

    @GetMapping("/review/write")
    public String reviewWritePage(
            @RequestParam("transactionId") int transactionId,
            @RequestParam String targetEmail,
            @RequestParam String productName,
            @RequestParam(required = false) String imgUrl,
            @RequestParam(required = false) String amount,
            @RequestParam(required = false) String paymentType,
            @RequestParam(required = false) String saleType,
            Model model) {
        model.addAttribute("transactionId", transactionId);
        model.addAttribute("targetEmail", targetEmail);
        model.addAttribute("productName", productName);
        model.addAttribute("imgUrl", imgUrl);
        model.addAttribute("amount", amount);
        model.addAttribute("paymentType", paymentType);
        model.addAttribute("saleType", saleType);
        MemberDTO seller = memberService.getMemberByEmail(targetEmail);
        if (seller != null) {
            model.addAttribute("sellerNickname", seller.getNickname());
            model.addAttribute("sellerProfileImg", seller.getProfileImg());
        }
        return "mypage/review_write";
    }

    @PostMapping("/review/write")
    @ResponseBody
    public Map<String, Object> writeReview(
            @RequestParam("transactionId") int transactionId,
            @RequestParam("targetEmail") String targetEmail,
            @RequestParam("rating") double rating,
            @RequestParam("oneLineCat") String oneLineCat,
            @RequestParam(value = "reviewContent", required = false) String reviewContent,
            Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("writerEmail", principal.getName());
            params.put("targetEmail", targetEmail);
            params.put("transactionId", transactionId);
            params.put("rating", rating);
            params.put("oneLineCat", oneLineCat);
            params.put("reviewContent", reviewContent);
            myPageService.insertReview(params);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "리뷰 작성에 실패했습니다: " + e.getMessage());
        }
        return result;
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

        // 계좌번호 형식 검사 (하이픈 제거 후 숫자 10~14자리)
        String digitsOnly = account.getAccountNum() == null ? "" : account.getAccountNum().replaceAll("-", "");
        if (!digitsOnly.matches("\\d{10,14}")) {
            result.put("success", false);
            result.put("message", "계좌번호는 숫자 10~14자리로 입력해주세요.");
            return result;
        }
        account.setAccountNum(digitsOnly); // 하이픈 제거 후 저장

        // 예금주 이름 = 회원 이름 일치 확인
        MemberDTO member = memberService.getMemberByEmail(principal.getName());
        if (member == null || !member.getUserName().equals(account.getAccountHolder())) {
            result.put("success", false);
            result.put("message", "예금주 이름이 회원 가입 시 등록한 이름과 일치하지 않습니다.");
            return result;
        }

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

        // 계좌번호 형식 검사
        String digitsOnly = account.getAccountNum() == null ? "" : account.getAccountNum().replaceAll("-", "");
        if (!digitsOnly.matches("\\d{10,14}")) {
            result.put("success", false);
            result.put("message", "계좌번호는 숫자 10~14자리로 입력해주세요.");
            return result;
        }
        account.setAccountNum(digitsOnly);

        // 예금주 이름 = 회원 이름 일치 확인
        MemberDTO member = memberService.getMemberByEmail(principal.getName());
        if (member == null || !member.getUserName().equals(account.getAccountHolder())) {
            result.put("success", false);
            result.put("message", "예금주 이름이 회원 가입 시 등록한 이름과 일치하지 않습니다.");
            return result;
        }

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

    // ===== 대표계좌 조회 (채팅용) =====

    @GetMapping("/primary-account")
    @ResponseBody
    public Map<String, Object> getPrimaryAccount(Principal principal) {
        Map<String, Object> result = new java.util.HashMap<>();
        if (principal == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }
        AccountDTO account = myPageService.getPrimaryAccount(principal.getName());
        if (account == null) {
            result.put("success", false);
            result.put("message", "등록된 대표계좌가 없습니다. 마이페이지에서 계좌를 등록해주세요.");
            return result;
        }
        result.put("success", true);
        result.put("bankName", account.getBankName());
        result.put("accountNum", account.getAccountNum());
        result.put("accountHolder", account.getAccountHolder());
        return result;
    }

    // ===== 송장번호 / 배송조회 =====

    @PostMapping("/product/invoice")
    @ResponseBody
    public Map<String, Object> saveInvoice(@RequestBody Map<String, Object> payload, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) { result.put("success", false); return result; }

        int productNo = Integer.parseInt(String.valueOf(payload.get("productNo")));
        String carrierCode = String.valueOf(payload.get("carrierCode"));
        String invoiceNo = String.valueOf(payload.get("invoiceNo")).replaceAll("[^0-9]", "");

        // 본인 상품 여부 확인
        Map<String, Object> checkParams = new HashMap<>();
        checkParams.put("productNo", productNo);
        checkParams.put("email", principal.getName());
        if (myPageService.getMyProductByNo(checkParams) == null) {
            result.put("success", false);
            result.put("message", "상품을 찾을 수 없습니다.");
            return result;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("productNo", productNo);
        params.put("carrierCode", carrierCode);
        params.put("invoiceNo", invoiceNo);
        params.put("email", principal.getName());
        myPageService.saveInvoice(params);
        result.put("success", true);
        return result;
    }

    @GetMapping("/product/track")
    @ResponseBody
    public Map<String, Object> trackDelivery(@RequestParam("productNo") int productNo, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) { result.put("success", false); return result; }

        Map<String, Object> params = new HashMap<>();
        params.put("productNo", productNo);
        params.put("email", principal.getName());
        ProductDTO product = myPageService.getInvoice(params);

        if (product == null || product.getInvoiceNo() == null || product.getInvoiceNo().isEmpty()) {
            result.put("success", false);
            result.put("message", "등록된 송장번호가 없습니다.");
            return result;
        }

        String carrierCode = product.getCarrierCode();
        String invoiceNo = product.getInvoiceNo();

        try {
            String url = "https://info.sweettracker.co.kr/api/v1/trackingInfo"
                + "?t_key=" + sweetTrackerApiKey
                + "&t_code=" + carrierCode
                + "&t_invoice=" + invoiceNo;

            RestTemplate rt = new RestTemplate();
            @SuppressWarnings("unchecked")
            Map<String, Object> apiResult = rt.getForObject(url, Map.class);
            result.put("success", true);
            result.put("data", apiResult);
            result.put("carrierCode", carrierCode);
            result.put("invoiceNo", invoiceNo);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "배송 조회 중 오류가 발생했습니다.");
        }
        return result;
    }

    @GetMapping("/product/track-by-invoice")
    @ResponseBody
    public Map<String, Object> trackDeliveryByInvoice(
            @RequestParam("carrierCode") String carrierCode,
            @RequestParam("invoiceNo") String invoiceNo,
            Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) { result.put("success", false); return result; }
        if (carrierCode == null || carrierCode.isEmpty() || invoiceNo == null || invoiceNo.isEmpty()) {
            result.put("success", false);
            result.put("message", "송장정보가 없습니다.");
            return result;
        }
        try {
            String url = "https://info.sweettracker.co.kr/api/v1/trackingInfo"
                + "?t_key=" + sweetTrackerApiKey
                + "&t_code=" + carrierCode
                + "&t_invoice=" + invoiceNo;
            RestTemplate rt = new RestTemplate();
            @SuppressWarnings("unchecked")
            Map<String, Object> apiResult = rt.getForObject(url, Map.class);
            result.put("success", true);
            result.put("data", apiResult);
            result.put("carrierCode", carrierCode);
            result.put("invoiceNo", invoiceNo);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "배송 조회 중 오류가 발생했습니다.");
        }
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

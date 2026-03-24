package com.spring.app.product.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.app.common.FileManager;
import com.spring.app.product.domain.ProductDTO;
import com.spring.app.product.domain.ProductImageDTO;
import com.spring.app.product.domain.ProductMeetLocationDTO;
import com.spring.app.product.domain.ProductPriceStatsDTO;
import com.spring.app.product.domain.ProductPriceTrendDTO;
import com.spring.app.product.domain.ProductReportDTO;
import com.spring.app.product.domain.ProductShippingOptionDTO;
import com.spring.app.product.domain.ReviewDTO;
import com.spring.app.product.domain.ReviewSummaryDTO;
import com.spring.app.product.domain.SearchKeywordDTO;
import com.spring.app.product.domain.SearchLogDTO;
import com.spring.app.product.domain.WishlistDTO;
import com.spring.app.product.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/product/")
@Slf4j
public class ProductController {

    private final ProductService pservice;
    private final FileManager fileManager;
    private final ObjectMapper objectMapper;
    
    @Value("${kakao.map.key}")
    private String kakaoMapKey;

    @ModelAttribute("kakaoMapKey")
    public String kakaoMapKey() {
        return kakaoMapKey;
    }

    @Value("${file.images-dir}")
    private String imagesDir;

    private static final String IMAGE_WEB_PREFIX = "/images/";

    @Value("${file.reports-dir}")
    private String reportsDir;

    private static final String REPORT_WEB_PREFIX = "/report_images/";

    private String getLoginEmail(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        String email = authentication.getName();

        if (email == null || email.trim().isEmpty() || "anonymousUser".equals(email)) {
            return null;
        }

        return email.trim();
    }

    private Map<String, Object> failResult(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }

    // 상품목록(장터)
    @GetMapping("/product_list")
    public String product_list(
            @RequestParam(name = "searchWord", required = false) String searchWord,
            @RequestParam(name = "areaDong", required = false) String areaDong,
            @RequestParam(name = "tradeAvailable", required = false) String tradeAvailable,
            @RequestParam(name = "parcelAvailable", required = false) String parcelAvailable,
            @RequestParam(name = "freeOnly", required = false) String freeOnly,
            @RequestParam(name = "categoryNo", required = false) Integer categoryNo,
            @RequestParam(name = "sortType", required = false) String sortType,
            @RequestParam(name = "priceMin", required = false) Integer priceMin,
            @RequestParam(name = "priceMax", required = false) Integer priceMax,
            Model model,
            Authentication authentication,
            HttpServletRequest request,
            HttpSession session) {

        if (searchWord != null) searchWord = searchWord.trim();
        if (areaDong != null) areaDong = areaDong.trim();
        if (sortType == null || "".equals(sortType.trim())) sortType = "latest";
        if (searchWord != null) {
            searchWord = searchWord.trim().replaceAll("\\s+", " ");
        }

        if (freeOnly != null && !"".equals(freeOnly.trim())) {
            priceMin = null;
            priceMax = null;
        }

        String memberEmail = getLoginEmail(authentication);

        int startRow = 1;
        int endRow = 12;

        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("searchWord", searchWord);
        paraMap.put("areaDong", areaDong);
        paraMap.put("tradeAvailable", tradeAvailable);
        paraMap.put("parcelAvailable", parcelAvailable);
        paraMap.put("freeOnly", freeOnly);
        paraMap.put("categoryNo", categoryNo);
        paraMap.put("sortType", sortType);
        paraMap.put("priceMin", priceMin);
        paraMap.put("priceMax", priceMax);
        paraMap.put("startRow", startRow);
        paraMap.put("endRow", endRow);
        paraMap.put("memberEmail", memberEmail);

        List<ProductDTO> list = pservice.selectProductListByConditionMore(paraMap);

        if (searchWord != null && searchWord.length() >= 2 && list != null && !list.isEmpty()) {
            SearchLogDTO searchLogDto = new SearchLogDTO();
            searchLogDto.setKeyword(searchWord);
            searchLogDto.setSearchType("PRODUCT");
            searchLogDto.setIpAddress(request.getRemoteAddr());
            searchLogDto.setUserAgent(request.getHeader("User-Agent"));

            if (memberEmail != null) {
                searchLogDto.setMemberEmail(memberEmail);
            } else {
                searchLogDto.setSessionId(session.getId());
            }

            pservice.insertSearchLog(searchLogDto);
        }

        List<SearchKeywordDTO> popularKeywordList = pservice.selectPopularKeywordList();

        Map<String, Object> priceParaMap = new HashMap<>();
        priceParaMap.put("searchWord", searchWord);
        priceParaMap.put("areaDong", areaDong);
        priceParaMap.put("tradeAvailable", tradeAvailable);
        priceParaMap.put("parcelAvailable", parcelAvailable);
        priceParaMap.put("freeOnly", freeOnly);
        priceParaMap.put("categoryNo", categoryNo);
        priceParaMap.put("priceMin", priceMin);
        priceParaMap.put("priceMax", priceMax);

        ProductPriceStatsDTO priceStats = pservice.selectRecentProductPriceStats(priceParaMap);

        model.addAttribute("list", list);
        model.addAttribute("popularKeywordList", popularKeywordList);
        model.addAttribute("priceStats", priceStats);
        model.addAttribute("isLogin", memberEmail != null);

        return "product/product_list";
    }

    // 판매하기
    @GetMapping("/sell")
    public String sellPage() {
        return "product/sell";
    }

    // 상품등록하기
    @PostMapping("/sellRegister")
    @ResponseBody
    public Map<String, Object> sellRegister(
            ProductDTO productDto,
            @RequestParam(name = "images", required = false) List<MultipartFile> images,
            @RequestParam(name = "mainIndex", required = false) Integer mainIndex,
            Authentication authentication) {

        String loginEmail = getLoginEmail(authentication);
        if (loginEmail == null) {
            return failResult("로그인 정보가 없습니다. 다시 로그인 해주세요.");
        }

        productDto.setSellerEmail(loginEmail);

        if (images == null) images = new ArrayList<>();
        if (mainIndex == null) mainIndex = 0;

        if (mainIndex < 0) mainIndex = 0;
        if (mainIndex >= images.size()) mainIndex = 0;

        List<ProductShippingOptionDTO> shippingOptionList = parseShippingOptions(productDto.getShippingOptionsJson());
        List<ProductMeetLocationDTO> meetLocationList = parseMeetLocations(productDto.getMeetLocationsJson());

        productDto.setShippingOptionList(shippingOptionList);
        productDto.setMeetLocationList(meetLocationList);

        if (images.size() > 3) {
            return failResult("이미지는 최대 3장까지 가능합니다.");
        }

        String tradeMethod = productDto.getTradeMethod();

        if ("택배".equals(tradeMethod)) {
            if (shippingOptionList == null || shippingOptionList.isEmpty()) {
                return failResult("택배 거래는 배송옵션을 1개 이상 선택해야 합니다.");
            }

            for (ProductShippingOptionDTO opt : shippingOptionList) {
                if (opt.getParcelType() == null || opt.getParcelType().trim().isEmpty()) {
                    return failResult("배송옵션 타입이 비어있습니다.");
                }
                if (opt.getShippingFee() == null) {
                    return failResult(opt.getParcelType() + " 배송비를 입력해 주세요.");
                }
            }
        }

        if ("직거래".equals(tradeMethod)) {
            if (meetLocationList == null || meetLocationList.isEmpty() || meetLocationList.size() > 3) {
                return failResult("직거래 위치는 1~3개까지 설정해야 합니다.");
            }

            for (ProductMeetLocationDTO loc : meetLocationList) {
                if (loc.getFullAddress() == null || loc.getFullAddress().trim().isEmpty()) {
                    return failResult("직거래 위치 주소가 비어있습니다.");
                }
            }
        }

        List<ProductImageDTO> imageDtoList = new ArrayList<>();
        List<String> savedFileNames = new ArrayList<>();

        try {
            int sortNo = 1;

            for (int i = 0; i < images.size(); i++) {
                MultipartFile mf = images.get(i);

                if (mf == null || mf.isEmpty()) continue;

                String originalFilename = mf.getOriginalFilename();
                byte[] bytes = mf.getBytes();

                String savedFileName = fileManager.doFileUpload(bytes, originalFilename, imagesDir);
                savedFileNames.add(savedFileName);

                ProductImageDTO imgDto = new ProductImageDTO();
                imgDto.setOrgfilename(originalFilename);
                imgDto.setFilename(savedFileName);
                imgDto.setImgUrl(IMAGE_WEB_PREFIX + savedFileName);
                imgDto.setSortNo(sortNo++);
                imgDto.setIsMain(i == mainIndex ? "Y" : "N");

                imageDtoList.add(imgDto);
            }

        } catch (Exception e) {
            log.error("이미지 업로드 실패", e);

            for (String fn : savedFileNames) {
                try {
                    fileManager.doFileDelete(fn, imagesDir);
                } catch (Exception ignore) {}
            }

            return failResult("이미지 업로드 실패");
        }

        try {
            int n = pservice.productSellRegister(productDto, imageDtoList, shippingOptionList, meetLocationList);

            if (n != 1) {
                log.warn("DB 저장 실패: productNo={}, sellerEmail={}", productDto.getProductNo(), productDto.getSellerEmail());

                for (String fn : savedFileNames) {
                    try {
                        fileManager.doFileDelete(fn, imagesDir);
                    } catch (Exception ignore) {}
                }

                return failResult("DB 저장 실패");
            }

        } catch (Exception e) {
            log.error("DB 저장 중 예외 발생", e);

            for (String fn : savedFileNames) {
                try {
                    fileManager.doFileDelete(fn, imagesDir);
                } catch (Exception ignore) {}
            }

            return failResult("DB 저장 실패");
        }

        Map<String, Object> ok = new LinkedHashMap<>();
        ok.put("success", true);
        ok.put("productNo", productDto.getProductNo());
        ok.put("redirectUrl", "/finalProject_3/product/product_list");
        return ok;
    }

    private List<ProductShippingOptionDTO> parseShippingOptions(String json) {
        if (json == null || json.trim().isEmpty()) return new ArrayList<>();

        try {
            return objectMapper.readValue(json, new TypeReference<List<ProductShippingOptionDTO>>() {});
        } catch (Exception e) {
            log.warn("shippingOptionsJson 파싱 실패: {}", json, e);
            return new ArrayList<>();
        }
    }

    private List<ProductMeetLocationDTO> parseMeetLocations(String json) {
        if (json == null || json.trim().isEmpty()) return new ArrayList<>();

        try {
            return objectMapper.readValue(json, new TypeReference<List<ProductMeetLocationDTO>>() {});
        } catch (Exception e) {
            log.warn("meetLocationsJson 파싱 실패: {}", json, e);
            return new ArrayList<>();
        }
    }

    // 상품목록 더보기
    @GetMapping("/product_list_more")
    @ResponseBody
    public List<ProductDTO> product_list_more(
            @RequestParam(name = "searchWord", required = false) String searchWord,
            @RequestParam(name = "areaDong", required = false) String areaDong,
            @RequestParam(name = "tradeAvailable", required = false) String tradeAvailable,
            @RequestParam(name = "parcelAvailable", required = false) String parcelAvailable,
            @RequestParam(name = "freeOnly", required = false) String freeOnly,
            @RequestParam(name = "categoryNo", required = false) Integer categoryNo,
            @RequestParam(name = "sortType", required = false) String sortType,
            @RequestParam(name = "priceMin", required = false) Integer priceMin,
            @RequestParam(name = "priceMax", required = false) Integer priceMax,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            Authentication authentication) {

        if (searchWord != null) searchWord = searchWord.trim();
        if (areaDong != null) areaDong = areaDong.trim();
        if (sortType == null || "".equals(sortType.trim())) sortType = "latest";

        if (freeOnly != null && !"".equals(freeOnly.trim())) {
            priceMin = null;
            priceMax = null;
        }

        int startRow = ((page - 1) * size) + 1;
        int endRow = page * size;

        String memberEmail = getLoginEmail(authentication);

        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("searchWord", searchWord);
        paraMap.put("areaDong", areaDong);
        paraMap.put("tradeAvailable", tradeAvailable);
        paraMap.put("parcelAvailable", parcelAvailable);
        paraMap.put("freeOnly", freeOnly);
        paraMap.put("categoryNo", categoryNo);
        paraMap.put("sortType", sortType);
        paraMap.put("priceMin", priceMin);
        paraMap.put("priceMax", priceMax);
        paraMap.put("startRow", startRow);
        paraMap.put("endRow", endRow);
        paraMap.put("memberEmail", memberEmail);

        return pservice.selectProductListByConditionMore(paraMap);
    }

    // 나눔하기
    @GetMapping("/share")
    public String share() {
        return "product/share";
    }

    @GetMapping("/product_detail/{productNo}")
    public String detail(@PathVariable("productNo") int productNo,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttr) {

        String memberEmail = getLoginEmail(authentication);

        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("productNo", productNo);
        paraMap.put("memberEmail", memberEmail);

        ProductDTO productDto = pservice.getProductDetailFull(paraMap);

        if (productDto == null) {
            redirectAttr.addFlashAttribute("message", "존재하지 않는 상품입니다.");
            return "redirect:/product/product_list";
        }

        String tradeStatus = productDto.getTradeStatus();

        boolean isSeller = memberEmail != null && memberEmail.equals(productDto.getSellerEmail());
        boolean isBuyer = memberEmail != null && pservice.isBuyerOfProduct(productNo, memberEmail);
        boolean isOwner = memberEmail != null && memberEmail.equals(productDto.getSellerEmail());

        if (("예약중".equals(tradeStatus) || "판매완료".equals(tradeStatus))
                && !isSeller && !isBuyer) {
            redirectAttr.addFlashAttribute("message", "해당 상품은 거래 당사자만 조회할 수 있습니다.");
            return "redirect:/product/product_list";
        }

        pservice.updateViewCount(productNo);

        List<ProductDTO> similarProductList = pservice.selectSimilarProducts(productDto);
        List<ReviewDTO> recentReviewList = pservice.selectRecentReviewsByProductNo(productNo);

        productDto.setRecentReviewList(recentReviewList);

        model.addAttribute("product", productDto);
        model.addAttribute("similarProductList", similarProductList);
        model.addAttribute("isLogin", memberEmail != null);
        model.addAttribute("isOwner", isOwner);

        return "product/product_detail";
    }

    // 찜
    @PostMapping("/wishlist/toggle")
    @ResponseBody
    public Map<String, Object> toggleWishlist(@RequestParam("productNo") Integer productNo,
                                              Authentication authentication) {

        String memberEmail = getLoginEmail(authentication);
        if (memberEmail == null) {
            return failResult("로그인이 필요합니다.");
        }

        WishlistDTO wishlistDto = new WishlistDTO();
        wishlistDto.setMemberEmail(memberEmail);
        wishlistDto.setProductNo(productNo);

        boolean wished = pservice.toggleWishlist(wishlistDto);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("wished", wished);
        return result;
    }

    // 시세조회
    @GetMapping("/price_check")
    public String price_check(
            @RequestParam(name = "searchWord", required = false) String searchWord,
            @RequestParam(name = "sortType", required = false) String sortType,
            @RequestParam(name = "priceMode", required = false) String priceMode,
            Authentication authentication,
            HttpServletRequest request,
            HttpSession session,
            Model model) {

        if (searchWord != null) {
            searchWord = searchWord.trim().replaceAll("\\s+", " ");
        }

        if (sortType == null || "".equals(sortType.trim())) {
            sortType = "latest";
        }

        if (priceMode == null || "".equals(priceMode.trim())) {
            priceMode = "list";
        }

        boolean hasSearch = searchWord != null && !"".equals(searchWord);
        String memberEmail = getLoginEmail(authentication);

        List<ProductDTO> list = new ArrayList<>();
        ProductPriceStatsDTO priceStats = null;
        List<ProductPriceTrendDTO> priceChartData = new ArrayList<>();

        if (hasSearch) {
            Map<String, Object> paraMap = new LinkedHashMap<>();
            paraMap.put("searchWord", searchWord);
            paraMap.put("sortType", sortType);
            paraMap.put("priceMode", priceMode);
            paraMap.put("memberEmail", memberEmail);

            list = pservice.selectPriceCheckProductList(paraMap);

            if (searchWord.length() >= 2 && list != null && !list.isEmpty()) {
                SearchLogDTO searchLogDto = new SearchLogDTO();
                searchLogDto.setKeyword(searchWord);
                searchLogDto.setSearchType("PRICE");
                searchLogDto.setIpAddress(request.getRemoteAddr());
                searchLogDto.setUserAgent(request.getHeader("User-Agent"));

                if (memberEmail != null) {
                    searchLogDto.setMemberEmail(memberEmail);
                } else {
                    searchLogDto.setSessionId(session.getId());
                }

                pservice.insertSearchLog(searchLogDto);
            }

            if (list != null && !list.isEmpty()) {
                priceStats = pservice.selectPriceCheckStats(paraMap);
                priceChartData = pservice.selectPriceCheckChartData(paraMap);
            }
        }

        boolean hasResult = list != null && !list.isEmpty();

        model.addAttribute("searchWord", searchWord);
        model.addAttribute("sortType", sortType);
        model.addAttribute("priceMode", priceMode);
        model.addAttribute("priceStats", priceStats);
        model.addAttribute("priceChartData", priceChartData);
        model.addAttribute("list", list);
        model.addAttribute("hasSearch", hasSearch);
        model.addAttribute("hasResult", hasResult);
        model.addAttribute("isLogin", memberEmail != null);

        return "product/price_check";
    }

    // 판매자 정보 페이지
    @GetMapping("/product_user_profile")
    public String product_user_profile(@RequestParam("productNo") int productNo,
                                       Model model,
                                       Authentication authentication) {
        model.addAttribute("productNo", productNo);
        model.addAttribute("isLogin", getLoginEmail(authentication) != null);
        return "product/product_user_profile";
    }

    // 판매자 기본 정보 조회
    @GetMapping("/seller/profile")
    @ResponseBody
    public Map<String, Object> sellerProfile(@RequestParam("productNo") int productNo) {

        ProductDTO sellerDto = pservice.selectSellerProfileByProductNo(productNo);

        Map<String, Object> result = new LinkedHashMap<>();

        if (sellerDto == null) {
            result.put("success", false);
            result.put("message", "판매자 정보를 찾을 수 없습니다.");
            return result;
        }

        result.put("success", true);
        result.put("seller", sellerDto);
        return result;
    }

    // 판매자 판매상품 조회
    @GetMapping("/seller/products")
    @ResponseBody
    public Map<String, Object> sellerProducts(@RequestParam("productNo") int productNo,
                                              @RequestParam(name = "sortType", defaultValue = "latest") String sortType,
                                              @RequestParam(name = "page", defaultValue = "1") int page,
                                              @RequestParam(name = "size", defaultValue = "8") int size,
                                              Authentication authentication) {

        String memberEmail = getLoginEmail(authentication);

        int startRow = ((page - 1) * size) + 1;
        int endRow = page * size;

        Map<String, Object> paraMap = new LinkedHashMap<>();
        paraMap.put("productNo", productNo);
        paraMap.put("sortType", sortType);
        paraMap.put("startRow", startRow);
        paraMap.put("endRow", endRow);
        paraMap.put("memberEmail", memberEmail);

        List<ProductDTO> productList = pservice.selectSellerProductsByProductNo(paraMap);
        int totalCount = pservice.selectSellerProductCountByProductNo(productNo);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("productList", productList);
        result.put("totalCount", totalCount);
        result.put("hasMore", totalCount > endRow);

        return result;
    }

    // 자동 검색어
    @GetMapping("/wordSearchShow")
    @ResponseBody
    public List<Map<String, String>> wordSearchShow(@RequestParam Map<String, String> paraMap) {
        List<String> wordList = pservice.wordSearchShow(paraMap);

        List<Map<String, String>> mapList = new ArrayList<>();

        if (wordList != null) {
            for (String word : wordList) {
                Map<String, String> map = new HashMap<>();
                map.put("word", word);
                mapList.add(map);
            }
        }

        return mapList;
    }

    // 게시글 신고하기
    @PostMapping("/report")
    @ResponseBody
    public Map<String, Object> reportProduct(
            @RequestParam("productNo") int productNo,
            @RequestParam("reportMainCategory") String reportMainCategory,
            @RequestParam("reportSubCategory") String reportSubCategory,
            @RequestParam("reportContent") String reportContent,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication authentication) {

        String loginEmail = getLoginEmail(authentication);

        if (loginEmail == null) {
            return failResult("로그인이 필요합니다.");
        }

        if (reportMainCategory == null || reportMainCategory.trim().isEmpty()) {
            return failResult("신고 대분류를 선택해주세요.");
        }

        if (reportSubCategory == null || reportSubCategory.trim().isEmpty()) {
            return failResult("신고 유형을 선택해주세요.");
        }

        if (reportContent == null || reportContent.trim().isEmpty()) {
            return failResult("신고 내용을 입력해주세요.");
        }

        reportMainCategory = reportMainCategory.trim();
        reportSubCategory = reportSubCategory.trim();
        reportContent = reportContent.trim();

        if (reportContent.length() > 150) {
            return failResult("신고 내용은 150자 이내로 입력해주세요.");
        }

        String targetEmail = pservice.selectSellerEmailByProductNo(productNo);

        if (targetEmail == null || targetEmail.trim().isEmpty()) {
            return failResult("신고 대상 상품을 찾을 수 없습니다.");
        }

        if (loginEmail.equals(targetEmail)) {
            return failResult("본인 게시글은 신고할 수 없습니다.");
        }

        ProductReportDTO reportDto = new ProductReportDTO();
        reportDto.setReporterEmail(loginEmail);
        reportDto.setTargetEmail(targetEmail);
        reportDto.setTypeId(null);
        reportDto.setProductNum(productNo);
        reportDto.setReviewNum(null);
        reportDto.setRoomId(null);
        reportDto.setNosqlMsgKey(null);
        reportDto.setReportDetail(reportContent);
        reportDto.setReportStatus("접수");
        reportDto.setReportImg(null);
        reportDto.setReportMainCategory(reportMainCategory);
        reportDto.setReportSubCategory(reportSubCategory);

        Integer typeId = pservice.selectProductReportTypeId(reportDto);

        if (typeId == null) {
            return failResult("유효하지 않은 신고 유형입니다.");
        }

        reportDto.setTypeId(typeId);

        String savedFileName = null;

        try {
            if (image != null && !image.isEmpty()) {
                String originalFilename = image.getOriginalFilename();
                byte[] bytes = image.getBytes();

                savedFileName = fileManager.doFileUpload(bytes, originalFilename, reportsDir);
                reportDto.setReportImg(REPORT_WEB_PREFIX + savedFileName);
            }

            int n = pservice.insertProductReport(reportDto);

            if (n != 1) {
                if (savedFileName != null) {
                    try {
                        fileManager.doFileDelete(savedFileName, reportsDir);
                    } catch (Exception ignore) {}
                }
                return failResult("신고 접수에 실패했습니다.");
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("message", "신고가 정상적으로 접수되었습니다.");
            return result;
        } catch (Exception e) {
            log.error("상품 신고 등록 실패", e);

            if (savedFileName != null) {
                try {
                    fileManager.doFileDelete(savedFileName, reportsDir);
                } catch (Exception ignore) {}
            }

            return failResult("신고 처리 중 오류가 발생했습니다.");
        }
    }
    
    //리뷰 요약
    @GetMapping("/seller/reviews/summary")
    @ResponseBody
    public Map<String, Object> sellerReviewSummary(@RequestParam("productNo") int productNo) {

        ReviewSummaryDTO summary = pservice.selectSellerReviewSummaryByProductNo(productNo);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("summary", summary);
        return result;
    }
    
    //리뷰목록
    @GetMapping("/seller/reviews")
    @ResponseBody
    public Map<String, Object> sellerReviews(@RequestParam("productNo") int productNo,
                                             @RequestParam(name = "sortType", defaultValue = "latest") String sortType,
                                             @RequestParam(name = "reviewCategory", required = false) String reviewCategory,
                                             @RequestParam(name = "page", defaultValue = "1") int page,
                                             @RequestParam(name = "size", defaultValue = "5") int size) {

        int startRow = ((page - 1) * size) + 1;
        int endRow = page * size;

        Map<String, Object> paraMap = new LinkedHashMap<>();
        paraMap.put("productNo", productNo);
        paraMap.put("sortType", sortType);
        paraMap.put("reviewCategory", reviewCategory);
        paraMap.put("startRow", startRow);
        paraMap.put("endRow", endRow);

        List<ReviewDTO> reviewList = pservice.selectSellerReviewListByProductNo(paraMap);
        int totalCount = pservice.selectSellerReviewTotalCountByProductNo(paraMap);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("reviewList", reviewList);
        result.put("totalCount", totalCount);
        result.put("hasMore", totalCount > endRow);

        return result;
    }
}
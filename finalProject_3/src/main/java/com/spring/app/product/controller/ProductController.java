package com.spring.app.product.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.app.common.FileManager;
import com.spring.app.product.domain.ProductDTO;
import com.spring.app.product.domain.ProductImageDTO;
import com.spring.app.product.domain.ProductMeetLocationDTO;
import com.spring.app.product.domain.ProductPriceStatsDTO;
import com.spring.app.product.domain.ProductShippingOptionDTO;
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
    
    @Value("${file.images-dir}")
    private String imagesDir;

    private static final String IMAGE_WEB_PREFIX = "/images/";

    @GetMapping("/product_list")
    public String product_list(
            @RequestParam(name = "searchWord", required = false) String searchWord,
            @RequestParam(name = "areaDong", required = false) String areaDong,
            @RequestParam(name = "tradeAvailable", required = false) String tradeAvailable,
            @RequestParam(name = "parcelAvailable", required = false) String parcelAvailable,
            @RequestParam(name = "categoryNo", required = false) Integer categoryNo,
            @RequestParam(name = "sortType", required = false) String sortType,
            @RequestParam(name = "priceMin", required = false) Integer priceMin,
            @RequestParam(name = "priceMax", required = false) Integer priceMax,
            Model model,
            Principal principal,
            HttpServletRequest request,
            HttpSession session) {

        if (searchWord != null) searchWord = searchWord.trim();
        if (areaDong != null) areaDong = areaDong.trim();
        if (sortType == null || "".equals(sortType.trim())) sortType = "latest";

        if (searchWord != null && !"".equals(searchWord)) {
            SearchLogDTO searchLogDto = new SearchLogDTO();
            searchLogDto.setKeyword(searchWord);
            searchLogDto.setSearchType("PRODUCT");
            searchLogDto.setIpAddress(request.getRemoteAddr());
            searchLogDto.setUserAgent(request.getHeader("User-Agent"));

            if (principal != null && principal.getName() != null && !"".equals(principal.getName().trim())) {
                searchLogDto.setMemberEmail(principal.getName().trim());
            }
            else {
                searchLogDto.setSessionId(session.getId());
            }

            pservice.insertSearchLog(searchLogDto);
        }

        int startRow = 1;
        int endRow = 12;

        String memberEmail = null;
        if (principal != null && principal.getName() != null && !"".equals(principal.getName().trim())) {
            memberEmail = principal.getName().trim();
        }

        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("searchWord", searchWord);
        paraMap.put("areaDong", areaDong);
        paraMap.put("tradeAvailable", tradeAvailable);
        paraMap.put("parcelAvailable", parcelAvailable);
        paraMap.put("categoryNo", categoryNo);
        paraMap.put("sortType", sortType);
        paraMap.put("priceMin", priceMin);
        paraMap.put("priceMax", priceMax);
        paraMap.put("startRow", startRow);
        paraMap.put("endRow", endRow);
        paraMap.put("memberEmail", memberEmail);

        List<ProductDTO> list = pservice.selectProductListByConditionMore(paraMap);
        
        List<SearchKeywordDTO> popularKeywordList = pservice.selectPopularKeywordList();
        
        Map<String, Object> priceParaMap = new HashMap<>();
        priceParaMap.put("searchWord", searchWord);
        priceParaMap.put("areaDong", areaDong);
        priceParaMap.put("tradeAvailable", tradeAvailable);
        priceParaMap.put("parcelAvailable", parcelAvailable);
        priceParaMap.put("categoryNo", categoryNo);
        priceParaMap.put("priceMin", priceMin);
        priceParaMap.put("priceMax", priceMax);

        ProductPriceStatsDTO priceStats = pservice.selectRecentProductPriceStats(priceParaMap);
        
        model.addAttribute("list", list);
        model.addAttribute("popularKeywordList", popularKeywordList);
        model.addAttribute("priceStats", priceStats);
        model.addAttribute("isLogin", principal != null);
        
        return "product/product_list";
    }
    
   
 
    
    //판매하기
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sell")
    public String sellPage() {
        return "product/sell";
    }

    /**
     * 판매하기 등록 (상품 + 이미지(1~3) + 배송옵션(N) + 직거래위치(1~3))
     * - shippingOptionsJson / meetLocationsJson 을 컨트롤러에서 List로 파싱해 DTO에 세팅
     * - sellerEmail은 Principal에서 서버가 직접 세팅
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("sellRegister")
    @ResponseBody
    public Map<String, Object> sellRegister(
            ProductDTO productDto,
            @RequestParam(name = "images", required = false) List<MultipartFile> images,
            @RequestParam(name = "mainIndex", required = false) Integer mainIndex,
            Principal principal
    ) {

        Map<String, Object> fail = new LinkedHashMap<>();
        fail.put("success", false);

        // ===== 0) 로그인 이메일을 판매자 이메일로 세팅 =====
        if (principal == null || principal.getName() == null || principal.getName().trim().isEmpty()) {
            fail.put("message", "로그인 정보가 없습니다. 다시 로그인 해주세요.");
            return fail;
        }
        productDto.setSellerEmail(principal.getName().trim());

        if (images == null) images = new ArrayList<>();
        if (mainIndex == null) mainIndex = 0;

        if (mainIndex < 0) mainIndex = 0;
        if (mainIndex >= images.size()) mainIndex = 0;

        // ===== 1) JSON(hidden) -> List 파싱해서 DTO에 세팅 =====
        List<ProductShippingOptionDTO> shippingOptionList = parseShippingOptions(productDto.getShippingOptionsJson());
        List<ProductMeetLocationDTO> meetLocationList = parseMeetLocations(productDto.getMeetLocationsJson());

        productDto.setShippingOptionList(shippingOptionList);
        productDto.setMeetLocationList(meetLocationList);

        // ===== 2) 검증 =====
        if (images.size() > 3) {
            fail.put("message", "이미지는 최대 3장까지 가능합니다.");
            return fail;
        }

        String tradeMethod = productDto.getTradeMethod(); // radio(name=tradeMethod)

        if ("택배".equals(tradeMethod)) {
            if (shippingOptionList == null || shippingOptionList.isEmpty()) {
                fail.put("message", "택배 거래는 배송옵션을 1개 이상 선택해야 합니다.");
                return fail;
            }
            for (ProductShippingOptionDTO opt : shippingOptionList) {
                if (opt.getParcelType() == null || opt.getParcelType().trim().isEmpty()) {
                    fail.put("message", "배송옵션 타입이 비어있습니다.");
                    return fail;
                }
                if (opt.getShippingFee() == null) {
                    fail.put("message", opt.getParcelType() + " 배송비를 입력해 주세요.");
                    return fail;
                }
            }
        }

        if ("직거래".equals(tradeMethod)) {
            if (meetLocationList == null || meetLocationList.isEmpty() || meetLocationList.size() > 3) {
                fail.put("message", "직거래 위치는 1~3개까지 설정해야 합니다.");
                return fail;
            }
            for (ProductMeetLocationDTO loc : meetLocationList) {
                if (loc.getFullAddress() == null || loc.getFullAddress().trim().isEmpty()) {
                    fail.put("message", "직거래 위치 주소가 비어있습니다.");
                    return fail;
                }
            }
        }

        // ===== 3) 파일 업로드 -> ProductImageDTO 생성 =====
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
                try { fileManager.doFileDelete(fn, imagesDir); } catch (Exception ignore) {}
            }
            fail.put("message", "이미지 업로드 실패");
            return fail;
        }

        // ===== 4) DB 저장 =====
        try {
            int n = pservice.productSellRegister(productDto, imageDtoList, shippingOptionList, meetLocationList);

            if (n != 1) {
                // 서비스가 0 리턴하는 경우도 대비
                log.warn("DB 저장 실패: productNo={}, sellerEmail={}", productDto.getProductNo(), productDto.getSellerEmail());
                for (String fn : savedFileNames) {
                    try { fileManager.doFileDelete(fn, imagesDir); } catch (Exception ignore) {}
                }
                fail.put("message", "DB 저장 실패");
                return fail;
            }

        } catch (Exception e) {
            log.error("DB 저장 중 예외 발생", e);

            for (String fn : savedFileNames) {
                try { fileManager.doFileDelete(fn, imagesDir); } catch (Exception ignore) {}
            }
            fail.put("message", "DB 저장 실패");
            return fail;
        }

        // ===== 5) 프론트 응답 =====
        Map<String, Object> ok = new LinkedHashMap<>();
        ok.put("success", true);
        ok.put("productNo", productDto.getProductNo());
        ok.put("redirectUrl", "/product/product_list");
        return ok;
    }

    // ---------- JSON 파싱 유틸 ----------
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
    
    
    //상품 더보기
    @GetMapping("/product_list_more")
    @ResponseBody
    public List<ProductDTO> product_list_more(
            @RequestParam(name = "searchWord", required = false) String searchWord,
            @RequestParam(name = "areaDong", required = false) String areaDong,
            @RequestParam(name = "tradeAvailable", required = false) String tradeAvailable,
            @RequestParam(name = "parcelAvailable", required = false) String parcelAvailable,
            @RequestParam(name = "categoryNo", required = false) Integer categoryNo,
            @RequestParam(name = "sortType", required = false) String sortType,
            @RequestParam(name = "priceMin", required = false) Integer priceMin,
            @RequestParam(name = "priceMax", required = false) Integer priceMax,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            Principal principal
    ) {
        if (searchWord != null) searchWord = searchWord.trim();
        if (areaDong != null) areaDong = areaDong.trim();
        if (sortType == null || "".equals(sortType.trim())) sortType = "latest";

        int startRow = ((page - 1) * size) + 1;
        int endRow = page * size;

        String memberEmail = null;
        if (principal != null && principal.getName() != null && !"".equals(principal.getName().trim())) {
            memberEmail = principal.getName().trim();
        }

        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("searchWord", searchWord);
        paraMap.put("areaDong", areaDong);
        paraMap.put("tradeAvailable", tradeAvailable);
        paraMap.put("parcelAvailable", parcelAvailable);
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

    
 // 상품상세페이지
    @GetMapping("/product_detail/{productNo}")
    public String detail(@PathVariable("productNo") int productNo,
                         Principal principal,
                         Model model) {

        pservice.updateViewCount(productNo);

        ProductDTO productDto = pservice.getProductDetailFull(productNo);

        boolean isLogin = false;
        if (principal != null && principal.getName() != null && !"".equals(principal.getName().trim())) {
            isLogin = true;
        }

        List<ProductDTO> similarProductList = pservice.selectSimilarProducts(productDto);

        model.addAttribute("product", productDto);
        model.addAttribute("similarProductList", similarProductList);
        model.addAttribute("isLogin", isLogin);

        return "product/product_detail";
    }
    
    
    //찜
    @PostMapping("/wishlist/toggle")
    @ResponseBody
    public Map<String, Object> toggleWishlist(@RequestParam("productNo") Integer productNo,
                                              Principal principal) {

        Map<String, Object> result = new LinkedHashMap<>();

        if(principal == null || principal.getName() == null || "".equals(principal.getName().trim())) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }

        WishlistDTO wishlistDto = new WishlistDTO();
        wishlistDto.setMemberEmail(principal.getName().trim());
        wishlistDto.setProductNo(productNo);

        boolean wished = pservice.toggleWishlist(wishlistDto);

        result.put("success", true);
        result.put("wished", wished);

        return result;
    }
    


    // 시세조회
    @GetMapping("/price_check")
    public String price_check() {
        return "product/price_check";
    }

 

    // 판매자정보
    @GetMapping("/product_user_profile")
    public String product_user_profile() {
        return "product/product_user_profile";
    }
    
    //검색
    @GetMapping("wordSearchShow")
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
}
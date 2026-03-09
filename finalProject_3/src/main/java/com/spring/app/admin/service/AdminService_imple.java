package com.spring.app.admin.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.spring.app.admin.ad.domain.AdDTO;
import com.spring.app.admin.domain.InquiryDTO;
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
	@Value("${file.upload-dir}")
	private String uploadDir;

	@Override
	public int registerAd(AdDTO adDto) {

	    MultipartFile attachment = adDto.getAttachment();

	    if (attachment != null && !attachment.isEmpty()) {

	        try {

	            Path uploadPath = Paths.get(uploadDir, "ad");

	            if (!Files.exists(uploadPath)) {
	                Files.createDirectories(uploadPath);
	            }

	            String savedName =
	                    UUID.randomUUID().toString() + "_" + attachment.getOriginalFilename();

	            Files.copy(
	                    attachment.getInputStream(),
	                    uploadPath.resolve(savedName),
	                    StandardCopyOption.REPLACE_EXISTING
	            );

	            adDto.setFilePath("ad/" + savedName);

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
		public List<ProductDTO> getProductList(int page, int size) {
			int offset = (page - 1) * size;
			return dao.selectProductList(offset,size);
		}

		//총 상품 개수 
		@Override
		public int getTotalProductsCount() {
			return dao.selectProductCount();
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
		
		
		
}






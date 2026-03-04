package com.spring.app.admin.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.spring.app.admin.ad.domain.AdDTO;
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

	//광고 신청 insert하기
	@Override
	public int registerAd(AdDTO adDto) {
		MultipartFile attachment = adDto.getAttachment();
		if (attachment != null && !attachment.isEmpty()) {
			try {
				Path uploadPath = Paths.get(uploadDir);
				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}
				String savedName = UUID.randomUUID().toString() + "_" + attachment.getOriginalFilename();
				Files.copy(attachment.getInputStream(), uploadPath.resolve(savedName), StandardCopyOption.REPLACE_EXISTING);
				adDto.setFilePath(savedName);
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


}






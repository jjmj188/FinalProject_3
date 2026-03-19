package com.spring.app.mypage.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.spring.app.common.AccountEncryptUtil;
import com.spring.app.mypage.domain.AccountDTO;
import com.spring.app.mypage.domain.DeliveryAddressDTO;
import com.spring.app.mypage.domain.MyPurchaseDTO;
import com.spring.app.mypage.domain.MyReportDTO;
import com.spring.app.mypage.domain.NotificationDTO;
import com.spring.app.product.domain.ProductDTO;
import com.spring.app.product.domain.ProductImageDTO;
import com.spring.app.product.domain.ProductShippingOptionDTO;
import com.spring.app.product.domain.ProductMeetLocationDTO;
import com.spring.app.mypage.model.MyPageDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPageService_imple implements MyPageService {

    private final MyPageDAO myPageDAO;

    @Override
    public List<NotificationDTO> getNotifications(String email) {
        return myPageDAO.getNotifications(email);
    }

    @Override
    public int getUnreadCount(String email) {
        return myPageDAO.getUnreadCount(email);
    }

    @Override
    public List<NotificationDTO> getUnreadPreview(String email) {
        return myPageDAO.getUnreadPreview(email);
    }

    // 찜 목록
    @Override
    public List<ProductDTO> getWishlist(String email) {
        return myPageDAO.getWishlist(email);
    }

    // 내 판매상품
    @Override
    public List<ProductDTO> getMyProducts(String email) {
        return myPageDAO.getMyProducts(email);
    }

    @Override
    public ProductDTO getMyProductByNo(Map<String, Object> params) {
        return myPageDAO.getMyProductByNo(params);
    }

    @Override
    public int updateMyProduct(Map<String, Object> params) {
        return myPageDAO.updateMyProduct(params);
    }

    @Override
    public int getProductTransactionCount(int productNo) {
        return myPageDAO.getProductTransactionCount(productNo);
    }

    @Override
    public int getProductReportCount(int productNo) {
        return myPageDAO.getProductReportCount(productNo);
    }

    @Override
    public int deleteMyProduct(Map<String, Object> params) {
        return myPageDAO.deleteMyProduct(params);
    }

    @Override
    public int saveInvoice(Map<String, Object> params) {
        return myPageDAO.saveInvoice(params);
    }

    @Override
    public ProductDTO getInvoice(Map<String, Object> params) {
        return myPageDAO.getInvoice(params);
    }

    @Override public ProductImageDTO getProductImageByNo(int prdImgNo) { return myPageDAO.getProductImageByNo(prdImgNo); }
    @Override public int deleteProductImageByNo(int prdImgNo) { return myPageDAO.deleteProductImageByNo(prdImgNo); }
    @Override public int insertProductImageEdit(ProductImageDTO img) { return myPageDAO.insertProductImageEdit(img); }
    @Override public int resetMainImages(int productNo) { return myPageDAO.resetMainImages(productNo); }
    @Override public int setFirstImageAsMain(int productNo) { return myPageDAO.setFirstImageAsMain(productNo); }
    @Override public int deleteProductShippingOptions(int productNo) { return myPageDAO.deleteProductShippingOptions(productNo); }
    @Override public int insertShippingOptionEdit(ProductShippingOptionDTO opt) { return myPageDAO.insertShippingOptionEdit(opt); }
    @Override public int deleteProductMeetLocations(int productNo) { return myPageDAO.deleteProductMeetLocations(productNo); }
    @Override public int insertMeetLocationEdit(ProductMeetLocationDTO loc) { return myPageDAO.insertMeetLocationEdit(loc); }

    // 내 구매상품
    @Override
    public List<MyPurchaseDTO> getMyPurchases(String email) {
        return myPageDAO.getMyPurchases(email);
    }

    @Override
    public int insertReview(Map<String, Object> params) {
        return myPageDAO.insertReview(params);
    }

    // 계좌
    @Override
    public AccountDTO getPrimaryAccount(String email) {
        AccountDTO account = myPageDAO.getPrimaryAccount(email);
        if (account != null) account.setAccountNum(AccountEncryptUtil.decrypt(account.getAccountNum()));
        return account;
    }

    @Override
    public List<AccountDTO> getAccountList(String email) {
        List<AccountDTO> list = myPageDAO.getAccountList(email);
        if (list != null) list.forEach(a -> a.setAccountNum(AccountEncryptUtil.decrypt(a.getAccountNum())));
        return list;
    }

    @Override
    public int getAccountCount(String email) {
        return myPageDAO.getAccountCount(email);
    }

    @Override
    public int insertAccount(AccountDTO account) {
        account.setAccountNum(AccountEncryptUtil.encrypt(account.getAccountNum()));
        return myPageDAO.insertAccount(account);
    }

    @Override
    public int updateAccount(AccountDTO account) {
        account.setAccountNum(AccountEncryptUtil.encrypt(account.getAccountNum()));
        return myPageDAO.updateAccount(account);
    }

    @Override
    public int deleteAccount(Map<String, Object> params) {
        return myPageDAO.deleteAccount(params);
    }

    @Override
    public int setPrimaryAccount(Map<String, Object> params) {
        return myPageDAO.setPrimaryAccount(params);
    }

    // 배송지
    @Override
    public List<DeliveryAddressDTO> getDeliveryList(String email) {
        return myPageDAO.getDeliveryList(email);
    }

    @Override
    public int getDeliveryCount(String email) {
        return myPageDAO.getDeliveryCount(email);
    }

    @Override
    public int insertDelivery(DeliveryAddressDTO delivery) {
        return myPageDAO.insertDelivery(delivery);
    }

    @Override
    public int updateDelivery(DeliveryAddressDTO delivery) {
        return myPageDAO.updateDelivery(delivery);
    }

    @Override
    public int deleteDelivery(Map<String, Object> params) {
        return myPageDAO.deleteDelivery(params);
    }

    @Override
    public int setPrimaryDelivery(Map<String, Object> params) {
        return myPageDAO.setPrimaryDelivery(params);
    }

    // 내 통계
    @Override
    public int getMySafePayCount(String email) {
        return myPageDAO.getMySafePayCount(email);
    }

    @Override
    public int getMyTradeCount(String email) {
        return myPageDAO.getMyTradeCount(email);
    }

    // 신고관리
    @Override
    public List<MyReportDTO> getMyReportsSent(String email) {
        return myPageDAO.getMyReportsSent(email);
    }

    @Override
    public List<MyReportDTO> getMyReportsReceived(String email) {
        return myPageDAO.getMyReportsReceived(email);
    }
}

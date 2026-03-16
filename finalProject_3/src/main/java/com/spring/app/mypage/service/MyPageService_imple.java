package com.spring.app.mypage.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.spring.app.mypage.domain.AccountDTO;
import com.spring.app.mypage.domain.DeliveryAddressDTO;
import com.spring.app.mypage.domain.MyPurchaseDTO;
import com.spring.app.mypage.domain.MyReportDTO;
import com.spring.app.mypage.domain.NotificationDTO;
import com.spring.app.product.domain.ProductDTO;
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
    public int updateMyProduct(Map<String, Object> params) {
        return myPageDAO.updateMyProduct(params);
    }

    @Override
    public int getProductTransactionCount(int productNo) {
        return myPageDAO.getProductTransactionCount(productNo);
    }

    @Override
    public int deleteMyProduct(Map<String, Object> params) {
        return myPageDAO.deleteMyProduct(params);
    }

    // 내 구매상품
    @Override
    public List<MyPurchaseDTO> getMyPurchases(String email) {
        return myPageDAO.getMyPurchases(email);
    }

    // 계좌
    @Override
    public List<AccountDTO> getAccountList(String email) {
        return myPageDAO.getAccountList(email);
    }

    @Override
    public int getAccountCount(String email) {
        return myPageDAO.getAccountCount(email);
    }

    @Override
    public int insertAccount(AccountDTO account) {
        return myPageDAO.insertAccount(account);
    }

    @Override
    public int updateAccount(AccountDTO account) {
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

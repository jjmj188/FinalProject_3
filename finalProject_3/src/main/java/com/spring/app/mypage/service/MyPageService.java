package com.spring.app.mypage.service;

import java.util.List;
import java.util.Map;

import com.spring.app.mypage.domain.AccountDTO;
import com.spring.app.mypage.domain.DeliveryAddressDTO;
import com.spring.app.mypage.domain.NotificationDTO;
import com.spring.app.product.domain.ProductDTO;

public interface MyPageService {
    List<NotificationDTO> getNotifications(String email);
    int getUnreadCount(String email);
    List<NotificationDTO> getUnreadPreview(String email);

    // 찜 목록
    List<ProductDTO> getWishlist(String email);

    // 내 판매상품
    List<ProductDTO> getMyProducts(String email);

    // 계좌
    List<AccountDTO> getAccountList(String email);
    int getAccountCount(String email);
    int insertAccount(AccountDTO account);
    int updateAccount(AccountDTO account);
    int deleteAccount(Map<String, Object> params);
    int setPrimaryAccount(Map<String, Object> params);

    // 배송지
    List<DeliveryAddressDTO> getDeliveryList(String email);
    int getDeliveryCount(String email);
    int insertDelivery(DeliveryAddressDTO delivery);
    int updateDelivery(DeliveryAddressDTO delivery);
    int deleteDelivery(Map<String, Object> params);
    int setPrimaryDelivery(Map<String, Object> params);
}

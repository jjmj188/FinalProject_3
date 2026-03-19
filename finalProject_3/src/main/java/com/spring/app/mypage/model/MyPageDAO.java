package com.spring.app.mypage.model;

import java.util.List;
import java.util.Map;

import com.spring.app.mypage.domain.AccountDTO;
import com.spring.app.mypage.domain.DeliveryAddressDTO;
import com.spring.app.mypage.domain.MyPurchaseDTO;
import com.spring.app.mypage.domain.MyReportDTO;
import com.spring.app.mypage.domain.NotificationDTO;
import com.spring.app.product.domain.ProductDTO;
import com.spring.app.product.domain.ProductImageDTO;
import com.spring.app.product.domain.ProductShippingOptionDTO;
import com.spring.app.product.domain.ProductMeetLocationDTO;

public interface MyPageDAO {
    List<NotificationDTO> getNotifications(String email);
    int getUnreadCount(String email);
    List<NotificationDTO> getUnreadPreview(String email);

    // 찜 목록
    List<ProductDTO> getWishlist(String email);

    // 내 판매상품
    List<ProductDTO> getMyProducts(String email);
    ProductDTO getMyProductByNo(Map<String, Object> params);
    int updateMyProduct(Map<String, Object> params);
    int getProductTransactionCount(int productNo);
    int getProductReportCount(int productNo);
    int deleteMyProduct(Map<String, Object> params);

    // 송장번호
    int saveInvoice(Map<String, Object> params);
    ProductDTO getInvoice(Map<String, Object> params);

    // 수정 페이지용 이미지/배송/위치
    ProductImageDTO getProductImageByNo(int prdImgNo);
    int deleteProductImageByNo(int prdImgNo);
    int insertProductImageEdit(ProductImageDTO img);
    int resetMainImages(int productNo);
    int setFirstImageAsMain(int productNo);
    int deleteProductShippingOptions(int productNo);
    int insertShippingOptionEdit(ProductShippingOptionDTO opt);
    int deleteProductMeetLocations(int productNo);
    int insertMeetLocationEdit(ProductMeetLocationDTO loc);

    // 내 구매상품
    List<MyPurchaseDTO> getMyPurchases(String email);
    int insertReview(Map<String, Object> params);

    // 계좌
    AccountDTO getPrimaryAccount(String email);
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

    // 내 통계
    int getMySafePayCount(String email);
    int getMyTradeCount(String email);

    // 신고관리
    List<MyReportDTO> getMyReportsSent(String email);
    List<MyReportDTO> getMyReportsReceived(String email);
}

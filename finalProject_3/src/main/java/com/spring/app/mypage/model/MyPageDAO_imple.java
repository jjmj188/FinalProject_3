package com.spring.app.mypage.model;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.spring.app.mypage.domain.AccountDTO;
import com.spring.app.mypage.domain.DeliveryAddressDTO;
import com.spring.app.mypage.domain.MyPurchaseDTO;
import com.spring.app.mypage.domain.MyReportDTO;
import com.spring.app.mypage.domain.NotificationDTO;
import com.spring.app.product.domain.ProductDTO;
import com.spring.app.product.domain.ProductImageDTO;
import com.spring.app.product.domain.ProductShippingOptionDTO;
import com.spring.app.product.domain.ProductMeetLocationDTO;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MyPageDAO_imple implements MyPageDAO {

    @Qualifier("sqlsession")
    private final SqlSessionTemplate sqlsession;

    private static final String ns = "mypage";

    @Override
    public List<NotificationDTO> getNotifications(String email) {
        return sqlsession.selectList(ns + ".getNotifications", email);
    }

    @Override
    public int getUnreadCount(String email) {
        return sqlsession.selectOne(ns + ".getUnreadCount", email);
    }

    @Override
    public List<NotificationDTO> getUnreadPreview(String email) {
        return sqlsession.selectList(ns + ".getUnreadPreview", email);
    }

    // 찜 목록
    @Override
    public List<ProductDTO> getWishlist(String email) {
        return sqlsession.selectList(ns + ".getWishlist", email);
    }

    // 내 판매상품
    @Override
    public List<ProductDTO> getMyProducts(String email) {
        return sqlsession.selectList(ns + ".getMyProducts", email);
    }

    @Override
    public ProductDTO getMyProductByNo(Map<String, Object> params) {
        return sqlsession.selectOne(ns + ".getMyProductByNo", params);
    }

    @Override
    public int saveInvoice(Map<String, Object> params) {
        return sqlsession.update(ns + ".saveInvoice", params);
    }

    @Override
    public ProductDTO getInvoice(Map<String, Object> params) {
        return sqlsession.selectOne(ns + ".getInvoice", params);
    }

    @Override
    public int updateMyProduct(Map<String, Object> params) {
        return sqlsession.update(ns + ".updateMyProduct", params);
    }

    @Override
    public int getProductTransactionCount(int productNo) {
        return sqlsession.selectOne(ns + ".getProductTransactionCount", productNo);
    }

    @Override
    public int getProductReportCount(int productNo) {
        return sqlsession.selectOne(ns + ".getProductReportCount", productNo);
    }

    @Override
    public int deleteMyProduct(Map<String, Object> params) {
        return sqlsession.delete(ns + ".deleteMyProduct", params);
    }

    @Override
    public ProductImageDTO getProductImageByNo(int prdImgNo) {
        return sqlsession.selectOne(ns + ".getProductImageByNo", prdImgNo);
    }

    @Override
    public int deleteProductImageByNo(int prdImgNo) {
        return sqlsession.delete(ns + ".deleteProductImageByNo", prdImgNo);
    }

    @Override
    public int insertProductImageEdit(ProductImageDTO img) {
        return sqlsession.insert(ns + ".insertProductImageEdit", img);
    }

    @Override
    public int resetMainImages(int productNo) {
        return sqlsession.update(ns + ".resetMainImages", productNo);
    }

    @Override
    public int setFirstImageAsMain(int productNo) {
        return sqlsession.update(ns + ".setFirstImageAsMain", productNo);
    }

    @Override
    public int deleteProductShippingOptions(int productNo) {
        return sqlsession.delete(ns + ".deleteProductShippingOptions", productNo);
    }

    @Override
    public int insertShippingOptionEdit(ProductShippingOptionDTO opt) {
        return sqlsession.insert(ns + ".insertShippingOptionEdit", opt);
    }

    @Override
    public int deleteProductMeetLocations(int productNo) {
        return sqlsession.delete(ns + ".deleteProductMeetLocations", productNo);
    }

    @Override
    public int insertMeetLocationEdit(ProductMeetLocationDTO loc) {
        return sqlsession.insert(ns + ".insertMeetLocationEdit", loc);
    }

    // 내 구매상품
    @Override
    public List<MyPurchaseDTO> getMyPurchases(String email) {
        return sqlsession.selectList(ns + ".getMyPurchases", email);
    }

    @Override
    public int insertReview(Map<String, Object> params) {
        return sqlsession.insert(ns + ".insertReview", params);
    }

    // 계좌
    @Override
    public AccountDTO getPrimaryAccount(String email) {
        return sqlsession.selectOne(ns + ".getPrimaryAccount", email);
    }

    @Override
    public List<AccountDTO> getAccountList(String email) {
        return sqlsession.selectList(ns + ".getAccountList", email);
    }

    @Override
    public int getAccountCount(String email) {
        return sqlsession.selectOne(ns + ".getAccountCount", email);
    }

    @Override
    public int insertAccount(AccountDTO account) {
        return sqlsession.insert(ns + ".insertAccount", account);
    }

    @Override
    public int updateAccount(AccountDTO account) {
        return sqlsession.update(ns + ".updateAccount", account);
    }

    @Override
    public int deleteAccount(Map<String, Object> params) {
        return sqlsession.delete(ns + ".deleteAccount", params);
    }

    @Override
    public int setPrimaryAccount(Map<String, Object> params) {
        return sqlsession.update(ns + ".setPrimaryAccount", params);
    }

    // 배송지
    @Override
    public List<DeliveryAddressDTO> getDeliveryList(String email) {
        return sqlsession.selectList(ns + ".getDeliveryList", email);
    }

    @Override
    public int getDeliveryCount(String email) {
        return sqlsession.selectOne(ns + ".getDeliveryCount", email);
    }

    @Override
    public int insertDelivery(DeliveryAddressDTO delivery) {
        return sqlsession.insert(ns + ".insertDelivery", delivery);
    }

    @Override
    public int updateDelivery(DeliveryAddressDTO delivery) {
        return sqlsession.update(ns + ".updateDelivery", delivery);
    }

    @Override
    public int deleteDelivery(Map<String, Object> params) {
        return sqlsession.delete(ns + ".deleteDelivery", params);
    }

    @Override
    public int setPrimaryDelivery(Map<String, Object> params) {
        return sqlsession.update(ns + ".setPrimaryDelivery", params);
    }

    // 내 통계
    @Override
    public int getMySafePayCount(String email) {
        return sqlsession.selectOne(ns + ".getMySafePayCount", email);
    }

    @Override
    public int getMyTradeCount(String email) {
        return sqlsession.selectOne(ns + ".getMyTradeCount", email);
    }

    // 신고관리
    @Override
    public List<MyReportDTO> getMyReportsSent(String email) {
        return sqlsession.selectList(ns + ".getMyReportsSent", email);
    }

    @Override
    public List<MyReportDTO> getMyReportsReceived(String email) {
        return sqlsession.selectList(ns + ".getMyReportsReceived", email);
    }
}

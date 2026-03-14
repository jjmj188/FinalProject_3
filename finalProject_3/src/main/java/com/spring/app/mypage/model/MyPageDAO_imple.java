package com.spring.app.mypage.model;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.spring.app.mypage.domain.AccountDTO;
import com.spring.app.mypage.domain.DeliveryAddressDTO;
import com.spring.app.mypage.domain.NotificationDTO;
import com.spring.app.product.domain.ProductDTO;

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

    // 계좌
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
}

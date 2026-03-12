package com.spring.app.mypage.model;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.spring.app.mypage.domain.NotificationDTO;

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
}

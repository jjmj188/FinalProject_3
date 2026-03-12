package com.spring.app.mypage.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.spring.app.mypage.domain.NotificationDTO;
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
}

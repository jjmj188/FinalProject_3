package com.spring.app.mypage.service;

import java.util.List;

import com.spring.app.mypage.domain.NotificationDTO;

public interface MyPageService {
    List<NotificationDTO> getNotifications(String email);
    int getUnreadCount(String email);
    List<NotificationDTO> getUnreadPreview(String email);
}

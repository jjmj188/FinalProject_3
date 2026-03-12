package com.spring.app.mypage.model;

import java.util.List;

import com.spring.app.mypage.domain.NotificationDTO;

public interface MyPageDAO {
    List<NotificationDTO> getNotifications(String email);
    int getUnreadCount(String email);
    List<NotificationDTO> getUnreadPreview(String email);
}

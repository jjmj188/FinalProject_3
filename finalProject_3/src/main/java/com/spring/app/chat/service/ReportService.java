package com.spring.app.chat.service;

import org.springframework.web.multipart.MultipartFile;

import com.spring.app.chat.domain.ReportDTO;

public interface ReportService {
    void submitChatReport(ReportDTO reportDto, String myEmail, MultipartFile image) throws Exception;
}

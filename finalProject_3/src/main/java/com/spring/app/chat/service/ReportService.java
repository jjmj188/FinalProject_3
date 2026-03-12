package com.spring.app.chat.service;

import com.spring.app.chat.domain.ReportDTO;

public interface ReportService {
    void submitChatReport(ReportDTO reportDto, String myEmail) throws Exception;
}
package com.spring.app.common;

import com.spring.app.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final AdminService adminService;

    @ModelAttribute("promoBanner")
    public String promoBanner() {
        try {
            return adminService.getBanner();
        } catch (Exception e) {
            return "카페 1900만 고객이 판매하는 하루 10만개의 상품을 편리하게 검색해보세요";
        }
    }
}

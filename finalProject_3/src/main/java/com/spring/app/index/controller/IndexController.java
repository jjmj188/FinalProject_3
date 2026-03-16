package com.spring.app.index.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.spring.app.admin.service.AdminService;
import com.spring.app.index.service.IndexService;
import com.spring.app.product.domain.ProductDTO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final IndexService service;
    private final AdminService adminService;

    @GetMapping("/")
    public String index(Model model, Authentication authentication) {

        boolean isLogin = authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName());

        String loginUserEmail = isLogin ? authentication.getName() : null;

        List<ProductDTO> latestList = service.getMainLatestList(loginUserEmail);
        List<ProductDTO> recommendList = service.getMainRecommendList(loginUserEmail);
        List<ProductDTO> freeList = service.getMainFreeList(loginUserEmail);

        model.addAttribute("latestList", latestList);
        model.addAttribute("recommendList", recommendList);
        model.addAttribute("freeList", freeList);
        model.addAttribute("isLogin", isLogin);
        model.addAttribute("activeAds", adminService.getActiveAds());

        return "index";
    }
}
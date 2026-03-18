package com.spring.app.ai.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.spring.app.ai.dto.AiSellTextRequest;
import com.spring.app.ai.dto.AiSellTextResponse;
import com.spring.app.ai.service.GeminiAiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ai/sell")
@RequiredArgsConstructor
public class AiSellController {

    private final GeminiAiService geminiAiService;

    @PostMapping("/description")
    public AiSellTextResponse generateDescription(@RequestBody AiSellTextRequest request) throws Exception {
        return geminiAiService.generateSellDescription(request);
    }

    @ExceptionHandler(Exception.class)
    public Map<String, Object> handle(Exception e) {
        return Map.of(
                "success", false,
                "message", e.getMessage()
        );
    }
}
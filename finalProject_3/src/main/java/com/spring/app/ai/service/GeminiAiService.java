package com.spring.app.ai.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spring.app.ai.dto.AiSellTextRequest;
import com.spring.app.ai.dto.AiSellTextResponse;

@Service
public class GeminiAiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.text-model}")
    private String textModel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AiSellTextResponse generateSellDescription(AiSellTextRequest req) throws Exception {

        validateConfig();
        validateRequest(req);

        String prompt = buildPrompt(req);

        ObjectNode requestBody = objectMapper.createObjectNode();

        ArrayNode contents = requestBody.putArray("contents");
        ObjectNode content = contents.addObject();
        content.put("role", "user");
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        ObjectNode generationConfig = requestBody.putObject("generationConfig");
        generationConfig.put("responseMimeType", "application/json");

        ObjectNode responseSchema = generationConfig.putObject("responseSchema");
        responseSchema.put("type", "OBJECT");

        ObjectNode properties = responseSchema.putObject("properties");

        ObjectNode titleSuggestionProp = properties.putObject("titleSuggestion");
        titleSuggestionProp.put("type", "STRING");

        ObjectNode descriptionProp = properties.putObject("description");
        descriptionProp.put("type", "STRING");

        ObjectNode cautionsProp = properties.putObject("cautions");
        cautionsProp.put("type", "ARRAY");
        cautionsProp.putObject("items").put("type", "STRING");

        ArrayNode required = responseSchema.putArray("required");
        required.add("titleSuggestion");
        required.add("description");
        required.add("cautions");

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + textModel + ":generateContent?key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        String rawBody = response.body();
        System.out.println("===== Gemini raw response =====");
        System.out.println(rawBody);

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Gemini API 호출 실패 (" + response.statusCode() + "): " + rawBody);
        }

        JsonNode root = objectMapper.readTree(rawBody);

        JsonNode textNode = root.path("candidates")
                                .path(0)
                                .path("content")
                                .path("parts")
                                .path(0)
                                .path("text");

        if (textNode.isMissingNode() || textNode.isNull()) {
            throw new RuntimeException("Gemini 응답에서 text를 찾을 수 없습니다.");
        }

        String aiText = stripCodeFence(textNode.asText()).trim();

        if (aiText.isBlank()) {
            throw new RuntimeException("Gemini 응답 text가 비어 있습니다.");
        }

        System.out.println("===== Gemini parsed text =====");
        System.out.println(aiText);

        AiSellTextResponse result = parseAiResponse(aiText);

        if (result.getCautions() == null) {
            result.setCautions(new ArrayList<>());
        }

        String titleSuggestion = safeTrim(result.getTitleSuggestion());
        String description = safeTrim(result.getDescription());

        if (titleSuggestion.isBlank() && description.isBlank()) {
            throw new RuntimeException("AI 응답 파싱은 성공했지만 titleSuggestion 과 description 이 모두 비어 있습니다. 원문: " + aiText);
        }

        result.setTitleSuggestion(titleSuggestion);
        result.setDescription(description);

        System.out.println("===== Final AiSellTextResponse =====");
        System.out.println(result);

        return result;
    }

    private void validateConfig() {
        if (safeTrim(apiKey).isBlank()) {
            throw new RuntimeException("Gemini API 키가 비어 있습니다.");
        }

        if (safeTrim(textModel).isBlank()) {
            throw new RuntimeException("gemini.text-model 값이 비어 있습니다.");
        }
    }

    private void validateRequest(AiSellTextRequest req) {
        if (req == null) {
            throw new RuntimeException("요청 값이 비어 있습니다.");
        }

        if (safeTrim(req.getProductName()).isBlank()) {
            throw new RuntimeException("상품명이 비어 있습니다.");
        }

        if (safeTrim(req.getProductPrice()).isBlank()) {
            throw new RuntimeException("판매가격이 비어 있습니다.");
        }
    }

    private String buildPrompt(AiSellTextRequest req) {
        String productName = safeTrim(req.getProductName());
        String categoryName = safeTrim(req.getCategoryName());
        String productPrice = safeTrim(req.getProductPrice());
        String productDesc = safeTrim(req.getProductDesc());

        return """
                너는 중고거래 판매글 작성 도우미다.
                과장하지 말고, 확인되지 않은 사실은 쓰지 마라.
                입력값을 바탕으로 한국어 판매글을 작성해라.

                [입력]
                상품명: %s
                카테고리: %s
                가격: %s
                기존설명: %s

                [규칙]
                1. 중고거래용 자연스러운 문장으로 작성
                2. 확인되지 않은 성능, 정품, 새상품 표현 금지
                3. 사용자가 이미 쓴 내용을 정리하고 가독성 좋게 재작성
                4. 제목 추천도 함께 작성
                5. 반드시 JSON만 반환
                6. 거래방법(택배, 직거래, 택배거래, 직거래 가능 등)은 절대 작성하지 마라
                7. 배송비, 무료배송, 착불 등 배송 관련 내용도 절대 작성하지 마라
                8. 거래 위치(홍대, 강남, 신촌 등) 절대 작성하지 마라
                9. 불필요한 장식문구, 이모지 금지
                10. 결과는 아래 JSON 형식만 허용한다

                {
                  "titleSuggestion": "추천 제목",
                  "description": "문장1\\n문장2\\n문장3",
                  "cautions": []
                }
                """.formatted(productName, categoryName, productPrice, productDesc);
    }

    private AiSellTextResponse parseAiResponse(String aiText) throws IOException {
        JsonNode json = objectMapper.readTree(aiText);

        AiSellTextResponse result = new AiSellTextResponse();

        String titleSuggestion = "";
        if (json.hasNonNull("titleSuggestion")) {
            titleSuggestion = json.get("titleSuggestion").asText("");
        }
        else if (json.hasNonNull("title")) {
            titleSuggestion = json.get("title").asText("");
        }

        String description = json.hasNonNull("description")
                ? json.get("description").asText("")
                : "";

        result.setTitleSuggestion(safeTrim(titleSuggestion));
        result.setDescription(safeTrim(description));

        ArrayList<String> cautions = new ArrayList<>();
        JsonNode cautionsNode = json.get("cautions");
        if (cautionsNode != null && cautionsNode.isArray()) {
            for (JsonNode node : cautionsNode) {
                String text = safeTrim(node.asText(""));
                if (!text.isBlank()) {
                    cautions.add(text);
                }
            }
        }

        result.setCautions(cautions);

        return result;
    }

    private String stripCodeFence(String text) {
        String v = safeTrim(text);

        if (v.startsWith("```json")) {
            v = v.substring(7).trim();
        }
        else if (v.startsWith("```")) {
            v = v.substring(3).trim();
        }

        if (v.endsWith("```")) {
            v = v.substring(0, v.length() - 3).trim();
        }

        return v;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
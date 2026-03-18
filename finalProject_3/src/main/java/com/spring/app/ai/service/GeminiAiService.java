package com.spring.app.ai.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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

    	String prompt =
    	        """
    	        너는 중고거래 판매글 작성 도우미다.
    	        과장하지 말고, 확인되지 않은 사실은 쓰지 마라.
    	        입력값을 바탕으로 한국어 판매글을 작성해라.

    	        [입력]
    	        상품명: %s
    	        카테고리: %s
    	        가격: %s
    	        기존설명: %s

    	        [출력 규칙]
    	        1. 판매글 본문은 반드시 한국어 6~8문장으로 작성한다.
    	        2. 각 문장은 최소 15자 이상으로 작성하여 너무 짧지 않게 한다.
    	        3. 상품 상태, 사용감, 특징, 구성, 사용 목적 등을 구체적으로 포함한다.
    	        4. 중고거래용 자연스러운 문장으로 작성한다.
    	        5. 확인되지 않은 성능, 정품, 미개봉, 새상품 표현은 금지한다.
    	        6. 사용자가 이미 쓴 내용을 정리해 가독성 좋게 재작성한다.
    	        7. 가격은 입력값 그대로 유지하고 본문에 자연스럽게 포함한다.
    	        8. 제목 추천은 1개만 작성한다.
    	        9. 거래방법(택배, 직거래, 반값택배 등)은 절대 작성하지 마라.
    	        10. 배송 관련 내용(배송비, 무료배송, 착불 등)은 절대 작성하지 마라.
    	        11. 거래 위치, 지역명은 절대 작성하지 마라.
    	        12. JSON만 반환한다.
    	        13. JSON 형식은 반드시 아래와 같이만 반환한다.

    	        {
    	          "title": "추천 제목",
    	          "description": "문장1\\n문장2\\n문장3\\n문장4\\n문장5\\n문장6"
    	        }
    	        """
    	        .formatted(
    	                nvl(req.getProductName()),
    	                nvl(req.getCategoryName()),
    	                nvl(req.getProductPrice()),
    	                nvl(req.getProductDesc())
    	        );

        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = schema.putObject("properties");
        properties.putObject("titleSuggestion").put("type", "string");
        properties.putObject("description").put("type", "string");

        ObjectNode cautions = properties.putObject("cautions");
        cautions.put("type", "array");
        cautions.putObject("items").put("type", "string");

        ArrayNode required = schema.putArray("required");
        required.add("titleSuggestion");
        required.add("description");
        required.add("cautions");

        ObjectNode body = objectMapper.createObjectNode();
        ArrayNode contents = body.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        ObjectNode generationConfig = body.putObject("generationConfig");
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.set("responseJsonSchema", schema);
        generationConfig.put("temperature", 0.5);

        JsonNode root = callGemini(textModel, body);
        String text = extractText(root);

        return objectMapper.readValue(text, AiSellTextResponse.class);
    }

    private JsonNode callGemini(String model, ObjectNode body) throws IOException, InterruptedException {

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Gemini 호출 실패: " + response.statusCode() + " / " + response.body());
        }

        return objectMapper.readTree(response.body());
    }

    private String extractText(JsonNode root) {
        JsonNode textNode = root.path("candidates")
                                .path(0)
                                .path("content")
                                .path("parts")
                                .path(0)
                                .path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new RuntimeException("Gemini 응답에서 text를 찾지 못했습니다.");
        }

        return textNode.asText();
    }

    private String nvl(String s) {
        return s == null ? "" : s.trim();
    }
}
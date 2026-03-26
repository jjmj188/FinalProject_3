package com.spring.app.location.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.app.location.dto.IpRegionResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/location")
@Slf4j
public class LocationController {

    private static final String DEFAULT_KEYWORD = "서울";
    private static final String DEFAULT_LABEL = "서울";

    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/ip-region")
    public ResponseEntity<IpRegionResponse> getIpRegion(
            HttpServletRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor) {

        try {
            String clientIp = extractClientIp(request, forwardedFor);

            log.info("clientIp = {}", clientIp);

            // 로컬/사설 IP여도 실패시키지 말고 기본 지역 반환
            if (clientIp == null || clientIp.isBlank() || isLocalOrPrivateIp(clientIp)) {
                log.warn("로컬 또는 사설 IP 감지 -> 기본 지역 반환");
                return ResponseEntity.ok(IpRegionResponse.ok(DEFAULT_KEYWORD, DEFAULT_LABEL));
            }

            String encodedIp = URLEncoder.encode(clientIp, StandardCharsets.UTF_8);
            String apiUrl = "https://ipapi.co/" + encodedIp + "/json/";

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            log.info("ipapi status = {}", response.statusCode());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("ipapi 응답 실패 -> 기본 지역 반환");
                return ResponseEntity.ok(IpRegionResponse.ok(DEFAULT_KEYWORD, DEFAULT_LABEL));
            }

            JsonNode root = objectMapper.readTree(response.body());

            String city = safeText(root, "city");
            String region = safeText(root, "region");
            String countryName = safeText(root, "country_name");
            String countryCode = safeText(root, "country_code");

            String keyword = normalizeKeyword(city, region, countryCode);
            String regionLabel = joinLabel(city, region, countryName);

            if (keyword.isBlank()) {
                log.warn("keyword 비어 있음 -> 기본 지역 반환");
                return ResponseEntity.ok(IpRegionResponse.ok(DEFAULT_KEYWORD, DEFAULT_LABEL));
            }

            if (regionLabel.isBlank()) {
                regionLabel = keyword;
            }

            return ResponseEntity.ok(IpRegionResponse.ok(keyword, regionLabel));
        }
        catch (IOException e) {
            log.error("IP 지역 조회 IOException", e);
            return ResponseEntity.ok(IpRegionResponse.ok(DEFAULT_KEYWORD, DEFAULT_LABEL));
        }
        catch (InterruptedException e) {
            log.error("IP 지역 조회 InterruptedException", e);
            Thread.currentThread().interrupt();
            return ResponseEntity.ok(IpRegionResponse.ok(DEFAULT_KEYWORD, DEFAULT_LABEL));
        }
        catch (Exception e) {
            log.error("IP 지역 조회 예외", e);
            return ResponseEntity.ok(IpRegionResponse.ok(DEFAULT_KEYWORD, DEFAULT_LABEL));
        }
    }

    private String extractClientIp(HttpServletRequest request, String forwardedFor) {
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] parts = forwardedFor.split(",");
            if (parts.length > 0 && !parts[0].isBlank()) {
                return parts[0].trim();
            }
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.isBlank() && !"unknown".equalsIgnoreCase(proxyClientIp)) {
            return proxyClientIp.trim();
        }

        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.isBlank() && !"unknown".equalsIgnoreCase(wlProxyClientIp)) {
            return wlProxyClientIp.trim();
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr.trim() : "";
    }

    private boolean isLocalOrPrivateIp(String ip) {
        return ip.startsWith("127.")
                || ip.equals("0:0:0:0:0:0:0:1")
                || ip.equals("::1")
                || ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || ip.startsWith("172.16.")
                || ip.startsWith("172.17.")
                || ip.startsWith("172.18.")
                || ip.startsWith("172.19.")
                || ip.startsWith("172.20.")
                || ip.startsWith("172.21.")
                || ip.startsWith("172.22.")
                || ip.startsWith("172.23.")
                || ip.startsWith("172.24.")
                || ip.startsWith("172.25.")
                || ip.startsWith("172.26.")
                || ip.startsWith("172.27.")
                || ip.startsWith("172.28.")
                || ip.startsWith("172.29.")
                || ip.startsWith("172.30.")
                || ip.startsWith("172.31.");
    }

    private String safeText(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull()) {
            return "";
        }
        return node.asText("").trim();
    }

    private String normalizeKeyword(String city, String region, String countryCode) {
        String raw = firstNonBlank(city, region);

        if (raw.isBlank()) {
            return "";
        }

        // 한국 주요 지역 영문 -> 한글 보정
        if ("KR".equalsIgnoreCase(countryCode)) {
            switch (raw.toLowerCase()) {
                case "seoul":
                    return "서울";
                case "busan":
                    return "부산";
                case "incheon":
                    return "인천";
                case "daegu":
                    return "대구";
                case "daejeon":
                    return "대전";
                case "gwangju":
                    return "광주";
                case "ulsan":
                    return "울산";
                case "sejong":
                    return "세종";
                case "gyeonggi-do":
                case "gyeonggi":
                    return "경기";
                case "gangwon-do":
                case "gangwon":
                    return "강원";
                case "chungcheongbuk-do":
                    return "충북";
                case "chungcheongnam-do":
                    return "충남";
                case "jeollabuk-do":
                    return "전북";
                case "jeollanam-do":
                    return "전남";
                case "gyeongsangbuk-do":
                    return "경북";
                case "gyeongsangnam-do":
                    return "경남";
                case "jeju-do":
                case "jeju":
                    return "제주";
                default:
                    return raw;
            }
        }

        return raw;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String joinLabel(String city, String region, String countryName) {
        StringBuilder sb = new StringBuilder();
        appendLabel(sb, city);
        appendLabel(sb, region);
        appendLabel(sb, countryName);
        return sb.toString();
    }

    private void appendLabel(StringBuilder sb, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        if (sb.length() > 0) {
            sb.append(" / ");
        }

        sb.append(value.trim());
    }
}
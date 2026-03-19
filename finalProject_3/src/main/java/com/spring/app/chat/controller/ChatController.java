package com.spring.app.chat.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import com.spring.app.chat.domain.ChatMessageDTO;
import com.spring.app.chat.domain.ChatRoomDTO;
import com.spring.app.chat.domain.ReportDTO;
import com.spring.app.chat.service.ChatService;
import com.spring.app.chat.service.FirebaseService;
import com.spring.app.chat.service.ReportService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping; 
import org.springframework.messaging.simp.SimpMessagingTemplate; 
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // ★ 추가됨
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // ★ 추가됨
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController // 데이터를 JSON으로 바로 반환하기 위해 @RestController 사용
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final FirebaseService firebaseService;
    
    // 특정 구독자들에게 웹소켓 메시지를 쏴주는 스프링 내장 객체
    private final SimpMessagingTemplate messagingTemplate; 

    // 1. 나의 채팅방 목록 가져오기 API 
    @PreAuthorize("isAuthenticated()") 
    @GetMapping("/roomList")
    public Map<String, Object> getMyChatRooms(Principal principal) {
        Map<String, Object> resultMap = new HashMap<>();
        
        try {
            String loginUserEmail = principal.getName();
            List<ChatRoomDTO> roomList = chatService.getMyChatRooms(loginUserEmail);
            
            resultMap.put("success", true);
            resultMap.put("roomList", roomList);
        } catch (Exception e) {
            resultMap.put("success", false);
            resultMap.put("message", "채팅방 목록을 불러오는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        
        return resultMap;
    }
    
    // 2. 특정 방의 이전 메시지 불러오기 API 
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/messages/{roomId}")
    public Map<String, Object> getMessages(@PathVariable("roomId") String roomId) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            List<ChatMessageDTO> messages = firebaseService.getMessages(roomId);
            resultMap.put("success", true);
            resultMap.put("messages", messages);
        } catch (Exception e) {
            resultMap.put("success", false);
            resultMap.put("message", "메시지를 불러오지 못했습니다.");
            e.printStackTrace();
        }
        return resultMap;
    }

    // 3. 실시간 웹소켓 메시지 수신 및 발송 API 
    // 프론트에서 stompClient.send("/app/chat/send", ...) 로 보낸 메시지가 여기로 들어옵니다.
    @MessageMapping("/chat/send")
    public void sendMessage(ChatMessageDTO message) {
        // 1) 서버의 정확한 시간으로 타임스탬프 세팅 (클라이언트 시간 조작 방지)
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        message.setTimestamp(now);

        // 2) 파이어베이스(NoSQL)에 메시지 영구 저장
        firebaseService.saveMessage(message);

        // 3) 웹소켓을 통해 해당 방("/topic/room/방ID")을 열고 있는 모든 사람에게 즉시 메시지 쏘기!
        messagingTemplate.convertAndSend("/topic/room/" + message.getRoomId(), message);

        // 4) 수신자의 채팅 뱃지 카운트 증가 알림 + DB 저장
        try {
            ChatRoomDTO room = chatService.getRoomById(message.getRoomId());
            if (room != null && message.getSender() != null) {
                String recipient = message.getSender().equals(room.getSellerEmail())
                        ? room.getBuyerEmail()
                        : room.getSellerEmail();
                if (recipient != null) {
                    // DB에 미읽음 카운트 저장
                    chatService.incrementUnread(message.getRoomId(), recipient, room.getSellerEmail());
                    // WebSocket으로 실시간 알림
                    messagingTemplate.convertAndSend("/topic/chat-unread/" + recipient, Map.of("count", 1));
                }
            }
        } catch (Exception ignored) {}
    }

    // 총 미읽음 채팅 카운트 조회 (페이지 로드 시 사용)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(Principal principal) {
        Map<String, Object> result = new HashMap<>();
        result.put("count", chatService.getTotalUnreadCount(principal.getName()));
        return result;
    }

    // 미읽음 카운트 초기화 (채팅 팝업 열 때 호출)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reset-all-unread")
    public Map<String, Object> resetAllUnread(Principal principal) {
        chatService.resetAllUnread(principal.getName());
        return Map.of("success", true);
    }

    // ★ 4. 새로운 채팅방 생성 (또는 기존 방 찾기) API 추가
 // ★ 4. 새로운 채팅방 생성 (또는 기존 방 찾기) API (수정됨)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/createRoom")
    public Map<String, Object> createRoom(@RequestBody Map<String, Object> payload, Principal principal) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 프론트에서 보낸 JSON 데이터를 안전하게 꺼내고 숫자로 변환합니다.
            int productNo = Integer.parseInt(payload.get("productNo").toString());
            String sellerEmail = payload.get("sellerEmail").toString();
            String buyerEmail = principal.getName(); // 현재 로그인한 내 이메일
            
            // ★ 디버깅용 로그: 서버에 진짜 무슨 값이 도착했는지 눈으로 확인!
            System.out.println("====== [채팅방 생성 요청 도착] ======");
            System.out.println("넘어온 상품번호: " + productNo);
            System.out.println("판매자: " + sellerEmail);
            System.out.println("구매자: " + buyerEmail);
            System.out.println("=====================================");

            // 내 상품에 내가 채팅을 걸 수는 없도록 방어 로직
            if (buyerEmail.equals(sellerEmail)) {
                resultMap.put("success", false);
                resultMap.put("message", "자신의 상품에는 채팅을 걸 수 없습니다.");
                return resultMap;
            }

            // 서비스 호출해서 방 번호 받아오기 (있으면 기존 방, 없으면 새 방)
            String roomId = chatService.getOrCreateRoom(productNo, sellerEmail, buyerEmail);

            resultMap.put("success", true);
            resultMap.put("roomId", roomId);

        } catch (Exception e) {
            resultMap.put("success", false);
            resultMap.put("message", "채팅방 생성 중 오류가 발생했습니다.");
            e.printStackTrace();
            return resultMap;
        }

        // 채팅창 배너에 필요한 상품 정보 조회 (실패해도 채팅방 생성에는 영향 없음)
        try {
            int productNo = Integer.parseInt(payload.get("productNo").toString());
            String sellerEmail = payload.get("sellerEmail").toString();
            ChatRoomDTO productInfo = chatService.getProductInfoForChat(productNo, sellerEmail);
            if (productInfo != null) {
                resultMap.put("nickname", productInfo.getNickname());
                resultMap.put("productImgUrl", productInfo.getProductImgUrl());
                resultMap.put("productPrice", productInfo.getProductPrice());
                resultMap.put("tradeStatus", productInfo.getTradeStatus());
                resultMap.put("tradeMethod", productInfo.getTradeMethod());
                resultMap.put("reservedRoomId", productInfo.getReservedRoomId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }
    
 // 5. 채팅방 나가기 API
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/leave/{roomId}")
    public Map<String, Object> leaveRoom(@PathVariable("roomId") String roomId, Principal principal) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 나가기 전에 예약된 방인지 확인 (예약 취소 알림용)
            boolean wasReserved = false;
            Integer productNo = chatService.getProductNoByRoomId(roomId);
            if (productNo != null) {
                wasReserved = roomId.equals(chatService.getReservedRoomId(productNo));
            }

            boolean isDeleted = chatService.leaveChatRoom(roomId);

            if (isDeleted) {
                // 예약된 방이었으면 판매자에게 예약 취소 알림 전송
                if (wasReserved) {
                    ChatMessageDTO cancelMsg = new ChatMessageDTO();
                    cancelMsg.setSender(principal.getName());
                    cancelMsg.setContent("__CANCEL_RESERVE__:");
                    cancelMsg.setRoomId(roomId);
                    cancelMsg.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    messagingTemplate.convertAndSend("/topic/room/" + roomId, cancelMsg);
                }
                resultMap.put("success", true);
                resultMap.put("message", "채팅방을 나갔습니다.");
            } else {
                resultMap.put("success", false);
                resultMap.put("message", "이미 존재하지 않는 방입니다.");
            }
        } catch (IllegalStateException e) {
            resultMap.put("success", false);
            resultMap.put("message", e.getMessage());
        } catch (Exception e) {
            resultMap.put("success", false);
            resultMap.put("message", "나가기 처리 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        return resultMap;
    }

    @RestController
    @RequestMapping("/api/chat")
    public class ChatReportController {

        @Autowired
        private ReportService service;

        @PostMapping(value = "/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public Map<String, Object> submitReport(
                @RequestParam("roomId") String roomId,
                @RequestParam("reportMainCategory") String reportMainCategory,
                @RequestParam("reportSubCategory") String reportSubCategory,
                @RequestParam(value = "reportContent", required = false, defaultValue = "") String reportContent,
                @RequestParam(value = "image", required = false) MultipartFile image) {
            Map<String, Object> resultMap = new HashMap<>();

            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String myEmail = auth.getName();

                ReportDTO reportDto = new ReportDTO();
                reportDto.setRoomId(roomId);
                reportDto.setReportMainCategory(reportMainCategory);
                reportDto.setReportSubCategory(reportSubCategory);
                reportDto.setReportContent(reportContent);

                service.submitChatReport(reportDto, myEmail, image);

                resultMap.put("success", true);
            } catch (Exception e) {
                e.printStackTrace();
                resultMap.put("success", false);
            }

            return resultMap;
        }
    }
    
 // 6. 예약 확정 API - roomId도 함께 저장하여 예약된 채팅방 추적
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reserve")
    public Map<String, Object> reserveProduct(@RequestBody Map<String, Object> payload, Principal principal) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            int productNo = Integer.parseInt(payload.get("productNo").toString());
            String roomId = payload.get("roomId").toString();

            // 예약 확정: TRADE_STATUS='예약중' + RESERVED_ROOM_ID=roomId
            chatService.confirmReserve(productNo, roomId);

            resultMap.put("success", true);
        } catch (Exception e) {
            resultMap.put("success", false);
            resultMap.put("message", "상태 변경 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        return resultMap;
    }

    // 7. 예약 취소 API - TRADE_STATUS='판매중', RESERVED_ROOM_ID=NULL
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cancelReserve")
    public Map<String, Object> cancelReserve(@RequestBody Map<String, Object> payload, Principal principal) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            int productNo = Integer.parseInt(payload.get("productNo").toString());
            String roomId = payload.get("roomId").toString();

            // 실제 예약된 방인지 검증 - 예약된 방이 아니면 취소 거부
            String reservedRoomId = chatService.getReservedRoomId(productNo);
            if (reservedRoomId != null && !reservedRoomId.equals(roomId)) {
                resultMap.put("success", false);
                resultMap.put("message", "예약이 확정된 채팅방에서만 취소할 수 있습니다.");
                return resultMap;
            }

            chatService.cancelReserve(productNo);

            // 채팅방에 예약취소 안내 메시지 전송
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            ChatMessageDTO cancelMsg = new ChatMessageDTO();
            cancelMsg.setSender(principal.getName());
            cancelMsg.setContent("__CANCEL_RESERVE__:");
            cancelMsg.setRoomId(roomId);
            cancelMsg.setTimestamp(now);

            firebaseService.saveMessage(cancelMsg);
            messagingTemplate.convertAndSend("/topic/room/" + roomId, cancelMsg);

            resultMap.put("success", true);
        } catch (Exception e) {
            resultMap.put("success", false);
            resultMap.put("message", "예약 취소 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        return resultMap;
    }
}
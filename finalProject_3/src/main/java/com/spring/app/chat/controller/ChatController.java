package com.spring.app.chat.controller;

import org.springframework.web.bind.annotation.RequestBody;
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
        
        /* * TODO: 4) 오라클 DB의 CHAT_ROOM 테이블에 '마지막 메시지' 업데이트 하기
         * (그래야 채팅방 목록을 열었을 때 방금 보낸 내용이 미리보기에 뜹니다.)
         * ex) chatService.updateLastMessage(message.getRoomId(), message.getContent(), now); 
         */
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
        }
        return resultMap;
    }
    
 // 5. 채팅방 나가기 API
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/leave/{roomId}")
    public Map<String, Object> leaveRoom(@PathVariable("roomId") String roomId) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            boolean isDeleted = chatService.leaveChatRoom(roomId);
            
            if (isDeleted) {
                resultMap.put("success", true);
                resultMap.put("message", "채팅방을 나갔습니다.");
            } else {
                resultMap.put("success", false);
                resultMap.put("message", "이미 존재하지 않는 방입니다.");
            }
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

        @PostMapping("/report")
        public Map<String, Object> submitReport(@RequestBody ReportDTO reportDto) {
            Map<String, Object> resultMap = new HashMap<>();

            try {
                // 현재 로그인한 사용자 이메일 꺼내기 (우리가 만든 하이브리드 JWT 방식!)
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String myEmail = auth.getName();

                // 서비스로 넘겨서 DB 저장 처리
                service.submitChatReport(reportDto, myEmail);

                resultMap.put("success", true);
            } catch (Exception e) {
                e.printStackTrace();
                resultMap.put("success", false);
            }

            return resultMap;
        }
    }
    
 // 6. 예약 확정 시 상품 상태 '예약중'으로 변경 API
    @PostMapping("/reserve")
    public Map<String, Object> reserveProduct(@RequestBody Map<String, Object> payload) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            int productNo = Integer.parseInt(payload.get("productNo").toString());
            
            // ★ 핵심: 이제 눈속임이 아니라 진짜 서비스(DB)를 호출해서 '예약중'으로 덮어씌웁니다!
            chatService.updateTradeStatus(productNo, "예약중"); 
            
            resultMap.put("success", true);
        } catch (Exception e) {
            resultMap.put("success", false);
            resultMap.put("message", "상태 변경 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        return resultMap;
    }
}
package com.spring.app.chat.service;

import com.spring.app.chat.domain.ChatRoomDTO;
import java.util.List;

public interface ChatService {
    
    // 나의 채팅방 목록 가져오기
    List<ChatRoomDTO> getMyChatRooms(String loginUserEmail);

	String getOrCreateRoom(int productNo, String sellerEmail, String buyerEmail);

    ChatRoomDTO getProductInfoForChat(int productNo, String sellerEmail);
    
	// 나가기 메서드 선언
	boolean leaveChatRoom(String roomId);

	void updateTradeStatus(int productNo, String string);

    // 예약 확정 (roomId와 함께 저장)
    void confirmReserve(int productNo, String roomId);

    // 예약 취소
    void cancelReserve(int productNo);

    // 거래완료 (직거래/나눔)
    boolean completeTrade(int productNo, String roomId, String buyerEmail);

    // 현재 예약된 채팅방 ID 조회
    String getReservedRoomId(int productNo);

    // roomId로 상품 번호 조회
    Integer getProductNoByRoomId(String roomId);

    // roomId로 채팅방 조회
    ChatRoomDTO getRoomById(String roomId);

    // 미읽음 카운트 증가 (수신자 이메일 기준으로 buyer/seller 판별)
    void incrementUnread(String roomId, String recipientEmail, String sellerEmail);

    // 미읽음 카운트 초기화
    void resetAllUnread(String email);

    // 총 미읽음 카운트 조회
    int getTotalUnreadCount(String email);
}

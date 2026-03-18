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

    // 현재 예약된 채팅방 ID 조회
    String getReservedRoomId(int productNo);

    // roomId로 채팅방 조회
    ChatRoomDTO getRoomById(String roomId);
}

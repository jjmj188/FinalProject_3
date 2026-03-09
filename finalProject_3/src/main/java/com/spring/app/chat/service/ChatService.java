package com.spring.app.chat.service;

import com.spring.app.chat.domain.ChatRoomDTO;
import java.util.List;

public interface ChatService {
    
    // 나의 채팅방 목록 가져오기
    List<ChatRoomDTO> getMyChatRooms(String loginUserEmail);

	String getOrCreateRoom(int productNo, String sellerEmail, String buyerEmail);
    
	// 나가기 메서드 선언
	boolean leaveChatRoom(String roomId);
}

package com.spring.app.chat.service;

import com.spring.app.chat.domain.ChatRoomDTO;
import com.spring.app.chat.mapper.ChatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    // 의존성 주입 (Lombok의 @RequiredArgsConstructor 덕분에 자동으로 주입됩니다)
    private final ChatMapper chatMapper;

    @Override
    public List<ChatRoomDTO> getMyChatRooms(String loginUserEmail) {
        // 매퍼를 통해 DB에서 데이터를 조회해서 바로 리턴합니다.
        return chatMapper.selectMyChatRooms(loginUserEmail);
    }
    
 // ChatServiceImpl.java
    @Override
    public String getOrCreateRoom(int productNo, String sellerEmail, String buyerEmail) {
        // 판매자 이메일까지 넣어서 정확히 조회
        String roomId = chatMapper.findRoomId(productNo, sellerEmail, buyerEmail);
        
        if (roomId == null) {
            roomId = UUID.randomUUID().toString();
            ChatRoomDTO newRoom = new ChatRoomDTO();
            newRoom.setRoomId(roomId);
            newRoom.setProductNo(productNo);
            newRoom.setSellerEmail(sellerEmail);
            newRoom.setBuyerEmail(buyerEmail);
            
            chatMapper.insertChatRoom(newRoom);
        }
        return roomId;
    }
    
    @Override
    public ChatRoomDTO getProductInfoForChat(int productNo, String sellerEmail) {
        return chatMapper.selectProductInfoForChat(productNo, sellerEmail);
    }

    @Override
    public boolean leaveChatRoom(String roomId) {
        if (chatMapper.countReportsByRoomId(roomId) > 0) {
            throw new IllegalStateException("신고가 접수된 채팅방은 나갈 수 없습니다.");
        }

        // 나가는 방이 예약 확정된 방이면 예약 취소 (판매중으로 복귀)
        Integer productNo = chatMapper.findProductNoByRoomId(roomId);
        if (productNo != null) {
            String reservedRoomId = chatMapper.getReservedRoomId(productNo);
            if (roomId.equals(reservedRoomId)) {
                Map<String, Object> map = new HashMap<>();
                map.put("productNo", productNo);
                chatMapper.cancelReserveStatus(map);
            }
        }

        int result = chatMapper.deleteChatRoom(roomId);
        return result > 0;
    }
    

	 // 매퍼(DAO)를 호출하여 상품 상태를 업데이트하는 메서드 추가
	 public void updateTradeStatus(int productNo, String status) {
	     Map<String, Object> map = new HashMap<>();
	     map.put("productNo", productNo);
	     map.put("status", status);
	     chatMapper.updateTradeStatus(map);
	 }

    @Override
    public void confirmReserve(int productNo, String roomId) {
        Map<String, Object> map = new HashMap<>();
        map.put("productNo", productNo);
        map.put("roomId", roomId);
        chatMapper.updateReserveStatus(map);
    }

    @Override
    public void cancelReserve(int productNo) {
        Map<String, Object> map = new HashMap<>();
        map.put("productNo", productNo);
        chatMapper.cancelReserveStatus(map);
    }

    @Override
    public String getReservedRoomId(int productNo) {
        return chatMapper.getReservedRoomId(productNo);
    }

    @Override
    public Integer getProductNoByRoomId(String roomId) {
        return chatMapper.findProductNoByRoomId(roomId);
    }

    @Override
    public ChatRoomDTO getRoomById(String roomId) {
        return chatMapper.selectRoomById(roomId);
    }

    @Override
    public void incrementUnread(String roomId, String recipientEmail, String sellerEmail) {
        if (recipientEmail.equals(sellerEmail)) {
            chatMapper.incrementSellerUnread(roomId);
        } else {
            chatMapper.incrementBuyerUnread(roomId);
        }
    }

    @Override
    public void resetAllUnread(String email) {
        chatMapper.resetAllUnread(email);
    }

    @Override
    public int getTotalUnreadCount(String email) {
        return chatMapper.getTotalUnreadCount(email);
    }
}
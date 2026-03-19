package com.spring.app.chat.mapper;

import com.spring.app.chat.domain.ChatRoomDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatMapper {
    
    // 나의 채팅방 목록 가져오기 (XML의 id와 메서드명이 일치해야 합니다)
    List<ChatRoomDTO> selectMyChatRooms(String loginUserEmail);
    
    ChatRoomDTO selectProductInfoForChat(@Param("productNo") int productNo,
                                         @Param("sellerEmail") String sellerEmail);

    String findRoomId(@Param("productNo") int productNo,
		              @Param("sellerEmail") String sellerEmail, 
		              @Param("buyerEmail") String buyerEmail);

    // 새로운 채팅방 생성 (INSERT)
    void insertChatRoom(ChatRoomDTO chatRoomDTO);
    
    // 방 번호로 채팅방을 삭제하는 메서드 추가
    int deleteChatRoom(@Param("roomId") String roomId);

    // 채팅방에 신고 이력이 있는지 확인
    int countReportsByRoomId(@Param("roomId") String roomId);

    // roomId로 상품 번호 조회
    Integer findProductNoByRoomId(@Param("roomId") String roomId);

	void updateTradeStatus(Map<String, Object> map);

    // 예약 확정: TRADE_STATUS='예약중' + RESERVED_ROOM_ID 저장
    void updateReserveStatus(Map<String, Object> map);

    // 예약 취소: TRADE_STATUS='판매중' + RESERVED_ROOM_ID=NULL
    void cancelReserveStatus(Map<String, Object> map);

    // 상품의 현재 예약된 채팅방 ID 조회
    String getReservedRoomId(@Param("productNo") int productNo);

    // roomId로 채팅방 조회 (발신자/수신자 파악용)
    ChatRoomDTO selectRoomById(@Param("roomId") String roomId);

    // 미읽음 카운트 증가
    void incrementSellerUnread(String roomId);
    void incrementBuyerUnread(String roomId);

    // 미읽음 카운트 초기화 (채팅 팝업 열 때)
    void resetAllUnread(@Param("email") String email);

    // 총 미읽음 카운트 조회 (페이지 로드 시)
    int getTotalUnreadCount(@Param("email") String email);
}
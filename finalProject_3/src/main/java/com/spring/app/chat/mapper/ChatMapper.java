package com.spring.app.chat.mapper;

import com.spring.app.chat.domain.ChatRoomDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMapper {
    
    // 나의 채팅방 목록 가져오기 (XML의 id와 메서드명이 일치해야 합니다)
    List<ChatRoomDTO> selectMyChatRooms(String loginUserEmail);
    
    String findRoomId(@Param("productNo") int productNo, 
		              @Param("sellerEmail") String sellerEmail, 
		              @Param("buyerEmail") String buyerEmail);

    // 새로운 채팅방 생성 (INSERT)
    void insertChatRoom(ChatRoomDTO chatRoomDTO);
    
    // 방 번호로 채팅방을 삭제하는 메서드 추가
    int deleteChatRoom(@Param("roomId") String roomId);
}
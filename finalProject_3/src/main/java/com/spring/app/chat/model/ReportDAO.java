package com.spring.app.chat.model;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.spring.app.chat.domain.ReportDTO;

@Mapper
public interface ReportDAO {
    // 1. 채팅방 정보로 상대방 이메일 찾기
    String findOtherUserByRoomId(@Param("roomId") String roomId, @Param("myEmail") String myEmail);
    
    // 2. 글자(소분류)로 TYPE_ID 찾기
    int findTypeIdByName(String typeName);
    
    // 3. 실제 REPORTS 테이블에 INSERT
    void insertReport(ReportDTO reportDto);

    // 4. 채팅방으로 상품 번호 조회
    Integer findProductNoByRoomId(String roomId);
}
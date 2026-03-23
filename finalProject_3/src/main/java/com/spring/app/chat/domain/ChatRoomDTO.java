package com.spring.app.chat.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChatRoomDTO {
    private String roomId;          // 채팅방 고유 ID
    private String nickname;
    private int productNo;          // 상품 번호
    private String sellerEmail;     // 판매자 이메일
    private String buyerEmail;      // 구매자 이메일
    private String reserveTime;     // 예약 시간
    private String reservePlace;    // 예약 장소
    private String lastMessage;     // 마지막 메시지 내용
    private String lastMessageAt;   // 마지막 메시지 시간
    private String muteYn;          // 알림 음소거 여부 (기본값 'N')
    private int productPrice;      // 상품 가격
    private String tradeStatus;
    private String reservedRoomId;  // 예약 확정된 채팅방 ID (PRODUCTS.RESERVED_ROOM_ID)

    // ====== 화면(UI) 출력을 위해 JOIN해서 가져올 추가 필드 ======
    private String otherUserEmail;  // 상대방 이메일 (내가 판매자면 구매자, 구매자면 판매자)
    private String productName;     // 상품명 (Product 테이블과 조인)
    private String productImgUrl;   // 상품 대표 이미지
    private String tradeMethod;     // 거래방식 (택배/직거래)
    private String saleType;        // 판매유형 (판매/나눔/경매)
    private String payStatus;       // 결제 상태 (DONE = 안전결제 완료, 구매확정 대기 중)
    private Integer transactionId;  // 결제 완료된 거래 ID (구매확정 시 사용)
}

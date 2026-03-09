package com.spring.app.chat.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChatMessageDTO {
    private String sender;    // 보낸 사람 이메일
    private String content;   // 메시지 내용
    private String roomId;    // 채팅방 ID
    private String timestamp; // 보낸 시간
}
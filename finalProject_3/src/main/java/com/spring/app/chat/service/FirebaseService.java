package com.spring.app.chat.service;

import com.google.firebase.database.*;
import com.spring.app.chat.domain.ChatMessageDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class FirebaseService {

    // 1. (기존) 특정 방의 이전 메시지 목록 가져오기
    public List<ChatMessageDTO> getMessages(String roomId) throws Exception {
        CompletableFuture<List<ChatMessageDTO>> future = new CompletableFuture<>();
        
        // 파이어베이스 데이터베이스의 "chat_rooms/방ID/messages" 경로를 가리킵니다.
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("chat_rooms/" + roomId + "/messages");

        // 단일 이벤트 리스너를 달아서 데이터를 한 번만 싹 읽어옵니다.
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<ChatMessageDTO> messageList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ChatMessageDTO msg = child.getValue(ChatMessageDTO.class);
                    messageList.add(msg);
                }
                future.complete(messageList); // 다 담았으면 완료 신호!
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException()); // 에러 나면 에러 신호!
            }
        });

        // 데이터가 다 담길 때까지 기다렸다가 리턴합니다.
        return future.get(); 
    }

    // ★ 2. (신규 추가!) 새로운 메시지를 파이어베이스에 저장하기
    public void saveMessage(ChatMessageDTO message) {
        // 저장할 경로: chat_rooms/방ID/messages
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("chat_rooms/" + message.getRoomId() + "/messages");
        
        // push()를 쓰면 고유 키가 자동 생성되며, 그 안에 메시지 데이터를 집어넣습니다.
        ref.push().setValueAsync(message);
    }
}
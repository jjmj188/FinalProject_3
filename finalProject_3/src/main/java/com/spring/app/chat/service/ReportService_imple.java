package com.spring.app.chat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.app.chat.domain.ReportDTO;
import com.spring.app.chat.model.ReportDAO;

// ★ Firebase 연동을 위한 필수 임포트
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

@Service
public class ReportService_imple implements ReportService {

    @Autowired
    private ReportDAO dao;

    @Override
    public void submitChatReport(ReportDTO reportDto, String myEmail) throws Exception {
        // 1. 신고자 이메일 셋팅
        reportDto.setReporterEmail(myEmail);

        // 2. 채팅방 번호를 통해 상대방(피신고자) 이메일 셋팅
        String targetEmail = dao.findOtherUserByRoomId(reportDto.getRoomId(), myEmail);
        reportDto.setTargetEmail(targetEmail);

        // 3. 한글 카테고리명으로 TYPE_ID(번호) 조회 후 셋팅
        int typeId = dao.findTypeIdByName(reportDto.getReportSubCategory());
        reportDto.setTypeId(typeId);

        /* =========================================================
           ★ [핵심] Firebase 스냅샷(복사본) 생성 및 백그라운드 저장 로직
           ========================================================= */
        String roomId = reportDto.getRoomId();
        
        // Firebase DB 객체 가져오기 (Firebase 초기화가 이미 되어있다고 가정)
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        
        // 원본 채팅방 경로와 새로운 스냅샷(금고) 경로 설정
        // ※ 원본 경로가 "messages"가 아니라면 회원님 프로젝트 환경에 맞게 수정해주세요!
        DatabaseReference originalRoomRef = database.getReference("chat_rooms").child(roomId).child("messages");
        
        // push()를 쓰면 Firebase가 겹치지 않는 고유한 스냅샷 폴더를 자동으로 만들어줍니다!
        DatabaseReference snapshotRef = database.getReference("report_snapshots").push(); 
        
        // 방금 막 생성된 고유 스냅샷 ID 가져오기 (예: -Nx8A1b2c3d4e5f)
        String snapshotId = snapshotRef.getKey(); 
        
        // 4. 이 고유 ID를 오라클 DB 제약조건 통과를 위해 DTO에 쏙 넣어줍니다.
        reportDto.setNosqlMsgKey(snapshotId);

        // 5. 비동기(백그라운드)로 채팅 내역 복사 실행
        // (사용자를 기다리게 하지 않고, 서버 뒤편에서 알아서 복사합니다)
        originalRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 원본 채팅 내역이 존재하면 통째로 스냅샷 경로에 복사(Save)
                    snapshotRef.setValueAsync(dataSnapshot.getValue());
                } else {
                    // 혹시 채팅 내역이 비어있을 경우를 대비한 방어 코드
                    snapshotRef.setValueAsync("No messages found at the time of report.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 복사 실패 시 서버 콘솔에만 에러를 남깁니다.
                System.err.println("Firebase 채팅 로그 스냅샷 복사 실패: " + databaseError.getMessage());
            }
        });

        // 6. 오라클 DB의 REPORTS 테이블에 최종 INSERT 
        // (채팅 복사가 끝나는 걸 안 기다리고 바로 실행되므로 엄청 빠릅니다!)
        dao.insertReport(reportDto);
    }
}
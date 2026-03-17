package com.spring.app.chat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.spring.app.chat.domain.ReportDTO;
import com.spring.app.chat.model.ReportDAO;
import com.spring.app.common.FileManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

@Service
public class ReportService_imple implements ReportService {

    @Autowired
    private ReportDAO dao;

    @Autowired
    private FileManager fileManager;

    @Value("${file.reports-dir}")
    private String reportsDir;

    @Override
    public void submitChatReport(ReportDTO reportDto, String myEmail, MultipartFile image) throws Exception {
        // 1. 신고자 이메일 셋팅
        reportDto.setReporterEmail(myEmail);

        // 2. 상대방 이메일 셋팅
        String targetEmail = dao.findOtherUserByRoomId(reportDto.getRoomId(), myEmail);
        reportDto.setTargetEmail(targetEmail);

        // 2-1. 채팅방에 연결된 상품 번호 저장
        Integer productNo = dao.findProductNoByRoomId(reportDto.getRoomId());
        reportDto.setProductNum(productNo);

        // 3. TYPE_ID 조회
        int typeId = dao.findTypeIdByName(reportDto.getReportSubCategory());
        reportDto.setTypeId(typeId);

        // 4. 첨부 이미지 저장
        if (image != null && !image.isEmpty()) {
            String fileName = fileManager.doFileUpload(image.getBytes(), image.getOriginalFilename(), reportsDir);
            reportDto.setReportImg(fileName);
        }

        // 5. Firebase 채팅 스냅샷 (비동기)
        String roomId = reportDto.getRoomId();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference originalRoomRef = database.getReference("chat_rooms").child(roomId).child("messages");
        DatabaseReference snapshotRef = database.getReference("report_snapshots").push();
        String snapshotId = snapshotRef.getKey();
        reportDto.setNosqlMsgKey(snapshotId);

        originalRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    snapshotRef.setValueAsync(dataSnapshot.getValue());
                } else {
                    snapshotRef.setValueAsync("No messages found at the time of report.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Firebase 채팅 로그 스냅샷 복사 실패: " + databaseError.getMessage());
            }
        });

        // 6. REPORTS 테이블 INSERT
        dao.insertReport(reportDto);
    }
}

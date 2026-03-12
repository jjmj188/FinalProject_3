package com.spring.app.security.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.spring.app.security.model.MemberDAO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MemberIdleScheduler {

    @Autowired
    private MemberDAO memberDAO;

    /**
     * 매일 새벽 2시: 마지막 로그인으로부터 1년 이상 지난 회원을 휴면 처리
     * 1. USER_DORMANT 테이블에 스냅샷 INSERT
     * 2. MEMBER 테이블의 IDLE = 1 로 업데이트
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void processIdleMembers() {
        log.info("[휴면 배치] 시작");
        memberDAO.moveMembersToUserDormant();
        memberDAO.setIdleForDormantMembers();
        log.info("[휴면 배치] 완료");
    }
}

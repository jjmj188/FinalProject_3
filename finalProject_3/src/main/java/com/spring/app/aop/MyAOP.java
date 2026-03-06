package com.spring.app.aop;

import java.util.List;
import java.util.Locale;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.spring.app.exception.BadWordException;
import com.spring.app.product.domain.ProductDTO;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class MyAOP {

    // ================================
    // ProductController 대상 지정
    // ================================
    @Pointcut("execution(public * com.spring.app.product.controller.ProductController.*(..))")
    public void productControllerMethods(){}

    
    // ================================
    // 금지어 목록
    // ================================
    private static final List<String> FORBIDDEN_WORDS = List.of(
    		
    		// 욕설 / 비속어
    	      "씨발","시발","ㅅㅂ","ㅆㅂ",
    	      "개새끼","개새","개자식",
    	      "병신","븅신","ㅂㅅ",
    	      "지랄","염병",
    	      "미친놈","미친년","미친새끼",
    	      "또라이","또라이새끼",
    	      "좆","존나","좆같","ㅈㄴ",
    	      "꺼져","닥쳐","죽어",
    	      "씹","씹새","씹새끼",
    	      "걸레","창녀","창년",
    		
    		  // 개인정보
    		  "주민등록번호","주민번호","민증","신분증","여권","운전면허증","면허증","등본","초본","인감","인감증명서","통장사본","유심","USIM","번호판매","회선판매","명의",

    		  // 금융/환전
    		  "통장판매","계좌판매","체크카드","신용카드","카드깡","현금화","대출","암호화폐","코인","비트코인","USDT","환전","환치기","외화","송금대행",

    		  // 해외/면세
    		  "해외직구","직구","구매대행","배송대행","면세","면세품","면세점","duty free","tax free",

    		  // 가품/침해
    		  "레플리카","레플","짝퉁","이미테이션","가품","정품아님","미러급","SA급","불법복제","복제본",

    		  // 동물
    		  "강아지분양","고양이분양","분양","교배",

    		  // 건강/약/의료
    		  "건강기능식품","건기식","홍삼","오메가3","유산균","비타민","의약품","처방약","전문의약품","항생제","수면제",
    		  "의료기기","혈당측정기","혈압계","산소포화도","보청기",

    		  // 담배/주류
    		  "담배","전자담배","액상","니코틴","주류","술","와인","위스키","맥주","소주","양주",

    		  // 샘플/증정
    		  "샘플","테스터","증정","사은품","비매품","Not for sale",

    		  // 티켓/상품권/게임
    		  "상품권","기프티콘","모바일쿠폰","구글기프트카드","애플기프트카드","티켓","입장권","양도",
    		  "게임머니","골드","다이아","계정판매","아이템판매",

    		  // 식품
    		  "식품","먹거리","반찬","수제","유통기한",

    		  // 위험/불법
    		  "도난","장물","분실물","습득물","몰카","도청","위치추적","스파이카메라",
    		  "리콜","회수","판매중지","유통금지",
    		  "불법","위법","밀수","위조","변조","마약","대마"
    		);


    // ================================
    // 상품 등록 / 수정 전에 검사
    // ================================
    @Before("productControllerMethods()")
    public void checkProductBadWord(JoinPoint joinPoint) {

        String methodName = joinPoint.getSignature().getName();
        log.info(">>> [AOP] 실행 메서드 : {}", methodName);

        Object[] args = joinPoint.getArgs();

        if(args == null || args.length == 0) {
            return;
        }

        for(Object arg : args) {

            if(arg == null) continue;

            // ProductDTO 파라미터만 검사
            if(arg instanceof ProductDTO dto) {

                log.info(">>> 상품명 길이 : {}", safeLen(dto.getProductName()));
                log.info(">>> 상품설명 길이 : {}", safeLen(dto.getProductDesc()));

                checkBadWord(dto.getProductName(), "상품명");
                checkBadWord(dto.getProductDesc(), "상품설명");
            }
        }
    }


    // ================================
    // 금지어 검사
    // ================================
    private void checkBadWord(String text, String fieldName) {

        if(text == null || text.isBlank()) {
            return;
        }

        String normalized = normalize(text);

        for(String bad : FORBIDDEN_WORDS) {

            if(normalized.contains(normalize(bad))) {

                log.warn("금지어 발견 -> {} : {}", fieldName, bad);

                throw new BadWordException(
                        fieldName + "에 금지어(\"" + bad + "\")가 포함되어 있습니다."
                );
            }
        }
    }


    // ================================
    // 문자열 정규화
    // ================================
    private String normalize(String str) {

        return str
                .replaceAll("\\s+", "")
                .toLowerCase(Locale.ROOT);
    }


    // ================================
    // 문자열 길이 안전 반환
    // ================================
    private int safeLen(String str) {

        if(str == null) return 0;

        return str.length();
    }

}
select * from MEMBER
/* =========================================================
   회원 (MEMBER)
   - PK: EMAIL
   - USER_NO: UNIQUE
   - 주소: POSTCODE / ADDRESS / DETAILADDRESS / EXTRAADDRESS
   - 탈퇴사유: STATUS = 0 일 때 사용
   - SUSPENDED: 1이면 관리자에 의해 일시정지된 계정
   ========================================================= */
CREATE TABLE MEMBER (
    EMAIL              VARCHAR2(100) NOT NULL,                -- 회원이메일(PK)
    USER_NO            NUMBER        NOT NULL,                -- 회원번호(UNIQUE)
    PASSWORD           VARCHAR2(255) NOT NULL,                -- 비밀번호(해시)

    USER_NAME          VARCHAR2(50)  NOT NULL,                -- 회원명
    NICKNAME           VARCHAR2(50)  NOT NULL,                -- 닉네임(UNIQUE)
    PHONE              VARCHAR2(20),                           -- 휴대폰번호
    GENDER             CHAR(1),                                -- 성별('M','F')
    BIRTH_DATE         VARCHAR2(8),                            -- 생년월일('YYYYMMDD')

    POSTCODE           VARCHAR2(5)   NOT NULL,                -- 우편번호(5자리)
    ADDRESS            VARCHAR2(200) NOT NULL,                -- 주소(기본주소)
    DETAILADDRESS      VARCHAR2(200) NOT NULL,                -- 상세주소
    EXTRAADDRESS       VARCHAR2(200),                          -- 참고항목(동/건물명 등)

    REG_DATE           DATE DEFAULT SYSDATE NOT NULL,         -- 가입일자
    LAST_PW_DATE       DATE,                                  -- 마지막암호변경일시
    LAST_LOGIN_DATE    DATE,                                  -- 마지막로그인일자

    STATUS             NUMBER(1) DEFAULT 1 NOT NULL,          -- 1:사용가능 / 0:탈퇴
    IDLE               NUMBER(1) DEFAULT 0 NOT NULL,          -- 0:활동중 / 1:휴면중
    SUSPENDED          NUMBER(1) DEFAULT 0 NOT NULL,          -- 0:정상 / 1:일시정지
    WITHDRAW_REASON    VARCHAR2(500),                         -- 탈퇴사유(STATUS=0일 때 기록)

    CASH_BALANCE       NUMBER DEFAULT 0 NOT NULL,             -- 보유캐시
    MANNER_TEMP        NUMBER DEFAULT 50 NOT NULL,            -- 매너온도(기본 36.5)

    PROFILE_IMG        VARCHAR2(500),                         -- 프로필이미지
    RECENT_CATEGORY    VARCHAR2(50),                          -- 최근거래 카테고리
    ROOM_ID            VARCHAR2(100),                         -- 채팅방키(NoSQL용)

    TOSS_CUSTOMER_KEY  VARCHAR2(200),                         -- 토스 고객 키
    TOSS_BILLING_KEY   VARCHAR2(200),                         -- 토스 빌링 키

    CONSTRAINT PK_MEMBER PRIMARY KEY (EMAIL),
    CONSTRAINT UQ_MEMBER_USER_NO UNIQUE (USER_NO),
    CONSTRAINT UQ_MEMBER_NICKNAME UNIQUE (NICKNAME),
    CONSTRAINT CK_MEMBER_STATUS CHECK (STATUS IN (0,1)),
    CONSTRAINT CK_MEMBER_IDLE CHECK (IDLE IN (0,1)),
    CONSTRAINT CK_MEMBER_SUSPENDED CHECK (SUSPENDED IN (0,1)),
    CONSTRAINT CK_MEMBER_GENDER CHECK (GENDER IN ('M','F'))
);

CREATE SEQUENCE SEQ_USER_NO
START WITH 1
INCREMENT BY 1
NOMAXVALUE
NOMINVALUE
NOCYCLE
NOCACHE;

/* =========================================================
    카테고리 (CATEGORY)
   ========================================================= */
CREATE TABLE CATEGORY (
  CATEGORY_NO   NUMBER         NOT NULL,     -- 카테고리번호(PK)
  CATEGORY_NAME VARCHAR2(100)  NOT NULL,     -- 카테고리명

  CONSTRAINT PK_CATEGORY PRIMARY KEY (CATEGORY_NO)
);

CREATE SEQUENCE SEQ_CATEGORY_NO
START WITH 1
INCREMENT BY 1
NOMAXVALUE
NOMINVALUE
NOCYCLE
NOCACHE;

/* =========================================================
   CATEGORY 기본 데이터 입력
   ========================================================= */

INSERT INTO CATEGORY (CATEGORY_NO, CATEGORY_NAME) VALUES (1, '패션');
INSERT INTO CATEGORY (CATEGORY_NO, CATEGORY_NAME) VALUES (2, '육아');
INSERT INTO CATEGORY (CATEGORY_NO, CATEGORY_NAME) VALUES (3, '가전');
INSERT INTO CATEGORY (CATEGORY_NO, CATEGORY_NAME) VALUES (4, '홈·인테리어');
INSERT INTO CATEGORY (CATEGORY_NO, CATEGORY_NAME) VALUES (5, '취미');
INSERT INTO CATEGORY (CATEGORY_NO, CATEGORY_NAME) VALUES (6, '여행');
INSERT INTO CATEGORY (CATEGORY_NO, CATEGORY_NAME) VALUES (7, '공구/산업용품');


/* =========================================================
   지역 (REGION)
   ========================================================= */
CREATE TABLE REGION (
  REGION_NO          NUMBER         NOT NULL,          -- 지역번호(PK)
  REGION_NAME        VARCHAR2(100)  NOT NULL,          -- 지역명
  PARENT_REGION_NO   NUMBER         NULL,              -- 상위지역번호(FK, 자기참조)
  LATITUDE           NUMBER(10,7)   NULL,              -- 위도
  LONGITUDE          NUMBER(10,7)   NULL,              -- 경도

  CONSTRAINT PK_REGION PRIMARY KEY (REGION_NO),
  CONSTRAINT FK_REGION_PARENT FOREIGN KEY (PARENT_REGION_NO)
    REFERENCES REGION(REGION_NO)
);

CREATE SEQUENCE SEQ_REGION_NO
START WITH 1 
INCREMENT BY 1
NOMAXVALUE 
NOMINVALUE
NOCYCLE 
NOCACHE;


/* =========================================================
   회원지역 (MEMBER_REGION)
   - 정책: 회원당 지역 최소 1개 ~ 최대 3개
   ========================================================= */

CREATE TABLE MEMBER_REGION (
  MEMBER_REGION_NO   NUMBER          NOT NULL,         -- 회원지역번호(PK)
  MEMBER_EMAIL       VARCHAR2(100)   NOT NULL,         -- 회원이메일(FK)
  REGION_NO          NUMBER          NOT NULL,         -- 지역번호(FK)

  IS_ACTIVE          CHAR(1) DEFAULT 'Y' NOT NULL,     -- 현재활성지역여부(Y/N)
  IS_VERIFIED        CHAR(1) DEFAULT 'N' NOT NULL,     -- 동네인증여부(Y/N)

  CONSTRAINT PK_MEMBER_REGION PRIMARY KEY (MEMBER_REGION_NO),

  -- ✅ 같은 지역 중복 등록 방지
  CONSTRAINT UQ_MEMBER_REGION_MEMBER_REGION UNIQUE (MEMBER_EMAIL, REGION_NO),

  CONSTRAINT FK_MEMBER_REGION_MEMBER FOREIGN KEY (MEMBER_EMAIL)
    REFERENCES MEMBER(EMAIL),

  CONSTRAINT FK_MEMBER_REGION_REGION FOREIGN KEY (REGION_NO)
    REFERENCES REGION(REGION_NO),

  CONSTRAINT CK_MEMBER_REGION_ACTIVE CHECK (IS_ACTIVE IN ('Y','N')),
  CONSTRAINT CK_MEMBER_REGION_VERIFIED CHECK (IS_VERIFIED IN ('Y','N'))
);

CREATE SEQUENCE SEQ_MEMBER_REGION_NO
START WITH 1
INCREMENT BY 1
NOMAXVALUE
NOMINVALUE
NOCYCLE
NOCACHE;


/* =========================================================
    비회원 위치 (GUEST_REGION)
   ========================================================= */
CREATE TABLE GUEST_REGION (
  GUEST_REGION_NO   NUMBER          NOT NULL,        -- PK
  GUEST_KEY         VARCHAR2(100)   NOT NULL,        -- 세션/디바이스/쿠키키
  REGION_NO         NUMBER          NOT NULL,        -- 지역번호(FK)

  LATITUDE          NUMBER(10,7)    NULL,            -- 위도
  LONGITUDE         NUMBER(10,7)    NULL,            -- 경도
  UPDATED_AT        TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL, -- 갱신시각

  CONSTRAINT PK_GUEST_REGION PRIMARY KEY (GUEST_REGION_NO),
  CONSTRAINT UQ_GUEST_REGION_KEY UNIQUE (GUEST_KEY),
  CONSTRAINT FK_GUEST_REGION_REGION FOREIGN KEY (REGION_NO)
    REFERENCES REGION(REGION_NO)
);

CREATE SEQUENCE SEQ_GUEST_REGION_NO
START WITH 1 INCREMENT BY 1
NOMAXVALUE NOMINVALUE
NOCYCLE NOCACHE;

select * from PRODUCTS

/* =========================================================
   상품 (PRODUCTS) - 수정본
   - 이미지: PRODUCT_IMAGE에서 관리 (IS_MAIN='Y' 대표)
   - 택배옵션(복수): PRODUCT_SHIPPING_OPTION에서 관리
   - 직거래 위치(1~3개): PRODUCT_MEET_LOCATION에서 관리
   ========================================================= */
CREATE TABLE PRODUCTS (
  PRODUCT_NO        NUMBER            NOT NULL,        -- 상품번호(PK)
  SELLER_EMAIL      VARCHAR2(100)     NOT NULL,        -- 판매자 이메일(FK)
  CATEGORY_NO       NUMBER            NOT NULL,        -- 카테고리번호(FK)

  SALE_TYPE         VARCHAR2(20)      NOT NULL,        -- 판매유형(판매/나눔/경매)
  PRODUCT_NAME      VARCHAR2(200)     NOT NULL,        -- 상품명
  PRODUCT_PRICE     NUMBER            NULL,            -- 판매(>0) / 나눔(0 or NULL) / 경매(시작가 >0)
  PRODUCT_DESC      CLOB              NULL,            -- 설명

  PRODUCT_CONDITION VARCHAR2(10)      NOT NULL,        -- 상태(상/중/하)
  TRADE_STATUS      VARCHAR2(20)      NOT NULL,        -- 거래상태(판매중/예약중/판매완료)
  TRADE_METHOD      VARCHAR2(20)      NOT NULL,        -- 거래방법(택배/직거래)

  VIEW_COUNT        NUMBER DEFAULT 0  NOT NULL,        -- 조회수
  REG_DATE          DATE DEFAULT SYSDATE NOT NULL,     -- 등록일

  CONSTRAINT PK_PRODUCTS PRIMARY KEY (PRODUCT_NO),

  CONSTRAINT FK_PRODUCTS_SELLER FOREIGN KEY (SELLER_EMAIL)
    REFERENCES MEMBER(EMAIL),

  CONSTRAINT FK_PRODUCTS_CATEGORY FOREIGN KEY (CATEGORY_NO)
    REFERENCES CATEGORY(CATEGORY_NO),

  CONSTRAINT CK_PRODUCTS_SALE_TYPE CHECK (SALE_TYPE IN ('판매','나눔','경매')),
  CONSTRAINT CK_PRODUCTS_CONDITION CHECK (PRODUCT_CONDITION IN ('상','중','하')),
  CONSTRAINT CK_PRODUCTS_TRADE_STATUS CHECK (TRADE_STATUS IN ('판매중','예약중','판매완료')),
  CONSTRAINT CK_PRODUCTS_TRADE_METHOD CHECK (TRADE_METHOD IN ('택배','직거래')),

  -- SALE_TYPE별 가격 규칙: 판매(>0), 나눔(0 또는 NULL), 경매(시작가 >0)
  CONSTRAINT CK_PRODUCTS_PRICE_BY_SALETYPE CHECK (
    (SALE_TYPE = '판매' AND PRODUCT_PRICE IS NOT NULL AND PRODUCT_PRICE > 0)
    OR
    (SALE_TYPE = '나눔' AND (PRODUCT_PRICE IS NULL OR PRODUCT_PRICE = 0))
    OR
    (SALE_TYPE = '경매' AND PRODUCT_PRICE IS NOT NULL AND PRODUCT_PRICE > 0)
  ),

  CONSTRAINT CK_PRODUCTS_VIEW_COUNT CHECK (VIEW_COUNT >= 0)
);


CREATE SEQUENCE SEQ_PRODUCT_NO
START WITH 1
INCREMENT BY 1
NOMAXVALUE
NOMINVALUE
NOCYCLE
NOCACHE;

select * from PRODUCT_IMAGE

/* =========================================================
   상품이미지 (PRODUCT_IMAGE) - 수정본
   - 상품당 1~3장 (수량 제한은 서비스/트리거로)
   - 대표이미지 IS_MAIN='Y' 는 상품당 1개만 허용(함수기반 유니크 인덱스)
   - IMG_URL 또는 FILENAME 중 하나는 반드시 존재
   - 상품당 SORT_NO 중복 방지
   ========================================================= */
CREATE TABLE PRODUCT_IMAGE (
  PRD_IMG_NO       NUMBER           NOT NULL,        -- 상품이미지번호(PK)
  PRODUCT_NO       NUMBER           NOT NULL,        -- 상품번호(FK)

  IMG_URL          VARCHAR2(500)    NULL,            -- 이미지 URL/경로
  ORGFILENAME      VARCHAR2(255)    NULL,            -- 업로드 원본파일명
  FILENAME         VARCHAR2(255)    NULL,            -- 서버 저장파일명

  SORT_NO          NUMBER DEFAULT 1 NOT NULL,        -- 정렬순서(1부터)
  IS_MAIN          CHAR(1) DEFAULT 'N' NOT NULL,     -- 대표여부(Y/N)

  CONSTRAINT PK_PRODUCT_IMAGE PRIMARY KEY (PRD_IMG_NO),

  CONSTRAINT FK_PRODUCT_IMAGE_PRODUCT FOREIGN KEY (PRODUCT_NO)
    REFERENCES PRODUCTS(PRODUCT_NO) ON DELETE CASCADE,

  CONSTRAINT CK_PRODUCT_IMAGE_SORT CHECK (SORT_NO >= 1),
  CONSTRAINT CK_PRODUCT_IMAGE_IS_MAIN CHECK (IS_MAIN IN ('Y','N')),
  CONSTRAINT CK_PRODUCT_IMAGE_FILE CHECK (IMG_URL IS NOT NULL OR FILENAME IS NOT NULL),

  -- 같은 상품에서 정렬순서 중복 방지
  CONSTRAINT UQ_PRODUCT_IMAGE_SORT UNIQUE (PRODUCT_NO, SORT_NO)
);

CREATE SEQUENCE SEQ_PRODUCT_IMAGE_NO
START WITH 1
INCREMENT BY 1
NOMAXVALUE
NOMINVALUE
NOCYCLE
NOCACHE;

-- (권장) 대표이미지(Y)는 상품당 1개만 허용 (Oracle: 함수 기반 유니크 인덱스)
CREATE UNIQUE INDEX UQ_PRODUCT_MAIN_IMAGE
ON PRODUCT_IMAGE (
  CASE WHEN IS_MAIN = 'Y' THEN PRODUCT_NO END
);

-- (권장) 목록 조회/정렬 성능용
CREATE INDEX IDX_PRODUCT_IMAGE_PRODUCT ON PRODUCT_IMAGE(PRODUCT_NO);
CREATE INDEX IDX_PRODUCT_IMAGE_MAIN   ON PRODUCT_IMAGE(PRODUCT_NO, IS_MAIN);

select * from PRODUCT_SHIPPING_OPTION

/* =========================================================
   상품 배송 옵션 (PRODUCT_SHIPPING_OPTION)
   - 상품 1개당 배송옵션 N개 저장
   - 같은 상품에서 같은 PARCEL_TYPE 중복 금지
   ========================================================= */
CREATE TABLE PRODUCT_SHIPPING_OPTION (
  OPTION_NO    NUMBER        NOT NULL,              -- 옵션번호(PK)
  PRODUCT_NO   NUMBER        NOT NULL,              -- 상품번호(FK)
  PARCEL_TYPE  VARCHAR2(50)  NOT NULL,              -- '일반택배','CU반값','GS반값','무료배송'
  SHIPPING_FEE NUMBER        NOT NULL,              -- 배송비(무료배송=0)

  CONSTRAINT PK_PSO PRIMARY KEY (OPTION_NO),

  CONSTRAINT FK_PSO_PRODUCT FOREIGN KEY (PRODUCT_NO)
    REFERENCES PRODUCTS(PRODUCT_NO) ON DELETE CASCADE,

  CONSTRAINT CK_PSO_PARCEL CHECK (PARCEL_TYPE IN ('일반택배','CU반값','GS반값','무료배송')),
  CONSTRAINT CK_PSO_FEE CHECK (SHIPPING_FEE >= 0),

  CONSTRAINT UQ_PSO UNIQUE (PRODUCT_NO, PARCEL_TYPE)
);

CREATE SEQUENCE SEQ_PSO_NO
START WITH 1
INCREMENT BY 1
NOMAXVALUE
NOMINVALUE
NOCYCLE
NOCACHE;

select * from PRODUCT_MEET_LOCATION

/* =========================================================
   상품 직거래 위치 (PRODUCT_MEET_LOCATION)
   - 상품당 1~3개 (수량 제한은 서비스/트리거로)
   - 대표 위치 개념 제거
   ========================================================= */
CREATE TABLE PRODUCT_MEET_LOCATION (
  LOCATION_NO   NUMBER          NOT NULL,           -- 위치번호(PK)
  PRODUCT_NO    NUMBER          NOT NULL,           -- 상품번호(FK)

  PLACE_NAME    VARCHAR2(200)   NULL,               -- 장소명(예: OO역 3번출구)
  FULL_ADDRESS  VARCHAR2(300)   NOT NULL,           -- 주소(도로명/지번 등)
  LATITUDE      NUMBER(10,7)    NOT NULL,           -- 위도
  LONGITUDE     NUMBER(10,7)    NOT NULL,           -- 경도

  SORT_NO       NUMBER DEFAULT 1 NOT NULL,          -- 정렬순서(1부터, 선택)

  CONSTRAINT PK_PML PRIMARY KEY (LOCATION_NO),

  CONSTRAINT FK_PML_PRODUCT FOREIGN KEY (PRODUCT_NO)
    REFERENCES PRODUCTS(PRODUCT_NO) ON DELETE CASCADE,

  CONSTRAINT CK_PML_SORT CHECK (SORT_NO >= 1),

  -- 같은 상품에서 정렬순서 중복 방지
  CONSTRAINT UQ_PML_SORT UNIQUE (PRODUCT_NO, SORT_NO)
);

-- (권장) FK 조회/조인 성능
CREATE INDEX IX_PML_PRODUCT_NO 
ON PRODUCT_MEET_LOCATION (PRODUCT_NO);

-- 시퀀스
CREATE SEQUENCE SEQ_PML_NO
START WITH 1 
INCREMENT BY 1 
NOMAXVALUE 
NOMINVALUE 
NOCYCLE 
NOCACHE;


/* =========================================================
    경매 (AUCTION)
   ========================================================= */
CREATE TABLE AUCTION (
  AUCTION_NO       NUMBER          NOT NULL,         -- 경매번호(PK)
  PRODUCT_NO       NUMBER          NOT NULL,         -- 상품번호(FK)

  TOP_BIDDER_EMAIL VARCHAR2(100)   NULL,             -- 현재최고입찰자 이메일(FK)
  START_AT         TIMESTAMP       NOT NULL,         -- 시작일시
  END_AT           TIMESTAMP       NOT NULL,         -- 종료일시
  BID_UNIT         NUMBER DEFAULT 5000 NOT NULL,     -- 입찰단위(고정)
  BUY_NOW_PRICE    NUMBER          NULL,             -- 즉시구매가

  CONSTRAINT PK_AUCTION PRIMARY KEY (AUCTION_NO),

  CONSTRAINT FK_AUCTION_PRODUCT FOREIGN KEY (PRODUCT_NO)
    REFERENCES PRODUCTS(PRODUCT_NO) ON DELETE CASCADE,

  CONSTRAINT FK_AUCTION_TOP_BIDDER FOREIGN KEY (TOP_BIDDER_EMAIL)
    REFERENCES MEMBER(EMAIL),

  CONSTRAINT CK_AUCTION_BID_UNIT CHECK (BID_UNIT = 5000),
  CONSTRAINT CK_AUCTION_TIME CHECK (END_AT > START_AT),
  CONSTRAINT CK_AUCTION_BUY_NOW CHECK (BUY_NOW_PRICE IS NULL OR BUY_NOW_PRICE > 0)
);

CREATE SEQUENCE SEQ_AUCTION_NO
START WITH 1 INCREMENT BY 1
NOMAXVALUE NOMINVALUE
NOCYCLE NOCACHE;



/* =========================================================
    경매입찰 (AUCTION_BID)
   ========================================================= */
CREATE TABLE AUCTION_BID (
  AUCTION_BID_NO   NUMBER          NOT NULL,         -- 입찰번호(PK)
  AUCTION_NO       NUMBER          NOT NULL,         -- 경매번호(FK)
  BIDDER_EMAIL     VARCHAR2(100)   NOT NULL,         -- 입찰자 이메일(FK)
  BID_AMOUNT       NUMBER          NOT NULL,         -- 입찰금액
  BID_AT           TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL, -- 입찰일시

  CONSTRAINT PK_AUCTION_BID PRIMARY KEY (AUCTION_BID_NO),

  CONSTRAINT FK_AUCTION_BID_AUCTION FOREIGN KEY (AUCTION_NO)
    REFERENCES AUCTION(AUCTION_NO) ON DELETE CASCADE,

  CONSTRAINT FK_AUCTION_BID_BIDDER FOREIGN KEY (BIDDER_EMAIL)
    REFERENCES MEMBER(EMAIL),

  CONSTRAINT CK_AUCTION_BID_AMOUNT CHECK (BID_AMOUNT > 0)
);

CREATE SEQUENCE SEQ_AUCTION_BID_NO
START WITH 1 INCREMENT BY 1
NOMAXVALUE NOMINVALUE
NOCYCLE NOCACHE;



/* =========================================================
    인기검색어 (POPULAR_KEYWORD)
   ========================================================= */
CREATE TABLE SEARCH_KEYWORD (
  SEARCH_ID       NUMBER          NOT NULL,
  KEYWORD         VARCHAR2(100)   NOT NULL,
  SEARCH_TYPE     VARCHAR2(20)    NOT NULL,                 -- 'GENERAL' / 'PRICE'
  MEMBER_EMAIL    VARCHAR2(100)   NULL,                     -- 회원이면 값, 비회원이면 NULL
  SESSION_ID      VARCHAR2(100)   NULL,                     -- 비회원 식별(쿠키/세션)
  IP_ADDRESS      VARCHAR2(45)    NULL,                     -- IPv4/IPv6 (보조)
  USER_AGENT      VARCHAR2(300)   NULL,                     -- 보조(선택)
  SEARCHED_AT     TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

  CONSTRAINT PK_SEARCH_KEYWORD PRIMARY KEY (SEARCH_ID),
  CONSTRAINT CK_SEARCH_TYPE CHECK (SEARCH_TYPE IN ('GENERAL','PRICE')),
  CONSTRAINT CK_SEARCH_ACTOR CHECK (MEMBER_EMAIL IS NOT NULL OR SESSION_ID IS NOT NULL),
  CONSTRAINT FK_SEARCH_MEMBER_EMAIL
  FOREIGN KEY (MEMBER_EMAIL) REFERENCES MEMBER(EMAIL)
);

CREATE SEQUENCE SEQ_SEARCH_KEYWORD_ID
START WITH 1 
INCREMENT BY 1
NOMAXVALUE 
NOMINVALUE
NOCYCLE
NOCACHE;



/* =========================================================
    찜 (WISHLIST)
   ========================================================= */
CREATE TABLE WISHLIST (
  MEMBER_EMAIL   VARCHAR2(100)  NOT NULL,            -- 회원이메일(FK)
  PRODUCT_NO     NUMBER         NOT NULL,            -- 상품번호(FK)

  CONSTRAINT PK_WISHLIST PRIMARY KEY (MEMBER_EMAIL, PRODUCT_NO),

  CONSTRAINT FK_WISHLIST_MEMBER FOREIGN KEY (MEMBER_EMAIL)
    REFERENCES MEMBER(EMAIL) ON DELETE CASCADE,

  CONSTRAINT FK_WISHLIST_PRODUCT FOREIGN KEY (PRODUCT_NO)
    REFERENCES PRODUCTS(PRODUCT_NO) ON DELETE CASCADE
);

CREATE INDEX IDX_WISHLIST_MEMBER  ON WISHLIST(MEMBER_EMAIL);
CREATE INDEX IDX_WISHLIST_PRODUCT ON WISHLIST(PRODUCT_NO);
-----------------------------------------------------------------------

-------- **** 매장찾기(카카오지도) 테이블 생성하기 **** ----------
create table tbl_map 
(storeID       varchar2(20) not null   --  매장id
,storeName     varchar2(100) not null  --  매장명
,storeUrl      varchar2(200)            -- 매장 홈페이지(URL)주소
,storeImg      varchar2(200) not null   -- 매장소개 이미지파일명  
,storeAddress  varchar2(200) not null   -- 매장주소 및 매장전화번호
,lat           number not null          -- 위도
,lng           number not null          -- 경도 
,zindex        number not null          -- zindex 
,constraint PK_tbl_map primary key(storeID)
,constraint UQ_tbl_map_zindex unique(zindex)
);
-- Table TBL_MAP이(가) 생성되었습니다.

create sequence seq_tbl_map_zindex
start with 1
increment by 1
nomaxvalue
nominvalue
nocycle
nocache;
-- Sequence SEQ_TBL_MAP_ZINDEX이(가) 생성되었습니다.



 
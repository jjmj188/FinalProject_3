show user;
--USER?ù¥(Í∞?) "FINAL_ORAUSER3"?ûÖ?ãà?ã§.

select *
from member;

CREATE SEQUENCE SEQ_MEMBER_NO
START WITH 1
INCREMENT BY 1
NOMAXVALUE
NOMINVALUE
NOCYCLE
NOCACHE;

desc member
commit;
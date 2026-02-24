$(document).ready(function() {
    
    let isEmailChecked = false;
    let isNicknameChecked = false;

    // 카카오 우편번호 API 호출
    $("#btnZipcode").click(function() {
        new daum.Postcode({
            oncomplete: function(data) {
                let addr = ''; 
                let extraAddr = ''; 

                if (data.userSelectedType === 'R') { 
                    addr = data.roadAddress;
                } else { 
                    addr = data.jibunAddress;
                }

                if(data.userSelectedType === 'R'){
                    if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                        extraAddr += data.bname;
                    }
                    if(data.buildingName !== '' && data.apartment === 'Y'){
                        extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                    }
                    if(extraAddr !== ''){
                        extraAddr = ' (' + extraAddr + ')';
                    }
                    $("#extraaddress").val(extraAddr);
                } else {
                    $("#extraaddress").val('');
                }

                $("#postcode").val(data.zonecode);
                $("#address").val(addr);
                $("#detailaddress").focus();
            }
        }).open();
    });

    // 가입하기 버튼 유효성 검사
    $("#btnRegister").click(function() {
        // ... (이전의 이메일, 비밀번호, 닉네임 유효성 검사 로직 동일) ...
		
        // 주소 유효성 검사 (NOT NULL)
        if ($("#postcode").val().trim() === "" || $("#address").val().trim() === "" || $("#detailaddress").val().trim() === "") {
            $("#addressError").text("우편번호, 기본주소, 상세주소는 필수 입력사항입니다.");
            return;
        } else { $("#addressError").text(""); }

        // 생년월일 형식 변경 (YYYY-MM-DD -> YYYYMMDD) 처리는 컨트롤러에서 수행하는 것이 안전합니다.
        document.registerFrm.submit();
    });
});
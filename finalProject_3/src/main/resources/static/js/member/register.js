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

$(document).ready(function() {
    
    // 1. 이메일 중복 확인 버튼 클릭 이벤트
    $('#btnEmailCheck').click(function() {
        let email = $('#email').val().trim();
        if(email === '') {
            $('#emailError').text('이메일을 입력해주세요.').css('color', 'red');
            return;
        }

        $.ajax({
            url: '/member/emailDuplicateCheck',
            type: 'POST',
            data: { "email": email },
            success: function(res) {
                if(res.isExist) {
                    $('#emailError').text('이미 사용 중인 이메일입니다.').css('color', 'red');
                } else {
                    $('#emailError').text('사용 가능한 이메일입니다. 인증을 진행해주세요.').css('color', 'green');
                    $('#email').attr('readonly', true); // 이메일 수정 불가 처리
                    
                    // 핵심: 버튼 텍스트와 ID 변경
                    $('#btnEmailCheck').text('이메일인증').attr('id', 'btnEmailSend');
                    $('#btnEmailSend').removeClass('btn-outline-secondary').addClass('btn-primary');
                }
            },
            error: function() {
                alert("서버 통신 오류");
            }
        });
    });

    // 2. 이메일 인증(발송) 버튼 클릭 이벤트 (동적으로 ID가 바뀐 버튼 이벤트 처리)
    $(document).on('click', '#btnEmailSend', function() {
        let email = $('#email').val();
        
        // 로딩 상태 표시 (선택사항)
        let $btn = $(this);
        $btn.text('발송중...').prop('disabled', true);

        $.ajax({
            url: '/member/sendAuthEmail',
            type: 'POST',
            data: { "email": email },
            success: function(res) {
                alert("인증 메일이 발송되었습니다. 메일함을 확인해주세요.");
                $btn.text('재발송').prop('disabled', false); // 재발송 버튼으로 변경
                
                // 인증번호 입력 폼이 없으면 추가
                if($('#authCodeDiv').length === 0) {
                    let authHtml = `
                        <div class="input-group mt-2" id="authCodeDiv">
                            <input type="text" id="authCode" class="form-control" placeholder="인증번호 6자리 입력">
                            <div class="input-group-append ml-2">
                                <button type="button" id="btnAuthConfirm" class="btn btn-dark">확인</button>
                            </div>
                        </div>
                        <small id="authError" class="text-danger"></small>
                    `;
                    $('#emailError').after(authHtml);
                }
            },
            error: function() {
                alert("메일 발송에 실패했습니다.");
                $btn.text('이메일인증').prop('disabled', false);
            }
        });
    });

    // 3. 인증번호 확인 버튼 클릭 이벤트
    $(document).on('click', '#btnAuthConfirm', function() {
        let authCode = $('#authCode').val();
        
        $.ajax({
            url: '/member/verifyAuthCode',
            type: 'POST',
            data: { "authCode": authCode },
            success: function(res) {
                if(res.isSuccess) {
                    $('#authError').text('인증이 완료되었습니다.').removeClass('text-danger').addClass('text-success');
                    $('#authCode').attr('readonly', true);
                    $('#btnAuthConfirm').prop('disabled', true);
                    $('#btnEmailSend').prop('disabled', true); // 더 이상 재발송 못하게 막음
                } else {
                    $('#authError').text('인증번호가 일치하지 않습니다.');
                }
            }
        });
    });
    
    // 닉네임 중복 확인 (참고용)
    $('#btnNicknameCheck').click(function() {
        // 이메일과 동일한 패턴으로 /member/nicknameDuplicateCheck 호출
    });
});
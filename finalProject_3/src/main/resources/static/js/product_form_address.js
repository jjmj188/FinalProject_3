function waitForKakaoServices(cb, tries = 40) {
		  const ok = window.kakao && kakao.maps && kakao.maps.services;
		  if (ok) return cb();

		  if (tries <= 0) {
		    console.error("카카오 SDK 로드 실패: Network에서 sdk.js 200 확인 + 중복 로드 제거");
		    return;
		  }
		  setTimeout(() => waitForKakaoServices(cb, tries - 1), 100);
		}

		waitForKakaoServices(function(){
		  // 여기서부터만 카카오/주소 이벤트 연결
		  $("#pfLocationBtn").off("click").on("click", openPostcodeForLocation);
		  $("#pfMyLocationBtn").off("click").on("click", setMyLocation);
		});
	
	
		const openLink = document.getElementById('pfShipSettingLink');
		const modal = document.getElementById('pfShipModal');
		const closeBtn = document.getElementById('pfShipModalClose');

		openLink.addEventListener('click', function(e) {
			e.preventDefault();
			modal.classList.add('is-open');
		});

		closeBtn.addEventListener('click', function() {
			modal.classList.remove('is-open');
		});

		// 배경 클릭 시 닫기
		modal.addEventListener('click', function(e) {
			if (e.target === modal) {
				modal.classList.remove('is-open');
			}
		});
		
		$(document).ready(function(){

			  // 1) 주소 검색(daum Postcode)로 위치 설정
			  $("#pfLocationBtn").click(function(){
			    openPostcodeForLocation();
			  });

			  // 2) 내 위치로 설정
			  $("#pfMyLocationBtn").click(function(){
			    setMyLocation();
			  });

			});

			/* =========================
			   공통: 설정 완료 처리
			========================= */
			function applyLocationResult(opts){
			  // opts: { fullAddress, extraAddress, lat, lng, shortAddress }

			  $("#pfAddress").val(opts.fullAddress || "");
			  $("#pfExtraAddress").val(opts.extraAddress || "");
			  $("#pfLat").val(opts.lat || "");
			  $("#pfLng").val(opts.lng || "");
			  $("#pfShortAddress").val(opts.shortAddress || "");

			  // 화면 표시(축약 주소 우선)
			  $("#pfLocationText").text(opts.fullAddress || "위치를 설정해주세요");

			  // 버튼 텍스트 변경
			  $("#pfLocationBtn").text("위치 변경");
			}

			/* =========================
			   1) 주소 검색 팝업
			========================= */
			function openPostcodeForLocation(){

			  new daum.Postcode({
			    oncomplete: function(data) {

			      let addr = '';
			      let extraAddr = '';

			      if (data.userSelectedType === 'R') {
			        addr = data.roadAddress;
			      } else {
			        addr = data.jibunAddress;
			      }

			      if (data.userSelectedType === 'R') {
			        if (data.bname !== '' && /[동|로|가]$/g.test(data.bname)) {
			          extraAddr += data.bname;
			        }
			        if (data.buildingName !== '' && data.apartment === 'Y') {
			          extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
			        }
			        if (extraAddr !== '') {
			          extraAddr = ' (' + extraAddr + ')';
			        }
			      } else {
			        extraAddr = '';
			      }

			      // 주소 텍스트만 먼저 적용(좌표는 아래에서 붙임)
			      // 축약 주소는 "내 위치"처럼 regionCode 기반이 아니라
			      // 일단 주소 문자열에서 대략 자르는 방식으로 처리
			      const shortAddr = shortenAddressFromText(addr);

			      applyLocationResult({
			    	  fullAddress: addr + extraAddr,   
			    	  extraAddress: extraAddr,
			    	  lat: "",
			    	  lng: "",
			    	  shortAddress: ""
			    	});

			      // 주소 -> 위/경도 변환 후 hidden 저장 (표시는 이미 했음)
			      addressToLatLng(addr, function(lat, lng){
			        $("#pfLat").val(lat);
			        $("#pfLng").val(lng);
			      });

			    }
			  }).open();

			}

			/* =========================
			   2) 내 위치(geolocation)
			========================= */
			function setMyLocation(){

			  if (!navigator.geolocation) {
			    alert("이 브라우저는 위치 기능을 지원하지 않습니다.");
			    return;
			  }

			  // HTTPS(또는 localhost)에서만 정확히 동작하는 경우가 많음
			  navigator.geolocation.getCurrentPosition(
			    function(pos){
			      const lat = pos.coords.latitude;
			      const lng = pos.coords.longitude;

			      // 좌표 -> 행정동/법정동 기반 축약주소 얻기 (시/구/동)
			      latLngToRegionShortAddress(lat, lng, function(shortAddr){
			        // 좌표 -> 전체 주소(지번/도로명)도 얻기 (저장용)
			        latLngToFullAddress(lat, lng, function(fullAddr){
			          applyLocationResult({
			            fullAddress: fullAddr || "",
			            extraAddress: "",
			            lat: lat,
			            lng: lng,
			            shortAddress: shortAddr || (fullAddr ? shortenAddressFromText(fullAddr) : "")
			          });
			        });
			      });

			    },
			    function(err){
			      // err.code: 1(PERMISSION_DENIED), 2(POSITION_UNAVAILABLE), 3(TIMEOUT)
			      if (err.code === 1) alert("위치 권한이 거부되었습니다.");
			      else if (err.code === 2) alert("위치 정보를 가져올 수 없습니다.");
			      else if (err.code === 3) alert("위치 조회 시간이 초과되었습니다.");
			      else alert("위치 조회 중 오류가 발생했습니다.");
			    },
			    {
			      enableHighAccuracy: true,
			      timeout: 10000,
			      maximumAge: 0
			    }
			  );
			}

			/* =========================
			   카카오 SDK 로드 체크
			========================= */
			function ensureKakaoServices(){
			  if (!window.kakao || !kakao.maps || !kakao.maps.services) {
			    console.error("kakao.maps.services 로드 안됨 (&libraries=services 확인)");
			    return false;
			  }
			  return true;
			}

			/* =========================
			   주소 -> 위/경도
			========================= */
			function addressToLatLng(address, cb){
			  if (!ensureKakaoServices()) return;

			  const geocoder = new kakao.maps.services.Geocoder();
			  geocoder.addressSearch(address, function(result, status){
			    if (status === kakao.maps.services.Status.OK) {
			      cb(result[0].y, result[0].x);
			    } else {
			      cb("", "");
			    }
			  });
			}

			/* =========================
			   위/경도 -> 전체 주소(저장용)
			   coord2Address(경도, 위도)
			========================= */
			function latLngToFullAddress(lat, lng, cb){
			  if (!ensureKakaoServices()) return;

			  const geocoder = new kakao.maps.services.Geocoder();
			  geocoder.coord2Address(lng, lat, function(result, status){
			    if (status === kakao.maps.services.Status.OK) {
			      const jibun = result[0].address ? result[0].address.address_name : "";
			      const road  = result[0].road_address ? result[0].road_address.address_name : "";
			      cb(road || jibun);
			    } else {
			      cb("");
			    }
			  });
			}

			/* =========================
			   위/경도 -> 시/구/동 축약 주소
			   coord2RegionCode(경도, 위도)
			   (region_1depth_name, region_2depth_name, region_3depth_name)
			========================= */
			function latLngToRegionShortAddress(lat, lng, cb){
			  if (!ensureKakaoServices()) return;

			  const geocoder = new kakao.maps.services.Geocoder();
			  geocoder.coord2RegionCode(lng, lat, function(result, status){
			    if (status === kakao.maps.services.Status.OK && result && result.length > 0) {

			      // 보통 H(행정동) / B(법정동) 둘 다 올 수 있음
			      // H 우선, 없으면 첫 번째
			      const h = result.find(r => r.region_type === "H") || result[0];

			      const r1 = h.region_1depth_name || "";
			      const r2 = h.region_2depth_name || "";
			      const r3 = h.region_3depth_name || "";

			      const shortAddr = [r1, r2, r3].filter(Boolean).join(" ");
			      cb(shortAddr);
			    } else {
			      cb("");
			    }
			  });
			}

			/* =========================
			   텍스트 주소에서 대충 시/구/동만 추출(보조용)
			   - Postcode로 받은 문자열은 지역코드가 없으니
			     일단 최소한으로 깔끔하게 보이도록 처리
			========================= */
			function shortenAddressFromText(addr){
			  if (!addr) return "";

			  // 공백 기준 토큰 분해
			  const parts = addr.trim().split(/\s+/);

			  // 일반적인 케이스: [시/도] [구/군] [동/읍/면] ...
			  // 최소 3개까지만 붙여서 보여줌
			  const shortParts = parts.slice(0, 3);

			  return shortParts.join(" ");
			}
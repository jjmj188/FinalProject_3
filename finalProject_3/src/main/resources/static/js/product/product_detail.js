document.addEventListener("DOMContentLoaded", function () {

    /* =========================
       1. 대표 이미지 변경
    ========================= */
    const mainImg = document.getElementById("pdMainImage");
    const thumbImgs = document.querySelectorAll(".pd-thumb img");

    if (mainImg && thumbImgs.length > 0) {
        thumbImgs.forEach(function (img) {
            img.addEventListener("click", function () {
                const nextSrc = img.src.includes("w=400")
                    ? img.src.replace("w=400", "w=1200")
                    : img.src;

                mainImg.src = nextSrc;
                mainImg.alt = img.alt.replace("썸네일", "상품 이미지");
            });
        });
    }

    /* =========================
       2. 판매자 다른 물품 슬라이더
    ========================= */
    (function () {
        const viewport = document.getElementById("locViewport");
        const prevBtn = document.getElementById("locPrevBtn");
        const nextBtn = document.getElementById("locNextBtn");

        if (!viewport || !prevBtn || !nextBtn) {
            return;
        }

        function maxScroll() {
            return viewport.scrollWidth - viewport.clientWidth;
        }

        function updateBtns() {
            prevBtn.disabled = viewport.scrollLeft <= 0;
            nextBtn.disabled = viewport.scrollLeft >= (maxScroll() - 1);
        }

        prevBtn.addEventListener("click", function () {
            viewport.scrollLeft = Math.max(0, viewport.scrollLeft - viewport.clientWidth);
            setTimeout(updateBtns, 80);
        });

        nextBtn.addEventListener("click", function () {
            viewport.scrollLeft = Math.min(maxScroll(), viewport.scrollLeft + viewport.clientWidth);
            setTimeout(updateBtns, 80);
        });

        viewport.addEventListener("scroll", updateBtns);
        window.addEventListener("resize", updateBtns);
        window.addEventListener("load", updateBtns);

        updateBtns();
    })();

    /* =========================
       3. 카카오 지도
    ========================= */

	/* =========================
	   3. 카카오 지도 + 거래희망장소 연동
	========================= */
	(function () {
	    const container = document.getElementById("locMapKakao");
	    const pdLocationButtons = document.querySelectorAll(".pd-location[data-lat][data-lng]");
	    const mapPlaceButtons = document.querySelectorAll(".loc-map-place[data-lat][data-lng]");
	    const locWrap = document.getElementById("locWrap");

	    if (!container) {
	        return;
	    }

	    try {
	        if (!(window.kakao && kakao.maps && kakao.maps.load)) {
	            return;
	        }

	        kakao.maps.load(function () {

	            let defaultLat = 37.5665;
	            let defaultLng = 126.9780;
	            let defaultPlace = "위치 정보 없음";

	            if (mapPlaceButtons.length > 0) {
	                defaultLat = parseFloat(mapPlaceButtons[0].dataset.lat);
	                defaultLng = parseFloat(mapPlaceButtons[0].dataset.lng);
	                defaultPlace = mapPlaceButtons[0].dataset.place || "거래 희망 위치";
	            }

	            if (isNaN(defaultLat) || isNaN(defaultLng)) {
	                defaultLat = 37.5665;
	                defaultLng = 126.9780;
	            }

	            const map = new kakao.maps.Map(container, {
	                center: new kakao.maps.LatLng(defaultLat, defaultLng),
	                level: 3
	            });

	            const marker = new kakao.maps.Marker({
	                position: new kakao.maps.LatLng(defaultLat, defaultLng)
	            });
	            marker.setMap(map);

	            const infoWindow = new kakao.maps.InfoWindow({
	                content: '<div style="padding:6px 10px; font-size:12px;">' + defaultPlace + '</div>'
	            });
	            infoWindow.open(map, marker);

	            function setActiveMapPlaceButton(place) {
	                mapPlaceButtons.forEach(function (btn) {
	                    btn.classList.remove("is-active");

	                    if ((btn.dataset.place || "") === place) {
	                        btn.classList.add("is-active");
	                    }
	                });
	            }

	            function moveMapTo(lat, lng, place, shouldScroll) {
	                if (isNaN(lat) || isNaN(lng)) {
	                    return;
	                }

	                const position = new kakao.maps.LatLng(lat, lng);

	                map.panTo(position);
	                marker.setPosition(position);

	                infoWindow.close();
	                infoWindow.setContent('<div style="padding:6px 10px; font-size:12px;">' + place + '</div>');
	                infoWindow.open(map, marker);

	                setActiveMapPlaceButton(place);

	                if (shouldScroll && locWrap) {
	                    locWrap.scrollIntoView({
	                        behavior: "smooth",
	                        block: "start"
	                    });
	                }
	            }

	            mapPlaceButtons.forEach(function (btn) {
	                btn.addEventListener("click", function () {
	                    const lat = parseFloat(btn.dataset.lat);
	                    const lng = parseFloat(btn.dataset.lng);
	                    const place = btn.dataset.place || "거래 희망 위치";

	                    moveMapTo(lat, lng, place, false);
	                });
	            });

	            pdLocationButtons.forEach(function (btn) {
	                btn.addEventListener("click", function () {
	                    const lat = parseFloat(btn.dataset.lat);
	                    const lng = parseFloat(btn.dataset.lng);
	                    const place = btn.dataset.place || "거래 희망 위치";

	                    moveMapTo(lat, lng, place, true);
	                });
	            });

	            setActiveMapPlaceButton(defaultPlace);
	        });
	    } catch (e) {
	        console.warn("Kakao map load failed:", e);
	    }
	})();


	/* =========================
	   4. 찜 하트 기능
	========================= */
	$(document).on("click", ".pd-like", function (e) {
	    e.preventDefault();
	    e.stopPropagation();

	    const $btn = $(this);
	    const isLogin = String($btn.attr("data-login")) === "true";

	    if (!isLogin) {
	        alert("로그인이 필요합니다.");
	        location.href = "/finalProject_3/login/login";
	        return;
	    }

	    const productNo = $btn.attr("data-product-no");

	    $.ajax({
	        url: "/finalProject_3/product/wishlist/toggle",
	        type: "post",
	        data: { productNo: productNo },
	        dataType: "json",
	        success: function (json) {
	            if (!json.success) {
	                alert(json.message || "찜 처리에 실패했습니다.");
	                return;
	            }

	            const $icon = $btn.find("i");
	            const $wishCountText = $("#wishCountText");
	            let currentCount = parseInt($wishCountText.text(), 10);

	            if (isNaN(currentCount)) {
	                currentCount = 0;
	            }

	            if (json.wished) {
	                $btn.addClass("is-active");
	                $icon.removeClass("fa-regular").addClass("fa-solid");
	                $wishCountText.text(currentCount + 1);
	                alert("찜 성공");
	            } 
	            else {
	                $btn.removeClass("is-active");
	                $icon.removeClass("fa-solid").addClass("fa-regular");

	                if (currentCount > 0) {
	                    $wishCountText.text(currentCount - 1);
	                }

	                alert("찜 취소");
	            }
	        },
	        error: function (request, status, error) {
	            console.log("찜 AJAX ERROR");
	            console.log("status =", request.status);
	            console.log("responseText =", request.responseText);
	            console.log("error =", error);
	            alert("찜 처리 중 오류가 발생했습니다.");
	        }
	    });
	});
    /* =========================
       5. 등록시간 표시
    ========================= */
    (function () {
        const elements = document.querySelectorAll(".time-ago");

        elements.forEach(function (el) {
            const regDateStr = el.dataset.regdate;

            if (!regDateStr) {
                return;
            }

            const regDate = new Date(regDateStr.replace(" ", "T"));
            const now = new Date();
            const diff = Math.floor((now - regDate) / 1000);

            if (isNaN(diff)) {
                el.innerText = "";
                return;
            }

            if (diff < 60) {
                el.innerText = "방금전";
            } else if (diff < 3600) {
                el.innerText = Math.floor(diff / 60) + "분전";
            } else if (diff < 86400) {
                el.innerText = Math.floor(diff / 3600) + "시간전";
            } else if (diff < 2592000) {
                el.innerText = Math.floor(diff / 86400) + "일전";
            } else if (diff < 31536000) {
                el.innerText = Math.floor(diff / 2592000) + "개월전";
            } else {
                el.innerText = Math.floor(diff / 31536000) + "년전";
            }
        });
    })();

    /* =========================
       6. 상품정보 더보기
    ========================= */
    (function () {
        const textBox = document.getElementById("pdinfoTextBox");
        const moreBtn = document.getElementById("pdinfoMoreBtn");

        if (!textBox || !moreBtn) {
            return;
        }

        function checkOverflow() {
            textBox.classList.add("is-collapsed");
            textBox.classList.remove("is-expanded");

            const isOverflowing = textBox.scrollHeight > textBox.clientHeight + 1;

            if (isOverflowing) {
                moreBtn.classList.remove("is-hidden");
                moreBtn.innerText = "더보기";
            } else {
                moreBtn.classList.add("is-hidden");
            }
        }

        moreBtn.addEventListener("click", function () {
            const isExpanded = textBox.classList.contains("is-expanded");

            if (isExpanded) {
                textBox.classList.remove("is-expanded");
                textBox.classList.add("is-collapsed");
                moreBtn.innerText = "더보기";
            } else {
                textBox.classList.remove("is-collapsed");
                textBox.classList.add("is-expanded");
                moreBtn.innerText = "접기";
            }
        });

        window.addEventListener("load", checkOverflow);
        window.addEventListener("resize", checkOverflow);

        checkOverflow();
    })();

    /* =========================
       7. 매너온도 바 길이 + 색상
    ========================= */
    (function () {
        const tempValueEl = document.getElementById("pdinfoTempValue");
        const barFill = document.getElementById("pdinfoBarFill");

        if (!tempValueEl || !barFill) {
            return;
        }

        let temp = parseFloat(tempValueEl.dataset.temp);

        if (isNaN(temp)) {
            temp = 0;
        }

        if (temp < 0) {
            temp = 0;
        }

        if (temp > 100) {
            temp = 100;
        }

        let color = "#9ca3af"; // 회색

        if (temp <= 12.5) {
            color = "#9ca3af";
        } else if (temp <= 30) {
            color = "#60a5fa";
        } else if (temp <= 36.5) {
            color = "#2563eb";
        } else if (temp <= 50.5) {
            color = "#22c55e";
        } else if (temp <= 65.5) {
            color = "#eab308";
        } else {
            color = "#ef4444";
        }

        barFill.style.width = temp + "%";
        barFill.style.backgroundColor = color;
        tempValueEl.style.color = color;
    })();

});
document.addEventListener("DOMContentLoaded", function () {
    const bodyData = document.body.dataset || {};
    const ctxPath = (bodyData.ctxPath || "/finalProject_3/").replace(/([^/])$/, "$1/");

    let isChatLoading = false;
    let isSubmittingProductReport = false;
    let productReportSelectedFile = null;

    const productReportData = {
        "불법 행위": ["판매 금지 품목 게시", "장물 및 습득물 판매", "가품(이미테이션) 판매", "청소년 유해물 게시"],
        "사기 피해": ["직거래 유도 후 잠적", "외부 채널 유도", "물품 정보 허위 기재", "택배 사기"],
        "비매너 행위": ["노쇼(No-Show)", "무리한 가격 제안", "반말 및 욕설", "거래 확정 후 변심"],
        "스팸성 홍보": ["전문 업자의 반복 게시", "홍보 및 광고", "도배 게시물", "낚시성 키워드 사용"],
        "기타 사유": ["기타 사유 직접 입력"]
    };

    function moveLogin() {
        alert("로그인이 필요합니다.");
        location.href = ctxPath + "security/login";
    }

    function getMainElement() {
        return document.querySelector("main");
    }

    function getMainData(name) {
        const mainEl = getMainElement();
        return mainEl ? mainEl.getAttribute(name) : "";
    }

    function buildImageUrl(path, defaultUrl) {
        if (!path || String(path).trim() === "") {
            return defaultUrl;
        }

        const value = String(path).trim();

        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }

        if (value.startsWith(ctxPath)) {
            return value;
        }

        if (value.startsWith("/images/") || value.startsWith("/upload/")) {
            return ctxPath.replace(/\/$/, "") + value;
        }

        if (value.startsWith("images/") || value.startsWith("upload/")) {
            return ctxPath + value;
        }

        return ctxPath + "images/" + value;
    }

    function calcTempStyle(temp) {
        let value = parseFloat(temp);

        if (isNaN(value)) value = 0;
        if (value < 0) value = 0;
        if (value > 100) value = 100;

        let color = "#9ca3af";

        if (value <= 12.5) {
            color = "#9ca3af";
        } else if (value <= 30) {
            color = "#60a5fa";
        } else if (value <= 36.5) {
            color = "#2563eb";
        } else if (value <= 50.5) {
            color = "#22c55e";
        } else if (value <= 65.5) {
            color = "#eab308";
        } else {
            color = "#ef4444";
        }

        return {
            percent: value,
            color: color
        };
    }

    function bindMainImageChange() {
        const mainImg = document.getElementById("pdMainImage");
        const thumbImgs = document.querySelectorAll(".pd-thumb img");

        if (!mainImg || thumbImgs.length === 0) {
            return;
        }

        thumbImgs.forEach(function (img) {
            img.addEventListener("click", function () {
                const fullSrc = img.dataset.full || img.src;
                mainImg.src = fullSrc;
                mainImg.alt = (img.alt || "상품 이미지").replace("썸네일", "상품 이미지");
            });
        });
    }

    function bindSimilarProductSlider() {
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
    }

    function bindKakaoMap() {
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
                    content: '<div style="padding:6px 10px; font-size:12px;">' + defaultPlace + "</div>"
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
                    infoWindow.setContent('<div style="padding:6px 10px; font-size:12px;">' + place + "</div>");
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
    }

    function bindWishlist() {
        if (typeof window.jQuery === "undefined") {
            return;
        }

        $(document).on("click", ".pd-like", function (e) {
            e.preventDefault();
            e.stopPropagation();

            const $btn = $(this);
            const isLogin = String($btn.attr("data-login")) === "true";

            if (!isLogin) {
                moveLogin();
                return;
            }

            const productNo = $btn.attr("data-product-no");

            $.ajax({
                url: ctxPath + "product/wishlist/toggle",
                type: "post",
                data: { productNo: productNo },
                dataType: "json",
                success: function (json) {
                    if (!json.success) {
                        alert(json.message || "찜 처리에 실패했습니다.");
                        return;
                    }

                    const $icon = $btn.find("i");
                    const wishCountTargets = document.querySelectorAll(".wish-count-text, #wishCountText");
                    let currentCount = 0;

                    if (wishCountTargets.length > 0) {
                        const parsed = parseInt(wishCountTargets[0].textContent, 10);
                        currentCount = isNaN(parsed) ? 0 : parsed;
                    }

                    if (json.wished) {
                        $btn.addClass("is-active");
                        $icon.removeClass("fa-regular").addClass("fa-solid");
                        wishCountTargets.forEach(function (el) {
                            el.textContent = String(currentCount + 1);
                        });
                        alert("찜 성공");
                    } else {
                        $btn.removeClass("is-active");
                        $icon.removeClass("fa-solid").addClass("fa-regular");
                        wishCountTargets.forEach(function (el) {
                            el.textContent = String(Math.max(0, currentCount - 1));
                        });
                        alert("찜 취소");
                    }
                },
                error: function (request, status, error) {
                    console.log("찜 AJAX ERROR");
                    console.log("status =", request.status);
                    console.log("responseText =", request.responseText);
                    console.log("error =", error);

                    if (request.status === 401) {
                        alert("로그인이 필요하거나 인증이 만료되었습니다.");
                        moveLogin();
                        return;
                    }

                    if (request.status === 403) {
                        alert("접근 권한이 없습니다.");
                        return;
                    }

                    alert("찜 처리 중 오류가 발생했습니다.");
                }
            });
        });
    }

    function bindTimeAgo() {
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
    }

    function bindProductInfoMoreButton() {
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
    }

    function applyTemperatureBar() {
        const tempValueEl = document.getElementById("pdinfoTempValue");
        const barFill = document.getElementById("pdinfoBarFill");

        if (!tempValueEl || !barFill) {
            return;
        }

        const tempInfo = calcTempStyle(tempValueEl.dataset.temp);

        barFill.style.width = tempInfo.percent + "%";
        barFill.style.backgroundColor = tempInfo.color;
        tempValueEl.style.color = tempInfo.color;
    }

    function bindChatButton() {
        const chatBtn = document.getElementById("btnStartChat");

        if (!chatBtn) {
            return;
        }

        chatBtn.addEventListener("click", function () {
            const productNo = this.getAttribute("data-product-no");
            const sellerEmail = this.getAttribute("data-seller-email");
            const productName = this.getAttribute("data-product-name");

            startChat(productNo, sellerEmail, productName);
        });
    }

    function startChat(productNo, sellerEmail, productName) {
        if (isChatLoading) {
            return;
        }

        isChatLoading = true;

        fetch(ctxPath + "api/chat/createRoom", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                productNo: parseInt(productNo, 10),
                sellerEmail: sellerEmail
            })
        })
            .then(function (response) {
                if (response.status === 401 || response.status === 403) {
                    isChatLoading = false;
                    if (typeof window.toggleChatPopup === "function") {
                        window.toggleChatPopup();
                    }
                    return null;
                }
                if (!response.ok) {
                    throw new Error("서버 에러 발생");
                }
                return response.json();
            })
            .then(function (data) {
                if (!data) return;
                isChatLoading = false;

                if (data.success) {
                    const modal = document.getElementById("chatModal");

                    if (!modal || modal.style.display === "none" || modal.style.display === "") {
                        if (typeof window.toggleChatPopup === "function") {
                            window.toggleChatPopup();
                        }
                    }

                    setTimeout(function () {
                        if (typeof window.openChatRoom === "function") {
                            window.openChatRoom(
                                data.roomId,
                                data.nickname || sellerEmail,
                                productName,
                                data.productImgUrl,
                                data.productPrice,
                                data.tradeStatus,
                                sellerEmail,
                                parseInt(productNo, 10),
                                data.reservedRoomId,
                                data.tradeMethod,
                                data.saleType
                            );
                        }
                    }, 300);
                } else {
                    alert(data.message || "채팅방 생성에 실패했습니다.");
                }
            })
            .catch(function (error) {
                isChatLoading = false;
                console.error("방 생성 에러:", error);
            });
    }

    function loadProductDetailSellerProfile() {
        const productNo = getMainData("data-product-no");

        if (!productNo) {
            return;
        }

        fetch(ctxPath + "product/seller/profile?productNo=" + encodeURIComponent(productNo), {
            method: "GET",
            headers: {
                Accept: "application/json"
            }
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("판매자 정보를 불러오지 못했습니다.");
                }
                return response.json();
            })
            .then(function (data) {
                if (!data.success) {
                    throw new Error(data.message || "판매자 정보를 불러오지 못했습니다.");
                }

                const seller = data.seller || {};

                const sellerName = seller.sellerName || "판매자";
                const sellerProfileImg = buildImageUrl(
                    seller.sellerProfileImg,
                    ctxPath + "images/default_profile.png"
                );
                const mannerTemp = seller.mannerTemp != null ? Number(seller.mannerTemp) : 36.5;
                const safePayCount = seller.safePayCount != null ? seller.safePayCount : 0;
                const reviewCount = seller.reviewCount != null ? seller.reviewCount : 0;
                const tradeCount = seller.tradeCount != null ? seller.tradeCount : 0;

                const sellerNameEl = document.getElementById("pdinfoSellerName");
                const sellerImgEl = document.getElementById("pdinfoSellerImg");
                const tempValueEl = document.getElementById("pdinfoTempValue");
                const tempBarFillEl = document.getElementById("pdinfoBarFill");
                const safePayEl = document.getElementById("pdinfoSafePayCount");
                const reviewEl = document.getElementById("pdinfoReviewCount");
                const tradeEl = document.getElementById("pdinfoTradeCount");

                if (sellerNameEl) {
                    sellerNameEl.textContent = sellerName;
                }

                if (sellerImgEl) {
                    sellerImgEl.src = sellerProfileImg;
                }

                if (tempValueEl) {
                    tempValueEl.textContent = mannerTemp.toFixed(1) + "°C";
                    tempValueEl.setAttribute("data-temp", String(mannerTemp));
                }

                const tempInfo = calcTempStyle(mannerTemp);

                if (tempBarFillEl) {
                    tempBarFillEl.style.width = tempInfo.percent + "%";
                    tempBarFillEl.style.backgroundColor = tempInfo.color;
                }

                if (tempValueEl) {
                    tempValueEl.style.color = tempInfo.color;
                }

                if (safePayEl) {
                    safePayEl.textContent = String(safePayCount);
                }

                if (reviewEl) {
                    reviewEl.textContent = String(reviewCount);
                }

                if (tradeEl) {
                    tradeEl.textContent = String(tradeCount);
                }
            })
            .catch(function (error) {
                console.error("상품상세 판매자 정보 로딩 오류:", error);
            });
    }

    function backToProductReportStep1() {
        const step1 = document.getElementById("productReportStep1");
        const step2 = document.getElementById("productReportStep2");

        if (step1) {
            step1.classList.add("is-active");
        }

        if (step2) {
            step2.classList.remove("is-active");
        }
    }

    function updateProductReportCharCount(obj) {
        const countEl = document.getElementById("productReportCurrentCharCount");

        if (countEl && obj) {
            countEl.innerText = obj.value.length;
        }
    }

    function renderProductReportPhotoPreview() {
        const preview = document.getElementById("productReportPhotoPreview");
        const countEl = document.getElementById("productReportPhotoCount");

        if (!preview || !countEl) {
            return;
        }

        preview.innerHTML = "";

        if (!productReportSelectedFile) {
            countEl.textContent = "0/1";
            return;
        }

        countEl.textContent = "1/1";

        const reader = new FileReader();
        reader.onload = function (e) {
            const wrap = document.createElement("div");
            wrap.style.cssText = "position:relative; width:60px; height:60px;";

            const img = document.createElement("img");
            img.src = e.target.result;
            img.style.cssText =
                "width:60px; height:60px; object-fit:cover; border-radius:6px; border:1px solid #ddd;";

            const del = document.createElement("button");
            del.type = "button";
            del.textContent = "✕";
            del.style.cssText =
                "position:absolute; top:-6px; right:-6px; width:18px; height:18px; border-radius:50%; border:none; background:#e44; color:#fff; font-size:10px; cursor:pointer; padding:0; line-height:18px; text-align:center;";
            del.onclick = function () {
                productReportSelectedFile = null;
                const input = document.getElementById("productReportFileInput");
                if (input) {
                    input.value = "";
                }
                renderProductReportPhotoPreview();
            };

            wrap.appendChild(img);
            wrap.appendChild(del);
            preview.appendChild(wrap);
        };
        reader.readAsDataURL(productReportSelectedFile);
    }

    function onProductReportFileSelected(input) {
        if (!input || !input.files || input.files.length === 0) {
            return;
        }

        productReportSelectedFile = input.files[0];
        renderProductReportPhotoPreview();
    }

    function goProductReportStep2(typeName) {
        const step1 = document.getElementById("productReportStep1");
        const step2 = document.getElementById("productReportStep2");
        const titleEl = document.getElementById("productReportMainTitle");
        const subSelect = document.getElementById("productReportSubCategory");
        const textEl = document.getElementById("productReportText");
        const fileInputEl = document.getElementById("productReportFileInput");
        const photoCountEl = document.getElementById("productReportPhotoCount");
        const previewEl = document.getElementById("productReportPhotoPreview");

        if (step1) {
            step1.classList.remove("is-active");
        }

        if (step2) {
            step2.classList.add("is-active");
        }

        if (titleEl) {
            titleEl.innerText = typeName;
        }

        if (subSelect) {
            subSelect.innerHTML = "";

            const subItems = productReportData[typeName] || [];
            subItems.forEach(function (item) {
                const option = document.createElement("option");
                option.value = item;
                option.text = item;
                subSelect.appendChild(option);
            });
        }

        if (textEl) {
            textEl.value = "";
            updateProductReportCharCount(textEl);
        }

        productReportSelectedFile = null;

        if (fileInputEl) {
            fileInputEl.value = "";
        }

        if (photoCountEl) {
            photoCountEl.textContent = "0/1";
        }

        if (previewEl) {
            previewEl.innerHTML = "";
        }
    }

    function openProductReportModal() {
        const isLogin = getMainData("data-login") === "true";
        const isOwner = getMainData("data-owner") === "true";

        if (!isLogin) {
            alert("로그인 후 신고할 수 있습니다.");
            location.href = ctxPath + "security/login";
            return;
        }

        if (isOwner) {
            alert("본인 게시글은 신고할 수 없습니다.");
            return;
        }

        const overlay = document.getElementById("productReportOverlay");
        const panel = document.getElementById("productReportPanel");

        if (!overlay || !panel) {
            return;
        }

        overlay.classList.add("show");
        panel.classList.add("show");
        backToProductReportStep1();
        document.body.style.overflow = "hidden";
    }

    function closeProductReportModal() {
        const overlay = document.getElementById("productReportOverlay");
        const panel = document.getElementById("productReportPanel");
        const textEl = document.getElementById("productReportText");
        const countEl = document.getElementById("productReportCurrentCharCount");
        const fileInputEl = document.getElementById("productReportFileInput");
        const photoCountEl = document.getElementById("productReportPhotoCount");
        const previewEl = document.getElementById("productReportPhotoPreview");

        if (overlay) {
            overlay.classList.remove("show");
        }

        if (panel) {
            panel.classList.remove("show");
        }

        document.body.style.overflow = "";

        if (textEl) {
            textEl.value = "";
        }

        if (countEl) {
            countEl.innerText = "0";
        }

        if (fileInputEl) {
            fileInputEl.value = "";
        }

        if (photoCountEl) {
            photoCountEl.textContent = "0/1";
        }

        if (previewEl) {
            previewEl.innerHTML = "";
        }

        productReportSelectedFile = null;
        backToProductReportStep1();
    }

    function submitProductReport() {
        if (isSubmittingProductReport) {
            return;
        }

        const mainTypeEl = document.getElementById("productReportMainTitle");
        const subTypeEl = document.getElementById("productReportSubCategory");
        const contentEl = document.getElementById("productReportText");
        const productNo = getMainData("data-product-no");

        const mainType = mainTypeEl ? mainTypeEl.innerText : "";
        const subType = subTypeEl ? subTypeEl.value : "";
        const content = contentEl ? contentEl.value.trim() : "";

        if (!subType) {
            alert("상세 신고 유형을 선택해주세요.");
            return;
        }

        if (!content) {
            alert("신고 사유를 입력해주세요.");
            return;
        }

        if (!productNo) {
            alert("상품 정보가 올바르지 않습니다.");
            return;
        }

        isSubmittingProductReport = true;

        const formData = new FormData();
        formData.append("productNo", productNo);
        formData.append("reportMainCategory", mainType);
        formData.append("reportSubCategory", subType);
        formData.append("reportContent", content);

        if (productReportSelectedFile) {
            formData.append("image", productReportSelectedFile);
        }

        fetch(ctxPath + "product/report", {
            method: "POST",
            body: formData
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("신고 접수 실패");
                }
                return response.json();
            })
            .then(function (data) {
                isSubmittingProductReport = false;

                if (data.success) {
                    alert("신고가 정상적으로 접수되었습니다.");
                    closeProductReportModal();
                } else {
                    alert(data.message || "신고 접수에 실패했습니다.");
                }
            })
            .catch(function (error) {
                isSubmittingProductReport = false;
                console.error("상품 신고 에러:", error);
                alert("신고 처리 중 오류가 발생했습니다.");
            });
    }



    window.openProductReportModal = openProductReportModal;
    window.closeProductReportModal = closeProductReportModal;
    window.goProductReportStep2 = goProductReportStep2;
    window.backToProductReportStep1 = backToProductReportStep1;
    window.updateProductReportCharCount = updateProductReportCharCount;
    window.onProductReportFileSelected = onProductReportFileSelected;
    window.submitProductReport = submitProductReport;
    window.goToPayment = goToPayment;

    bindMainImageChange();
    bindSimilarProductSlider();
    bindKakaoMap();
    bindWishlist();
    bindTimeAgo();
    bindProductInfoMoreButton();
    applyTemperatureBar();
    bindChatButton();
    loadProductDetailSellerProfile();
});
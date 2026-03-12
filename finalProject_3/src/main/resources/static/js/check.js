if (window.__priceCheckInitialized) {
    console.log("check.js already initialized");
} else {
    window.__priceCheckInitialized = true;

    document.addEventListener("DOMContentLoaded", function () {

        const ctxPath = (window.ctxPath || "").replace(/\/$/, "");
        const searchBtn = document.getElementById("searchBtn");
        const searchWordInput = document.getElementById("searchWord");
        const displayList = document.getElementById("displayList");

        function goPriceSearch(sortType, priceMode) {
            const searchWord = (searchWordInput?.value || "").trim();

            if (searchWord === "") {
                alert("검색어를 입력하세요.");
                searchWordInput?.focus();
                return;
            }

            let url = ctxPath + "/product/price_check?searchWord=" + encodeURIComponent(searchWord);
            url += "&sortType=" + encodeURIComponent(sortType || window.priceSortType || "latest");
            url += "&priceMode=" + encodeURIComponent(priceMode || window.priceMode || "list");

            location.href = url;
        }

        searchBtn?.addEventListener("click", function () {
            goPriceSearch("latest", window.priceMode || "list");
        });

        searchWordInput?.addEventListener("keydown", function (e) {
            if (e.key === "Enter") {
                e.preventDefault();
                hideAutoComplete();
                goPriceSearch(window.priceSortType || "latest", window.priceMode || "list");
            }
        });

        /* =========================
           검색어 자동완성
        ========================= */
        function hideAutoComplete() {
            if (!displayList) return;
            displayList.innerHTML = "";
            displayList.style.display = "none";
        }

        function fetchAutoComplete(searchWord) {
            if (!displayList) return;

            if (!searchWord || searchWord.length === 0) {
                hideAutoComplete();
                return;
            }

            fetch(ctxPath + "/product/wordSearchShow?searchWord=" + encodeURIComponent(searchWord), {
                method: "GET"
            })
            .then(function (res) {
                if (!res.ok) {
                    throw new Error("HTTP status " + res.status);
                }
                return res.json();
            })
            .then(function (json) {
                if (!Array.isArray(json) || json.length === 0) {
                    hideAutoComplete();
                    return;
                }

                let html = "";

                json.forEach(function (item) {
                    const word = item.word || "";
                    const lowerWord = word.toLowerCase();
                    const lowerSearchWord = searchWord.toLowerCase();
                    const idx = lowerWord.indexOf(lowerSearchWord);
                    const len = searchWord.length;

                    let result = word;

                    if (idx > -1) {
                        result =
                            word.substring(0, idx) +
                            "<span class='search-highlight'>" +
                            word.substring(idx, idx + len) +
                            "</span>" +
                            word.substring(idx + len);
                    }

                    html += "<div class='result search-display-item'>" + result + "</div>";
                });

                displayList.innerHTML = html;
                displayList.style.display = "block";
            })
            .catch(function (err) {
                console.error("자동완성 AJAX 오류:", err);
                hideAutoComplete();
            });
        }

        searchWordInput?.addEventListener("input", function () {
            fetchAutoComplete(searchWordInput.value.trim());
        });

        searchWordInput?.addEventListener("focus", function () {
            const keyword = searchWordInput.value.trim();
            if (keyword.length > 0) {
                fetchAutoComplete(keyword);
            }
        });

        document.addEventListener("click", function (e) {
            if (!e.target.closest(".search-pill")) {
                hideAutoComplete();
            }
        });

        document.addEventListener("click", function (e) {
            const item = e.target.closest("#displayList .result");
            if (!item) return;

            const word = item.textContent.trim();
            searchWordInput.value = word;
            hideAutoComplete();
            goPriceSearch(window.priceSortType || "latest", window.priceMode || "list");
        });

        document.querySelectorAll(".sps-sortBtn").forEach(function (btn) {
            btn.addEventListener("click", function () {
                const sortType = btn.dataset.sort || "latest";
                hideAutoComplete();
                goPriceSearch(sortType, window.priceMode || "list");
            });
        });

        /* =========================
           찜 버튼
        ========================= */
        document.querySelectorAll(".sps-like").forEach(function (btn) {

            if (btn.dataset.bindWish === "true") {
                return;
            }
            btn.dataset.bindWish = "true";

            btn.addEventListener("click", function (e) {
                e.preventDefault();
                e.stopPropagation();

                if (btn.dataset.loading === "true") {
                    return;
                }

                const productNo = btn.dataset.productNo;
                const isLogin = String(btn.dataset.login) === "true";

                if (!isLogin) {
                    alert("로그인이 필요합니다.");
                    location.href = ctxPath + "/security/login";
                    return;
                }

                if (!productNo) {
                    alert("상품번호가 없습니다.");
                    return;
                }

                btn.dataset.loading = "true";
                btn.disabled = true;

                const headers = {
                    "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
                };

                const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }

                fetch(ctxPath + "/product/wishlist/toggle", {
                    method: "POST",
                    headers: headers,
                    body: "productNo=" + encodeURIComponent(productNo)
                })
                .then(function (res) {
                    if (!res.ok) {
                        throw new Error("HTTP status " + res.status);
                    }
                    return res.json();
                })
                .then(function (data) {
                    if (!data.success) {
                        alert(data.message || "찜 처리에 실패했습니다.");
                        return;
                    }

                    const wished = !!data.wished;

                    btn.classList.toggle("is-on", wished);
                    btn.setAttribute("aria-pressed", wished ? "true" : "false");

                    const icon = btn.querySelector("i");
                    if (icon) {
                        icon.className = wished ? "fa-solid fa-heart" : "fa-regular fa-heart";
                    }

                    if (wished) {
                        alert("찜 성공");
                    } else {
                        alert("찜 취소");
                    }
                })
                .catch(function (err) {
                    console.error("찜 AJAX 오류:", err);
                    alert("찜 처리 중 오류가 발생했습니다.");
                })
                .finally(function () {
                    btn.dataset.loading = "false";
                    btn.disabled = false;
                });
            });
        });

        if (!window.hasResult) {
            return;
        }

        const canvas = document.getElementById("mkChart");
        const tip = document.getElementById("mkTip");
        const priceEl = document.getElementById("mkPrice");
        const tabs = Array.from(document.querySelectorAll(".mk-tab"));
        const emptyMsg = document.getElementById("mkEmptyMsg");

        if (!canvas || !tip || !priceEl || tabs.length === 0) {
            return;
        }

        const ctx = canvas.getContext("2d");

        const css = getComputedStyle(document.documentElement);
        const COLORS = {
            green: css.getPropertyValue("--green").trim() || "#16c36c",
            blue: css.getPropertyValue("--blue").trim() || "#2f7df6",
            grid: css.getPropertyValue("--line").trim() || "#e9eef5",
            muted: css.getPropertyValue("--muted").trim() || "#6b7684"
        };

        const serverChartData = Array.isArray(window.priceChartData) ? window.priceChartData : [];
        const statsData = window.priceStatsData || null;

        let mode = window.priceMode || "list";
        let hitPoints = [];

        function fmtMoney(v) {
            return Math.round(Number(v || 0)).toLocaleString("ko-KR");
        }

        function fmtTick(v) {
            const n = Number(v || 0);

            if (n === 0) return "0원";
            if (n < 10000) return fmtMoney(n) + "원";
            if (n % 10000 === 0) return (n / 10000) + "만원";
            return (n / 10000).toFixed(1) + "만원";
        }

        function fmtMMDD(ymd) {
            if (!ymd || ymd.length < 10) return "";
            return ymd.substring(5, 7) + "/" + ymd.substring(8, 10);
        }

        let DATA = serverChartData.map(function (item) {
            return {
                x: fmtMMDD(item.priceDate),
                fullDate: item.priceDate,
                list: Number(item.avgPrice || 0),
                sale: 0
            };
        });

        function syncTabUI() {
            tabs.forEach(function (btn) {
                const btnMode = btn.dataset.mode || "";
                const isActive = btnMode === mode;

                btn.classList.toggle("is-active", isActive);
                btn.setAttribute("aria-selected", isActive ? "true" : "false");
            });
        }

        function getYMax(maxValue) {
            if (maxValue <= 0) return 10000;

            let yMax = Math.ceil(maxValue * 1.2 / 1000) * 1000;
            if (yMax < 10000) yMax = 10000;
            return yMax;
        }

        function buildYTicks(yMin, yMax) {
            const range = yMax - yMin;
            const unit = Math.ceil(range / 5 / 1000) * 1000 || 1000;
            const ticks = [];

            for (let i = 0; i <= 5; i++) {
                ticks.push(yMin + (unit * i));
            }

            return ticks;
        }

        function resizeCanvas() {
            const wrap = canvas.parentElement;
            const rect = wrap.getBoundingClientRect();
            const dpr = Math.max(1, window.devicePixelRatio || 1);

            if (rect.width === 0 || rect.height === 0) return;

            canvas.width = Math.floor(rect.width * dpr);
            canvas.height = Math.floor(rect.height * dpr);

            canvas.style.width = rect.width + "px";
            canvas.style.height = rect.height + "px";

            ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
            draw();
        }

        function draw() {
            const W = canvas.clientWidth;
            const H = canvas.clientHeight;

            if (W === 0 || H === 0) return;

            ctx.clearRect(0, 0, W, H);
            hideTip();

            const maxListValue = Math.max(...DATA.map(function (d) { return d.list || 0; }), 0);
            const Y_MIN = 0;
            const Y_MAX = getYMax(maxListValue);
            const Y_TICKS = buildYTicks(Y_MIN, Y_MAX);

            if (mode === "sale") {
                emptyMsg.style.display = "flex";
                emptyMsg.textContent = "판매가 기능은 아직 준비 중입니다.";
            } else {
                emptyMsg.style.display = "none";
            }

            const pad = { l: 60, r: 16, t: 16, b: 34 };
            const innerW = W - pad.l - pad.r;
            const innerH = H - pad.t - pad.b;
            const xCount = DATA.length;
            const xStep = xCount > 1 ? innerW / (xCount - 1) : 0;

            const yToPx = function (y) {
                const ratio = (y - Y_MIN) / (Y_MAX - Y_MIN || 1);
                return pad.t + innerH - (ratio * innerH);
            };

            const xToPx = function (i) {
                return xCount === 1 ? (pad.l + innerW / 2) : (pad.l + i * xStep);
            };

            ctx.save();
            ctx.lineWidth = 1;
            ctx.font = '12px system-ui, -apple-system, "Segoe UI", Roboto, "Noto Sans KR", Arial';
            ctx.textAlign = "right";
            ctx.textBaseline = "middle";
            ctx.fillStyle = COLORS.muted;

            Y_TICKS.forEach(function (val) {
                const y = yToPx(val);

                ctx.strokeStyle = COLORS.grid;
                ctx.setLineDash([3, 6]);
                ctx.beginPath();
                ctx.moveTo(pad.l, y);
                ctx.lineTo(W - pad.r, y);
                ctx.stroke();

                ctx.setLineDash([]);
                ctx.fillText(fmtTick(val), pad.l - 10, y);
            });

            ctx.restore();

            const listPts = DATA.map(function (d, i) {
                return {
                    x: xToPx(i),
                    y: yToPx(d.list),
                    v: d.list,
                    label: d.x,
                    fullDate: d.fullDate,
                    key: "등록가"
                };
            });

            hitPoints = [];

            if (mode === "all" || mode === "list") {
                hitPoints = listPts.map(function (p) {
                    return {
                        x: p.x,
                        y: p.y,
                        v: p.v,
                        label: p.label,
                        fullDate: p.fullDate,
                        key: p.key,
                        color: COLORS.green
                    };
                });

                drawLine(listPts, COLORS.green, 3);
                drawPoints(listPts, COLORS.green);
            }

            if (DATA.length > 0) {
                ctx.save();
                ctx.fillStyle = COLORS.muted;
                ctx.font = '12px system-ui, -apple-system, "Segoe UI", Roboto, "Noto Sans KR", Arial';
                ctx.textBaseline = "top";

                ctx.textAlign = "left";
                ctx.fillText(DATA[0].x, pad.l, H - pad.b + 8);

                if (DATA.length > 1) {
                    ctx.textAlign = "right";
                    ctx.fillText(DATA[DATA.length - 1].x, W - pad.r, H - pad.b + 8);
                }

                ctx.restore();
            }
        }

        function drawLine(points, color, width) {
            if (!points || points.length === 0) return;

            ctx.save();
            ctx.strokeStyle = color;
            ctx.lineWidth = width;
            ctx.lineJoin = "round";
            ctx.lineCap = "round";

            ctx.beginPath();
            ctx.moveTo(points[0].x, points[0].y);

            for (let i = 1; i < points.length; i++) {
                ctx.lineTo(points[i].x, points[i].y);
            }

            ctx.stroke();
            ctx.restore();
        }

        function drawPoints(points, color) {
            ctx.save();

            points.forEach(function (p) {
                ctx.fillStyle = "#fff";
                ctx.beginPath();
                ctx.arc(p.x, p.y, 4.5, 0, Math.PI * 2);
                ctx.fill();

                ctx.fillStyle = color;
                ctx.beginPath();
                ctx.arc(p.x, p.y, 3, 0, Math.PI * 2);
                ctx.fill();
            });

            ctx.restore();
        }

        function updatePrice() {
            if (mode === "sale") {
                priceEl.innerHTML = `0<small>원</small>`;
                priceEl.style.color = COLORS.blue;
                return;
            }

            let v = 0;

            if (statsData && statsData.avgPrice != null) {
                v = Number(statsData.avgPrice);
            }

            priceEl.innerHTML = `${fmtMoney(v)}<small>원</small>`;
            priceEl.style.color = COLORS.green;
        }

        function hideTip() {
            tip.style.opacity = "0";
        }

        function showTip(p) {
            tip.style.opacity = "1";
            tip.style.left = p.x + "px";
            tip.style.top = p.y + "px";
            tip.innerHTML = `
                <div class="mk-tip-date">${p.fullDate}</div>
                <div class="mk-tip-price">등록가: ${fmtMoney(p.v)}원</div>
            `;
        }

        function getMousePos(e) {
            const rect = canvas.getBoundingClientRect();
            return {
                x: e.clientX - rect.left,
                y: e.clientY - rect.top
            };
        }

        function findNearestPoint(mx, my) {
            let best = null;
            let bestD = Infinity;

            for (const p of hitPoints) {
                const dx = p.x - mx;
                const dy = p.y - my;
                const d = Math.sqrt(dx * dx + dy * dy);

                if (d < bestD) {
                    bestD = d;
                    best = p;
                }
            }

            return (best && bestD <= 18) ? best : null;
        }

        canvas.addEventListener("mousemove", function (e) {
            if (mode === "sale") {
                hideTip();
                return;
            }

            const pos = getMousePos(e);
            const p = findNearestPoint(pos.x, pos.y);

            if (p) showTip(p);
            else hideTip();
        });

        canvas.addEventListener("mouseleave", hideTip);

        tabs.forEach(function (btn) {
            btn.addEventListener("click", function () {
                mode = btn.dataset.mode || "list";
                window.priceMode = mode;

                syncTabUI();
                updatePrice();
                draw();
                hideTip();
            });
        });

        syncTabUI();
        updatePrice();
        window.addEventListener("resize", resizeCanvas);
        resizeCanvas();

        /* =========================
           시간 표시
        ========================= */
        document.querySelectorAll(".time-ago").forEach(function(el){
            const timeStr = el.dataset.time;
            if(!timeStr) return;

            const regDate = new Date(timeStr.replace(" ", "T"));
            const now = new Date();
            const diff = Math.floor((now - regDate) / 1000);

            let text = "";

            if (diff < 60) {
                text = "방금 전";
            }
            else if (diff < 3600) {
                text = Math.floor(diff / 60) + "분 전";
            }
            else if (diff < 86400) {
                text = Math.floor(diff / 3600) + "시간 전";
            }
            else if (diff < 604800) {
                text = Math.floor(diff / 86400) + "일 전";
            }
            else {
                text = regDate.toLocaleDateString();
            }

            el.textContent = text;
        });

    });
}
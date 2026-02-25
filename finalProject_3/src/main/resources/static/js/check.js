(() => {
  // ================================
  // 1) "오늘 기준 최근 30일" 날짜 생성
  // ================================
  const DAYS = 30;

  // KST 기준 "오늘" (브라우저 로컬이 KST면 그냥 써도 됨)
  // 혹시 서버시간/UTC 섞일까봐 안전하게 로컬 Date 사용
  const today = new Date();
  today.setHours(0,0,0,0);

  // 시작일 = 오늘 - 29일  (오늘 포함해서 30개)
  const start = new Date(today);
  start.setDate(start.getDate() - (DAYS - 1));

  function pad2(n){ return String(n).padStart(2, "0"); }

  function fmtMMDD(d){
    const mm = pad2(d.getMonth() + 1);
    const dd = pad2(d.getDate());
    return `${mm}/${dd}`;
  }

  function fmtYYYYMMDD(d){
    const y = d.getFullYear();
    const m = pad2(d.getMonth() + 1);
    const day = pad2(d.getDate());
    return `${y}-${m}-${day}`;
  }

  // =====================================================
  // 2) 날짜 배열 만들기 (start ~ today, 총 30개)
  // =====================================================
  const dateList = [];
  for(let i=0; i<DAYS; i++){
    const d = new Date(start);
    d.setDate(start.getDate() + i);
    dateList.push(d);
  }

  // =====================================================
  // 3) 시세 데이터 구성 (여기서 "매일" DATA 생성)
  //    - 지금은 예시(더미) 생성
  //    - 실제는 서버에서 30일치 받아서 매핑만 하면 됨
  // =====================================================

  // ✅ 더미 생성: 너무 튀지 않게 랜덤 워크 느낌
  function clamp(v, min, max){ return Math.max(min, Math.min(max, v)); }
  function randInt(min, max){
    return Math.floor(Math.random() * (max - min + 1)) + min;
  }

  let baseList = 25000;  // 등록가 기준값
  let baseSale = 28000;  // 판매가 기준값

  const DATA = dateList.map((d, idx) => {
    // 변동폭(예시): 하루 1~4천원 정도 흔들리게
    baseList += randInt(-3500, 3500);
    baseSale += randInt(-4000, 4000);

    // 가끔 급등/급락(예시)
    if(idx % 11 === 0 && idx !== 0) baseSale += randInt(8000, 25000);
    if(idx % 13 === 0 && idx !== 0) baseList -= randInt(5000, 12000);

    baseList = clamp(baseList, 5000, 120000);
    baseSale = clamp(baseSale, 5000, 120000);

    return {
      x: fmtMMDD(d),          // 차트 라벨(보이는 값)
      date: fmtYYYYMMDD(d),   // 실제 날짜(서버 매핑용)
      list: baseList,
      sale: baseSale
    };
  });

  // ================================
  // 4) 기존 차트 로직 (거의 그대로)
  // ================================
  const canvas = document.getElementById("mkChart");
  const tip = document.getElementById("mkTip");
  const priceEl = document.getElementById("mkPrice");
  const tabs = Array.from(document.querySelectorAll(".mk-tab"));

  if (!canvas || !tip || !priceEl || tabs.length === 0) {
    console.warn("[mk-chart] 필요한 DOM이 없습니다. (#mkChart, #mkTip, #mkPrice, .mk-tab)");
    return;
  }

  const ctx = canvas.getContext("2d");

  const css = getComputedStyle(document.documentElement);
  const COLORS = {
    green: css.getPropertyValue("--green").trim() || "#16c36c",
    blue:  css.getPropertyValue("--blue").trim()  || "#2f7df6",
    grid:  css.getPropertyValue("--line").trim()  || "#e9eef5",
    muted: css.getPropertyValue("--muted").trim() || "#6b7684",
  };

  // y범위 고정(너 코드 그대로)
  const Y_MIN = 5000;
  const Y_MAX = 120000;
  const Y_TICKS = [5000, 30000, 50000, 70000, 100000, 120000];

  let mode = "all"; // all | list | sale
  let hitPoints = [];

  function fmtMoney(v){ return Math.round(v).toLocaleString("ko-KR"); }
  function fmtTick(v){
    if (v === 5000) return "5 천원";
    if (v >= 10000) return (v/10000) + " 만원";
    return fmtMoney(v) + "원";
  }

  function resizeCanvas(){
    const wrap = canvas.parentElement;
    const rect = wrap.getBoundingClientRect();
    const dpr = Math.max(1, window.devicePixelRatio || 1);

    canvas.width  = Math.floor(rect.width * dpr);
    canvas.height = Math.floor(rect.height * dpr);

    canvas.style.width = rect.width + "px";
    canvas.style.height = rect.height + "px";

    ctx.setTransform(dpr,0,0,dpr,0,0);
    draw();
  }

  function draw(){
    const W = canvas.clientWidth;
    const H = canvas.clientHeight;

    ctx.clearRect(0,0,W,H);

    const pad = { l: 54, r: 10, t: 10, b: 26 };
    const innerW = W - pad.l - pad.r;
    const innerH = H - pad.t - pad.b;

    const xCount = DATA.length;            // ✅ 30
    const xStep = innerW / Math.max(1, (xCount - 1));

    const yToPx = (y) => {
      const t = (y - Y_MIN) / (Y_MAX - Y_MIN);
      return pad.t + innerH - (t * innerH);
    };
    const xToPx = (i) => pad.l + i * xStep;

    // y grid + label
    ctx.save();
    ctx.lineWidth = 1;
    Y_TICKS.forEach((val) => {
      const y = yToPx(val);
      ctx.strokeStyle = COLORS.grid;
      ctx.setLineDash([3,6]);
      ctx.beginPath();
      ctx.moveTo(pad.l, y);
      ctx.lineTo(W - pad.r, y);
      ctx.stroke();

      ctx.setLineDash([]);
      ctx.fillStyle = COLORS.muted;
      ctx.font = '12px system-ui, -apple-system, "Segoe UI", Roboto, "Noto Sans KR", Arial';
      ctx.textAlign = "right";
      ctx.textBaseline = "middle";
      ctx.fillText(fmtTick(val), pad.l - 10, y);
    });
    ctx.restore();

    const listPts = DATA.map((d,i)=>({ x:xToPx(i), y:yToPx(d.list), v:d.list, label:d.x, key:"등록가" }));
    const salePts = DATA.map((d,i)=>({ x:xToPx(i), y:yToPx(d.sale), v:d.sale, label:d.x, key:"판매가" }));

    const showList = (mode === "all" || mode === "list");
    const showSale = (mode === "all" || mode === "sale");

    hitPoints = [];
    if (showList) hitPoints.push(...listPts.map(p=>({ ...p, series:"list", color:COLORS.green })));
    if (showSale) hitPoints.push(...salePts.map(p=>({ ...p, series:"sale", color:COLORS.blue })));

    if (showList) drawSmoothLine(listPts, COLORS.green, 3);
    if (showSale) drawSmoothLine(salePts, COLORS.blue, 3);

    drawPoints(listPts, COLORS.green, showList);
    drawPoints(salePts, COLORS.blue, showSale);

    // x 라벨(양끝만) - 너 스타일 유지
    ctx.save();
    ctx.fillStyle = COLORS.muted;
    ctx.font = '12px system-ui, -apple-system, "Segoe UI", Roboto, "Noto Sans KR", Arial';
    ctx.textBaseline = "top";
    ctx.textAlign = "left";
    ctx.fillText(DATA[0].x, pad.l, H - pad.b + 8);
    ctx.textAlign = "right";
    ctx.fillText(DATA[DATA.length-1].x, W - pad.r, H - pad.b + 8);
    ctx.restore();
  }

  function drawSmoothLine(pts, color, width){
    ctx.save();
    ctx.strokeStyle = color;
    ctx.lineWidth = width;
    ctx.lineJoin = "round";
    ctx.lineCap = "round";

    ctx.shadowColor = color;
    ctx.shadowBlur = 8;
    ctx.shadowOffsetX = 0;
    ctx.shadowOffsetY = 0;

    ctx.beginPath();
    ctx.moveTo(pts[0].x, pts[0].y);

    for (let i=0; i<pts.length-1; i++){
      const p0 = pts[i-1] || pts[i];
      const p1 = pts[i];
      const p2 = pts[i+1];
      const p3 = pts[i+2] || p2;

      const cp1x = p1.x + (p2.x - p0.x) / 6;
      const cp1y = p1.y + (p2.y - p0.y) / 6;
      const cp2x = p2.x - (p3.x - p1.x) / 6;
      const cp2y = p2.y - (p3.y - p1.y) / 6;

      ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y);
    }

    ctx.stroke();
    ctx.restore();
  }

  function drawPoints(pts, color, enabled){
    if(!enabled) return;
    ctx.save();
    for(const p of pts){
      ctx.fillStyle = "#fff";
      ctx.beginPath();
      ctx.arc(p.x, p.y, 4.5, 0, Math.PI*2);
      ctx.fill();

      ctx.fillStyle = color;
      ctx.beginPath();
      ctx.arc(p.x, p.y, 3, 0, Math.PI*2);
      ctx.fill();
    }
    ctx.restore();
  }

  function updatePrice(){
    const last = DATA[DATA.length - 1];
    let v;
    if (mode === "list") v = last.list;
    else if (mode === "sale") v = last.sale;
    else v = Math.round((last.list + last.sale) / 2);

    priceEl.innerHTML = `${fmtMoney(v)}<small>원</small>`;
    priceEl.style.color = (mode === "sale") ? COLORS.blue : COLORS.green;
  }

  function hideTip(){ tip.style.opacity = "0"; }

  function showTip(p){
    tip.style.opacity = "1";
    tip.style.left = p.x + "px";
    tip.style.top = p.y + "px";
    tip.innerHTML = `
      <b>${p.label}</b>
      <div class="mk-tipRow">
        <span class="mk-pill">
          <span style="display:inline-block;width:8px;height:8px;border-radius:999px;background:${p.color}"></span>
          ${p.key}
        </span>
        <span>${fmtMoney(p.v)}원</span>
      </div>
    `;
  }

  function getMousePos(e){
    const rect = canvas.getBoundingClientRect();
    return { x: e.clientX - rect.left, y: e.clientY - rect.top };
  }

  function findNearestPoint(mx, my){
    let best = null;
    let bestD = Infinity;
    for(const p of hitPoints){
      const dx = p.x - mx;
      const dy = p.y - my;
      const d = Math.sqrt(dx*dx + dy*dy);
      if(d < bestD){ bestD = d; best = p; }
    }
    return (best && bestD <= 18) ? best : null;
  }

  canvas.addEventListener("mousemove", (e)=>{
    const {x,y} = getMousePos(e);
    const p = findNearestPoint(x,y);
    if(p) showTip(p);
    else hideTip();
  });
  canvas.addEventListener("mouseleave", hideTip);

  tabs.forEach(btn=>{
    btn.addEventListener("click", ()=>{
      tabs.forEach(b=>{
        b.classList.remove("is-active");
        b.setAttribute("aria-selected","false");
      });
      btn.classList.add("is-active");
      btn.setAttribute("aria-selected","true");
      mode = btn.dataset.mode || "all";
      updatePrice();
      draw();
      hideTip();
    });
  });

  updatePrice();
  window.addEventListener("resize", resizeCanvas);
  resizeCanvas();

})();

(function(){
  const openBtn = document.querySelector(".btn-area");
  const modal = document.getElementById("areaModal");
  const searchInput = document.getElementById("areaSearchInput");
  const list = document.getElementById("areaList");
  const useGeoBtn = document.getElementById("areaUseGeoBtn");

  if(!openBtn || !modal || !searchInput || !list) return;

  let lastFocusedEl = null;

  function openModal(){
    lastFocusedEl = document.activeElement;
    modal.classList.add("is-open");
    modal.setAttribute("aria-hidden", "false");
    document.body.style.overflow = "hidden";
    setTimeout(() => searchInput.focus(), 0);
  }

  function closeModal(){
    modal.classList.remove("is-open");
    modal.setAttribute("aria-hidden", "true");
    document.body.style.overflow = "";
    if(lastFocusedEl) lastFocusedEl.focus();
  }

  // 열기
  openBtn.addEventListener("click", openModal);

  // 닫기(배경/닫기버튼)
  modal.addEventListener("click", (e) => {
    const closeTarget = e.target.closest("[data-area-close='true']");
    if(closeTarget) closeModal();
  });

  // ESC 닫기
  window.addEventListener("keydown", (e) => {
    if(e.key === "Escape" && modal.classList.contains("is-open")) closeModal();
  });

  // 추천 리스트 클릭 -> 버튼 텍스트에 반영
  list.addEventListener("click", (e) => {
    const btn = e.target.closest(".area-item");
    if(!btn) return;

    const val = btn.getAttribute("data-area-value") || btn.textContent.trim();
    const labelSpan = openBtn.querySelector("span");

    if(labelSpan){
      labelSpan.innerHTML = `<span class="area-selected">${val}</span>`;
    }
    closeModal();
  });

  // 검색 필터 (추천 리스트 텍스트 기준)
  searchInput.addEventListener("input", () => {
    const q = searchInput.value.trim().toLowerCase();
    const items = Array.from(list.querySelectorAll(".area-item"));

    items.forEach((btn) => {
      const text = (btn.textContent || "").toLowerCase();
      const li = btn.closest("li");
      const show = !q || text.includes(q);
      if(li) li.style.display = show ? "" : "none";
    });
  });

  // 현재 위치 사용하기 (브라우저 geolocation)
  useGeoBtn?.addEventListener("click", () => {
    if(!navigator.geolocation){
      alert("이 브라우저에서는 위치 기능을 사용할 수 없어요.");
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        // 여기서 좌표 -> 주소 변환(역지오코딩)은 서버/지도 API가 필요함.
        // 지금은 “좌표 확인됨” 정도로만 표시.
        const { latitude, longitude } = pos.coords;
        const labelSpan = openBtn.querySelector("span");
        if(labelSpan){
          labelSpan.innerHTML = `<span class="area-selected">현재 위치(${latitude.toFixed(4)}, ${longitude.toFixed(4)})</span>`;
        }
        closeModal();
      },
      () => {
        alert("위치 권한이 거부되었거나 위치를 가져오지 못했어요.");
      },
      { enableHighAccuracy: true, timeout: 8000 }
    );
  });
})();
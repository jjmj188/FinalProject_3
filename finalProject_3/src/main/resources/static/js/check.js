

(() => {
  const DATA = [
    { x:"01/01", list:18000, sale:52000 },
    { x:"01/04", list:12000, sale:30000 },
    { x:"01/07", list:46000, sale:14000 },
    { x:"01/10", list:21000, sale:28000 },
    { x:"01/13", list:24000, sale:26000 },
    { x:"01/16", list:12000, sale:90000 },
    { x:"01/19", list:47000, sale:25000 },
    { x:"01/22", list:10000, sale:26000 },
    { x:"01/25", list:20000, sale:27000 },
    { x:"01/28", list:20000, sale:28000 },
    { x:"02/01", list:13000, sale:12000 },
    { x:"02/04", list:14000, sale:15000 },
    { x:"02/07", list:22000, sale:30000 },
    { x:"02/10", list:28000, sale:110000 },
    { x:"02/13", list:26000, sale:120000 },
    { x:"02/16", list:28000, sale:105000 },
  ];

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

  // 이미지 느낌으로 y범위 고정(원하면 자동범위로 바꿔도 됨)
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

    // dpr 반영(그리기 좌표는 CSS px 기준)
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

    const xCount = DATA.length;
    const xStep = innerW / Math.max(1, (xCount - 1));

    const yToPx = (y) => {
      const t = (y - Y_MIN) / (Y_MAX - Y_MIN);
      return pad.t + innerH - (t * innerH);
    };
    const xToPx = (i) => pad.l + i * xStep;

    // ===== y grid + label =====
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

    // ❌ 뒤 산포 동그라미(점) 없음

    // ===== series points =====
    const listPts = DATA.map((d,i)=>({ x:xToPx(i), y:yToPx(d.list), v:d.list, label:d.x, key:"등록가" }));
    const salePts = DATA.map((d,i)=>({ x:xToPx(i), y:yToPx(d.sale), v:d.sale, label:d.x, key:"판매가" }));

    const showList = (mode === "all" || mode === "list");
    const showSale = (mode === "all" || mode === "sale");

    // hover 판정
    hitPoints = [];
    if (showList) hitPoints.push(...listPts.map(p=>({ ...p, series:"list", color:COLORS.green })));
    if (showSale) hitPoints.push(...salePts.map(p=>({ ...p, series:"sale", color:COLORS.blue })));

    if (showList) drawSmoothLine(listPts, COLORS.green, 3);
    if (showSale) drawSmoothLine(salePts, COLORS.blue, 3);

    drawPoints(listPts, COLORS.green, showList);
    drawPoints(salePts, COLORS.blue, showSale);

    // x 라벨(양끝만)
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

    // 살짝 부드러운 느낌
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
      // 외곽(흰색)
      ctx.fillStyle = "#fff";
      ctx.beginPath();
      ctx.arc(p.x, p.y, 4.5, 0, Math.PI*2);
      ctx.fill();

      // 내부(색상)
      ctx.fillStyle = color;
      ctx.beginPath();
      ctx.arc(p.x, p.y, 3, 0, Math.PI*2);
      ctx.fill();
    }
    ctx.restore();
  }

  // ===== 상단 시세금액 표시 =====
  function updatePrice(){
    const last = DATA[DATA.length - 1];
    let v;
    if (mode === "list") v = last.list;
    else if (mode === "sale") v = last.sale;
    else v = Math.round((last.list + last.sale) / 2); // 전체는 중간값 느낌

    priceEl.innerHTML = `${fmtMoney(v)}<small>원</small>`;
    priceEl.style.color = (mode === "sale") ? COLORS.blue : COLORS.green;
  }

  // ===== tooltip =====
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

  // ===== tabs =====
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

  // ===== init =====
  updatePrice();
  window.addEventListener("resize", resizeCanvas);
  resizeCanvas();


})();


   
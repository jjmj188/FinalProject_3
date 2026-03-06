// ===============================
// 1) 가격 필터(그대로 유지)
// ===============================
(function(){
  const pillsWrap = document.getElementById("pricePills");
  const pills = pillsWrap ? Array.from(pillsWrap.querySelectorAll(".pill")) : [];
  const minEl = document.getElementById("priceMin");
  const maxEl = document.getElementById("priceMax");
  const rangeRow = document.querySelector(".range-row");

  if(!pillsWrap || !minEl || !maxEl || !rangeRow) return;

  function clearPillActive(){ pills.forEach(b => b.classList.remove("is-active")); }
  function setRangeActive(isActive){ rangeRow.classList.toggle("is-active", !!isActive); }
  function clearRangeValues(){ minEl.value = ""; maxEl.value = ""; setRangeActive(false); }

  pillsWrap.addEventListener("click", (e) => {
    const btn = e.target.closest(".pill");
    if(!btn) return;

    clearPillActive();
    btn.classList.add("is-active");
    clearRangeValues();

    const max = btn.dataset.max ? Number(btn.dataset.max) : null;
    if(max !== null && !Number.isNaN(max)){
      minEl.value = "";
      maxEl.value = max;
    }
  });

  function onRangeIntent(){ clearPillActive(); setRangeActive(true); }
  minEl.addEventListener("focus", onRangeIntent);
  maxEl.addEventListener("focus", onRangeIntent);
  minEl.addEventListener("input", onRangeIntent);
  maxEl.addEventListener("input", onRangeIntent);

  function syncRangeActive(){
    const hasAny = (minEl.value.trim() !== "") || (maxEl.value.trim() !== "");
    setRangeActive(hasAny);
  }
  minEl.addEventListener("blur", syncRangeActive);
  maxEl.addEventListener("blur", syncRangeActive);

  const resetBtn = document.querySelector(".filter-actions .btn-ghost");
  if(resetBtn){
    resetBtn.addEventListener("click", () => { clearPillActive(); clearRangeValues(); });
  }
})();


// ===============================
// 2) 지역 모달 + 카카오맵 + 최근 검색 기록(24h TTL)
// ===============================
(function(){
  const openBtn = document.querySelector(".btn-area");
  const modal = document.getElementById("areaModal");
  const list = document.getElementById("areaList"); // ✅ 최근 검색을 그릴 UL

  const useGeoBtn = document.getElementById("areaUseGeoBtn");     // 현재 위치
  const openSearchBtn = document.getElementById("areaSearchBtn"); // "검색어로" 버튼

  // areaModal 내부 지도/검색 UI
  const searchWrap = document.getElementById("areaSearchWrap");
  const mapEl = document.getElementById("areaKakaoMap");
  const kwInput = document.getElementById("areaKakaoKeyword");
  const kwBtn = document.getElementById("areaKakaoSearchBtn");
  const resultUl = document.getElementById("areaKakaoResult");

  if(!openBtn || !modal || !list) return;

  // -----------------------
  // ✅ 최근 검색 캐시 (localStorage + TTL 24h, 개수 제한 없음)
  // -----------------------
  const RECENT_AREA_KEY = "PF_RECENT_AREAS_V1";
  const RECENT_AREA_TTL_MS = 24 * 60 * 60 * 1000;

  function readRecentAreas(){
    try{
      const raw = localStorage.getItem(RECENT_AREA_KEY);
      if(!raw) return [];
      const arr = JSON.parse(raw);
      if(!Array.isArray(arr)) return [];

      const t = Date.now();
      const alive = arr.filter(x =>
        x &&
        typeof x.value === "string" &&
        x.value.trim() !== "" &&
        typeof x.savedAt === "number" &&
        (t - x.savedAt) <= RECENT_AREA_TTL_MS
      );

      if(alive.length !== arr.length){
        localStorage.setItem(RECENT_AREA_KEY, JSON.stringify(alive));
      }
      return alive;
    }catch{
      return [];
    }
  }

  function writeRecentAreas(arr){
    try{ localStorage.setItem(RECENT_AREA_KEY, JSON.stringify(arr)); }catch{}
  }

  function pushRecentArea(value){
    const v = String(value ?? "").trim();
    if(!v) return;

    const current = readRecentAreas();
    const filtered = current.filter(x => String(x.value).trim() !== v);
    filtered.unshift({ value: v, savedAt: Date.now() });
    writeRecentAreas(filtered);
  }

  function renderRecentAreaList(){
    // list(#areaList)가 ul 또는 그 안에 ul이 있을 수도 있어서 둘 다 대응
    const ul = list.tagName === "UL" ? list : list.querySelector("ul");
    if(!ul) return;

    const items = readRecentAreas();
    ul.innerHTML = "";

    if(items.length === 0){
      const li = document.createElement("li");
      li.style.padding = "10px 8px";
      li.style.opacity = ".7";
      li.textContent = "최근 검색 기록이 없어요.";
      ul.appendChild(li);
      return;
    }

    items.forEach(x => {
      const li = document.createElement("li");
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = "area-item";
      btn.setAttribute("data-area-value", x.value);
      btn.textContent = x.value;
      li.appendChild(btn);
      ul.appendChild(li);
    });
  }

  // -----------------------
  // 모달 open/close
  // -----------------------
  let lastFocusedEl = null;

  function openModal(){
    lastFocusedEl = document.activeElement;

    // 최근 검색 렌더
    renderRecentAreaList();

    // 검색 UI 기본 접힘
    if(searchWrap){
      searchWrap.classList.remove("is-open");
      searchWrap.setAttribute("aria-hidden", "true");
    }
    if(resultUl) resultUl.innerHTML = "";
    if(kwInput) kwInput.value = "";

    modal.classList.add("is-open");
    modal.setAttribute("aria-hidden", "false");
    document.body.style.overflow = "hidden";
  }

  function closeModal(){
    modal.classList.remove("is-open");
    modal.setAttribute("aria-hidden", "true");
    document.body.style.overflow = "";
    if(lastFocusedEl) lastFocusedEl.focus();
  }

  openBtn.addEventListener("click", openModal);

  modal.addEventListener("click", (e) => {
    if(e.target.closest("[data-area-close='true']")) closeModal();

    // 오버레이 클릭 닫기(패널 바깥)
    const panel = modal.querySelector(".area-modal__panel");
    if(panel && !panel.contains(e.target)) closeModal();
  });

  window.addEventListener("keydown", (e) => {
    if(e.key === "Escape" && modal.classList.contains("is-open")) closeModal();
  });

  // -----------------------
  // 선택값 표시(버튼 span)
  // -----------------------
  function setAreaLabel(placeName, fullAddress){
    const labelSpan = openBtn.querySelector("span");
    if(!labelSpan) return;

    const pn = String(placeName || "").trim();
    const fa = String(fullAddress || "").trim();
    const show = pn || fa || "";

    labelSpan.innerHTML = `<span class="area-selected">${escapeHtml(show)}</span>`;
  }

  function escapeHtml(s){
    return String(s)
      .replaceAll("&","&amp;")
      .replaceAll("<","&lt;")
      .replaceAll(">","&gt;")
      .replaceAll('"',"&quot;")
      .replaceAll("'","&#39;");
  }

  // -----------------------
  // "place / address" 분리
  // -----------------------
  function splitPlaceAndAddress(raw){
    const v = String(raw ?? "").trim();
    if(!v) return { placeName:"", fullAddress:"" };

    if(v.includes("/")){
      const [a,b] = v.split("/");
      return { placeName:(a||"").trim(), fullAddress:(b||"").trim() };
    }
    if(v.includes("·")){
      const [a,b] = v.split("·");
      return { placeName:(a||"").trim(), fullAddress:(b||"").trim() };
    }
    return { placeName:"", fullAddress:v };
  }

  // =========================================================
  // Kakao helpers
  // =========================================================
  function ensureKakaoServices(){
    return !!(window.kakao && kakao.maps && kakao.maps.services);
  }

  function waitForKakaoServices(cb, tries = 60){
    if(ensureKakaoServices()) return cb();
    if(tries <= 0){
      alert("카카오맵 로드에 실패했어요. appkey/도메인 등록과 libraries=services를 확인해 주세요.");
      return;
    }
    setTimeout(() => waitForKakaoServices(cb, tries - 1), 100);
  }

  function latLngToFullAddress(lat, lng, cb){
    if(!ensureKakaoServices()) return cb("");
    const geocoder = new kakao.maps.services.Geocoder();
    geocoder.coord2Address(lng, lat, (result, status) => {
      if(status === kakao.maps.services.Status.OK){
        const jibun = result[0].address ? result[0].address.address_name : "";
        const road  = result[0].road_address ? result[0].road_address.address_name : "";
        cb(road || jibun);
      }else cb("");
    });
  }

  // =========================================================
  // Kakao Map core (검색 + 지도 클릭)
  // =========================================================
  let map = null;
  let places = null;
  let markers = [];
  let pickMarker = null;

  function clearMarkers(){
    markers.forEach(m => m.setMap(null));
    markers = [];
  }

  function ensureMap(){
    if(map || !mapEl) return;

    map = new kakao.maps.Map(mapEl, {
      center: new kakao.maps.LatLng(37.5665, 126.9780),
      level: 4
    });

    places = new kakao.maps.services.Places();
    pickMarker = new kakao.maps.Marker();

    // ✅ 지도 클릭 선택 → 주소로 저장/표시
    kakao.maps.event.addListener(map, "click", function(mouseEvent){
      const latlng = mouseEvent.latLng;
      const lat = latlng.getLat();
      const lng = latlng.getLng();

      pickMarker.setPosition(latlng);
      pickMarker.setMap(map);

      latLngToFullAddress(lat, lng, (fullAddr) => {
        const fa = String(fullAddr || "").trim();
        if(!fa){
          alert("주소를 가져오지 못했어요. 다른 지점을 눌러주세요.");
          return;
        }

        pushRecentArea(fa);
        renderRecentAreaList();
        setAreaLabel("", fa);
        closeModal();
      });
    });
  }

  function renderSearchResults(items){
    if(!resultUl) return;
    resultUl.innerHTML = "";

    if(!items || items.length === 0){
      const li = document.createElement("li");
      li.style.padding = "10px 8px";
      li.style.opacity = ".7";
      li.textContent = "검색 결과가 없어요.";
      resultUl.appendChild(li);
      return;
    }

    items.forEach((p) => {
      const li = document.createElement("li");
      const btn = document.createElement("button");
      btn.type = "button";

      const placeName = (p.place_name || "").trim();
      const fullAddress = (p.road_address_name || p.address_name || "").trim();
      btn.textContent = `${placeName}${fullAddress ? " · " + fullAddress : ""}`;

      btn.addEventListener("click", () => {
        const lat = Number(p.y);
        const lng = Number(p.x);

        waitForKakaoServices(() => {
          ensureMap();
          const latlng = new kakao.maps.LatLng(lat, lng);
          map.setCenter(latlng);
          map.setLevel(3);
          pickMarker.setPosition(latlng);
          pickMarker.setMap(map);
        });

        // ✅ 최근 검색 저장 형태: "place / address"
        const recentText = `${placeName}${fullAddress ? " / " + fullAddress : ""}`.trim();
        pushRecentArea(recentText);
        renderRecentAreaList();

        setAreaLabel(placeName, fullAddress);
        closeModal();
      });

      li.appendChild(btn);
      resultUl.appendChild(li);
    });
  }

  function searchKeyword(){
    const keyword = String(kwInput?.value ?? "").trim();
    if(!keyword){
      alert("검색어를 입력해 주세요.");
      kwInput?.focus?.();
      return;
    }

    waitForKakaoServices(() => {
      ensureMap();

      places.keywordSearch(keyword, (data, status) => {
        if(status !== kakao.maps.services.Status.OK){
          renderSearchResults([]);
          return;
        }

        clearMarkers();
        const bounds = new kakao.maps.LatLngBounds();

        data.forEach((p) => {
          const lat = Number(p.y);
          const lng = Number(p.x);
          const latlng = new kakao.maps.LatLng(lat, lng);

          const m = new kakao.maps.Marker({ position: latlng });
          m.setMap(map);
          markers.push(m);
          bounds.extend(latlng);

          // 마커 클릭 선택도 최근검색 저장
          kakao.maps.event.addListener(m, "click", () => {
            const placeName = (p.place_name || "").trim();
            const fullAddress = (p.road_address_name || p.address_name || "").trim();

            const recentText = `${placeName}${fullAddress ? " / " + fullAddress : ""}`.trim();
            pushRecentArea(recentText);
            renderRecentAreaList();

            setAreaLabel(placeName, fullAddress);
            closeModal();
          });
        });

        map.setBounds(bounds);
        renderSearchResults(data);
      });
    });
  }

  function openSearchUI(){
    if(!searchWrap) return;

    searchWrap.classList.add("is-open");
    searchWrap.setAttribute("aria-hidden", "false");

    waitForKakaoServices(() => {
      ensureMap();
      setTimeout(() => {
        try { kakao.maps.event.trigger(map, "resize"); } catch {}
      }, 0);
    });

    kwInput?.focus?.();
  }

  function setMyLocation(){
    if(!navigator.geolocation){
      alert("이 브라우저에서는 위치 기능을 사용할 수 없어요.");
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const lat = pos.coords.latitude;
        const lng = pos.coords.longitude;

        waitForKakaoServices(() => {
          latLngToFullAddress(lat, lng, (fullAddr) => {
            const fa = (fullAddr || "현재 위치").trim();

            pushRecentArea(fa);
            renderRecentAreaList();

            setAreaLabel("", fa);
            closeModal();
          });
        });
      },
      () => alert("위치 권한이 거부되었거나 위치를 가져오지 못했어요."),
      { enableHighAccuracy: true, timeout: 8000 }
    );
  }

  // -----------------------
  // 최근 검색 클릭
  // - "place / address"면 placeName만 버튼 라벨에 보여줌(원하면 둘 다 표시로 바꿀 수 있음)
  // -----------------------
  list.addEventListener("click", (e) => {
    const btn = e.target.closest(".area-item");
    if(!btn) return;

    const raw = btn.getAttribute("data-area-value") || btn.textContent.trim();

    // 다시 맨 위로 올려서 "최근" 갱신 효과
    pushRecentArea(raw);
    renderRecentAreaList();

    const sp = splitPlaceAndAddress(raw);
    // placeName 있으면 placeName만 보여주고, 없으면 fullAddress
    setAreaLabel(sp.placeName || "", sp.placeName ? "" : sp.fullAddress);

    closeModal();
  });

  // "검색어로" → 지도/검색 UI 펼치기
  openSearchBtn?.addEventListener("click", openSearchUI);

  // 키워드 검색
  kwBtn?.addEventListener("click", searchKeyword);
  kwInput?.addEventListener("keydown", (e) => {
    if(e.key === "Enter"){ e.preventDefault(); searchKeyword(); }
  });

  // 현재 위치
  useGeoBtn?.addEventListener("click", setMyLocation);

})();


// ===============================
// 3) time-ago (그대로 유지)
// ===============================
document.addEventListener("DOMContentLoaded", function () {
  function formatTimeAgo(dateString) {
    if (!dateString) return "";

    const now = new Date();
    const past = new Date(dateString);
    const diff = Math.floor((now - past) / 1000);

    if (diff < 60) return diff + "초 전";

    const minutes = Math.floor(diff / 60);
    if (minutes < 60) return minutes + "분 전";

    const hours = Math.floor(minutes / 60);
    if (hours < 24) return hours + "시간 전";

    const days = Math.floor(hours / 24);
    return days + "일 전";
  }

  const times = document.querySelectorAll(".time-ago");
  times.forEach(el => {
    const dateString = el.dataset.time;
    el.textContent = formatTimeAgo(dateString);
  });
});
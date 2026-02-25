
(function(){
  const pillsWrap = document.getElementById("pricePills");
  const pills = pillsWrap ? Array.from(pillsWrap.querySelectorAll(".pill")) : [];
  const minEl = document.getElementById("priceMin");
  const maxEl = document.getElementById("priceMax");
  const rangeRow = document.querySelector(".range-row");

  if(!pillsWrap || !minEl || !maxEl || !rangeRow) return;

  function clearPillActive(){
    pills.forEach(b => b.classList.remove("is-active"));
  }

  function setRangeActive(isActive){
    rangeRow.classList.toggle("is-active", !!isActive);
  }

  function clearRangeValues(){
    minEl.value = "";
    maxEl.value = "";
    setRangeActive(false);
  }

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

  function onRangeIntent(){
    clearPillActive();
    setRangeActive(true);
  }

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
    resetBtn.addEventListener("click", () => {
      clearPillActive();
      clearRangeValues();
    });
  }
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
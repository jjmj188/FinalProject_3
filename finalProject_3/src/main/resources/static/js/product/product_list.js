// ===============================
// 1) 가격 필터
// ===============================
(function () {
  const pillsWrap = document.getElementById("pricePills");
  const pills = pillsWrap ? Array.from(pillsWrap.querySelectorAll(".pill")) : [];
  const minEl = document.getElementById("priceMin");
  const maxEl = document.getElementById("priceMax");
  const rangeRow = document.querySelector(".range-row");

  if (!pillsWrap || !minEl || !maxEl || !rangeRow) return;

  function clearPillActive() {
    pills.forEach((b) => b.classList.remove("is-active"));
  }

  function setRangeActive(isActive) {
    rangeRow.classList.toggle("is-active", !!isActive);
  }

  function clearRangeValues() {
    minEl.value = "";
    maxEl.value = "";
    setRangeActive(false);
  }

  pillsWrap.addEventListener("click", (e) => {
    const btn = e.target.closest(".pill");
    if (!btn) return;

    clearPillActive();
    btn.classList.add("is-active");
    clearRangeValues();

    const max = btn.dataset.max ? Number(btn.dataset.max) : null;
    if (max !== null && !Number.isNaN(max)) {
      minEl.value = "";
      maxEl.value = max;
    }
  });

  function onRangeIntent() {
    clearPillActive();
    setRangeActive(true);
  }

  minEl.addEventListener("focus", onRangeIntent);
  maxEl.addEventListener("focus", onRangeIntent);
  minEl.addEventListener("input", onRangeIntent);
  maxEl.addEventListener("input", onRangeIntent);

  function syncRangeActive() {
    const hasAny = minEl.value.trim() !== "" || maxEl.value.trim() !== "";
    setRangeActive(hasAny);
  }

  minEl.addEventListener("blur", syncRangeActive);
  maxEl.addEventListener("blur", syncRangeActive);

  const resetBtn = document.querySelector(".filter-actions .btn-ghost");
  if (resetBtn) {
    resetBtn.addEventListener("click", () => {
      clearPillActive();
      clearRangeValues();
    });
  }
})();


// ===============================
// 2) 지역 모달 + 카카오맵 + 최근 검색 기록
//    ✅ 동/읍/면만 표시
// ===============================
(function () {
  const openBtn = document.querySelector(".btn-area");
  const modal = document.getElementById("areaModal");
  const list = document.getElementById("areaList");

  const useGeoBtn = document.getElementById("areaUseGeoBtn");
  const openSearchBtn = document.getElementById("areaSearchBtn");

  const searchWrap = document.getElementById("areaSearchWrap");
  const mapEl = document.getElementById("areaKakaoMap");
  const kwInput = document.getElementById("areaKakaoKeyword");
  const kwBtn = document.getElementById("areaKakaoSearchBtn");
  const resultUl = document.getElementById("areaKakaoResult");

  if (!openBtn || !modal || !list) return;

  const RECENT_AREA_KEY = "PF_RECENT_DONG_AREAS_V1";
  const RECENT_AREA_TTL_MS = 24 * 60 * 60 * 1000;

  let lastFocusedEl = null;
  let map = null;
  let places = null;
  let markers = [];
  let pickMarker = null;

  // -----------------------
  // 공통
  // -----------------------
  function escapeHtml(s) {
    return String(s)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#39;");
  }

  function setAreaLabel(regionName) {
    const labelSpan = openBtn.querySelector("span");
    if (!labelSpan) return;

    const show = String(regionName || "").trim() || "지역설정";
    labelSpan.innerHTML = `<span class="area-selected">${escapeHtml(show)}</span>`;
  }

  function extractDongName(address) {
    const text = String(address || "").trim();
    if (!text) return "";

    const parts = text.split(/\s+/);

    for (let i = parts.length - 1; i >= 0; i--) {
      const word = parts[i].trim();

      if (
        word.endsWith("동") ||
        word.endsWith("읍") ||
        word.endsWith("면")
      ) {
        return word;
      }
    }

    return "";
  }

  // 도로명주소에서 동/읍/면이 안 잡히는 경우 지번주소 fallback용
  function pickDongName(roadAddress, jibunAddress) {
    const roadDong = extractDongName(roadAddress);
    if (roadDong) return roadDong;

    const jibunDong = extractDongName(jibunAddress);
    if (jibunDong) return jibunDong;

    return "";
  }

  // -----------------------
  // 최근 검색 캐시
  // -----------------------
  function readRecentAreas() {
    try {
      const raw = localStorage.getItem(RECENT_AREA_KEY);
      if (!raw) return [];

      const arr = JSON.parse(raw);
      if (!Array.isArray(arr)) return [];

      const now = Date.now();

      const alive = arr.filter((x) => {
        return (
          x &&
          typeof x.value === "string" &&
          x.value.trim() !== "" &&
          typeof x.savedAt === "number" &&
          now - x.savedAt <= RECENT_AREA_TTL_MS
        );
      });

      if (alive.length !== arr.length) {
        localStorage.setItem(RECENT_AREA_KEY, JSON.stringify(alive));
      }

      return alive;
    } catch {
      return [];
    }
  }

  function writeRecentAreas(arr) {
    try {
      localStorage.setItem(RECENT_AREA_KEY, JSON.stringify(arr));
    } catch {}
  }

  function pushRecentArea(value) {
    const v = String(value || "").trim();
    if (!v) return;

    const current = readRecentAreas();
    const filtered = current.filter((x) => String(x.value).trim() !== v);
    filtered.unshift({
      value: v,
      savedAt: Date.now()
    });

    writeRecentAreas(filtered);
  }

  function renderRecentAreaList() {
    const ul = list.tagName === "UL" ? list : list.querySelector("ul");
    if (!ul) return;

    const items = readRecentAreas();
    ul.innerHTML = "";

    if (items.length === 0) {
      const li = document.createElement("li");
      li.style.padding = "10px 8px";
      li.style.opacity = ".7";
      li.textContent = "최근 검색 기록이 없어요.";
      ul.appendChild(li);
      return;
    }

    items.forEach((x) => {
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
  // 모달
  // -----------------------
  function openModal() {
    lastFocusedEl = document.activeElement;

    renderRecentAreaList();

    if (searchWrap) {
      searchWrap.classList.remove("is-open");
      searchWrap.setAttribute("aria-hidden", "true");
    }

    if (resultUl) resultUl.innerHTML = "";
    if (kwInput) kwInput.value = "";

    modal.classList.add("is-open");
    modal.setAttribute("aria-hidden", "false");
    document.body.style.overflow = "hidden";
  }

  function closeModal() {
    modal.classList.remove("is-open");
    modal.setAttribute("aria-hidden", "true");
    document.body.style.overflow = "";

    if (lastFocusedEl) {
      lastFocusedEl.focus();
    }
  }

  openBtn.addEventListener("click", openModal);

  modal.addEventListener("click", (e) => {
    if (e.target.closest("[data-area-close='true']")) {
      closeModal();
      return;
    }

    const panel = modal.querySelector(".area-modal__panel");
    if (panel && !panel.contains(e.target)) {
      closeModal();
    }
  });

  window.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && modal.classList.contains("is-open")) {
      closeModal();
    }
  });

  // -----------------------
  // Kakao helpers
  // -----------------------
  function ensureKakaoServices() {
    return !!(window.kakao && kakao.maps && kakao.maps.services);
  }

  function waitForKakaoServices(cb, tries = 60) {
    if (ensureKakaoServices()) return cb();

    if (tries <= 0) {
      alert("카카오맵 로드에 실패했어요. appkey와 libraries=services를 확인해 주세요.");
      return;
    }

    setTimeout(() => waitForKakaoServices(cb, tries - 1), 100);
  }

  function coordToAddressInfo(lat, lng, cb) {
    if (!ensureKakaoServices()) {
      cb({ roadAddress: "", jibunAddress: "" });
      return;
    }

    const geocoder = new kakao.maps.services.Geocoder();
    geocoder.coord2Address(lng, lat, (result, status) => {
      if (status === kakao.maps.services.Status.OK) {
        const roadAddress = result[0].road_address ? result[0].road_address.address_name : "";
        const jibunAddress = result[0].address ? result[0].address.address_name : "";

        cb({
          roadAddress,
          jibunAddress
        });
      } else {
        cb({
          roadAddress: "",
          jibunAddress: ""
        });
      }
    });
  }

  // -----------------------
  // 지도
  // -----------------------
  function clearMarkers() {
    markers.forEach((m) => m.setMap(null));
    markers = [];
  }

  function ensureMap() {
    if (map || !mapEl) return;

    map = new kakao.maps.Map(mapEl, {
      center: new kakao.maps.LatLng(37.5665, 126.9780),
      level: 4
    });

    places = new kakao.maps.services.Places();
    pickMarker = new kakao.maps.Marker();

    kakao.maps.event.addListener(map, "click", function (mouseEvent) {
      const latlng = mouseEvent.latLng;
      const lat = latlng.getLat();
      const lng = latlng.getLng();

      pickMarker.setPosition(latlng);
      pickMarker.setMap(map);

      coordToAddressInfo(lat, lng, ({ roadAddress, jibunAddress }) => {
        const dongName = pickDongName(roadAddress, jibunAddress);

        if (!dongName) {
          alert("동네 정보를 찾지 못했어요. 다른 지점을 눌러주세요.");
          return;
        }

        pushRecentArea(dongName);
        renderRecentAreaList();
        setAreaLabel(dongName);
        closeModal();
      });
    });
  }

  function renderSearchResults(items) {
    if (!resultUl) return;

    resultUl.innerHTML = "";

    if (!items || items.length === 0) {
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

      const roadAddress = (p.road_address_name || "").trim();
      const jibunAddress = (p.address_name || "").trim();
      const dongName = pickDongName(roadAddress, jibunAddress);

      btn.textContent = dongName || jibunAddress || roadAddress || "선택";

      btn.addEventListener("click", () => {
        const lat = Number(p.y);
        const lng = Number(p.x);

        if (!dongName) {
          alert("동네 정보를 찾지 못했어요. 다른 위치를 선택해 주세요.");
          return;
        }

        waitForKakaoServices(() => {
          ensureMap();
          const latlng = new kakao.maps.LatLng(lat, lng);
          map.setCenter(latlng);
          map.setLevel(3);
          pickMarker.setPosition(latlng);
          pickMarker.setMap(map);
        });

        pushRecentArea(dongName);
        renderRecentAreaList();
        setAreaLabel(dongName);
        closeModal();
      });

      li.appendChild(btn);
      resultUl.appendChild(li);
    });
  }

  function searchKeyword() {
    const keyword = String(kwInput?.value || "").trim();

    if (!keyword) {
      alert("검색어를 입력해 주세요.");
      kwInput?.focus();
      return;
    }

    waitForKakaoServices(() => {
      ensureMap();

      places.keywordSearch(keyword, (data, status) => {
        if (status !== kakao.maps.services.Status.OK) {
          renderSearchResults([]);
          return;
        }

        clearMarkers();

        const bounds = new kakao.maps.LatLngBounds();

        data.forEach((p) => {
          const lat = Number(p.y);
          const lng = Number(p.x);
          const latlng = new kakao.maps.LatLng(lat, lng);

          const roadAddress = (p.road_address_name || "").trim();
          const jibunAddress = (p.address_name || "").trim();
          const dongName = pickDongName(roadAddress, jibunAddress);

          const marker = new kakao.maps.Marker({
            position: latlng
          });

          marker.setMap(map);
          markers.push(marker);
          bounds.extend(latlng);

          kakao.maps.event.addListener(marker, "click", () => {
            if (!dongName) {
              alert("동네 정보를 찾지 못했어요. 다른 위치를 선택해 주세요.");
              return;
            }

            pickMarker.setPosition(latlng);
            pickMarker.setMap(map);

            pushRecentArea(dongName);
            renderRecentAreaList();
            setAreaLabel(dongName);
            closeModal();
          });
        });

        map.setBounds(bounds);
        renderSearchResults(data);
      });
    });
  }

  function openSearchUI() {
    if (!searchWrap) return;

    searchWrap.classList.add("is-open");
    searchWrap.setAttribute("aria-hidden", "false");

    waitForKakaoServices(() => {
      ensureMap();

      setTimeout(() => {
        try {
          kakao.maps.event.trigger(map, "resize");
        } catch {}
      }, 0);
    });

    kwInput?.focus();
  }

  function setMyLocation() {
    if (!navigator.geolocation) {
      alert("이 브라우저에서는 위치 기능을 사용할 수 없어요.");
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const lat = pos.coords.latitude;
        const lng = pos.coords.longitude;

        waitForKakaoServices(() => {
          coordToAddressInfo(lat, lng, ({ roadAddress, jibunAddress }) => {
            const dongName = pickDongName(roadAddress, jibunAddress);

            if (!dongName) {
              alert("현재 위치의 동네 정보를 찾지 못했어요.");
              return;
            }

            pushRecentArea(dongName);
            renderRecentAreaList();
            setAreaLabel(dongName);
            closeModal();
          });
        });
      },
      () => {
        alert("위치 권한이 거부되었거나 위치를 가져오지 못했어요.");
      },
      {
        enableHighAccuracy: true,
        timeout: 8000
      }
    );
  }

  // -----------------------
  // 최근 검색 클릭
  // -----------------------
  list.addEventListener("click", (e) => {
    const btn = e.target.closest(".area-item");
    if (!btn) return;

    const raw = btn.getAttribute("data-area-value") || btn.textContent.trim();
    const dongName = String(raw || "").trim();

    if (!dongName) return;

    pushRecentArea(dongName);
    renderRecentAreaList();
    setAreaLabel(dongName);
    closeModal();
  });

  openSearchBtn?.addEventListener("click", openSearchUI);
  kwBtn?.addEventListener("click", searchKeyword);

  kwInput?.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      searchKeyword();
    }
  });

  useGeoBtn?.addEventListener("click", setMyLocation);
})();


// ===============================
// 3) time-ago
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

  times.forEach((el) => {
    const dateString = el.dataset.time;
    el.textContent = formatTimeAgo(dateString);
  });
});
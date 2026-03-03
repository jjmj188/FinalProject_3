/* =========================================================
   product_form.js (등록 제외 / 모듈만 제공) + 거래제한 모달 + 택배 UI + 직거래 위치(1~3)
========================================================= */

(() => {
  if (window.__PF_FORM_BOUND__ === true) return;
  window.__PF_FORM_BOUND__ = true;

  const $ = (sel, el = document) => el.querySelector(sel);
  const $$ = (sel, el = document) => Array.from(el.querySelectorAll(sel));

  const wrap = $(".pf-wrap");
  if (!wrap) return;

  window.PF = window.PF || {};

  function parseNumber(raw) {
    const n = String(raw ?? "").replace(/[^\d]/g, "");
    return n ? Number(n) : null;
  }

  function safeJsonParse(str, fallback) {
    try { return JSON.parse(str); } catch { return fallback; }
  }

  // =========================================================
  // 0) 거래 제한 품목 안내 모달
  // =========================================================
  (() => {
    const link = $("#pfShipSettingLink");
    const modal = $("#pfShipModal");
    const closeBtn = $("#pfShipModalClose");
    if (!link || !modal) return;

    modal.style.display = "none";
    modal.classList.remove("is-open");
    modal.setAttribute("aria-hidden", "true");

    function openModal() {
      modal.classList.add("is-open");
      modal.style.display = "flex";
      modal.setAttribute("aria-hidden", "false");
      document.body.style.overflow = "hidden";
    }

    function closeModal() {
      modal.classList.remove("is-open");
      modal.style.display = "none";
      modal.setAttribute("aria-hidden", "true");
      document.body.style.overflow = "";
    }

    link.addEventListener("click", (e) => { e.preventDefault(); openModal(); });
    if (closeBtn) closeBtn.addEventListener("click", (e) => { e.preventDefault(); closeModal(); });

    modal.addEventListener("click", (e) => {
      const content = modal.querySelector(".pf-modal-content");
      if (!content) return;
      if (!content.contains(e.target)) closeModal();
    });

    window.addEventListener("keydown", (e) => {
      if (e.key === "Escape" && modal.classList.contains("is-open")) closeModal();
    });

    window.PF.RestrictionModal = { open: openModal, close: closeModal };
  })();

  // =========================================================
  // 1) 탭 underline
  // =========================================================
  (() => {
    const tabs = $(".pf-tabs");
    const underline = $(".pf-tab-underline");
    const activeKey = wrap.dataset.active;

    function setActiveTab() {
      const all = $$(".pf-tab");
      const active = all.find(a => a.dataset.tab === activeKey) || all[0];
      all.forEach(a => a.classList.toggle("is-active", a === active));
      if (!tabs || !underline || !active) return;

      const tabsRect = tabs.getBoundingClientRect();
      const rect = active.getBoundingClientRect();
      underline.style.width = rect.width + "px";
      underline.style.transform = `translateX(${rect.left - tabsRect.left}px)`;
    }

    setActiveTab();
    window.addEventListener("resize", setActiveTab);
  })();

  // =========================================================
  // 2) 이미지(1~3) + 대표(mainIndex)
  // =========================================================
  const ImageModule = (() => {
    const fileInput = $("#pfImageInput");
    const previewWrap = $("#pfPreviewWrap") || $(".pf-preview");
    const mainHidden = $("#pfMainIndex");
    const MAX_FILES = 3;

    if (!fileInput || !previewWrap || !mainHidden) return null;

    let selectedFiles = [];
    let mainIndex = 0;

    function clampMain() {
      if (selectedFiles.length === 0) { mainIndex = 0; return; }
      if (mainIndex >= selectedFiles.length) mainIndex = 0;
      if (mainIndex < 0) mainIndex = 0;
    }

    function setMain(idx) {
      mainIndex = idx;
      clampMain();
      render();
    }

    function render() {
      previewWrap.innerHTML = "";
      previewWrap.classList.toggle("is-on", selectedFiles.length > 0);
      mainHidden.value = String(mainIndex);

      selectedFiles.forEach((file, idx) => {
        const url = URL.createObjectURL(file);

        const item = document.createElement("div");
        item.className = "pf-preview-item" + (idx === mainIndex ? " is-main" : "");

        const img = document.createElement("img");
        img.className = "pf-preview-img";
        img.src = url;
        img.alt = file.name || "업로드 이미지";
        img.addEventListener("click", () => setMain(idx));

        const badge = document.createElement("label");
        badge.className = "pf-main-badge";
        badge.innerHTML = `
          <input type="radio" name="pfMainImagePick" class="pf-main-radio" ${idx === mainIndex ? "checked" : ""}>
          <span class="pf-main-text">대표</span>
        `;
        badge.querySelector("input").addEventListener("change", () => setMain(idx));

        const del = document.createElement("button");
        del.type = "button";
        del.className = "pf-preview-remove";
        del.textContent = "×";
        del.setAttribute("aria-label", "이미지 삭제");
        del.addEventListener("click", () => {
          URL.revokeObjectURL(url);

          selectedFiles = selectedFiles.filter((_, i) => i !== idx);
          if (idx === mainIndex) mainIndex = 0;
          else if (idx < mainIndex) mainIndex -= 1;

          clampMain();
          render();
        });

        item.appendChild(img);
        item.appendChild(badge);
        item.appendChild(del);
        previewWrap.appendChild(item);
      });
    }

    fileInput.addEventListener("change", (e) => {
      const files = Array.from(e.target.files || []);
      if (files.length === 0) return;

      if (selectedFiles.length >= MAX_FILES) {
        alert("이미지는 최대 3장까지 등록할 수 있어요.");
        fileInput.value = "";
        return;
      }

      const remain = MAX_FILES - selectedFiles.length;
      selectedFiles = selectedFiles.concat(files.slice(0, remain));

      clampMain();
      render();

      // ✅ picker만 비움 (selectedFiles 유지)
      fileInput.value = "";
    });

    render();

    return {
      validate() {
        if (selectedFiles.length < 1) return { ok: false, msg: "상품 이미지는 최소 1장 등록해야 합니다." };
        if (selectedFiles.length > 3) return { ok: false, msg: "상품 이미지는 최대 3장까지 등록할 수 있습니다." };
        if (Number(mainHidden.value) >= selectedFiles.length) mainHidden.value = "0";
        return { ok: true };
      },
      appendToFormData(fd) {
        // ✅ 중복 방지: 기존 images 제거 후 selectedFiles로만 구성
        fd.delete("images");
        selectedFiles.forEach(f => fd.append("images", f));
      }
    };
  })();

  window.PF.Image = ImageModule;

  // =========================================================
  // 3) textarea 글자수
  // =========================================================
  (() => {
    const ta = $(".pf-textarea");
    const now = $(".pf-count-now");
    if (!ta || !now) return;
    const update = () => { now.textContent = String(ta.value.length); };
    ta.addEventListener("input", update);
    update();
  })();

  // =========================================================
  // 4) 택배(배송비 포함/별도) + shippingOptionsJson
  // =========================================================
  const ShippingModule = (() => {
    const shipToggle = $("#pfShipToggle");
    const meetToggle = $("#pfMeetToggle");

    const shipCard = $("#pfShipCard");
    const feeIncluded = $("#pfFeeIncluded");
    const feeSeparate = $("#pfFeeSeparate");
    const feeDetail = $("#pfShipFeeDetail");

    const shipOptionsHidden = $("#pfShipOptionsJson");

    const optChecks = $$(".pf-ship-opt");
    const feeInputs = $$("[data-fee-for]");

    if (!shipToggle || !meetToggle || !shipOptionsHidden) return null;

    function syncOne(type) {
      const chk = optChecks.find(x => x.dataset.type === type);
      const inp = $(`[data-fee-for="${type}"]`);
      if (!chk || !inp) return;

      inp.disabled = !chk.checked;
      if (!chk.checked) inp.value = "";
    }

    function syncShipOptionsJson() {
      if (!shipToggle.checked) {
        shipOptionsHidden.value = "[]";
        return [];
      }

      if (feeIncluded && feeIncluded.checked) {
        const payload = [{ parcelType: "무료배송", shippingFee: 0 }];
        shipOptionsHidden.value = JSON.stringify(payload);
        return payload;
      }

      const selected = [];
      optChecks.forEach(chk => {
        if (!chk.checked) return;
        const type = chk.dataset.type;
        const inp = $(`[data-fee-for="${type}"]`);
        const fee = inp ? parseNumber(inp.value) : null;
        selected.push({ parcelType: type, shippingFee: fee });
      });

      shipOptionsHidden.value = JSON.stringify(selected);
      return selected;
    }

    function setFeeDetailUI() {
      const isShip = shipToggle.checked;
      const isSeparate = !!feeSeparate?.checked;

      if (feeDetail) {
        feeDetail.classList.toggle("is-open", isShip && isSeparate);
        feeDetail.setAttribute("aria-hidden", (isShip && isSeparate) ? "false" : "true");
      }

      if (feeIncluded && feeIncluded.checked) {
        optChecks.forEach(chk => chk.checked = false);
        feeInputs.forEach(inp => { inp.value = ""; inp.disabled = true; });
      }

      syncShipOptionsJson();
    }

    function setTradeUI() {
      const isShip = shipToggle.checked;
      const isMeet = meetToggle.checked;

      if (shipCard) shipCard.classList.toggle("is-open", isShip);

      const locBtn = $("#pfLocationBtn");
      const chips = $("#pfLocChips");
      if (locBtn) locBtn.style.display = isMeet ? "" : "none";
      if (chips) chips.style.display = isMeet ? "" : "none";

      if (!isShip) {
        shipOptionsHidden.value = "[]";
        if (feeDetail) feeDetail.classList.remove("is-open");
        feeInputs.forEach(inp => { inp.value = ""; inp.disabled = true; });
        optChecks.forEach(chk => chk.checked = false);
      } else {
        setFeeDetailUI();
      }
    }

    if (feeIncluded) feeIncluded.addEventListener("change", setFeeDetailUI);
    if (feeSeparate) feeSeparate.addEventListener("change", setFeeDetailUI);

    optChecks.forEach(chk => {
      chk.addEventListener("change", () => {
        syncOne(chk.dataset.type);
        syncShipOptionsJson();
      });
    });

    feeInputs.forEach(inp => inp.addEventListener("input", syncShipOptionsJson));

    shipToggle.addEventListener("change", setTradeUI);
    meetToggle.addEventListener("change", setTradeUI);

    optChecks.forEach(chk => syncOne(chk.dataset.type));
    setTradeUI();
    setFeeDetailUI();

    return {
      syncShipOptionsJson,
      validate() {
        if (!shipToggle.checked) return { ok: true };

        if (feeIncluded && feeIncluded.checked) return { ok: true };

        const opts = syncShipOptionsJson();
        if (opts.length === 0) return { ok: false, msg: "배송비 별도를 선택했다면 배송 옵션을 1개 이상 선택해 주세요." };

        const missing = opts.find(x => x.shippingFee === null);
        if (missing) return { ok: false, msg: `"${missing.parcelType}" 배송비를 입력해 주세요.` };

        return { ok: true };
      }
    };
  })();

  window.PF.Shipping = ShippingModule;

  // =========================================================
  // 5) 직거래 위치 1~3 -> meetLocationsJson
  // =========================================================
  const MeetLocationModule = (() => {
    const openBtn = $("#pfLocationBtn");
    const modal = $("#areaModal");
    const list = $("#areaList");
    const useGeoBtn = $("#areaUseGeoBtn");
    const searchBtn = $("#areaSearchBtn");
    const chipsWrap = $("#pfLocChips");
    const hidden = $("#pfMeetLocations");

    if (!openBtn || !modal || !list || !chipsWrap || !hidden) return null;

    const MAX_LOC = 3;
    let lastFocusedEl = null;

    function load() {
      const v = safeJsonParse(hidden.value || "[]", []);
      return Array.isArray(v) ? v : [];
    }
    function save(arr) { hidden.value = JSON.stringify(arr); }

    function isDup(arr, fullAddress) {
      const key = (fullAddress || "").trim();
      return arr.some(x => (x.fullAddress || "").trim() === key);
    }

    function shortText(addr) {
      if (!addr) return "";
      return addr.length > 28 ? addr.slice(0, 28) + "…" : addr;
    }

    function renderChips() {
      const arr = load();
      chipsWrap.innerHTML = "";

      arr.forEach((loc, idx) => {
        const chip = document.createElement("div");
        chip.className = "pf-loc-chip";

        const text = document.createElement("span");
        text.className = "pf-loc-chip__text";
        text.title = loc.fullAddress || "";
        text.textContent = shortText(loc.fullAddress);

        const del = document.createElement("button");
        del.type = "button";
        del.className = "pf-loc-chip__del";
        del.setAttribute("aria-label", "삭제");
        del.textContent = "×";
        del.addEventListener("click", () => {
          const next = load().filter((_, i) => i !== idx);
          save(next);
          renderChips();
          updateAddButtonState();
        });

        chip.appendChild(text);
        chip.appendChild(del);
        chipsWrap.appendChild(chip);
      });
    }

    function updateAddButtonState() {
      const arr = load();
      const span = openBtn.querySelector("span");
      if (arr.length >= MAX_LOC) {
        openBtn.disabled = true;
        openBtn.style.opacity = "0.6";
        openBtn.style.cursor = "not-allowed";
        if (span) span.textContent = "위치 3개까지 설정 가능";
      } else {
        openBtn.disabled = false;
        openBtn.style.opacity = "";
        openBtn.style.cursor = "";
        if (span) span.textContent = "+ 위치 설정";
      }
    }

    function openModal() {
      const meetToggle = document.querySelector("#pfMeetToggle");
      if (meetToggle && !meetToggle.checked) return;

      if (load().length >= MAX_LOC) { alert("위치는 최대 3개까지 설정할 수 있어요."); return; }
      lastFocusedEl = document.activeElement;
      modal.classList.add("is-open");
      modal.setAttribute("aria-hidden", "false");
      document.body.style.overflow = "hidden";
    }
    function closeModal() {
      modal.classList.remove("is-open");
      modal.setAttribute("aria-hidden", "true");
      document.body.style.overflow = "";
      if (lastFocusedEl) lastFocusedEl.focus();
    }

    openBtn.addEventListener("click", openModal);
    modal.addEventListener("click", (e) => {
      if (e.target.closest("[data-area-close='true']")) closeModal();
    });
    window.addEventListener("keydown", (e) => {
      if (e.key === "Escape" && modal.classList.contains("is-open")) closeModal();
    });

    function ensureKakaoServices() { return !!(window.kakao && kakao.maps && kakao.maps.services); }
    function waitForKakaoServices(cb, tries = 40) {
      if (ensureKakaoServices()) return cb();
      if (tries <= 0) { console.error("카카오 SDK 로드 실패: &libraries=services 확인"); return; }
      setTimeout(() => waitForKakaoServices(cb, tries - 1), 100);
    }

    function addressToLatLng(address, cb) {
      if (!ensureKakaoServices()) return cb("", "");
      const geocoder = new kakao.maps.services.Geocoder();
      geocoder.addressSearch(address, (result, status) => {
        if (status === kakao.maps.services.Status.OK) cb(result[0].y, result[0].x);
        else cb("", "");
      });
    }

    function latLngToFullAddress(lat, lng, cb) {
      if (!ensureKakaoServices()) return cb("");
      const geocoder = new kakao.maps.services.Geocoder();
      geocoder.coord2Address(lng, lat, (result, status) => {
        if (status === kakao.maps.services.Status.OK) {
          const jibun = result[0].address ? result[0].address.address_name : "";
          const road = result[0].road_address ? result[0].road_address.address_name : "";
          cb(road || jibun);
        } else cb("");
      });
    }

    function addLocation(fullAddress, lat, lng) {
      const addr = (fullAddress || "").trim();
      if (!addr) return;

      const arr = load();
      if (arr.length >= MAX_LOC) { alert("위치는 최대 3개까지 설정할 수 있어요."); return; }
      if (isDup(arr, addr)) { alert("이미 추가된 위치예요."); return; }

      arr.push({ placeName: "", fullAddress: addr, latitude: lat || "", longitude: lng || "" });
      save(arr);
      renderChips();
      updateAddButtonState();
    }

    function openPostcodeForLocation() {
      new daum.Postcode({
        oncomplete: function (data) {
          const addr = data.userSelectedType === "R" ? data.roadAddress : data.jibunAddress;
          let extraAddr = "";

          if (data.userSelectedType === "R") {
            if (data.bname !== "" && /[동|로|가]$/g.test(data.bname)) extraAddr += data.bname;
            if (data.buildingName !== "" && data.apartment === "Y") {
              extraAddr += extraAddr !== "" ? ", " + data.buildingName : data.buildingName;
            }
            if (extraAddr !== "") extraAddr = " (" + extraAddr + ")";
          }

          const full = addr + extraAddr;
          addressToLatLng(addr, (lat, lng) => {
            addLocation(full, lat, lng);
            closeModal();
          });
        }
      }).open();
    }

    function setMyLocation() {
      if (!navigator.geolocation) { alert("이 브라우저는 위치 기능을 지원하지 않습니다."); return; }
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const lat = pos.coords.latitude;
          const lng = pos.coords.longitude;
          latLngToFullAddress(lat, lng, (fullAddr) => {
            addLocation(fullAddr || "현재 위치", lat, lng);
            closeModal();
          });
        },
        (err) => {
          if (err.code === 1) alert("위치 권한이 거부되었습니다.");
          else if (err.code === 2) alert("위치 정보를 가져올 수 없습니다.");
          else if (err.code === 3) alert("위치 조회 시간이 초과되었습니다.");
          else alert("위치 조회 중 오류가 발생했습니다.");
        },
        { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
      );
    }

    list.addEventListener("click", (e) => {
      const btn = e.target.closest(".area-item");
      if (!btn) return;
      const val = btn.getAttribute("data-area-value") || btn.textContent.trim();
      addLocation(val, "", "");
      closeModal();
    });

    waitForKakaoServices(() => {
      if (searchBtn) searchBtn.addEventListener("click", openPostcodeForLocation);
      if (useGeoBtn) useGeoBtn.addEventListener("click", setMyLocation);
    });

    renderChips();
    updateAddButtonState();

    return {
      validate(tradeMethod) {
        if (tradeMethod !== "직거래") return { ok: true };
        const arr = load();
        if (arr.length === 0) return { ok: false, msg: "직거래를 선택했다면 위치를 1개 이상 설정해 주세요." };
        if (arr.length > 3) return { ok: false, msg: "직거래 위치는 최대 3개까지 설정할 수 있어요." };
        return { ok: true };
      }
    };
  })();

  window.PF.Meet = MeetLocationModule;

})();
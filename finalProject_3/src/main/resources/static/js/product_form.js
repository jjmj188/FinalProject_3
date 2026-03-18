/* =========================================================
   product_form.js (등록 제외 / 모듈만 제공) - 전체본
   + 거래제한 모달 + 택배 UI + 직거래 위치(1~3)
   + 공백만 입력 방지(alert)
   + 판매가격 숫자만 입력/검증(alert)
   + 최근 검색한 동네 캐시(24h TTL, 개수 제한 없음)
   + 위치설정: 1차 모달(areaModal) 내부에서 "검색어로" 버튼 클릭 시
     지도/검색 UI(areaSearchWrap) 펼치기(2차 모달 제거)
   + placeName/fullAddress 분리 저장
   + AI 판매글 작성 기능 추가
   - AI 이미지 진단 기능 제외
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

  function isBlank(value) {
    return String(value ?? "").trim().length === 0;
  }

  function alertAndFocus(msg, el) {
    alert(msg);
    if (el && typeof el.focus === "function") el.focus();
  }

  function getContextPath() {
    const raw =
      document.documentElement.getAttribute("data-context-path") ||
      document.body.getAttribute("data-context-path") ||
      window.ctxPath ||
      "";
    return String(raw).replace(/\/$/, "");
  }

  // =========================================================
  // 최근 검색한 동네 캐시
  // =========================================================
  const RECENT_AREA_KEY = "PF_RECENT_AREAS_V1";
  const RECENT_AREA_TTL_MS = 24 * 60 * 60 * 1000;

  function readRecentAreas() {
    try {
      const raw = localStorage.getItem(RECENT_AREA_KEY);
      if (!raw) return [];
      const arr = JSON.parse(raw);
      if (!Array.isArray(arr)) return [];

      const t = Date.now();
      const alive = arr.filter(x =>
        x &&
        typeof x.value === "string" &&
        x.value.trim() !== "" &&
        typeof x.savedAt === "number" &&
        (t - x.savedAt) <= RECENT_AREA_TTL_MS
      );

      if (alive.length !== arr.length) {
        localStorage.setItem(RECENT_AREA_KEY, JSON.stringify(alive));
      }
      return alive;
    } catch {
      return [];
    }
  }

  function writeRecentAreas(arr) {
    try { localStorage.setItem(RECENT_AREA_KEY, JSON.stringify(arr)); } catch {}
  }

  function pushRecentArea(value) {
    const v = String(value ?? "").trim();
    if (!v) return;

    const current = readRecentAreas();
    const filtered = current.filter(x => String(x.value).trim() !== v);
    filtered.unshift({ value: v, savedAt: Date.now() });
    writeRecentAreas(filtered);
  }

  function renderRecentAreaList(ulEl) {
    if (!ulEl) return;

    const items = readRecentAreas();
    ulEl.innerHTML = "";

    if (items.length === 0) {
      const li = document.createElement("li");
      li.style.padding = "10px 6px";
      li.style.opacity = "0.7";
      li.textContent = "최근 검색 기록이 없어요.";
      ulEl.appendChild(li);
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
      ulEl.appendChild(li);
    });
  }

  // =========================================================
  // placeName / fullAddress 분리 도구
  // =========================================================
  function splitPlaceAndAddress(raw) {
    const v = String(raw ?? "").trim();
    if (!v) return { placeName: "", fullAddress: "" };

    if (v.includes("/")) {
      const [a, b] = v.split("/");
      return { placeName: (a || "").trim(), fullAddress: (b || "").trim() };
    }

    if (v.includes("·")) {
      const [a, b] = v.split("·");
      return { placeName: (a || "").trim(), fullAddress: (b || "").trim() };
    }

    return { placeName: "", fullAddress: v };
  }

  function displayNameOf(loc) {
    const pn = String(loc?.placeName ?? "").trim();
    if (pn) return pn;

    const fa = String(loc?.fullAddress ?? "").trim();
    if (!fa) return "";

    if (fa.includes("/")) return fa.split("/")[0].trim();
    if (fa.includes("·")) return fa.split("·")[0].trim();
    return fa;
  }

  // =========================================================
  // 공백만 입력 방지
  // =========================================================
  (() => {
    const candidates = [
      $("#pfTitle"),
      $("#pfName"),
      $("#pfPrice"),
      $("#pfSellPrice"),
      $(".pf-textarea"),
      ...$$("input[type='text']"),
      ...$$("textarea")
    ];

    const uniq = Array.from(new Set(candidates.filter(Boolean)));

    uniq.forEach((el) => {
      el.addEventListener("blur", () => {
        const raw = String(el.value ?? "");
        if (raw.length > 0 && isBlank(raw)) {
          el.value = "";
          alertAndFocus("공백만 입력할 수 없습니다.", el);
        }
      });
    });
  })();

  // =========================================================
  // 거래 제한 품목 안내 모달
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
  // 탭 underline
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
  // 이미지(1~3) + 대표(mainIndex)
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
        const radio = badge.querySelector("input");
        if (radio) radio.addEventListener("change", () => setMain(idx));

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
        fd.delete("images");
        selectedFiles.forEach(f => fd.append("images", f));
      }
    };
  })();

  window.PF.Image = ImageModule;

  // =========================================================
  // textarea 글자수
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
  // 택배(배송비 포함/별도) + shippingOptionsJson
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

    (() => {
      let lastFeeAlertAt = 0;

      feeInputs.forEach(inp => {
        inp.addEventListener("input", () => {
          const before = String(inp.value ?? "");
          const onlyDigits = before.replace(/[^\d]/g, "");

          if (before !== onlyDigits) {
            inp.value = onlyDigits;

            const now = Date.now();
            if (now - lastFeeAlertAt > 800) {
              lastFeeAlertAt = now;
              alertAndFocus("배송비는 숫자만 입력할 수 있습니다.", inp);
            }
          }

          syncShipOptionsJson();
        });

        inp.addEventListener("blur", () => {
          const raw = String(inp.value ?? "");
          if (raw.length > 0 && isBlank(raw)) {
            inp.value = "";
            alertAndFocus("공백만 입력할 수 없습니다.", inp);
            return;
          }

          const r = validateFeeRangeForInput(inp);
          if (!r.ok) {
            inp.value = "";
            alertAndFocus(r.msg, inp);
            syncShipOptionsJson();
          }
        });
      });
    })();

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

        for (const x of opts) {
          const range = getRange(x.parcelType);
          if (!range) continue;
          const fee = x.shippingFee;
          if (fee < range.min || fee > range.max) {
            return {
              ok: false,
              msg: `${x.parcelType} 배송비는 ${range.min.toLocaleString()}원 ~ ${range.max.toLocaleString()}원 이내로 입력해 주세요.`
            };
          }
        }

        return { ok: true };
      }
    };
  })();

  window.PF.Shipping = ShippingModule;

  // =========================================================
  // 판매가격 숫자만 입력 + 검증
  // =========================================================
  const PriceModule = (() => {
    const priceEl =
      $("#pfPrice") ||
      document.querySelector("input[name='productPrice']");

    if (!priceEl) return null;

    let lastAlertAt = 0;

    priceEl.addEventListener("input", () => {
      const before = String(priceEl.value ?? "");
      const onlyDigits = before.replace(/[^\d]/g, "");
      if (before !== onlyDigits) {
        priceEl.value = onlyDigits;

        const now = Date.now();
        if (now - lastAlertAt > 800) {
          lastAlertAt = now;
          alertAndFocus("판매가격은 숫자만 입력할 수 있습니다.", priceEl);
        }
      }
    });

    priceEl.addEventListener("blur", () => {
      const raw = String(priceEl.value ?? "");
      if (raw.length > 0 && isBlank(raw)) {
        priceEl.value = "";
        alertAndFocus("공백만 입력할 수 없습니다.", priceEl);
      }
    });

    return {
      el: priceEl,
      normalize() {
        priceEl.value = String(priceEl.value ?? "").replace(/[^\d]/g, "");
      },
      validate() {
        this.normalize();
        if (isBlank(priceEl.value)) return { ok: false, msg: "판매가격을 입력해 주세요." };

        const n = parseNumber(priceEl.value);
        if (n === null) return { ok: false, msg: "판매가격은 숫자만 입력해 주세요." };
        if (n <= 0) return { ok: false, msg: "판매가격은 0원보다 커야 합니다." };
        return { ok: true, value: n };
      }
    };
  })();

  window.PF.Price = PriceModule;

  // =========================================================
  // AI 판매글 작성
  // - /ai/sell/description 호출
  // - AI 이미지 진단 없음
  // =========================================================
  const AiSellModule = (() => {
    const btn = $("#pfAiWriteBtn");
    if (!btn) return null;

    const statusEl = $("#pfAiStatus");
    const cautionsWrap = $("#pfAiCautions");

    const titleEl =
      $("#pfTitle") ||
      $("#pfName") ||
      document.querySelector("input[name='productName']");

    const descEl =
      $(".pf-textarea") ||
      document.querySelector("textarea[name='productDesc']");

    const priceEl =
      $("#pfPrice") ||
      document.querySelector("input[name='productPrice']");

    const categoryEl =
      $("#pfCategoryName") ||
      $("#pfCategoryText") ||
      document.querySelector("select[name='categoryName']") ||
      document.querySelector("input[name='categoryName']");

    let busy = false;

    function setBusy(flag) {
      busy = flag;
      btn.disabled = flag;
      btn.classList.toggle("is-loading", flag);

      if (statusEl) {
        statusEl.textContent = flag ? "AI가 판매글을 작성하는 중입니다..." : "";
      }
    }

    function getCategoryName() {
      if (!categoryEl) return "";

      if (categoryEl.tagName === "SELECT") {
        const opt = categoryEl.options[categoryEl.selectedIndex];
        return String(opt?.text || categoryEl.value || "").trim();
      }

      return String(categoryEl.value ?? categoryEl.textContent ?? "").trim();
    }

    function renderCautions(cautions) {
      if (!cautionsWrap) return;

      const list = Array.isArray(cautions) ? cautions.filter(x => String(x ?? "").trim() !== "") : [];
      cautionsWrap.innerHTML = "";

      if (list.length === 0) return;

      const ul = document.createElement("ul");
      ul.className = "pf-ai-caution-list";

      list.forEach(text => {
        const li = document.createElement("li");
        li.textContent = text;
        ul.appendChild(li);
      });

      cautionsWrap.appendChild(ul);
    }

    async function requestAiDescription() {
      if (busy) return;

      if (!titleEl || isBlank(titleEl.value)) {
        alertAndFocus("상품명을 먼저 입력해 주세요.", titleEl);
        return;
      }

      if (!priceEl || isBlank(priceEl.value)) {
        alertAndFocus("판매가격을 먼저 입력해 주세요.", priceEl);
        return;
      }

      const priceV = window.PF.Price?.validate?.();
      if (priceV && priceV.ok === false) {
        alertAndFocus(priceV.msg, window.PF.Price?.el);
        return;
      }

      const payload = {
        productName: String(titleEl?.value ?? "").trim(),
        categoryName: getCategoryName(),
        productPrice: String(priceEl?.value ?? "").trim(),
        productDesc: String(descEl?.value ?? "").trim()
      };

      const ctxPath = getContextPath();
      const url = ctxPath + "/ai/sell/description";

      try {
        setBusy(true);

        const res = await fetch(url, {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify(payload)
        });

        const data = await res.json().catch(() => null);

        if (!res.ok) {
          const msg =
            data?.message ||
            `AI 판매글 작성 요청에 실패했습니다. (${res.status})`;
          throw new Error(msg);
        }

        if (!data || typeof data !== "object") {
          throw new Error("AI 응답 형식이 올바르지 않습니다.");
        }

        const nextTitle = String(data.titleSuggestion ?? "").trim();
        const nextDesc = String(data.description ?? "").trim();

        if (!nextTitle && !nextDesc) {
          throw new Error("AI가 작성 결과를 반환하지 않았습니다.");
        }

        if (titleEl && nextTitle) titleEl.value = nextTitle;
        if (descEl && nextDesc) {
          descEl.value = nextDesc;

          const now = $(".pf-count-now");
          if (now) now.textContent = String(descEl.value.length);
        }

        renderCautions(data.cautions);

        if (statusEl) {
          statusEl.textContent = "AI 판매글이 적용되었습니다.";
          setTimeout(() => {
            if (statusEl.textContent === "AI 판매글이 적용되었습니다.") {
              statusEl.textContent = "";
            }
          }, 2500);
        }
      } catch (err) {
        console.error(err);
        alert(err?.message || "AI 판매글 작성 중 오류가 발생했습니다.");
        if (statusEl) statusEl.textContent = "";
      } finally {
        setBusy(false);
      }
    }

    btn.addEventListener("click", requestAiDescription);

    return {
      request: requestAiDescription
    };
  })();

  window.PF.AiSell = AiSellModule;

  // =========================================================
  // 직거래 위치 1~3 -> meetLocationsJson
  // =========================================================
  const MeetLocationModule = (() => {
    const openBtn = $("#pfLocationBtn");
    const modal = $("#areaModal");
    const list = $("#areaList");
    const useGeoBtn = $("#areaUseGeoBtn");
    const searchBtn = $("#areaSearchBtn");
    const chipsWrap = $("#pfLocChips");
    const hidden = $("#pfMeetLocations");

    const searchWrap = $("#areaSearchWrap");
    const searchEmpty = $("#areaSearchEmpty");
    const mapEl = $("#areaKakaoMap");
    const kwInput = $("#areaKakaoKeyword");
    const kwBtn = $("#areaKakaoSearchBtn");
    const resultUl = $("#areaKakaoResult");

    if (!openBtn || !modal || !list || !chipsWrap || !hidden) return null;

    const MAX_LOC = 3;
    let lastFocusedEl = null;

    function load() {
      const v = safeJsonParse(hidden.value || "[]", []);
      return Array.isArray(v) ? v : [];
    }

    function save(arr) {
      hidden.value = JSON.stringify(arr);
    }

    function isDupByKey(arr, key) {
      const k = String(key ?? "").trim();
      if (!k) return false;

      return arr.some(x => {
        const xKey = (String(x.fullAddress ?? "").trim() || String(x.placeName ?? "").trim());
        return xKey === k;
      });
    }

    function shortText(t) {
      if (!t) return "";
      return t.length > 28 ? t.slice(0, 28) + "…" : t;
    }

    function renderChips() {
      const arr = load();
      chipsWrap.innerHTML = "";

      arr.forEach((loc, idx) => {
        const chip = document.createElement("div");
        chip.className = "pf-loc-chip";

        const text = document.createElement("span");
        text.className = "pf-loc-chip__text";
        text.title = (loc.placeName || loc.fullAddress || "");
        text.textContent = shortText(displayNameOf(loc));

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
      }
      else {
        openBtn.disabled = false;
        openBtn.style.opacity = "";
        openBtn.style.cursor = "";
        if (span) span.textContent = "+ 위치 설정";
      }
    }

    function openModal() {
      const meetToggle = document.querySelector("#pfMeetToggle");
      if (meetToggle && !meetToggle.checked) return;

      if (load().length >= MAX_LOC) {
        alert("위치는 최대 3개까지 설정할 수 있어요.");
        return;
      }

      renderRecentAreaList(list);

      if (searchWrap) {
        searchWrap.classList.remove("is-open");
        searchWrap.setAttribute("aria-hidden", "true");
      }

      if (searchEmpty) {
        searchEmpty.style.display = "flex";
      }

      if (resultUl) resultUl.innerHTML = "";
      if (kwInput) kwInput.value = "";

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

    function ensureKakaoServices() {
      return !!(window.kakao && kakao.maps && kakao.maps.services);
    }

    function waitForKakaoServices(cb, tries = 50) {
      if (ensureKakaoServices()) return cb();

      if (tries <= 0) {
        console.error("카카오 SDK 로드 실패: script appkey / libraries=services 확인");
        alert("카카오맵 로드에 실패했어요. appkey/도메인 등록을 확인해 주세요.");
        return;
      }

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
        }
        else {
          cb("");
        }
      });
    }

    function addLocation(placeName, fullAddress, lat, lng) {
      const pn = String(placeName ?? "").trim();
      const fa = String(fullAddress ?? "").trim();

      const key = fa || pn;
      if (!key) return;

      const arr = load();

      if (arr.length >= MAX_LOC) {
        alert("위치는 최대 3개까지 설정할 수 있어요.");
        return;
      }

      if (isDupByKey(arr, key)) {
        alert("이미 추가된 위치예요.");
        return;
      }

      arr.push({
        placeName: pn,
        fullAddress: fa,
        latitude: lat || "",
        longitude: lng || ""
      });

      save(arr);
      renderChips();
      updateAddButtonState();
    }

    function openSearchUI() {
      if (!searchWrap) return;

      searchWrap.classList.add("is-open");
      searchWrap.setAttribute("aria-hidden", "false");

      if (searchEmpty) {
        searchEmpty.style.display = "none";
      }

      waitForKakaoServices(() => {
        ensureMap();

        setTimeout(() => {
          try {
            kakao.maps.event.trigger(map, "resize");
          }
          catch {}
        }, 0);
      });

      if (kwInput) kwInput.focus();
    }

    let map = null;
    let places = null;
    let markers = [];
    let pickMarker = null;

    function clearMarkers() {
      markers.forEach(m => m.setMap(null));
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

        latLngToFullAddress(lat, lng, (fullAddr) => {
          const fa = String(fullAddr || "").trim();

          if (!fa) {
            alert("주소를 가져오지 못했어요. 다른 지점을 눌러주세요.");
            return;
          }

          pushRecentArea(fa);
          addLocation("", fa, lat, lng);
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

          if (!placeName && !fullAddress) {
            alert("선택한 위치의 주소 정보가 부족해요.");
            return;
          }

          pushRecentArea(`${placeName}${fullAddress ? " / " + fullAddress : ""}`);
          addLocation(placeName, fullAddress, lat, lng);
          closeModal();
        });

        li.appendChild(btn);
        resultUl.appendChild(li);
      });
    }

    function searchKeyword() {
      const keyword = String(kwInput?.value ?? "").trim();

      if (!keyword) {
        alert("검색어를 입력해 주세요.");
        kwInput?.focus?.();
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

            const m = new kakao.maps.Marker({ position: latlng });
            m.setMap(map);
            markers.push(m);

            bounds.extend(latlng);

            kakao.maps.event.addListener(m, "click", () => {
              const placeName = (p.place_name || "").trim();
              const fullAddress = (p.road_address_name || p.address_name || "").trim();

              if (!placeName && !fullAddress) return;

              pickMarker.setPosition(latlng);
              pickMarker.setMap(map);

              pushRecentArea(`${placeName}${fullAddress ? " / " + fullAddress : ""}`);
              addLocation(placeName, fullAddress, lat, lng);
              closeModal();
            });
          });

          map.setBounds(bounds);
          renderSearchResults(data);
        });
      });
    }

    function setMyLocation() {
      if (!navigator.geolocation) {
        alert("이 브라우저는 위치 기능을 지원하지 않습니다.");
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const lat = pos.coords.latitude;
          const lng = pos.coords.longitude;

          waitForKakaoServices(() => {
            ensureMap();

            const latlng = new kakao.maps.LatLng(lat, lng);
            map.setCenter(latlng);
            map.setLevel(3);
            pickMarker.setPosition(latlng);
            pickMarker.setMap(map);

            latLngToFullAddress(lat, lng, (fullAddr) => {
              const fa = (fullAddr || "현재 위치").trim();

              pushRecentArea(fa);
              addLocation(fa, fa, lat, lng);
              closeModal();
            });
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
      pushRecentArea(val);

      const sp = splitPlaceAndAddress(val);
      const query = sp.fullAddress || sp.placeName || val;

      waitForKakaoServices(() => {
        addressToLatLng(query, (lat, lng) => {
          addLocation(sp.placeName, sp.fullAddress, lat, lng);
          closeModal();
        });
      });
    });

    waitForKakaoServices(() => {
      if (searchBtn) searchBtn.addEventListener("click", openSearchUI);
      if (useGeoBtn) useGeoBtn.addEventListener("click", setMyLocation);

      if (kwBtn) kwBtn.addEventListener("click", searchKeyword);
      if (kwInput) {
        kwInput.addEventListener("keydown", (e) => {
          if (e.key === "Enter") {
            e.preventDefault();
            searchKeyword();
          }
        });
      }
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

  // =========================================================
  // 폼 submit 시 유효성 검사
  // =========================================================
  (() => {
    const form =
      $("#pfForm") ||
      wrap.querySelector("form") ||
      document.querySelector("form");

    if (!form) return;

    form.addEventListener("submit", (e) => {
      const titleEl = $("#pfTitle") || $("#pfName");
      if (titleEl && isBlank(titleEl.value)) {
        e.preventDefault();
        alertAndFocus("제목(상품명)을 입력해 주세요.", titleEl);
        return;
      }

      const descEl = $(".pf-textarea");
      if (descEl && isBlank(descEl.value)) {
        e.preventDefault();
        alertAndFocus("상품 설명을 입력해 주세요.", descEl);
        return;
      }

      const imgV = window.PF.Image?.validate?.();
      if (imgV && imgV.ok === false) {
        e.preventDefault();
        alertAndFocus(imgV.msg);
        return;
      }

      const shipV = window.PF.Shipping?.validate?.();
      if (shipV && shipV.ok === false) {
        e.preventDefault();
        alertAndFocus(shipV.msg);
        return;
      }

      const priceV = window.PF.Price?.validate?.();
      if (priceV && priceV.ok === false) {
        e.preventDefault();
        alertAndFocus(priceV.msg, window.PF.Price?.el);
        return;
      }

      const meetToggle = $("#pfMeetToggle");
      const tradeMethod = meetToggle?.checked ? "직거래" : "택배";

      const meetV = window.PF.Meet?.validate?.(tradeMethod);
      if (meetV && meetV.ok === false) {
        e.preventDefault();
        alertAndFocus(meetV.msg);
        return;
      }
    });
  })();

  // =========================================================
  // 배송비 허용 범위(원)
  // =========================================================
  const FEE_RANGE = {
    "일반택배": { min: 2700, max: 17000 },
    "CU반값": { min: 1800, max: 2700 },
    "GS반값": { min: 1900, max: 4400 }
  };

  function getRange(type) {
    return FEE_RANGE[type] || null;
  }

  function validateFeeRangeForInput(inp) {
    if (!inp) return { ok: true };

    const type = inp.getAttribute("data-fee-for");
    const range = getRange(type);
    if (!range) return { ok: true };

    if (inp.disabled) return { ok: true };

    const fee = parseNumber(inp.value);
    if (fee === null) return { ok: true };

    if (fee < range.min || fee > range.max) {
      return {
        ok: false,
        msg: `${type} 배송비는 ${range.min.toLocaleString()}원 ~ ${range.max.toLocaleString()}원 이내로 입력해 주세요.`
      };
    }

    return { ok: true };
  }

})();
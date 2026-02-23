(function(){
  const wrap = document.querySelector(".pf-wrap");
  if(!wrap) return;

  // ========== 탭 underline 위치 맞추기 ==========
  const tabs = document.querySelector(".pf-tabs");
  const underline = document.querySelector(".pf-tab-underline");
  const activeKey = wrap.dataset.active;

  function setActiveTab(){
    const all = Array.from(document.querySelectorAll(".pf-tab"));
    const active = all.find(a => a.dataset.tab === activeKey) || all[0];
    all.forEach(a => a.classList.toggle("is-active", a === active));

    if(!tabs || !underline || !active) return;

    const tabsRect = tabs.getBoundingClientRect();
    const rect = active.getBoundingClientRect();
    const left = rect.left - tabsRect.left;

    underline.style.width = rect.width + "px";
    underline.style.transform = `translateX(${left}px)`;
  }

  setActiveTab();
  window.addEventListener("resize", setActiveTab);


// =========================================================
// ========== 이미지 미리보기(최대 3개) + 대표이미지 설정 ==========
// =========================================================
const fileInput = document.getElementById("pfImageInput");
const previewWrap = document.getElementById("pfPreviewWrap") || document.querySelector(".pf-preview");
const MAX_FILES = 3;

let selectedFiles = [];
let mainIndex = 0; // 대표 이미지 인덱스

function clampMainIndex(){
  if(selectedFiles.length === 0){
    mainIndex = 0;
    return;
  }
  if(mainIndex >= selectedFiles.length) mainIndex = 0;
  if(mainIndex < 0) mainIndex = 0;
}

function setMain(idx){
  mainIndex = idx;
  clampMainIndex();
  renderPreviews();
}

function renderPreviews(){
  if(!previewWrap) return;

  previewWrap.innerHTML = "";
  previewWrap.classList.toggle("is-on", selectedFiles.length > 0);

  selectedFiles.forEach((file, idx) => {
    const url = URL.createObjectURL(file);

    const item = document.createElement("div");
    item.className = "pf-preview-item";
    if(idx === mainIndex) item.classList.add("is-main");
    item.dataset.index = String(idx);

    const img = document.createElement("img");
    img.className = "pf-preview-img";
    img.src = url;
    img.alt = file.name || "업로드 이미지";

    // 대표 선택(라디오)
    const badge = document.createElement("label");
    badge.className = "pf-main-badge";
    badge.innerHTML = `
      <input type="radio" name="pfMainImage" class="pf-main-radio" ${idx === mainIndex ? "checked" : ""}>
      <span class="pf-main-text">대표</span>
    `;

    // 라디오 변경
    const radio = badge.querySelector("input");
    radio.addEventListener("change", () => setMain(idx));

    // 이미지 클릭해도 대표로 설정(UX 편의)
    img.addEventListener("click", () => setMain(idx));

    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "pf-preview-remove";
    btn.setAttribute("aria-label", "이미지 삭제");
    btn.textContent = "×";

    btn.addEventListener("click", () => {
      URL.revokeObjectURL(url);

      // 삭제 전 대표 인덱스 보정
      if(idx === mainIndex){
        // 대표를 삭제하면: 우선 0번(또는 남는 첫 이미지)로
        selectedFiles = selectedFiles.filter((_, i) => i !== idx);
        mainIndex = 0;
      } else {
        selectedFiles = selectedFiles.filter((_, i) => i !== idx);
        // 삭제가 대표 앞쪽이면 대표 인덱스가 1 줄어듦
        if(idx < mainIndex) mainIndex -= 1;
      }

      clampMainIndex();
      syncInputFiles();
      renderPreviews();
    });

    item.appendChild(img);
    item.appendChild(badge);
    item.appendChild(btn);
    previewWrap.appendChild(item);
  });
}

function syncInputFiles(){
  if(!fileInput) return;
  const dt = new DataTransfer();
  selectedFiles.forEach(f => dt.items.add(f));
  fileInput.files = dt.files;
}

// 대표 인덱스를 폼 전송 시 같이 보내려면 hidden input 하나 sync
const mainHidden = document.getElementById("pfMainIndex"); // (옵션) hidden input

function syncMainHidden(){
  if(mainHidden) mainHidden.value = String(mainIndex);
}

function renderAll(){
  renderPreviews();
  syncMainHidden();
}

if(fileInput && previewWrap){
  fileInput.addEventListener("change", (e) => {
    const files = Array.from(e.target.files || []);
    if(files.length === 0) return;

    if(selectedFiles.length >= MAX_FILES){
      fileInput.value = "";
      return;
    }

    const remain = MAX_FILES - selectedFiles.length;
    const toAdd = files.slice(0, remain);

    const beforeLen = selectedFiles.length;
    selectedFiles = selectedFiles.concat(toAdd);

    // 처음 추가되는 경우 자동 대표(맨 처음 이미지)
    if(beforeLen === 0 && selectedFiles.length > 0){
      mainIndex = 0;
    }

    clampMainIndex();
    syncInputFiles();
    renderAll();
    fileInput.value = "";
  });

  renderAll();
}

  function syncInputFiles(){
    if(!fileInput) return;
    const dt = new DataTransfer();
    selectedFiles.forEach(f => dt.items.add(f));
    fileInput.files = dt.files;
  }

  if(fileInput && previewWrap){
    fileInput.addEventListener("change", (e) => {
      const files = Array.from(e.target.files || []);
      if(files.length === 0) return;

      if(selectedFiles.length >= MAX_FILES){
        fileInput.value = "";
        return;
      }

      const remain = MAX_FILES - selectedFiles.length;
      const toAdd = files.slice(0, remain);

      selectedFiles = selectedFiles.concat(toAdd);
      syncInputFiles();
      renderPreviews();
      fileInput.value = "";
    });

    renderPreviews();
  }


  // ========== textarea 글자수 ==========
  const ta = document.querySelector(".pf-textarea");
  const now = document.querySelector(".pf-count-now");
  if(ta && now){
    const update = () => { now.textContent = String(ta.value.length); };
    ta.addEventListener("input", update);
    update();
  }


  // =========================================================
  // ========== 거래방법 + 배송비 UI (슬라이드 애니메이션 포함) ==========
  // =========================================================
  const shipToggle = document.getElementById("pfShipToggle");
  const meetToggle = document.getElementById("pfMeetToggle");

  const shipCard = document.getElementById("pfShipCard");           // 배송비 포함 여부 카드
  const feeDetail = document.getElementById("pfShipFeeDetail");     // 배송비 입력 영역
  const shipSettingLink = document.getElementById("pfShipSettingLink");
  const locationBtn = document.getElementById("pfLocationBtn");

  const feeIncluded = document.getElementById("pfFeeIncluded");
  const feeSeparate = document.getElementById("pfFeeSeparate");

  function openEl(el){
    if(!el) return;
    el.classList.add("is-open");
    el.setAttribute("aria-hidden", "false");
  }

  function closeEl(el){
    if(!el) return;
    el.classList.remove("is-open");
    el.setAttribute("aria-hidden", "true");
  }

  function updateTradeUI(){
    if(!shipToggle || !meetToggle) return;

    const isShip = shipToggle.checked;
    const isMeet = meetToggle.checked;
    const isSeparate = !!(feeSeparate && feeSeparate.checked);

    // ===== 배송비 카드 슬라이드 표시 =====
    if(shipCard){
      if(isShip) openEl(shipCard);
      else closeEl(shipCard);
    }

    // ===== 배송비 입력 영역 =====
    if(feeDetail){
      if(isShip && isSeparate) openEl(feeDetail);
      else closeEl(feeDetail);
    }

    // ===== 직거래 버튼 =====
    if(locationBtn){
      locationBtn.style.display = isMeet ? "inline-flex" : "none";
    }

    // ===== 거래제한 품목 안내 노출 조건 =====
    if(shipSettingLink){
      shipSettingLink.style.display = (isShip && isSeparate) ? "inline" : "none";
    }
  }

  // 배송비 포함/별도 변경
  if(feeIncluded) feeIncluded.addEventListener("change", updateTradeUI);
  if(feeSeparate) feeSeparate.addEventListener("change", updateTradeUI);

  // 만나서 직거래 선택 시 → 배송비 카드 + 입력영역 모두 접고 포함으로 초기화
  if(meetToggle){
    meetToggle.addEventListener("change", () => {
      if(meetToggle.checked){
        if(feeIncluded) feeIncluded.checked = true;
      }
      updateTradeUI();
    });
  }

  // 택배거래 선택 시 → 카드 부드럽게 다시 열림
  if(shipToggle){
    shipToggle.addEventListener("change", updateTradeUI);
  }

  updateTradeUI();


  // ========== 기존 "택배 리스트 흐리게" 로직 ==========
  const shipList = document.getElementById("pfShipList");
  if(shipToggle && shipList){
    const syncShip = () => {
      shipList.style.opacity = shipToggle.checked ? "1" : "0.35";
      shipList.style.pointerEvents = shipToggle.checked ? "auto" : "none";
      shipList.style.filter = shipToggle.checked ? "none" : "grayscale(1)";
    };
    shipToggle.addEventListener("change", syncShip);
    syncShip();
  }


  // ========== 직거래 입력 활성/비활성 ==========
  const meetType = document.getElementById("pfMeetType");
  const meetPlace = document.getElementById("pfMeetPlace");

  function syncMeet(){
    if(!meetToggle || !meetType || !meetPlace) return;
    const on = meetToggle.checked && meetType.checked;
    meetPlace.disabled = !on;
    meetPlace.classList.toggle("pf-input-gray", !on);
    if(!on) meetPlace.value = "";
  }

  if(meetToggle && meetType && meetPlace){
    meetToggle.addEventListener("change", () => {
      if(!meetToggle.checked){
        meetType.checked = false;
      }
      syncMeet();
    });
    meetType.addEventListener("change", syncMeet);
    syncMeet();
  }

})();
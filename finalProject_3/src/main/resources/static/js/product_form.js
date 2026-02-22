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

  // ========== 이미지 미리보기 ==========
  const fileInput = document.getElementById("pfImageInput");
  const preview = document.querySelector(".pf-preview");
  const previewImg = document.querySelector(".pf-preview-img");
  const removeBtn = document.querySelector(".pf-preview-remove");

  if(fileInput && preview && previewImg && removeBtn){
    fileInput.addEventListener("change", (e) => {
      const file = e.target.files && e.target.files[0];
      if(!file) return;

      const url = URL.createObjectURL(file);
      previewImg.src = url;
      previewImg.alt = file.name || "업로드 이미지";
      preview.classList.add("is-on");
    });

    removeBtn.addEventListener("click", () => {
      fileInput.value = "";
      previewImg.removeAttribute("src");
      previewImg.alt = "";
      preview.classList.remove("is-on");
    });
  }

  // ========== textarea 글자수 ==========
  const ta = document.querySelector(".pf-textarea");
  const now = document.querySelector(".pf-count-now");
  if(ta && now){
    const update = () => { now.textContent = String(ta.value.length); };
    ta.addEventListener("input", update);
    update();
  }

  // ========== 거래방법 토글 ==========
  const shipToggle = document.getElementById("pfShipToggle");
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

  const meetToggle = document.getElementById("pfMeetToggle");
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

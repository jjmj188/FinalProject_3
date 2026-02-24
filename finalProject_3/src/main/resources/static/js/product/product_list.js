
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

document.addEventListener("click", (e) => {
  const heartBtn = e.target.closest(".heart");
  if (!heartBtn) return;
  e.preventDefault();
  e.stopPropagation();


  heartBtn.classList.toggle("on");
  const icon = heartBtn.querySelector("i");
  if (icon) {
    icon.classList.toggle("fa-regular");
    icon.classList.toggle("fa-solid");
  }
});
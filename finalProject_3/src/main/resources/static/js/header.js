(function(){
  // DOM 준비 보장
  function init(){
    const trigger = document.getElementById('categoryTrigger');
    const btn = document.getElementById('btnCategory');
    const panel = document.getElementById('categoryPanel');
    const mainMenu = document.getElementById('mainMenu');

    // ✅ 로드 실패/경로 오류/DOM 아직 없음 방어
    if(!trigger || !btn || !panel || !mainMenu) return;

    // ✅ 중복 바인딩 방지
    if (trigger.dataset.bound === "1") return;
    trigger.dataset.bound = "1";

    let closeTimer = null;

    function openMenu(){
      if (closeTimer) { clearTimeout(closeTimer); closeTimer = null; }
      trigger.classList.add('is-open');
      btn.setAttribute('aria-expanded', 'true');
    }

    function closeMenu(){
      trigger.classList.remove('is-open');
      btn.setAttribute('aria-expanded', 'false');
    }

    function scheduleClose(){
      if (closeTimer) clearTimeout(closeTimer);
      closeTimer = setTimeout(() => {
        closeMenu();
        closeTimer = null;
      }, 120);
    }

    trigger.addEventListener('mouseenter', () => {
      if (closeTimer) { clearTimeout(closeTimer); closeTimer = null; }
    });
    trigger.addEventListener('mouseleave', scheduleClose);

    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      trigger.classList.contains('is-open') ? closeMenu() : openMenu();
    });

    panel.addEventListener('click', (e) => e.stopPropagation());
    document.addEventListener('click', closeMenu);
    document.addEventListener('keydown', (e) => { if(e.key === 'Escape') closeMenu(); });

    // ✅ 대메뉴만: active 표시만 유지 (디자인 그대로)
    mainMenu.addEventListener('mouseover', (e) => {
      const li = e.target.closest('li');
      if(!li || !mainMenu.contains(li)) return;

      openMenu();
      mainMenu.querySelectorAll('li').forEach(x => x.classList.remove('is-active'));
      li.classList.add('is-active');
    });

    mainMenu.addEventListener('click', (e) => {
  const btnEl = e.target.closest('button');
  if(!btnEl || !mainMenu.contains(btnEl)) return;

  e.preventDefault();
  e.stopPropagation();

  const li = btnEl.closest('li');

  // active UI 유지
  openMenu();
  const items = Array.from(mainMenu.querySelectorAll('li'));
  items.forEach(x => x.classList.remove('is-active'));
  if (li) li.classList.add('is-active');

  // ★ 핵심: 몇 번째 메뉴인지 index만 구함
  const index = items.indexOf(li);

  console.log("선택된 주메뉴 index:", index);

  // DB 연결 후 이렇게 사용하면 됨
  // location.href = `/product/list?cateNo=${index + 1}`;
});

    // 초기 상태 유지(이미 HTML에 is-active가 있으니 그대로 둠)
  }

  // DOMContentLoaded 보장
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
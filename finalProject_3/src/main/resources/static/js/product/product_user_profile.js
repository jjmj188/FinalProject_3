(function () {
  const tabs = document.querySelectorAll('.sps-tab');
  const panelSell = document.getElementById('sps-panel-sell');
  const panelReview = document.getElementById('sps-panel-review');


  const tabsWrap = document.querySelector('.sps-tabs');
  const underline = document.querySelector('.sps-underline');

  function moveUnderline(activeBtn) {
    if (!tabsWrap || !underline || !activeBtn) return;

    const wrapRect = tabsWrap.getBoundingClientRect();
    const btnRect = activeBtn.getBoundingClientRect();

    const x = btnRect.left - wrapRect.left;
    underline.style.width = btnRect.width + 'px';
    underline.style.transform = `translateX(${x}px)`;
  }


  const initActive = document.querySelector('.sps-tab.is-active') || tabs[0];
  moveUnderline(initActive);


  window.addEventListener('resize', () => {
    const active = document.querySelector('.sps-tab.is-active') || tabs[0];
    moveUnderline(active);
  });

  // 탭 전환
  tabs.forEach(btn => {
    btn.addEventListener('click', () => {
      tabs.forEach(t => {
        t.classList.remove('is-active');
        t.setAttribute('aria-selected', 'false');
      });
      btn.classList.add('is-active');
      btn.setAttribute('aria-selected', 'true');

      const isSell = btn.dataset.tab === 'sell';
      panelSell.classList.toggle('is-show', isSell);
      panelReview.classList.toggle('is-show', !isSell);
      panelSell.hidden = !isSell;
      panelReview.hidden = isSell;


      moveUnderline(btn);
    });
  });

  // 하트 토글
  document.addEventListener('click', (e) => {
    const likeBtn = e.target.closest('.sps-like');
    if (!likeBtn) return;

    const icon = likeBtn.querySelector('i');
    const isOn = likeBtn.classList.contains('is-on');

    likeBtn.classList.toggle('is-on', !isOn);
    likeBtn.setAttribute('aria-pressed', String(!isOn));
    icon.classList.toggle('fa-solid', !isOn);
    icon.classList.toggle('fa-regular', isOn);
  });
})();
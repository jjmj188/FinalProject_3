(function () {
  /* =========================
     이벤트 슬라이더
  ========================= */
  const row = document.getElementById('bestRow');
  const viewport = document.getElementById('bestViewport');
  const track = document.getElementById('bestTrack');
  const prevBtn = document.getElementById('bestPrev');
  const nextBtn = document.getElementById('bestNext');

  if (row && viewport && track && prevBtn && nextBtn) {
    const GAP = 90;
    let isMoving = false;

    function cards() {
      return Array.from(track.querySelectorAll('.best-card'));
    }

    function cardWidth() {
      const card = track.querySelector('.best-card');
      return card ? card.getBoundingClientRect().width : 260;
    }

    function step() {
      return cardWidth() + GAP;
    }

    function updateRoles() {
      const list = cards();
      list.forEach(card => card.classList.remove('is-left', 'is-center', 'is-right'));

      if (list.length === 0) return;

      const viewportRect = viewport.getBoundingClientRect();
      const viewportCenterX = viewportRect.left + (viewportRect.width / 2);

      let centerIdx = 0;
      let minDist = Infinity;

      list.forEach((card, index) => {
        const rect = card.getBoundingClientRect();
        const centerX = rect.left + (rect.width / 2);
        const dist = Math.abs(centerX - viewportCenterX);

        if (dist < minDist) {
          minDist = dist;
          centerIdx = index;
        }
      });

      list[centerIdx].classList.add('is-center');

      if (list.length >= 2) {
        const leftIdx = (centerIdx - 1 + list.length) % list.length;
        const rightIdx = (centerIdx + 1) % list.length;

        if (leftIdx !== centerIdx) {
          list[leftIdx].classList.add('is-left');
        }

        if (rightIdx !== centerIdx && rightIdx !== leftIdx) {
          list[rightIdx].classList.add('is-right');
        }
      }
    }

    function finishMove() {
      track.classList.remove('is-moving');
      track.style.transform = 'translateX(0px)';
      row.classList.remove('is-animating');
      isMoving = false;
      updateRoles();
    }

    function moveToPrevVisual() {
      if (isMoving) return;
      if (cards().length <= 1) return;

      isMoving = true;
      row.classList.add('is-animating');

      const dist = step();

      track.classList.remove('is-moving');
      track.style.transform = `translateX(-${dist}px)`;

      const list = cards();
      track.insertBefore(list[list.length - 1], list[0]);

      track.getBoundingClientRect();

      track.classList.add('is-moving');
      track.style.transform = 'translateX(0px)';

      track.addEventListener('transitionend', function onEnd(event) {
        if (event.propertyName !== 'transform') return;
        track.removeEventListener('transitionend', onEnd);
        finishMove();
      }, { once: true });
    }

    function moveToNextVisual() {
      if (isMoving) return;
      if (cards().length <= 1) return;

      isMoving = true;
      row.classList.add('is-animating');

      const dist = step();

      track.classList.remove('is-moving');
      track.style.transform = `translateX(${dist}px)`;

      track.appendChild(cards()[0]);

      track.getBoundingClientRect();

      track.classList.add('is-moving');
      track.style.transform = 'translateX(0px)';

      track.addEventListener('transitionend', function onEnd(event) {
        if (event.propertyName !== 'transform') return;
        track.removeEventListener('transitionend', onEnd);
        finishMove();
      }, { once: true });
    }

    prevBtn.addEventListener('click', moveToPrevVisual);
    nextBtn.addEventListener('click', moveToNextVisual);

    window.addEventListener('keydown', function (event) {
      const eventArea = document.getElementById('eventArea');
      if (!eventArea) return;

      const rect = eventArea.getBoundingClientRect();
      const inView = rect.top < window.innerHeight && rect.bottom > 0;
      if (!inView) return;

      if (event.key === 'ArrowLeft') {
        moveToPrevVisual();
      }
      else if (event.key === 'ArrowRight') {
        moveToNextVisual();
      }
    });

    window.addEventListener('resize', function () {
      if (!isMoving) {
        track.style.transform = 'translateX(0px)';
        requestAnimationFrame(updateRoles);
      }
    });

    requestAnimationFrame(updateRoles);
  }

  /* =========================
     무료나눔 슬라이더
  ========================= */
  const wrap = document.querySelector('.free-deal-slider__wrap');
  const freeViewport = document.querySelector('.free-deal-slider__viewport');
  const freeTrack = document.querySelector('#freeDealTrack');
  const freePrevBtn = document.querySelector('.free-deal-slider__nav--prev');
  const freeNextBtn = document.querySelector('.free-deal-slider__nav--next');
  const dotsWrap = document.querySelector('#freeDealDots');

  if (wrap && freeViewport && freeTrack && freePrevBtn && freeNextBtn && dotsWrap) {
    const slides = Array.from(freeTrack.querySelectorAll('.free-deal-slider__item'));
    if (slides.length > 0) {
      let startIndex = 0;
      let autoTimer = null;

      let isDown = false;
      let downX = 0;
      let downTranslate = 0;
      let currentTranslate = 0;

      const DOT_BLOCK_SIZE = 3;

      function visibleCount() {
        const w = window.innerWidth;
        if (w <= 640) return 1;
        if (w <= 1024) return 2;
        return 3;
      }

      function step() {
        const firstSlide = freeTrack.querySelector('.free-deal-slider__item');
        if (!firstSlide) return 0;

        const slideW = firstSlide.getBoundingClientRect().width;
        const gap = parseFloat(getComputedStyle(freeTrack).gap || '0') || 0;
        return slideW + gap;
      }

      function maxStartIndex() {
        return Math.max(0, slides.length - visibleCount());
      }

      function totalBlocks() {
        return Math.max(1, Math.ceil(slides.length / DOT_BLOCK_SIZE));
      }

      function currentBlock() {
        const v = visibleCount();
        const lastVisibleIndex = Math.min(slides.length - 1, startIndex + v - 1);
        return Math.floor(lastVisibleIndex / DOT_BLOCK_SIZE);
      }

      function buildDots() {
        dotsWrap.innerHTML = '';

        const total = totalBlocks();

        for (let i = 0; i < total; i++) {
          const btn = document.createElement('button');
          btn.type = 'button';
          btn.className = 'free-deal-slider__dot';
          btn.setAttribute('aria-label', (i + 1) + '번째 블럭');

          btn.addEventListener('click', function () {
            stopAuto();
            startIndex = i * DOT_BLOCK_SIZE;
            update(true, false);
            startAuto();
          });

          dotsWrap.appendChild(btn);
        }
      }

      function setActiveDot() {
        const dots = Array.from(dotsWrap.querySelectorAll('.free-deal-slider__dot'));
        const cur = currentBlock();

        dots.forEach(function (dot, i) {
          dot.classList.toggle('is-active', i === cur);
        });
      }

      function applyTransform(px, animate) {
        freeTrack.style.transition = animate ? 'transform 420ms ease' : 'none';
        freeTrack.style.transform = 'translateX(' + px + 'px)';
      }

      function update(animate = true, clamp = true) {
        if (clamp) {
          startIndex = Math.min(Math.max(startIndex, 0), maxStartIndex());
        }
        else {
          startIndex = Math.min(Math.max(startIndex, 0), slides.length - 1);
        }

        currentTranslate = -step() * startIndex;
        applyTransform(currentTranslate, animate);

        freePrevBtn.disabled = (startIndex <= 0);
        freeNextBtn.disabled = (startIndex >= maxStartIndex());

        setActiveDot();
      }

      function goPrevBlock() {
        startIndex -= visibleCount();
        update(true, true);
      }

      function goNextBlock() {
        startIndex += visibleCount();
        update(true, true);
      }

      freePrevBtn.addEventListener('click', function () {
        stopAuto();
        goPrevBlock();
        startAuto();
      });

      freeNextBtn.addEventListener('click', function () {
        stopAuto();
        goNextBlock();
        startAuto();
      });

      function getClientX(e) {
        if (e.touches && e.touches[0]) return e.touches[0].clientX;
        if (e.changedTouches && e.changedTouches[0]) return e.changedTouches[0].clientX;
        return e.clientX;
      }

      function onDown(e) {
        isDown = true;
        downX = getClientX(e);
        downTranslate = currentTranslate;
        applyTransform(currentTranslate, false);
        stopAuto();
      }

      function onMove(e) {
        if (!isDown) return;

        const dx = getClientX(e) - downX;
        const raw = downTranslate + dx;

        const min = -step() * maxStartIndex();
        const max = 0;

        const softened =
          raw < min ? min - (min - raw) * 0.25 :
          raw > max ? max + (raw - max) * 0.25 :
          raw;

        applyTransform(softened, false);
      }

      function onUp(e) {
        if (!isDown) return;
        isDown = false;

        const endX = getClientX(e);
        const dx = endX - downX;
        const threshold = Math.max(60, freeViewport.getBoundingClientRect().width * 0.12);

        if (dx > threshold) {
          goPrevBlock();
        }
        else if (dx < -threshold) {
          goNextBlock();
        }
        else {
          update(true, true);
        }

        startAuto();
      }

      freeViewport.addEventListener('mousedown', onDown);
      window.addEventListener('mousemove', onMove);
      window.addEventListener('mouseup', onUp);

      freeViewport.addEventListener('touchstart', onDown, { passive: true });
      freeViewport.addEventListener('touchmove', onMove, { passive: true });
      freeViewport.addEventListener('touchend', onUp);

      function startAuto() {
        stopAuto();

        if (slides.length <= visibleCount()) return;

        autoTimer = setInterval(function () {
          const v = visibleCount();
          const max = maxStartIndex();

          if (startIndex >= max) {
            startIndex = 0;
          }
          else {
            startIndex += v;
          }

          update(true, true);
        }, 4500);
      }

      function stopAuto() {
        if (autoTimer) {
          clearInterval(autoTimer);
        }
        autoTimer = null;
      }

      wrap.addEventListener('mouseenter', stopAuto);
      wrap.addEventListener('mouseleave', startAuto);

      window.addEventListener('resize', function () {
        buildDots();
        update(false, true);
      });

      buildDots();
      update(false, true);
      startAuto();
    }
  }

  /* =========================
     접근 제한 공통 처리
  ========================= */
  function guardDetailLink(anchorEl, event) {
    if (!anchorEl) return;

    const tradeStatus = String(anchorEl.dataset.tradeStatus || '').trim();
    const canAccessDetail = Number(anchorEl.dataset.canAccess) === 1;

    if ((tradeStatus === '예약중' || tradeStatus === '판매완료') && !canAccessDetail) {
      event.preventDefault();

      if (tradeStatus === '예약중') {
        alert('예약중인 상품입니다.');
      }
      else {
        alert('판매완료된 상품입니다.');
      }
    }
  }

  document.addEventListener('click', function (event) {
    const nbAnchor = event.target.closest('a');
    if (nbAnchor && nbAnchor.querySelector('.nb-card')) {
      guardDetailLink(nbAnchor, event);
      return;
    }

    const freeAnchor = event.target.closest('.free-deal-link');
    if (freeAnchor) {
      guardDetailLink(freeAnchor, event);
    }
  });
})();
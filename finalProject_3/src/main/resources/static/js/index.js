(function () {
      const row = document.getElementById('bestRow');
      const viewport = document.getElementById('bestViewport');
      const track = document.getElementById('bestTrack');
      const prevBtn = document.getElementById('bestPrev');
      const nextBtn = document.getElementById('bestNext');

      const GAP = 90;
      let isMoving = false;

      function cards() { return Array.from(track.querySelectorAll('.best-card')); }
      function cardWidth() {
        const c = track.querySelector('.best-card');
        return c ? c.getBoundingClientRect().width : 260;
      }
      function step() { return cardWidth() + GAP; }

      function updateRoles() {
        const list = cards();
        list.forEach(c => c.classList.remove('is-left', 'is-center', 'is-right'));

        const vpRect = viewport.getBoundingClientRect();
        const vpCenterX = vpRect.left + (vpRect.width / 2);

        let centerIdx = 0;
        let minDist = Infinity;

        list.forEach((c, i) => {
          const r = c.getBoundingClientRect();
          const cCenterX = r.left + (r.width / 2);
          const d = Math.abs(cCenterX - vpCenterX);
          if (d < minDist) { minDist = d; centerIdx = i; }
        });

        const centerCard = list[centerIdx];
        if (centerCard) centerCard.classList.add('is-center');

        const leftCandidates = list
          .map((c, i) => ({ c, i, x: c.getBoundingClientRect().left + c.getBoundingClientRect().width / 2 }))
          .filter(o => o.i !== centerIdx && o.x < vpCenterX)
          .sort((a, b) => b.x - a.x);

        const rightCandidates = list
          .map((c, i) => ({ c, i, x: c.getBoundingClientRect().left + c.getBoundingClientRect().width / 2 }))
          .filter(o => o.i !== centerIdx && o.x > vpCenterX)
          .sort((a, b) => a.x - b.x);

        if (leftCandidates[0]) leftCandidates[0].c.classList.add('is-left');
        if (rightCandidates[0]) rightCandidates[0].c.classList.add('is-right');
      }

      function moveToPrevVisual() {
        if (isMoving) return;
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

        track.addEventListener('transitionend', function onEnd(e) {
          if (e.propertyName !== 'transform') return;
          track.removeEventListener('transitionend', onEnd);
          track.classList.remove('is-moving');
          row.classList.remove('is-animating');
          isMoving = false;
          updateRoles();
        }, { once: true });
      }

      function moveToNextVisual() {
        if (isMoving) return;
        isMoving = true;
        row.classList.add('is-animating');

        const dist = step();
        track.classList.remove('is-moving');
        track.style.transform = `translateX(${dist}px)`;

        track.appendChild(cards()[0]);

        track.getBoundingClientRect();
        track.classList.add('is-moving');
        track.style.transform = 'translateX(0px)';

        track.addEventListener('transitionend', function onEnd(e) {
          if (e.propertyName !== 'transform') return;
          track.removeEventListener('transitionend', onEnd);
          track.classList.remove('is-moving');
          row.classList.remove('is-animating');
          isMoving = false;
          updateRoles();
        }, { once: true });
      }

      prevBtn.addEventListener('click', moveToPrevVisual);
      nextBtn.addEventListener('click', moveToNextVisual);

      window.addEventListener('keydown', (e) => {
        if (e.key === 'ArrowLeft') moveToPrevVisual();
        if (e.key === 'ArrowRight') moveToNextVisual();
      });

      requestAnimationFrame(updateRoles);
      window.addEventListener('resize', () => {
        if (!isMoving) {
          track.style.transform = 'translateX(0px)';
          requestAnimationFrame(updateRoles);
        }
      });
    })();


     (function () {
      const wrap = document.querySelector('.free-deal-slider__wrap');
      const viewport = document.querySelector('.free-deal-slider__viewport');
      const track = document.querySelector('#freeDealTrack');
      const slides = Array.from(track.querySelectorAll('.free-deal-slider__item'));
      const prevBtn = document.querySelector('.free-deal-slider__nav--prev');
      const nextBtn = document.querySelector('.free-deal-slider__nav--next');
      const dotsWrap = document.querySelector('#freeDealDots');

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
        const slideW = slides[0].getBoundingClientRect().width;
        const g = parseFloat(getComputedStyle(track).gap || '0') || 0;
        return slideW + g;
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
          const b = document.createElement('button');
          b.type = 'button';
          b.className = 'free-deal-slider__dot';
          b.setAttribute('aria-label', (i + 1) + '번째 블럭');

          b.addEventListener('click', () => {
            stopAuto();

            startIndex = i * DOT_BLOCK_SIZE;

            update(true, false);

            startAuto();
          });

          dotsWrap.appendChild(b);
        }
      }

      function setActiveDot() {
        const dots = Array.from(dotsWrap.querySelectorAll('.free-deal-slider__dot'));
        const cur = currentBlock();
        dots.forEach((d, i) => d.classList.toggle('is-active', i === cur));
      }

      function applyTransform(px, animate) {
        track.style.transition = animate ? 'transform 420ms ease' : 'none';
        track.style.transform = `translateX(${px}px)`;
      }

      function update(animate = true, clamp = true) {
        if (slides.length === 0) return;

        if (clamp) {
          startIndex = Math.min(Math.max(startIndex, 0), maxStartIndex());
        } else {
          startIndex = Math.min(Math.max(startIndex, 0), slides.length - 1);
        }

        currentTranslate = -step() * startIndex;
        applyTransform(currentTranslate, animate);

        prevBtn.disabled = (startIndex <= 0);
        nextBtn.disabled = (startIndex >= maxStartIndex());

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

      prevBtn.addEventListener('click', () => {
        stopAuto();
        goPrevBlock();
        startAuto();
      });

      nextBtn.addEventListener('click', () => {
        stopAuto();
        goNextBlock();
        startAuto();
      });


      function cx(e) {
        if (e.touches && e.touches[0]) return e.touches[0].clientX;
        return e.clientX;
      }

      function onDown(e) {
        isDown = true;
        downX = cx(e);
        downTranslate = currentTranslate;
        applyTransform(currentTranslate, false);
        stopAuto();
      }

      function onMove(e) {
        if (!isDown) return;
        const dx = cx(e) - downX;
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

        const endX = cx(e.changedTouches ? e.changedTouches[0] : e);
        const dx = endX - downX;

        const threshold = Math.max(60, viewport.getBoundingClientRect().width * 0.12);

        if (dx > threshold) goPrevBlock();
        else if (dx < -threshold) goNextBlock();
        else update(true, true);

        startAuto();
      }

      viewport.addEventListener('mousedown', onDown);
      window.addEventListener('mousemove', onMove);
      window.addEventListener('mouseup', onUp);

      viewport.addEventListener('touchstart', onDown, { passive: true });
      viewport.addEventListener('touchmove', onMove, { passive: true });
      viewport.addEventListener('touchend', onUp);

      function startAuto() {
        stopAuto();
        if (slides.length <= visibleCount()) return;

        autoTimer = setInterval(() => {
          const v = visibleCount();
          const max = maxStartIndex();
          if (startIndex >= max) startIndex = 0;
          else startIndex += v;
          update(true, true);
        }, 4500);
      }

      function stopAuto() {
        if (autoTimer) clearInterval(autoTimer);
        autoTimer = null;
      }

      wrap.addEventListener('mouseenter', stopAuto);
      wrap.addEventListener('mouseleave', startAuto);

      window.addEventListener('resize', () => {
        buildDots();
        update(false, true);
      });

      buildDots();
      update(false, true);
      startAuto();
    })();
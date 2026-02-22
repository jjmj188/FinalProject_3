(function(){
      const section = document.querySelector('.evb-section');
      if(!section) return;

      const track = section.querySelector('.evb-track');
      const slides = Array.from(section.querySelectorAll('.evb-slide'));
      const dotsWrap = section.querySelector('.evb-dots');
      const btnPrev = section.querySelector('.evb-btn[data-dir="prev"]');
      const btnNext = section.querySelector('.evb-btn[data-dir="next"]');

      let current = 0;

      function maxIndex(){ return Math.max(0, slides.length - 1); }

      function slideWidth(){
        const first = slides[0];
        if(!first) return 0;
        return first.getBoundingClientRect().width;
      }

      function renderDots(){
        dotsWrap.innerHTML = '';
        for(let i=0;i<slides.length;i++){
          const b = document.createElement('button');
          b.type = 'button';
          b.className = 'evb-dot' + (i===current ? ' is-active' : '');
          b.setAttribute('aria-label', `이벤트 ${i+1} 보기`);
          b.addEventListener('click', ()=>goTo(i));
          dotsWrap.appendChild(b);
        }
      }

      function updateDots(){
        const dots = Array.from(dotsWrap.querySelectorAll('.evb-dot'));
        dots.forEach((d,i)=>d.classList.toggle('is-active', i===current));
      }

      function updateButtons(){
        btnPrev.disabled = current <= 0;
        btnNext.disabled = current >= maxIndex();
      }

      function goTo(idx){
        current = Math.min(Math.max(0, idx), maxIndex());
        const x = slideWidth() * current;
        track.style.transform = `translateX(${-x}px)`;
        updateDots();
        updateButtons();
      }

      btnPrev.addEventListener('click', ()=>goTo(current - 1));
      btnNext.addEventListener('click', ()=>goTo(current + 1));

      /* drag / swipe */
      const viewport = section.querySelector('.evb-viewport');
      let isDown=false, startX=0, startTx=0;

      function getTx(){
        const m = track.style.transform.match(/translateX\(([-0-9.]+)px\)/);
        return m ? parseFloat(m[1]) : 0;
      }

      viewport.addEventListener('pointerdown', (e)=>{
        isDown=true;
        viewport.setPointerCapture(e.pointerId);
        startX = e.clientX;
        startTx = getTx();
        track.style.transition = 'none';
      });

      viewport.addEventListener('pointermove', (e)=>{
        if(!isDown) return;
        const dx = e.clientX - startX;
        track.style.transform = `translateX(${startTx + dx}px)`;
      });

      function endDrag(e){
        if(!isDown) return;
        isDown=false;
        track.style.transition = 'transform .35s ease';

        const dx = e.clientX - startX;
        const threshold = Math.min(90, slideWidth() * 0.18);

        if(dx <= -threshold) goTo(current + 1);
        else if(dx >= threshold) goTo(current - 1);
        else goTo(current);
      }

      viewport.addEventListener('pointerup', endDrag);
      viewport.addEventListener('pointercancel', endDrag);
      viewport.addEventListener('pointerleave', (e)=>{ if(isDown) endDrag(e); });

      window.addEventListener('resize', ()=>goTo(current));

      renderDots();
      goTo(0);
    })();

    //=========================================================================================

     (function(){
      const row = document.getElementById('bestRow');
      const track = document.getElementById('bestTrack');
      const prevBtn = document.getElementById('bestPrev');
      const nextBtn = document.getElementById('bestNext');

      const GAP = 68;
      let isMoving = false;

      function cards(){ return Array.from(track.querySelectorAll('.best-card')); }

      function cardWidth(){
        const c = track.querySelector('.best-card');
        return c ? c.getBoundingClientRect().width : 210;
      }

      function step(){ return cardWidth() + GAP; }

      function updateRoles(){
        const list = cards();
        list.forEach(c=>c.classList.remove('is-left','is-center','is-right'));
        if(list[0]) list[0].classList.add('is-left');
        if(list[1]) list[1].classList.add('is-center');
        if(list[2]) list[2].classList.add('is-right');
      }

      function moveToPrevVisual(){ // 왼쪽 버튼(이전)
        if(isMoving) return;
        isMoving = true;
        row.classList.add('is-animating');

        const dist = step();

        // 트랜지션 OFF, -dist 상태 만들기
        track.classList.remove('is-moving');
        track.style.transform = `translateX(-${dist}px)`;

        // 마지막 카드를 앞으로
        const list = cards();
        track.insertBefore(list[list.length - 1], list[0]);
        updateRoles();

        // 리플로우 확정 후 0으로 애니메이션
        track.getBoundingClientRect();
        track.classList.add('is-moving');
        track.style.transform = 'translateX(0px)';

        track.addEventListener('transitionend', function onEnd(e){
          if(e.propertyName !== 'transform') return;
          track.removeEventListener('transitionend', onEnd);
          track.classList.remove('is-moving');
          row.classList.remove('is-animating');
          isMoving = false;
        });
      }

      function moveToNextVisual(){ // 오른쪽 버튼(다음)
        if(isMoving) return;
        isMoving = true;
        row.classList.add('is-animating');

        const dist = step();

        // ✅ 다음은 반대로 "첫 카드를 뒤로" 보내고 +dist 점프 후 0으로 애니메이션
        // 1) 트랜지션 OFF, +dist 상태로 점프 준비
        track.classList.remove('is-moving');
        track.style.transform = `translateX(${dist}px)`;

        // 2) 첫 카드를 맨 뒤로
        track.appendChild(cards()[0]);
        updateRoles();

        // 3) 리플로우 확정
        track.getBoundingClientRect();

        // 4) 0으로 애니메이션 (왼쪽에서 자연스럽게 들어오는 느낌)
        track.classList.add('is-moving');
        track.style.transform = 'translateX(0px)';

        track.addEventListener('transitionend', function onEnd(e){
          if(e.propertyName !== 'transform') return;
          track.removeEventListener('transitionend', onEnd);
          track.classList.remove('is-moving');
          row.classList.remove('is-animating');
          isMoving = false;
        });
      }

      prevBtn.addEventListener('click', moveToPrevVisual);
      nextBtn.addEventListener('click', moveToNextVisual);

      window.addEventListener('keydown', (e)=>{
        if(e.key === 'ArrowLeft') moveToPrevVisual();
        if(e.key === 'ArrowRight') moveToNextVisual();
      });

      updateRoles();
      window.addEventListener('resize', ()=>{
        if(!isMoving) track.style.transform = 'translateX(0px)';
      });
    })();
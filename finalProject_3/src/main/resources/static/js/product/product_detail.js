 const mainImg = document.getElementById('pdMainImage');
        const thumbImgs = document.querySelectorAll('.pd-thumb img');

        thumbImgs.forEach(img => {
            img.addEventListener('click', () => {
                // w=400 -> w=1200 (unsplash라서 이렇게 키우는게 깔끔함)
                const nextSrc = img.src.includes('w=400')
                    ? img.src.replace('w=400', 'w=1200')
                    : img.src;

                mainImg.src = nextSrc;
                mainImg.alt = img.alt.replace('썸네일', '상품 이미지');
            });
        });
        //======================================================================//
        /* 1) 슬라이더: 페이지(100%) 단위로 스크롤 -> 잘림 없음 */
        (function () {
            const viewport = document.getElementById('locViewport');
            const prevBtn = document.getElementById('locPrevBtn');
            const nextBtn = document.getElementById('locNextBtn');

            function maxScroll() { return viewport.scrollWidth - viewport.clientWidth; }

            function updateBtns() {
                prevBtn.disabled = viewport.scrollLeft <= 0;
                nextBtn.disabled = viewport.scrollLeft >= (maxScroll() - 1);
            }

            prevBtn.addEventListener('click', () => {
                viewport.scrollLeft = Math.max(0, viewport.scrollLeft - viewport.clientWidth);
                setTimeout(updateBtns, 80);
            });

            nextBtn.addEventListener('click', () => {
                viewport.scrollLeft = Math.min(maxScroll(), viewport.scrollLeft + viewport.clientWidth);
                setTimeout(updateBtns, 80);
            });

            viewport.addEventListener('scroll', updateBtns);
            window.addEventListener('resize', updateBtns);
            window.addEventListener('load', updateBtns);
            updateBtns();
        })();

        /* 2) 카카오 지도 (실패해도 슬라이더 영향 X) */
        try {
            if (window.kakao && kakao.maps && kakao.maps.load) {
                kakao.maps.load(() => {
                    const lat = 37.5133, lng = 127.1002;
                    const container = document.getElementById('locMapKakao');
                    const map = new kakao.maps.Map(container, {
                        center: new kakao.maps.LatLng(lat, lng),
                        level: 3
                    });
                    new kakao.maps.Marker({ position: new kakao.maps.LatLng(lat, lng) }).setMap(map);
                });
            }
        } catch (e) {
            console.warn('Kakao map load failed:', e);
        }

        /*====하트토글====*/
        (function () {
  const likeBtn = document.querySelector(".pd-like");
  if (!likeBtn) return;

  let liked = false; // 초기 상태 (서버에서 내려주면 그 값으로 세팅)

  likeBtn.addEventListener("click", function () {
    liked = !liked;

    const icon = likeBtn.querySelector("i");

    if (liked) {
      likeBtn.classList.add("is-active");
      icon.classList.remove("fa-regular");
      icon.classList.add("fa-solid");
    } else {
      likeBtn.classList.remove("is-active");
      icon.classList.remove("fa-solid");
      icon.classList.add("fa-regular");
    }
  });
})();
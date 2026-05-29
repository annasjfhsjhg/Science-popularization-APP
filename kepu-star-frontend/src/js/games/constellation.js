import { GAME_RESULT } from '../store.js';
import { addExp, unlockCollection } from '../store.js';
import { submitGameResult } from '../api.js';

const CORRECT_ORDER = [1, 2, 3, 4, 5, 6, 7];
let selected = [];
let isPlaying = false; // ✅ 本地锁（关键）

export function initAstronomyGame() {
  GAME_RESULT.finished = false;
  selected = [];

  const stars = document.querySelectorAll('.star');
  const tip = document.getElementById('starTip');
  const lineLayer = document.getElementById('lineLayer');

  if (lineLayer) lineLayer.innerHTML = '';
  if (tip) tip.innerText = '点击星星，按顺序连线（1 → 7）';

  stars.forEach(star => {
    star.replaceWith(star.cloneNode(true));
  });

  document.querySelectorAll('.star').forEach(star => {
    star.addEventListener('click', handleStarClick);
  });

  function handleStarClick() {
    if (GAME_RESULT.finished) {
      console.warn('游戏已结束，点击被拦截');
      return;
    }

    const id = Number(this.dataset.id);
    if (id !== CORRECT_ORDER[selected.length]) {
      tip.innerText = '❌ 顺序不对，重新连线！';
      setTimeout(resetGame, 800);
      return;
    }

    selected.push(id);
    this.classList.add('active');

    if (selected.length > 1) {
      drawLine(selected[selected.length - 2], id);
    }

    if (selected.length === 6) {
      tip.innerText = '再点一颗星完成连线！';
    } else if (selected.length === 7) {
      tip.innerText = '🎉 星座连线完成！';
      finishGame();
    } else {
      tip.innerText = `正在连线中... (${selected.length}/7)`;
    }
  }
}

function drawLine(id1, id2) {
  const svg = document.getElementById('lineLayer');
  const canvas = document.getElementById('starCanvas');

  const s1 = document.querySelector(`[data-id='${id1}']`);
  const s2 = document.querySelector(`[data-id='${id2}']`);
  if (!s1 || !s2) return;

  const r1 = s1.getBoundingClientRect();
  const r2 = s2.getBoundingClientRect();
  const rc = canvas.getBoundingClientRect();

  const x1 = r1.left + r1.width / 2 - rc.left;
  const y1 = r1.top + r1.height / 2 - rc.top;
  const x2 = r2.left + r2.width / 2 - rc.left;
  const y2 = r2.top + r2.height / 2 - rc.top;

  const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
  line.setAttribute('x1', x1);
  line.setAttribute('y1', y1);
  line.setAttribute('x2', x2);
  line.setAttribute('y2', y2);
  line.setAttribute('stroke', '#FFD700');
  line.setAttribute('stroke-width', '3');
  line.setAttribute('stroke-linecap', 'round');

  svg.appendChild(line);
}

function finishGame() {
  // ✅ 完成即清空，不跳转
  resetGame();

  if (GAME_RESULT.finished) {
    showReplayModal();
    return;
  }

  GAME_RESULT.finished = true;
  GAME_RESULT.stars = 3;

  addExp(60);
  unlockCollection('猎户座');

  showResultModal();

  submitGameResult(GAME_RESULT).catch(() => {
    console.log('后端未启动（不影响游戏）');
  });
}

function showResultModal() {
  const modal = document.createElement('div');
  modal.className = 'modal show';
  modal.innerHTML = `
    <div class="modal-content">
      <div class="big-emoji">🏆</div>
      <h3>成就已解锁！</h3>
      <p>
        经验 +60<br>
        解锁【猎户座】<br>
        解锁【天文新手】
      </p>
      <button class="pixel-btn" onclick="this.closest('.modal').remove()">
        继续探索
      </button>
    </div>
  `;
  document.body.appendChild(modal);
   window.closeModalAndRestart = () => {
    modal.remove();
    resetGame(); // ✅ 解锁 + 清线 + 可再点
  };
}

function showReplayModal() {
  const modal = document.createElement('div');
  modal.className = 'modal show';
  modal.innerHTML = `
    <div class="modal-content">
      <div class="big-emoji">🔁</div>
      <h3>恭喜再次完成猎户座！</h3>
      <p>
        你已经掌握这个星座啦 ✨<br>
        可以继续挑战哦！
      </p>
      <button class="pixel-btn" onclick="this.closest('.modal').remove()">
        继续探索
      </button>
    </div>
  `;
  document.body.appendChild(modal);
}

function resetGame() {
  isPlaying = true; // ✅ 关键
  selected = [];

  document.querySelectorAll('.star').forEach(star => {
    star.classList.remove('active');
  });

  const svg = document.getElementById('lineLayer');
  if (svg) svg.innerHTML = '';

  const tip = document.getElementById('starTip');
  if (tip) tip.innerText = '点击星星，按顺序连线（1 → 7）';
}
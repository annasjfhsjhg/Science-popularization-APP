import {
  submitGameResult,
  getCollectionList,
  getAchievementList,
  getGameLevel
} from '../api.js';

let selected = [];
let isPlaying = false;
let correctOrder = [];

/* ======================
   初始化（后端驱动）
====================== */
export async function initAstronomyGame() {
  isPlaying = true;
  selected = [];

  const tip = document.getElementById('starTip');
  const lineLayer = document.getElementById('lineLayer');

  if (lineLayer) lineLayer.innerHTML = '';
  if (tip) tip.innerText = '正在加载星座...';

  try {
    const res = await getGameLevel();
    correctOrder = res.data.correctOrder;

    renderStars(res.data.stars);
    bindStars();

    tip.innerText = '点击星星，按顺序连线';
  } catch {
    tip.innerText = '加载失败，请刷新重试';
  }
}

/* ======================
   渲染星星
====================== */
function renderStars(stars) {
  const canvas = document.getElementById('starCanvas');
  canvas.innerHTML = '';

  stars.forEach(star => {
    const el = document.createElement('div');
    el.className = 'star';
    el.dataset.id = star.id;
    el.style.left = `${star.x}px`;
    el.style.top = `${star.y}px`;
    canvas.appendChild(el);
  });
}

/* ======================
   绑定点击
====================== */
function bindStars() {
  document.querySelectorAll('.star').forEach(star =>
    star.addEventListener('click', handleStarClick)
  );
}

/* ======================
   点击逻辑
====================== */
function handleStarClick() {
  if (!isPlaying) return;

  const id = Number(this.dataset.id);
  if (id !== correctOrder[selected.length]) {
    document.getElementById('starTip').innerText = '❌ 顺序不对，重新连线！';
    setTimeout(resetGame, 800);
    return;
  }

  selected.push(id);
  this.classList.add('active');

  if (selected.length > 1) drawLine(selected[selected.length - 2], id);
  if (selected.length === correctOrder.length) finishGame();
}

/* ======================
   画线
====================== */
function drawLine(id1, id2) {
  const svg = document.getElementById('lineLayer');
  const c1 = document.querySelector(`[data-id='${id1}']`).getBoundingClientRect();
  const c2 = document.querySelector(`[data-id='${id2}']`).getBoundingClientRect();
  const box = document.getElementById('starCanvas').getBoundingClientRect();

  const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
  line.setAttribute('x1', c1.left + c1.width / 2 - box.left);
  line.setAttribute('y1', c1.top + c1.height / 2 - box.top);
  line.setAttribute('x2', c2.left + c2.width / 2 - box.left);
  line.setAttribute('y2', c2.top + c2.height / 2 - box.top);
  line.setAttribute('stroke', '#FFD700');
  line.setAttribute('stroke-width', '3');
  svg.appendChild(line);
}

/* ======================
   游戏结束
====================== */
async function finishGame() {
  isPlaying = false;
  resetGame();

  submitGameResult({
    gameType: 'astronomy',
    score: 100,
    timeUsed: 0,
    stars: 3
  }).catch(() => {
    console.log('后端未启动（不影响游戏）');
  });

  showAchievementModal(await getAchievementList());
}

/* ======================
   成就弹窗（用你现有 achievement.css）
====================== */
function showAchievementModal(list) {
  closeAllModals();

  const unlocked = list.filter(i => i.unlocked);

  const modal = document.createElement('div');
  modal.className = 'modal show';
  modal.innerHTML = `
    <div class="modal-content">
      <div class="big-emoji">🏆</div>
      <h3>成就解锁</h3>

      ${unlocked.map(a => `
        <div class="achievement-item">
          <div class="badge">🎖️</div>
          <div class="achievement-info">
            <h4>${a.name}</h4>
            <p>${a.progress}/${a.total}</p>
          </div>
        </div>
      `).join('')}

      <button class="pixel-btn" onclick="openCollectionModal()">
        查看图鉴
      </button>
      <button class="pixel-btn outline" onclick="closeAllModals()">
        继续探索
      </button>
    </div>
  `;
  document.body.appendChild(modal);
}

/* ======================
   图鉴弹窗（用你现有 collection.css）
====================== */
window.openCollectionModal = async () => {
  closeAllModals();

  const list = await getCollectionList();
  const unlocked = list.filter(i => i.unlocked);

  const modal = document.createElement('div');
  modal.className = 'modal show';
  modal.innerHTML = `
    <div class="modal-content">
      <div class="big-emoji">📖</div>
      <h3>已解锁图鉴</h3>

      <div class="collection-grid">
        ${unlocked.map(c => `
          <div class="collection-card">
            <div class="emoji">🌟</div>
            <p>${c.name}</p>
          </div>
        `).join('')}
      </div>

      <button class="pixel-btn outline" onclick="closeAllModals()">
        关闭
      </button>
    </div>
  `;
  document.body.appendChild(modal);
};

/* ======================
   工具
====================== */
window.closeAllModals = () => {
  document.querySelectorAll('.modal').forEach(m => m.remove());
  resetGame();
};

function resetGame() {
  isPlaying = true;
  selected = [];

  document.querySelectorAll('.star').forEach(star =>
    star.classList.remove('active')
  );

  const svg = document.getElementById('lineLayer');
  if (svg) svg.innerHTML = '';

  const tip = document.getElementById('starTip');
  if (tip) tip.innerText = '点击星星，按顺序连线';
}
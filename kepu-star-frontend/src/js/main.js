import { USER } from './store.js';
import { COLLECTIONS } from './store.js';
import { ACHIEVEMENTS } from './store.js';
import { initAstronomyGame } from './games/constellation.js';

/* ========== 工具 ========== */
function getLevelProgress() {
  const percent = Math.min(
    100,
    (USER.exp / USER.nextLevelExp) * 100
  );
  return { percent };
}

/* ========== 页面渲染 ========== */
function renderProfile() {
  setText('.profile-header h3', USER.nickname);
  setText('.profile-header p', `★ Lv.${USER.level} · 加入${USER.joinDays}天 ★`);
  setText('.profile-avatar', USER.avatar);

  const cards = document.querySelectorAll('.stat-card h4');
  if (cards[0]) cards[0].innerText = USER.stats.collection;
  if (cards[1]) cards[1].innerText = USER.stats.achievements;
  if (cards[2]) cards[2].innerText = USER.stats.expTotal;

  const fill = document.querySelector('.pixel-progress-fill');
  if (fill) fill.style.width = getLevelProgress().percent + '%';

  const tip = document.querySelector('.level-info span');
  if (tip) tip.innerText = `距离升级还差 ${USER.nextLevelExp - USER.exp} 经验`;
}

function renderCollection() {
  const grid = document.querySelector('.collection-grid');
  if (!grid) return;
  grid.innerHTML = '';

  COLLECTIONS.forEach(c => {
    const card = document.createElement('div');
    card.className = 'collection-card' + (c.unlocked ? '' : ' locked');
    card.innerHTML = `
      <div class="emoji">${c.unlocked ? '🌟' : '❓'}</div>
      <p>${c.unlocked ? c.name : '???'}</p>
    `;
    grid.appendChild(card);
  });
}

function renderAchievement() {
  const list = document.querySelector('#achievement .pages');
  if (!list) return;
  list.innerHTML = '';

  ACHIEVEMENTS.forEach(a => {
    const div = document.createElement('div');
    div.className = 'achievement-item';
    div.innerHTML = `
      <div class="badge ${a.unlocked ? '' : 'locked'}">
        ${a.unlocked ? '🌟' : '🔒'}
      </div>
      <div class="achievement-info">
        <h4>${a.name}</h4>
        <p>${a.desc}</p>
      </div>
    `;
    list.appendChild(div);
  });
}

/* ========== 页面切换 ========== */
function showPage(id) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  const page = document.getElementById(id);
  if (page) page.classList.add('active');

  if (id === 'profile') renderProfile();
  if (id === 'collection') renderCollection();
  if (id === 'achievement') renderAchievement();

  document.querySelector('.pages').scrollTop = 0;
}

/* ========== Tab ========== */
function switchTab(pageId, el) {
  document.querySelectorAll('.tab-item').forEach(t => t.classList.remove('active'));
  el.classList.add('active');
  showPage(pageId);
}

/* ========== 初始化 ========== */
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.tabbar .tab-item').forEach(item => {
    item.addEventListener('click', () => {
      const pageId = item.getAttribute('data-page');
      if (pageId) switchTab(pageId, item);
    });
  });

  document.querySelectorAll('.game-card').forEach(card => {
    card.addEventListener('click', () => {
      const targetPage = card.getAttribute('data-target');
      if (targetPage) showPage(targetPage);
    });
  });

  if (document.getElementById('astronomy')) {
    initAstronomyGame();
  }

  console.log('main.js loaded');
});

/* ========== 工具函数 ========== */
function setText(selector, text) {
  const el = document.querySelector(selector);
  if (el) el.innerText = text;
}

function showModal(title, text, emoji = '🌟') {
  const modal = document.createElement('div');
  modal.className = 'modal show';
  modal.innerHTML = `
    <div class="modal-content">
      <div class="big-emoji">${emoji}</div>
      <h3>${title}</h3>
      <p>${text}</p>
      <button class="pixel-btn" onclick="this.closest('.modal').remove()">
        继续探索
      </button>
    </div>
  `;
  document.body.appendChild(modal);
}

/* ========== 暴露 ========== */
window.showPage = showPage;

// 重置星座
window.resetStars = function () {
  document.querySelectorAll('.star').forEach(star => {
    star.classList.remove('active');
  });

  const svg = document.getElementById('lineLayer');
  if (svg) svg.innerHTML = '';

  const tip = document.getElementById('starTip');
  if (tip) tip.innerText = '点击两颗星把它们连起来！';
};

// 历史拼图完成
window.showAchievement = function () {
  showModal(
    '🏆 成就达成！',
    '文物拼图完成\n解锁【文物守护者】',
    '🏺'
  );
};
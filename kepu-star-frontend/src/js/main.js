const BASE_URL = 'https://10.252.136.168:8080';
import { USER } from './store.js';
import { COLLECTIONS } from './store.js';
import { ACHIEVEMENTS } from './store.js';
import { initAstronomyGame } from './games/constellation.js';
import {
  syncUserFromBackend,
  syncCollectionsFromBackend,
  syncAchievementsFromBackend
} from './store.js';

const APP_SETTINGS = {
  difficulty: 'normal'
};

const COLLECTION_CATEGORIES = {
  all: '全部',
  astronomy: '天文',
  history: '历史',
  insect: '昆虫'
};

const COLLECTION_LIBRARY = [
  { name: '猎户座', category: 'astronomy', icon: '🌟', desc: '星座连线的入门图鉴' },
  { name: '月球', category: 'astronomy', icon: '🌙', desc: '夜空中最熟悉的伙伴' },
  { name: '土星', category: 'astronomy', icon: '🪐', desc: '拥有美丽光环的行星' },
  { name: '四羊方尊', category: 'history', icon: '🏺', desc: '商代青铜礼器代表作' },
  { name: '兵马俑', category: 'history', icon: '🗿', desc: '秦始皇陵地下军团' },
  { name: '青铜鼎', category: 'history', icon: '⚱️', desc: '古代礼制与铸造工艺的象征' },
  { name: '蝴蝶', category: 'insect', icon: '🦋', desc: '完全变态发育的经典代表' },
  { name: '蜜蜂', category: 'insect', icon: '🐝', desc: '勤劳的授粉小能手' },
  { name: '瓢虫', category: 'insect', icon: '🐞', desc: '会帮植物“吃虫”的益虫' }
];

let currentCollectionCategory = 'all';


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

  const unlockedNames = new Set(COLLECTIONS.filter(c => c.unlocked).map(c => c.name));
  const visibleCards = COLLECTION_LIBRARY.filter(card => {
    return currentCollectionCategory === 'all' || card.category === currentCollectionCategory;
  });

  visibleCards.forEach(cardData => {
    const unlocked = unlockedNames.has(cardData.name);
    const card = document.createElement('div');
    card.className = 'collection-card category-card ' + cardData.category + (unlocked ? '' : ' locked');
    card.innerHTML = `
      <div class="emoji">${unlocked ? cardData.icon : '❓'}</div>
      <p>${unlocked ? cardData.name : '???'}</p>
      <small>${unlocked ? cardData.desc : '未解锁图鉴'}</small>
    `;
    grid.appendChild(card);
  });
}

function renderCollectionTabs() {
  document.querySelectorAll('.collection-tabs .tab').forEach(tab => {
    tab.classList.remove('active');
    if (tab.getAttribute('data-category') === currentCollectionCategory) {
      tab.classList.add('active');
    }
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

function renderLearningReport() {
  const reportDays = Math.min(7, Math.max(3, Math.floor(USER.joinDays / 5)));
  const reportFinish = USER.stats.collection + USER.stats.achievements;
  const strongText =
    USER.stats.collection >= USER.stats.achievements
      ? '你在知识收集上表现优秀，图鉴解锁速度很快。'
      : '你在挑战任务上完成度很高，成就推进非常稳定。';
  const suggestionText =
    USER.stats.collection < 30
      ? '建议优先补齐未解锁图鉴，形成更完整的知识网络。'
      : '建议开启挑战模式，提升答题速度和知识迁移能力。';

  setText('#report-days', String(reportDays));
  setText('#report-exp', String(USER.stats.expTotal));
  setText('#report-finish', String(reportFinish));
  setText('#report-strong', strongText);
  setText('#report-suggestion', suggestionText);
}

function renderDifficultySettings() {
  const textMap = {
    easy: '轻松模式：更多提示与引导，适合刚开始探索的小朋友。',
    normal: '普通模式：题目数量适中，提示较平衡。',
    hard: '挑战模式：题量更大、节奏更快，适合进阶玩家。'
  };

  setText('#difficulty-text', textMap[APP_SETTINGS.difficulty] || textMap.normal);

  document.querySelectorAll('.difficulty-item').forEach(item => {
    item.classList.remove('active');
  });

  const active = document.getElementById(`difficulty-${APP_SETTINGS.difficulty}`);
  if (active) active.classList.add('active');
}

function setActiveTab(pageId) {
  document.querySelectorAll('.tab-item').forEach(t => {
    t.classList.remove('active');
    if (t.getAttribute('data-page') === pageId) {
      t.classList.add('active');
    }
  });
}

function syncTabByPage(pageId) {
  const pageToTab = {
    home: 'home',
    astronomy: 'astronomy',
    history: 'history',
    insect: null,
    collection: 'collection',
    achievement: null,
    profile: 'profile',
    'learning-report': 'profile',
    'difficulty-settings': 'profile',
    'ai-assistant': 'profile',
    'parent-center': 'profile',
    'notification-center': 'profile',
    'about-us': 'profile'
  };

  const tabPage = pageToTab[pageId];
  if (tabPage) setActiveTab(tabPage);
}

/* ========== 页面切换 ========== */
function showPage(id) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  const page = document.getElementById(id);
  if (page) page.classList.add('active');

  syncTabByPage(id);


  if (id === 'profile') renderProfile();
  if (id === 'collection') renderCollection();
  if (id === 'collection') renderCollectionTabs();
  if (id === 'achievement') renderAchievement();
  if (id === 'learning-report') renderLearningReport();
  if (id === 'difficulty-settings') renderDifficultySettings();

  const pages = document.querySelector('.pages');
if (pages) pages.scrollTop = 0;
}

/* ========== Tab ========== */
function switchTab(pageId, el) {
  setActiveTab(pageId);
  showPage(pageId);
}

/* ========== 初始化 ========== */
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.collection-tabs .tab').forEach(tab => {
    tab.addEventListener('click', () => {
      const category = tab.getAttribute('data-category');
      if (!category) return;
      currentCollectionCategory = category;
      renderCollection();
      renderCollectionTabs();
    });
  });


  //登录态判断
  const token = localStorage.getItem('token');

  if (!token) {
    showPage('login');
    return; // ✅ 没登录就不初始化游戏
  }

  //已登录，先同步数据
  showPage('home');

  // Tab 切换
  document.querySelectorAll('.tabbar .tab-item').forEach(item => {
    item.addEventListener('click', () => {
      const pageId = item.getAttribute('data-page');
      if (pageId) switchTab(pageId, item);
    });
  });

  // 游戏卡片点击
  document.querySelectorAll('.game-card').forEach(card => {
    card.addEventListener('click', () => {
      const targetPage = card.getAttribute('data-target');
      if (targetPage) showPage(targetPage);
    });
  });

  // 初始化星座游戏
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
window.openProfileSection = function (pageId) {
  setActiveTab('profile');
  showPage(pageId);
};
window.setCollectionCategory = function (category) {
  currentCollectionCategory = category;
  renderCollection();
  renderCollectionTabs();
};
window.setDifficulty = function (level) {
  APP_SETTINGS.difficulty = level;
  renderDifficultySettings();

  const levelName = {
    easy: '轻松模式',
    normal: '普通模式',
    hard: '挑战模式'
  };

  showModal('设置已更新', `已切换到${levelName[level] || '普通模式'}`, '⚙️');
};

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
// 登录
window.handleLogin = async function () {
  const username = document.getElementById('username').value;
  const password = document.getElementById('password').value;

  console.log('发送 POST 登录请求 →', BASE_URL + '/api/auth/login');

  try {
    const res = await login({ username, password });

    if (res.code === 0 && res.data?.token) {
      localStorage.setItem('token', res.data.token);
      alert('登录成功 🎉');
      showPage('home');
    } else {
      alert('登录失败：' + (res.message || '未知错误'));
    }
  } catch (e) {
    alert('网络错误或后端未启动');
  }
};

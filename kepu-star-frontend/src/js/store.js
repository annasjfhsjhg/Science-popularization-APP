// 模拟用户数据
export const USER = {
  nickname: '小明同学',
  level: 5,
  exp: 1280,
  nextLevelExp: 2000,
  joinDays: 30,
  avatar: '🧒',
  stats: {
    collection: 28,
    achievements: 5,
    expTotal: 1280
  }
};
// 模拟图鉴数据
export const COLLECTIONS = [
  { id: 1, name: '猎户座', unlocked: false },
  { id: 2, name: '月球', unlocked: true },
  { id: 3, name: '四羊方尊', unlocked: true }
];

// 模拟成就数据
export const ACHIEVEMENTS = [
  {
    id: 1,
    name: '天文新手',
    desc: '解锁第一个天文图鉴',
    condition: (collections) =>
      collections.some(c => c.name === '猎户座' && c.unlocked),
    unlocked: false
  },
  {
    id: 2,
    name: '收藏达人',
    desc: '解锁 5 个图鉴',
    condition: (collections) =>
      collections.filter(c => c.unlocked).length >= 5,
    unlocked: false
  }
];

/* ===== 行为函数 ===== */
export function addExp(amount) {
  USER.exp += amount;
  USER.stats.expTotal += amount;

  while (USER.exp >= USER.nextLevelExp) {
    USER.exp -= USER.nextLevelExp;
    USER.level += 1;
    USER.nextLevelExp = Math.floor(USER.nextLevelExp * 1.2);
  }
}

export function unlockCollection(name) {
  const c = COLLECTIONS.find(i => i.name === name);
  if (c && !c.unlocked) {
    c.unlocked = true;
    USER.stats.collection += 1;
    checkAchievements();
  }
}

export function checkAchievements() {
  ACHIEVEMENTS.forEach(a => {
    if (!a.unlocked && a.condition(COLLECTIONS)) {
      a.unlocked = true;
      USER.stats.achievements += 1;
    }
  });
}

// 当前游戏结果
export let GAME_RESULT = {
  gameType: 'astronomy',
  score: 0,
  stars: 0,
  finished: false
};

// 获取等级进度
export function getLevelProgress() {
  return {
    percent: Math.min(100, (USER.exp / USER.nextLevelExp) * 100),
    text: `${USER.exp} / ${USER.nextLevelExp}`
  };
}

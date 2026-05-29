const BASE_URL = 'http://localhost:8080';
// ✅ 获取用户信息
export async function getUserInfo() {
  const res = await fetch(`${BASE_URL}/api/user/info`);
  return res.json();
}

// ✅ 提交游戏结果
export async function submitGameResult(data) {
  return fetch(`${BASE_URL}/api/game/result`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
}

// ✅ 获取图鉴列表
export async function getCollectionList() {
  const res = await fetch(`${BASE_URL}/api/collection/list`);
  return res.json();
}

// ✅ 获取成就列表
export async function getAchievementList() {
  const res = await fetch(`${BASE_URL}/api/achievement/list`);
  return res.json();
}

// ✅ 获取游戏关卡信息
export async function getGameLevel() {
  const res = await fetch(`${BASE_URL}/api/game/level?gameType=astronomy`);
  return res.json();
}
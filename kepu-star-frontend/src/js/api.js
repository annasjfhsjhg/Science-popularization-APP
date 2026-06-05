
/* ========== 用户登录（不需要 token） ========== */
export async function login(data) {
  const res = await fetch(`${BASE_URL}/api/auth/login`, {
    method: 'POST',
    mode: 'cors',
    cache: 'no-cache',
    credentials: 'omit',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    },
    body: JSON.stringify(data)
  });
  return res.json();
}

/* ========== 请求头（带 token） ========== */
function authHeader() {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token
  };
}

/* ========== 获取用户信息 ========== */
export async function getUserInfo() {
  const res = await fetch(`${BASE_URL}/api/user/info`, {
    headers: authHeader()
  });
  return res.json();
}

/* ========== 提交游戏结果 ========== */
export async function submitGameResult(data) {
  return fetch(`${BASE_URL}/api/game/result`, {
    method: 'POST',
    headers: authHeader(),
    body: JSON.stringify(data)
  });
}

/* ========== 获取图鉴列表 ========== */
export async function getCollectionList() {
  const res = await fetch(`${BASE_URL}/api/collection/list`, {
    headers: authHeader()
  });
  return res.json();
}

/* ========== 获取成就列表 ========== */
export async function getAchievementList() {
  const res = await fetch(`${BASE_URL}/api/achievement/list`, {
    headers: authHeader()
  });
  return res.json();
}

/* ========== 获取游戏关卡 ========== */
export async function getGameLevel() {
  const res = await fetch(`${BASE_URL}/api/game/level?gameType=astronomy`, {
    headers: authHeader()
  });
  return res.json();
}
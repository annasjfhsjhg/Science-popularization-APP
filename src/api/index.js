// #ifdef H5
const BASE_URL = ''
// #endif
// #ifndef H5
const BASE_URL = 'http://10.252.136.168:8080'
// #endif

function request(options) {
  return new Promise((resolve, reject) => {
    uni.request({
      ...options,
      success(res) {
        resolve(res.data)
      },
      fail(err) {
        reject(err)
      }
    })
  })
}

function authHeader() {
  return {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + uni.getStorageSync('token')
  }
}

export function login(data) {
  return request({
    url: `${BASE_URL}/api/auth/login`,
    method: 'POST',
    header: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
    data
  })
}

export function getUserInfo() {
  return request({
    url: `${BASE_URL}/api/user/info`,
    method: 'GET',
    header: authHeader()
  })
}

export function submitGameResult(data) {
  return request({
    url: `${BASE_URL}/api/game/result`,
    method: 'POST',
    header: authHeader(),
    data
  })
}

export function getCollectionList() {
  return request({
    url: `${BASE_URL}/api/collection/list`,
    method: 'GET',
    header: authHeader()
  })
}

export function getAchievementList() {
  return request({
    url: `${BASE_URL}/api/achievement/list`,
    method: 'GET',
    header: authHeader()
  })
}

export function getGameLevel() {
  return request({
    url: `${BASE_URL}/api/game/level?gameType=astronomy`,
    method: 'GET',
    header: authHeader()
  })
}

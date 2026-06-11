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
        const status = res.statusCode
        if (status === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userId')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error('未登录或Token已过期'))
          return
        }
        if (status >= 200 && status < 300) {
          resolve(res.data)
        } else {
          const err = new Error('HTTP ' + status)
          err.status = status
          err.data = res.data
          reject(err)
        }
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

export function getCollectionList(category = 'all') {
  return request({
    url: `${BASE_URL}/api/encyclopedia/list?category=${category}`,
    method: 'GET',
    header: authHeader()
  })
}

export function getUserProfile() {
  return request({
    url: `${BASE_URL}/api/user/profile`,
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

export function getDailyQuestions() {
  return request({
    url: `${BASE_URL}/api/game/daily`,
    method: 'GET',
    header: authHeader()
  })
}

export function getDashboard() {
  return request({
    url: `${BASE_URL}/api/home/dashboard`,
    method: 'GET',
    header: authHeader()
  })
}

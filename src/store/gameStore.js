import { reactive } from 'vue'

const STORAGE_KEY = 'kepuxingqiu_v1'

const defaultState = {
  user: {
    name: '小探索家',
    avatar: '🧑‍🚀',
    level: 1,
    stars: 0,
    exp: 0
  },
  encyclopedia: {
    orion: false,
    bigdipper: false,
    cassiopeia: false,
    bronze: false,
    ceramic: false,
    jade: false,
    butterfly: false,
    bee: false,
    ant: false
  },
  achievements: {
    astro_beginner: false,
    history_guardian: false,
    insect_explorer: false,
    collector: false,
    all_star: false
  },
  gameStats: {
    astronomy_completed: 0,
    history_completed: 0,
    insect_completed: 0
  }
}

function loadState() {
  try {
    const saved = uni.getStorageSync(STORAGE_KEY)
    if (saved) return JSON.parse(saved)
  } catch (e) {}
  return JSON.parse(JSON.stringify(defaultState))
}

function saveState() {
  try {
    uni.setStorageSync(STORAGE_KEY, JSON.stringify(state))
  } catch (e) {}
}

const state = reactive(loadState())

export const gameStore = {
  state,

  addStars(n) {
    state.user.stars += n
    state.user.exp += n * 10
    state.user.level = Math.floor(state.user.exp / 100) + 1
    saveState()
  },

  unlockCard(cardId) {
    if (!state.encyclopedia[cardId]) {
      state.encyclopedia[cardId] = true
      saveState()
      return true
    }
    return false
  },

  unlockAchievement(achieveId) {
    if (!state.achievements[achieveId]) {
      state.achievements[achieveId] = true
      saveState()
      return true
    }
    return false
  },

  completeGame(type) {
    if (type === 'astronomy') state.gameStats.astronomy_completed++
    if (type === 'history') state.gameStats.history_completed++
    if (type === 'insect') state.gameStats.insect_completed++
    saveState()

    // Auto-check achievements
    const enc = Object.values(state.encyclopedia).filter(v => v).length
    if (enc >= 5) this.unlockAchievement('collector')
    const g = state.gameStats
    if (g.astronomy_completed > 0 && g.history_completed > 0 && g.insect_completed > 0) {
      this.unlockAchievement('all_star')
    }
  },

  updateUser(field, value) {
    state.user[field] = value
    saveState()
  },

  resetAll() {
    const fresh = JSON.parse(JSON.stringify(defaultState))
    Object.assign(state.user, fresh.user)
    Object.assign(state.encyclopedia, fresh.encyclopedia)
    Object.assign(state.achievements, fresh.achievements)
    Object.assign(state.gameStats, fresh.gameStats)
    saveState()
  }
}

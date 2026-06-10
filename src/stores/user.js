import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = reactive({
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
  })

  const collections = reactive([
    { id: 1, name: '猎户座', category: 'astronomy', unlocked: false },
    { id: 2, name: '月球', category: 'astronomy', unlocked: true },
    { id: 3, name: '四羊方尊', category: 'history', unlocked: true }
  ])

  const achievements = reactive([
    { id: 1, name: '天文新手', desc: '解锁第一个天文图鉴', unlocked: false },
    { id: 2, name: '收藏达人', desc: '解锁 5 个图鉴', unlocked: false }
  ])

  const difficulty = ref('normal')

  function getLevelProgress() {
    return Math.min(100, (user.exp / user.nextLevelExp) * 100)
  }

  function addExp(amount) {
    user.exp += amount
    user.stats.expTotal += amount
    while (user.exp >= user.nextLevelExp) {
      user.exp -= user.nextLevelExp
      user.level += 1
      user.nextLevelExp = Math.floor(user.nextLevelExp * 1.2)
    }
  }

  function unlockCollection(name) {
    const c = collections.find(i => i.name === name)
    if (c && !c.unlocked) {
      c.unlocked = true
      user.stats.collection += 1
      checkAchievements()
    }
  }

  function checkAchievements() {
    const unlockedCount = collections.filter(c => c.unlocked).length
    achievements.forEach(a => {
      if (a.unlocked) return
      if (a.id === 1 && collections.some(c => c.name === '猎户座' && c.unlocked)) {
        a.unlocked = true
        user.stats.achievements += 1
      }
      if (a.id === 2 && unlockedCount >= 5) {
        a.unlocked = true
        user.stats.achievements += 1
      }
    })
  }

  function setDifficulty(level) {
    difficulty.value = level
  }

  function syncFromBackend(data) {
    if (!data) return
    if (data.nickname        != null) user.nickname              = data.nickname
    if (data.avatar          != null) user.avatar                = data.avatar
    if (data.avatarUrl       != null) user.avatar                = data.avatarUrl
    if (data.level           != null) user.level                 = data.level
    if (data.exp             != null) user.exp                   = data.exp
    if (data.experience      != null) { user.exp = data.experience; user.stats.expTotal = data.experience }
    if (data.nextLevelExp    != null) user.nextLevelExp           = data.nextLevelExp
    if (data.joinDays        != null) user.joinDays               = data.joinDays
    if (data.unlockedEncCount  != null) user.stats.collection    = data.unlockedEncCount
    if (data.achievementCount  != null) user.stats.achievements  = data.achievementCount
  }

  return {
    user,
    collections,
    achievements,
    difficulty,
    getLevelProgress,
    addExp,
    unlockCollection,
    checkAchievements,
    setDifficulty,
    syncFromBackend
  }
})

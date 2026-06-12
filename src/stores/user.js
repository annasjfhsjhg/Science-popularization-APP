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
    { id: 1, name: '猎户座', category: 'astronomy', description: '闪亮的猎户座，夜空中最容易辨认的星座。', unlocked: false },
    { id: 2, name: '月球', category: 'astronomy', description: '地球的唯一自然卫星，月相变化的神秘天体。', unlocked: true },
    { id: 3, name: '四羊方尊', category: 'history', description: '商代青铜礼器，雕工精美的文物瑰宝。', unlocked: false },
    { id: 4, name: '昆虫百科', category: 'insect', description: '昆虫的生命周期和演化奥秘。', unlocked: false }
  ])

  const achievements = reactive([
    { id: 1, name: '天文新手', desc: '解锁第一个天文图鉴', unlocked: false },
    { id: 2, name: '收藏达人', desc: '解锁 5 个图鉴', unlocked: false },
    { id: 3, name: '文物守护者', desc: '完成历史文物拼图', unlocked: false },
    { id: 4, name: '昆虫观察者', desc: '完成昆虫生命周期挑战', unlocked: false }
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
      const achievements = checkAchievements()
      return { unlocked: true, collection: c, achievements }
    }
    return { unlocked: false, collection: null, achievements: [] }
  }

  function unlockAchievement(name) {
    const a = achievements.find(i => i.name === name)
    if (a && !a.unlocked) {
      a.unlocked = true
      user.stats.achievements += 1
      return [a]
    }
    return []
  }

  function checkAchievements() {
    const unlockedCount = collections.filter(c => c.unlocked).length
    const unlockedAchievements = []
    achievements.forEach(a => {
      if (a.unlocked) return
      if (a.id === 1 && collections.some(c => c.name === '猎户座' && c.unlocked)) {
        a.unlocked = true
        user.stats.achievements += 1
        unlockedAchievements.push(a)
      }
      if (a.id === 2 && unlockedCount >= 5) {
        a.unlocked = true
        user.stats.achievements += 1
        unlockedAchievements.push(a)
      }
      if (a.id === 3 && collections.some(c => c.name === '四羊方尊' && c.unlocked)) {
        a.unlocked = true
        user.stats.achievements += 1
        unlockedAchievements.push(a)
      }
      if (a.id === 4 && collections.some(c => c.name === '昆虫百科' && c.unlocked)) {
        a.unlocked = true
        user.stats.achievements += 1
        unlockedAchievements.push(a)
      }
    })
    return unlockedAchievements
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

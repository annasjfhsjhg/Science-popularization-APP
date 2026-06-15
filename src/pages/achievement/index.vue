<template>
  <view class="ach-page">
    <!-- 顶部统计 -->
    <view class="ach-header">
      <view class="header-stat">
        <text class="hstat-num">{{ earnedCount }}</text>
        <text class="hstat-label">已获成就</text>
      </view>
      <view class="header-divider"></view>
      <view class="header-stat">
        <text class="hstat-num">{{ achievements.length }}</text>
        <text class="hstat-label">全部成就</text>
      </view>
      <view class="header-divider"></view>
      <view class="header-stat">
        <text class="hstat-num">{{ Math.round(earnedCount / achievements.length * 100) }}%</text>
        <text class="hstat-label">完成度</text>
      </view>
    </view>

    <!-- 总进度 -->
    <view class="total-progress">
      <view class="tp-track">
        <view class="tp-fill" :style="{ width: (earnedCount / achievements.length * 100) + '%' }"></view>
      </view>
    </view>

    <!-- 成就列表 -->
    <view class="ach-list">
      <view
        v-for="ach in achievements"
        :key="ach.id"
        class="ach-item"
        :class="{ earned: isEarned(ach.id) }"
      >
        <view class="ach-icon-wrap" :class="{ earned: isEarned(ach.id) }">
          <text class="ach-icon">{{ isEarned(ach.id) ? ach.icon : '🔒' }}</text>
        </view>
        <view class="ach-content">
          <text class="ach-name">{{ ach.name }}</text>
          <text class="ach-desc">{{ ach.desc }}</text>
          <view class="ach-prog-row" v-if="!isEarned(ach.id)">
            <view class="ach-prog-bg">
              <view class="ach-prog-fill" :style="{ width: getProgress(ach) + '%' }"></view>
            </view>
            <text class="ach-prog-text">{{ getProgressLabel(ach) }}</text>
          </view>
        </view>
        <view v-if="isEarned(ach.id)" class="earned-badge">
          <text>✓</text>
        </view>
      </view>
    </view>

    <view style="height: 150rpx;"></view>
    <CustomTabBar current="achievement" />
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { gameStore } from '@/store/gameStore'
import CustomTabBar from '@/components/CustomTabBar.vue'

const achievements = [
  {
    id: 'astro_beginner',
    icon: '🌟',
    name: '天文新手',
    desc: '完成第一个星座连线',
    type: 'astronomy',
    goal: 1
  },
  {
    id: 'history_guardian',
    icon: '🏺',
    name: '文物守护者',
    desc: '完成第一件文物拼图',
    type: 'history',
    goal: 1
  },
  {
    id: 'insect_explorer',
    icon: '🦋',
    name: '昆虫探索家',
    desc: '完成昆虫生命周期游戏',
    type: 'insect',
    goal: 1
  },
  {
    id: 'collector',
    icon: '📚',
    name: '图鉴收集者',
    desc: '解锁 5 张知识图鉴卡片',
    type: 'encyclopedia',
    goal: 5
  },
  {
    id: 'all_star',
    icon: '🏆',
    name: '全能探索家',
    desc: '完成天文、历史、昆虫全部三类游戏',
    type: 'all',
    goal: 3
  }
]

const earnedCount = computed(() =>
  achievements.filter(a => isEarned(a.id)).length
)

function isEarned(id) {
  return gameStore.state.achievements[id] === true
}

function getProgress(ach) {
  const s = gameStore.state
  switch (ach.type) {
    case 'astronomy':    return Math.min(100, (s.gameStats.astronomy_completed / ach.goal) * 100)
    case 'history':      return Math.min(100, (s.gameStats.history_completed   / ach.goal) * 100)
    case 'insect':       return Math.min(100, (s.gameStats.insect_completed    / ach.goal) * 100)
    case 'encyclopedia': {
      const cnt = Object.values(s.encyclopedia).filter(v => v).length
      return Math.min(100, (cnt / ach.goal) * 100)
    }
    case 'all': {
      const done = (s.gameStats.astronomy_completed > 0 ? 1 : 0)
               + (s.gameStats.history_completed   > 0 ? 1 : 0)
               + (s.gameStats.insect_completed    > 0 ? 1 : 0)
      return Math.min(100, (done / ach.goal) * 100)
    }
    default: return 0
  }
}

function getProgressLabel(ach) {
  const s = gameStore.state
  switch (ach.type) {
    case 'astronomy':    return `${Math.min(s.gameStats.astronomy_completed, ach.goal)} / ${ach.goal}`
    case 'history':      return `${Math.min(s.gameStats.history_completed,   ach.goal)} / ${ach.goal}`
    case 'insect':       return `${Math.min(s.gameStats.insect_completed,    ach.goal)} / ${ach.goal}`
    case 'encyclopedia': return `${Math.min(Object.values(s.encyclopedia).filter(v => v).length, ach.goal)} / ${ach.goal}`
    case 'all': {
      const done = (s.gameStats.astronomy_completed > 0 ? 1 : 0)
               + (s.gameStats.history_completed   > 0 ? 1 : 0)
               + (s.gameStats.insect_completed    > 0 ? 1 : 0)
      return `${done} / ${ach.goal}`
    }
    default: return ''
  }
}
</script>

<style scoped>
.ach-page {
  background: #FFF8F0;
  min-height: 100vh;
}

/* 顶部统计 */
.ach-header {
  background: linear-gradient(135deg, #FF6B35, #FFB347);
  padding: 36rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-around;
}

.header-stat { text-align: center; }

.hstat-num {
  display: block;
  font-size: 60rpx;
  font-weight: bold;
  color: #fff;
}

.hstat-label {
  display: block;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4rpx;
}

.header-divider {
  width: 2rpx;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.3);
}

/* 总进度 */
.total-progress {
  padding: 0 24rpx;
  margin-top: -10rpx;
}

.tp-track {
  background: rgba(255, 107, 53, 0.15);
  height: 16rpx;
  border-radius: 8rpx;
  overflow: hidden;
}

.tp-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF6B35, #FFD700);
  border-radius: 8rpx;
  transition: width 0.5s ease;
}

/* 成就列表 */
.ach-list {
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.ach-item {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  opacity: 0.65;
  transition: all 0.2s;
}

.ach-item.earned {
  opacity: 1;
  border-left: 8rpx solid #FFD700;
}

.ach-icon-wrap {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.ach-icon-wrap.earned {
  background: linear-gradient(135deg, #FFD700, #FFB300);
}

.ach-icon { font-size: 48rpx; }

.ach-content { flex: 1; }

.ach-name {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 6rpx;
}

.ach-desc {
  display: block;
  font-size: 24rpx;
  color: #888;
  margin-bottom: 12rpx;
  line-height: 1.5;
}

.ach-prog-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
}

.ach-prog-bg {
  flex: 1;
  background: #f0f0f0;
  height: 8rpx;
  border-radius: 4rpx;
  overflow: hidden;
}

.ach-prog-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF6B35, #FFB347);
  border-radius: 4rpx;
  transition: width 0.4s ease;
}

.ach-prog-text {
  font-size: 22rpx;
  color: #aaa;
  min-width: 60rpx;
  text-align: right;
}

.earned-badge {
  width: 56rpx;
  height: 56rpx;
  background: linear-gradient(135deg, #FFD700, #FFB300);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  margin-left: 16rpx;
  flex-shrink: 0;
}
</style>

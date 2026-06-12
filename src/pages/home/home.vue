<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '../../stores/user.js'
import { getDashboard } from '../../api/index.js'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import PixelStatusBar from '../../components/PixelStatusBar.vue'

const store = useUserStore()
const levelProgress = computed(() => store.getLevelProgress())
const expLeft = computed(() => store.user.nextLevelExp - store.user.exp)

const HOME_CATEGORIES = [
  { id: 'astronomy', name: '天文世界', subtitle: '星座连线', icon: '🔭' },
  { id: 'history', name: '历史文物', subtitle: '文物拼图', icon: '🏺' },
  { id: 'insect', name: '昆虫自然', subtitle: '生态探索', icon: '🦋' },
  { id: 'challenge', name: '每日挑战', subtitle: '小游戏任务', icon: '🎯' }
]
const categories = ref(HOME_CATEGORIES)
const CARD_BG = { astronomy: '#5DA2F6', history: '#FF7033', insect: '#7DC383', challenge: '#C7A6F0' }
const dailyRecommend = ref(null)

function goToCategory(cat) {
  if (cat.id === 'challenge') {
    uni.navigateTo({ url: '/pages/challenge/challenge' })
  } else if (cat.id === 'collection') {
    uni.switchTab({ url: '/pages/collection/collection' })
  } else {
    uni.navigateTo({ url: `/pages/${cat.id}/${cat.id}` })
  }
}

function goToRecommend() {
  uni.navigateTo({ url: '/pages/challenge/challenge' })
}

onMounted(async () => {
  try {
    // 3秒超时，避免后端慢导致首页卡
    const res = await Promise.race([
      getDashboard(),
      new Promise((_, reject) => setTimeout(() => reject(new Error('超时')), 3000))
    ])
    if (res.code === 0) {
      const data = res.data
      store.syncFromBackend(data)
      dailyRecommend.value = data.dailyRecommend || null
    }
  } catch (e) {
    // 后端可能未启动，提供前端回退数据，避免频繁抛错
    dailyRecommend.value = { name: '每日挑战', description: '完成小游戏，获得经验奖励！' }
    console.debug('首页数据回退到本地示例', e)
  }
})
</script>

<template>
  <PixelStatusBar />
  <view class="page-wrap">
    <view class="home-banner">
      <text class="banner-title">你好，{{ store.user.nickname || '小探险家' }}！</text>
      <text class="banner-sub">今天又是元气满满的探索日～</text>
    </view>

    <view class="level-card">
      <view class="pixel-avatar">{{ store.user.avatar }}</view>
      <view class="level-info">
        <text class="level-name">小科学家 Lv.{{ store.user.level }}</text>
        <view class="pixel-progress">
          <view class="pixel-progress-fill" :style="{ width: levelProgress + '%' }"></view>
        </view>
        <text class="exp-hint">距离升级还差 {{ expLeft }} 经验</text>
      </view>
    </view>

    <text class="section-title">★ 每日推荐</text>
    <view class="recommend-card" @tap="goToRecommend">
      <view class="rec-emoji">🔭</view>
      <view>
        <text class="rec-title">
          {{ dailyRecommend ? ('今日探索：' + dailyRecommend.name) : '今日探索：每日挑战' }}
        </text>
        <text class="rec-sub">
          {{ dailyRecommend ? dailyRecommend.description : '点击进入，完成每日小游戏，获得经验奖励！' }}
        </text>
      </view>
    </view>

    <text class="section-title">★ 游戏分类</text>
    <view class="game-grid">
      <view
        v-for="cat in categories"
        :key="cat.id"
        class="game-card"
        @tap="goToCategory(cat)"
      >
        <view class="game-icon" :style="{ background: CARD_BG[cat.id] || '#6998EC' }">{{ cat.icon }}</view>
        <text class="g-title">{{ cat.name }}</text>
        <text class="g-sub">{{ cat.subtitle }}</text>
      </view>
    </view>
  </view>
  <CustomTabBar current="home" />
</template>

<style>
.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
  margin-bottom: 24rpx;
  background: #6998EC;
  border: 4rpx solid #000;
  box-shadow: 6rpx 6rpx 0 #000;
  color: #FFEEC4;
}
.status-text,
.status-title,
.battery-text {
  font-size: 28rpx;
  font-weight: 700;
}
.status-title {
  flex: 1;
  text-align: center;
}
.battery {
  display: flex;
  align-items: center;
  gap: 10rpx;
}
.battery-icon {
  font-size: 30rpx;
}
.home-banner {
  background: #6998EC;
  border: 4rpx solid #000;
  box-shadow: 6rpx 6rpx 0 #000;
  padding: 30rpx 24rpx;
  margin-bottom: 24rpx;
}
.banner-title {
  font-size: 40rpx;
  color: #fff;
  margin-bottom: 14rpx;
}
.banner-sub {
  font-size: 28rpx;
  color: #fff;
}
.level-card {
  display: flex;
  align-items: center;
  background: #fff;
  border: 4rpx solid #000;
  box-shadow: 6rpx 6rpx 0 #000;
  padding: 24rpx;
  margin-bottom: 24rpx;
}
.pixel-avatar {
  width: 140rpx;
  height: 140rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60rpx;
  background: #FF8C42;
  border: 4rpx solid #000;
  margin-right: 24rpx;
}
.level-info {
  flex: 1;
}
.level-name {
  font-size: 30rpx;
  margin-bottom: 18rpx;
  display: block;
}
.pixel-progress {
  width: 100%;
  height: 32rpx;
  background: #f4f4f4;
  border: 4rpx solid #000;
  margin-bottom: 12rpx;
}
.pixel-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF6B35 50%, #FF8C42 50%);
}
.exp-hint {
  font-size: 24rpx;
  color: #555;
}
.recommend-card {
  display: flex;
  gap: 24rpx;
  align-items: center;
  background: #FFEF0F;
  border: 4rpx solid #000;
  box-shadow: 6rpx 6rpx 0 #000;
  padding: 24rpx;
  margin-bottom: 24rpx;
}
.rec-emoji {
  width: 120rpx;
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 52rpx;
  background: #fff;
  border: 4rpx solid #000;
}
.rec-title {
  font-size: 30rpx;
  font-weight: 700;
  margin-bottom: 10rpx;
}
.rec-sub {
  font-size: 26rpx;
  color: #333;
}
.game-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  margin-bottom: 120rpx;
}
.game-card {
  background: #fff;
  border: 4rpx solid #000;
  box-shadow: 6rpx 6rpx 0 #000;
  padding: 24rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 240rpx;
  justify-content: center;
}
.game-icon {
  width: 100rpx;
  height: 100rpx;
  background: #6998EC;
  border: 4rpx solid #000;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  margin-bottom: 12rpx;
}
.g-title {
  font-size: 28rpx;
  font-weight: 700;
  margin-bottom: 8rpx;
}
.g-sub {
  font-size: 22rpx;
  color: #555;
  text-align: center;
  line-height: 1.4;
}
</style>

<template>
  <view class="home-page">
    <!-- 顶部用户信息 -->
    <view class="user-header">
      <view class="user-info">
        <text class="avatar">{{ store.state.user.avatar }}</text>
        <view class="user-detail">
          <text class="username">{{ store.state.user.name }}</text>
          <text class="user-level">Lv.{{ store.state.user.level }} 探索家</text>
        </view>
      </view>
      <view class="stars-badge">
        <text class="star-icon">⭐</text>
        <text class="star-count">{{ store.state.user.stars }}</text>
      </view>
    </view>

    <!-- 每日推荐 -->
    <view class="daily-card" @click="go('/pages/astronomy/index')">
      <view class="daily-tag">每日推荐</view>
      <text class="daily-title">🌟 今日挑战：猎户座连线</text>
      <text class="daily-desc">完成星座连线，解锁专属知识卡片</text>
      <view class="daily-btn-row">
        <text class="daily-btn">立即挑战 →</text>
      </view>
    </view>

    <!-- 探索分类 -->
    <view class="section-title">探索世界</view>
    <view class="game-list">
      <view class="game-card astronomy-card" @click="go('/pages/astronomy/index')">
        <view class="card-left">
          <text class="card-icon">🌟</text>
        </view>
        <view class="card-right">
          <text class="card-title">天文世界</text>
          <text class="card-sub">星座连线 · {{ astroProgress }}% 完成</text>
          <view class="prog-bg">
            <view class="prog-fill" :style="{ width: astroProgress + '%' }"></view>
          </view>
        </view>
        <text class="card-arrow">›</text>
      </view>

      <view class="game-card history-card" @click="go('/pages/history/index')">
        <view class="card-left">
          <text class="card-icon">🏺</text>
        </view>
        <view class="card-right">
          <text class="card-title">历史文明</text>
          <text class="card-sub">文物拼图 · {{ historyProgress }}% 完成</text>
          <view class="prog-bg">
            <view class="prog-fill history-fill" :style="{ width: historyProgress + '%' }"></view>
          </view>
        </view>
        <text class="card-arrow">›</text>
      </view>

      <view class="game-card insect-card" @click="go('/pages/insect/index')">
        <view class="card-left">
          <text class="card-icon">🦋</text>
        </view>
        <view class="card-right">
          <text class="card-title">昆虫自然</text>
          <text class="card-sub">昆虫小游戏 · {{ insectProgress }}% 完成</text>
          <view class="prog-bg">
            <view class="prog-fill insect-fill" :style="{ width: insectProgress + '%' }"></view>
          </view>
        </view>
        <text class="card-arrow">›</text>
      </view>
    </view>

    <!-- 快速统计 -->
    <view class="stats-row">
      <view class="stat-item">
        <text class="stat-num">{{ unlockedCards }}</text>
        <text class="stat-label">已解锁图鉴</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-num">{{ earnedAchievements }}</text>
        <text class="stat-label">获得成就</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-num">{{ totalGames }}</text>
        <text class="stat-label">完成游戏</text>
      </view>
    </view>

    <view style="height: 150rpx;"></view>
    <CustomTabBar current="home" />
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { gameStore } from '@/store/gameStore'
import CustomTabBar from '@/components/CustomTabBar.vue'

const store = gameStore

const unlockedCards = computed(() =>
  Object.values(store.state.encyclopedia).filter(v => v).length
)
const earnedAchievements = computed(() =>
  Object.values(store.state.achievements).filter(v => v).length
)
const totalGames = computed(() =>
  store.state.gameStats.astronomy_completed +
  store.state.gameStats.history_completed +
  store.state.gameStats.insect_completed
)

const astroProgress = computed(() =>
  Math.min(100, Math.floor((store.state.gameStats.astronomy_completed / 3) * 100))
)
const historyProgress = computed(() =>
  Math.min(100, Math.floor((store.state.gameStats.history_completed / 3) * 100))
)
const insectProgress = computed(() =>
  Math.min(100, Math.floor((store.state.gameStats.insect_completed / 2) * 100))
)

function go(url) {
  uni.navigateTo({ url })
}
</script>

<style scoped>
.home-page {
  background: #F0F9FF;
  min-height: 100vh;
}

/* 顶部用户栏 */
.user-header {
  background: linear-gradient(135deg, #4ECDC4 0%, #45B7D1 100%);
  padding: 30rpx 40rpx 40rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.user-info {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.avatar {
  font-size: 76rpx;
  margin-right: 20rpx;
}

.username {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}

.user-level {
  display: block;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4rpx;
}

.stars-badge {
  background: rgba(255, 255, 255, 0.25);
  border-radius: 50rpx;
  padding: 12rpx 28rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
}

.star-icon {
  font-size: 32rpx;
  margin-right: 8rpx;
}

.star-count {
  font-size: 36rpx;
  font-weight: bold;
  color: #FFD700;
}

/* 每日推荐卡 */
.daily-card {
  margin: 24rpx;
  background: linear-gradient(135deg, #FF6B35 0%, #FFB347 100%);
  border-radius: 28rpx;
  padding: 28rpx;
  margin-top: -16rpx;
}

.daily-tag {
  display: inline-block;
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  font-size: 22rpx;
  padding: 4rpx 18rpx;
  border-radius: 20rpx;
  margin-bottom: 14rpx;
}

.daily-title {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 8rpx;
}

.daily-desc {
  display: block;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 20rpx;
}

.daily-btn-row {
  text-align: right;
}

.daily-btn {
  background: #fff;
  color: #FF6B35;
  font-size: 28rpx;
  font-weight: bold;
  padding: 12rpx 32rpx;
  border-radius: 40rpx;
}

/* 探索分类 */
.section-title {
  padding: 24rpx 40rpx 16rpx;
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.game-list {
  padding: 0 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.game-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.card-left {
  width: 100rpx;
  height: 100rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.astronomy-card .card-left { background: #1A1A3E; }
.history-card .card-left   { background: #6B3A2A; }
.insect-card .card-left    { background: #2E7D32; }

.card-icon {
  font-size: 56rpx;
}

.card-right {
  flex: 1;
}

.card-title {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #222;
  margin-bottom: 6rpx;
}

.card-sub {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.prog-bg {
  height: 8rpx;
  background: #f0f0f0;
  border-radius: 4rpx;
  overflow: hidden;
}

.prog-fill {
  height: 100%;
  background: linear-gradient(90deg, #4ECDC4, #45B7D1);
  border-radius: 4rpx;
  transition: width 0.4s ease;
}

.history-fill { background: linear-gradient(90deg, #FF6B35, #FFB347); }
.insect-fill  { background: linear-gradient(90deg, #4CAF50, #8BC34A); }

.card-arrow {
  font-size: 40rpx;
  color: #ccc;
  margin-left: 10rpx;
}

/* 统计行 */
.stats-row {
  margin: 24rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx 0;
  display: flex;
  flex-direction: row;
  align-items: center;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-divider {
  width: 2rpx;
  height: 60rpx;
  background: #f0f0f0;
}

.stat-num {
  display: block;
  font-size: 52rpx;
  font-weight: bold;
  color: #4ECDC4;
}

.stat-label {
  display: block;
  font-size: 22rpx;
  color: #aaa;
  margin-top: 6rpx;
}
</style>

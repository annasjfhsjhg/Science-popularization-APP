<template>
  <view class="tab-bar">
    <view
      v-for="item in tabs"
      :key="item.page"
      class="tab-item"
      :class="{ active: current === item.page }"
      @click="navigate(item)"
    >
      <text class="tab-icon">{{ item.icon }}</text>
      <text class="tab-text">{{ item.text }}</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  current: { type: String, default: 'home' }
})

const tabs = [
  { page: 'home',         text: '首页', icon: '🏠', path: '/pages/index/index' },
  { page: 'encyclopedia', text: '图鉴', icon: '📚', path: '/pages/encyclopedia/index' },
  { page: 'achievement',  text: '成就', icon: '🏆', path: '/pages/achievement/index' },
  { page: 'profile',      text: '我的', icon: '👤', path: '/pages/profile/index' }
]

function navigate(item) {
  if (item.page === props.current) return
  uni.reLaunch({ url: item.path })
}
</script>

<style scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background: #fff;
  display: flex;
  flex-direction: row;
  align-items: center;
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.08);
  z-index: 999;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10rpx 0;
}

.tab-icon {
  font-size: 44rpx;
  line-height: 1;
  filter: grayscale(80%);
  opacity: 0.5;
  transition: all 0.2s;
}

.tab-text {
  font-size: 20rpx;
  color: #aaa;
  margin-top: 6rpx;
  transition: all 0.2s;
}

.tab-item.active .tab-icon {
  filter: none;
  opacity: 1;
  transform: scale(1.1);
}

.tab-item.active .tab-text {
  color: #4ECDC4;
  font-weight: bold;
}
</style>

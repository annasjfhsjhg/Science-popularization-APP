<script setup>
const NAV_ITEMS = [
  { name: 'home', path: '/pages/home/home', icon: '🏠', label: '首页' },
  { name: 'collection', path: '/pages/collection/collection', icon: '📖', label: '图鉴' },
  { name: 'achievement', path: '/pages/achievement/achievement', icon: '🏆', label: '成就' },
  { name: 'profile', path: '/pages/profile/profile', icon: '👤', label: '我的' },
]

defineProps({
  current: { type: String, default: '' }
})

function goTo(item) {
  uni.switchTab({ url: item.path })
}
</script>

<template>
  <view id="custom-tabbar" class="tabbar">
    <view
      v-for="item in NAV_ITEMS"
      :key="item.name"
      class="tab-item"
      :class="{ active: current === item.name }"
      @tap="goTo(item)"
    >
      <text class="icon">{{ item.icon }}</text>
      <text class="label">{{ item.label }}</text>
    </view>
  </view>
</template>

<style>
.tabbar {
  flex-shrink: 0;
  height: 140rpx;
  background: #6998EC;
  border-top: 4rpx solid #000;
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12rpx 0;
  color: #FFEEC4;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.tab-item .icon {
  font-size: 44rpx;
  display: block;
  margin-bottom: 4rpx;
  opacity: 0.6;
}

.tab-item .label {
  font-size: 22rpx;
}

.tab-item.active .icon {
  opacity: 1;
  background: #FFEF0F;
  border: 2rpx solid #000;
  width: 72rpx;
  height: 72rpx;
  margin: 0 auto 4rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-item.active .label {
  color: #FFEF0F;
  font-weight: bold;
}
</style>

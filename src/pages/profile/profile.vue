<script setup>
import { useUserStore } from '../../stores/user.js'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'

const store = useUserStore()

const MENU_ITEMS = [
  { key: 'learning-report',      icon: '📊', label: '学习报告',    url: '/pages/profile/learning-report' },
  { key: 'difficulty-settings',  icon: '⚙️', label: '难度设置',    url: '/pages/profile/difficulty-settings' },
  { key: 'ai-assistant',         icon: '🤖', label: 'AI 科普助手', url: '/pages/profile/ai-assistant' },
  { key: 'parent-center',        icon: '👨‍👩‍👧', label: '家长中心',  url: '/pages/profile/parent-center' },
  { key: 'notification-center',  icon: '🔔', label: '消息通知',    url: '/pages/profile/notification-center' },
  { key: 'about-us',             icon: 'ℹ️', label: '关于我们',    url: '/pages/profile/about-us' },
]

function goToPage(url) {
  uni.navigateTo({ url })
}
</script>

<template>
  <view class="page-wrap">
    <view class="profile-header">
      <view class="profile-avatar">{{ store.user.avatar }}</view>
      <text class="p-name">{{ store.user.nickname }}</text>
      <text class="p-level">★ Lv.{{ store.user.level }} · 加入{{ store.user.joinDays }}天 ★</text>
    </view>

    <view class="stats-grid">
      <view class="stat-card">
        <text class="s-val">{{ store.user.stats.collection }}</text>
        <text class="s-label">解锁图鉴</text>
      </view>
      <view class="stat-card">
        <text class="s-val">{{ store.user.stats.achievements }}</text>
        <text class="s-label">获得成就</text>
      </view>
      <view class="stat-card">
        <text class="s-val">{{ store.user.stats.expTotal }}</text>
        <text class="s-label">探索经验</text>
      </view>
    </view>

    <view class="menu-list">
      <view
        v-for="item in MENU_ITEMS"
        :key="item.key"
        class="menu-item"
        @tap="goToPage(item.url)"
      >
        <text class="menu-icon">{{ item.icon }}</text>
        <text class="menu-label">{{ item.label }}</text>
        <text class="menu-arrow">▶</text>
      </view>
    </view>
  </view>
  <CustomTabBar current="profile" />
</template>

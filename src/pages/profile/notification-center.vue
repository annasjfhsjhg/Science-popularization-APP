<script setup>
import { onMounted } from 'vue'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import PixelStatusBar from '../../components/PixelStatusBar.vue'

onMounted(async () => {
  // 尝试从后端获取通知数据（3秒超时）
  try {
    await Promise.race([
      fetch('/api/user/notifications', {
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + uni.getStorageSync('token') }
      }).catch(() => {}),
      new Promise((_, reject) => setTimeout(() => reject(new Error('超时')), 3000))
    ])
  } catch (e) {
    console.debug('通知数据加载超时，使用本地数据', e)
  }
})
</script>

<template>
  <PixelStatusBar />
  <view class="page-wrap">
    <text class="section-title">🔔 消息通知</text>

    <view class="pixel-box notice-item">
      <text class="n-title">每日提醒</text>
      <text class="n-text">今天还差 1 次探索即可完成连续学习打卡。</text>
    </view>
    <view class="pixel-box notice-item">
      <text class="n-title">成就更新</text>
      <text class="n-text">你距离"知识达人"成就还差 4 张图鉴。</text>
    </view>
  </view>
  <CustomTabBar />
</template>


<script setup>
import { computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user.js'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import PixelStatusBar from '../../components/PixelStatusBar.vue'

const store = useUserStore()

onMounted(async () => {
  // 尝试从后端获取学习报告数据（3秒超时）
  try {
    await Promise.race([
      fetch('/api/user/learning-report', {
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + uni.getStorageSync('token') }
      }).catch(() => {}),
      new Promise((_, reject) => setTimeout(() => reject(new Error('超时')), 3000))
    ])
  } catch (e) {
    console.debug('学习报告加载超时，使用本地数据', e)
  }
})

const reportDays    = computed(() => Math.min(7, Math.max(3, Math.floor(store.user.joinDays / 5))))
const reportFinish  = computed(() => store.user.stats.collection + store.user.stats.achievements)
const strongText    = computed(() =>
  store.user.stats.collection >= store.user.stats.achievements
    ? '你在知识收集上表现优秀，图鉴解锁速度很快。'
    : '你在挑战任务上完成度很高，成就推进非常稳定。'
)
const suggestionText = computed(() =>
  store.user.stats.collection < 30
    ? '建议优先补齐未解锁图鉴，形成更完整的知识网络。'
    : '建议开启挑战模式，提升答题速度和知识迁移能力。'
)
</script>

<template>
  <PixelStatusBar />
  <view class="page-wrap">
    <text class="section-title">📊 学习报告</text>

    <view class="pixel-box">
      <text class="r-title">本周学习概览</text>
      <view class="stats-grid report-grid">
        <view class="stat-card"><text class="s-val">{{ reportDays }}</text><text class="s-label">学习天数</text></view>
        <view class="stat-card"><text class="s-val">{{ store.user.stats.expTotal }}</text><text class="s-label">累计经验</text></view>
        <view class="stat-card"><text class="s-val">{{ reportFinish }}</text><text class="s-label">完成挑战</text></view>
      </view>
    </view>

    <view class="knowledge-card">
      <text class="k-title">⭐ 优势领域</text>
      <text class="k-text">{{ strongText }}</text>
    </view>
    <view class="knowledge-card">
      <text class="k-title">🎯 学习建议</text>
      <text class="k-text">{{ suggestionText }}</text>
    </view>
    <view class="pixel-btn" @tap="uni.navigateBack()">返回我的主页</view>
  </view>
  <CustomTabBar />
</template>


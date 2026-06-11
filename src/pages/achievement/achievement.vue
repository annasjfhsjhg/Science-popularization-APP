<script setup>
import { ref, onMounted } from 'vue'
import { getAchievementList } from '../../api/index.js'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import PixelStatusBar from '../../components/PixelStatusBar.vue'

const defaultAchievements = [
  { id: 1, icon: '🌟', name: '天文新手', description: '解锁第一个天文图鉴', unlocked: true, progress: 1, total: 1 },
  { id: 2, icon: '📚', name: '收藏达人', description: '解锁 5 个图鉴', unlocked: false, progress: 3, total: 5 },
  { id: 3, icon: '🏺', name: '文物守护者', description: '完成历史文物拼图', unlocked: false, progress: 0, total: 1 },
  { id: 4, icon: '🦋', name: '昆虫观察者', description: '完成昆虫生命周期挑战', unlocked: false, progress: 0, total: 1 }
]

const achievements = ref([])
const loading = ref(false)

function getProgressPercent(item) {
  if (item.unlocked) return 100
  if (!item.total || item.total === 0) return 0
  return Math.min(100, Math.round((item.progress / item.total) * 100))
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getAchievementList()
    if (res.code === 0 && Array.isArray(res.data) && res.data.length > 0) {
      achievements.value = res.data
    } else {
      achievements.value = defaultAchievements
    }
  } catch (e) {
    console.error('成就加载失败', e)
    achievements.value = defaultAchievements
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <PixelStatusBar />
  <view class="page-wrap">
    <text class="section-title">🏆 成就中心</text>

    <view v-if="loading" style="text-align:center; padding: 40rpx;">
      <text>加载中...</text>
    </view>

    <view v-else>
      <view v-for="item in achievements" :key="item.id" class="achievement-item">
        <view class="badge" :class="{ locked: !item.unlocked }">{{ item.icon || '🔒' }}</view>
        <view class="achievement-info">
          <text class="a-name">{{ item.name }}</text>
          <text class="a-desc">{{ item.description }}</text>
          <view class="pixel-progress">
            <view class="pixel-progress-fill" :style="{ width: getProgressPercent(item) + '%' }"></view>
          </view>
          <text v-if="!item.unlocked" style="font-size: 22rpx; color: #999;">
            {{ item.progress }}/{{ item.total }}
          </text>
        </view>
      </view>
    </view>
  </view>
  <CustomTabBar current="achievement" />
</template>

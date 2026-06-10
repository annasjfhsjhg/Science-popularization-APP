<script setup>
import { ref, onMounted } from 'vue'
import { getAchievementList } from '../../api/index.js'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'

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
    if (res.code === 0) achievements.value = res.data || []
  } catch (e) {
    console.error('成就加载失败', e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <view class="page-wrap">
    <text class="section-title" style="display:block; text-align:center;">🏆 成就中心</text>

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

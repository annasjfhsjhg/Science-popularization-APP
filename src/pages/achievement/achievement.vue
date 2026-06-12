<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user.js'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import PixelStatusBar from '../../components/PixelStatusBar.vue'

const store = useUserStore()
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    // 3秒超时，避免成就页卡
    await Promise.race([
      fetch('/api/achievement/list', {
        method: 'GET',
        headers: {
          'Authorization': 'Bearer ' + uni.getStorageSync('token')
        }
      }).catch(() => {}),
      new Promise((_, reject) => setTimeout(() => reject(new Error('超时')), 3000))
    ])
  } catch (e) {
    // 超时或失败，使用本地数据
    console.debug('成就数据加载超时，使用本地数据', e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <PixelStatusBar />
  <view class="page-wrap">
    <text class="section-title">🏆 成就中心</text>

    <view>
      <view v-for="item in store.achievements" :key="item.id" class="achievement-item">
        <view class="badge" :class="{ locked: !item.unlocked }">{{ item.unlocked ? '⭐' : '🔒' }}</view>
        <view class="achievement-info">
          <text class="a-name">{{ item.name }}</text>
          <text class="a-desc">{{ item.desc }}</text>
          <view class="pixel-progress">
            <view class="pixel-progress-fill" :style="{ width: item.unlocked ? '100%' : '0%' }"></view>
          </view>
        </view>
      </view>
    </view>
  </view>
  <CustomTabBar current="achievement" />
</template>

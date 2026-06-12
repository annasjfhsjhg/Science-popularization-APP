<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user.js'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import PixelStatusBar from '../../components/PixelStatusBar.vue'

const store = useUserStore()
const activeCategory = ref('all')

const CATEGORY_ICON = { astronomy: '🌟', history: '🏺', insect: '🦋' }

const visibleCards = computed(() =>
  activeCategory.value === 'all'
    ? store.collections
    : store.collections.filter(c => c.category === activeCategory.value)
)

function getIcon(item) {
  return CATEGORY_ICON[item.category] || '📚'
}

onMounted(async () => {
  try {
    // 3秒超时，避免图鉴页卡
    await Promise.race([
      fetch('/api/encyclopedia/list?category=all', {
        method: 'GET',
        headers: {
          'Authorization': 'Bearer ' + uni.getStorageSync('token')
        }
      }).catch(() => {}),
      new Promise((_, reject) => setTimeout(() => reject(new Error('超时')), 3000))
    ])
  } catch (e) {
    console.debug('图鉴数据加载超时，使用本地数据', e)
  }
})
</script>

<template>
  <PixelStatusBar />
  <view class="page-wrap">
    <text class="section-title">📖 科普图鉴</text>

    <view class="collection-tabs">
      <view class="ctab" :class="{ active: activeCategory === 'all' }"       @tap="activeCategory = 'all'">全部</view>
      <view class="ctab" :class="{ active: activeCategory === 'astronomy' }" @tap="activeCategory = 'astronomy'">天文</view>
      <view class="ctab" :class="{ active: activeCategory === 'history' }"   @tap="activeCategory = 'history'">历史</view>
      <view class="ctab" :class="{ active: activeCategory === 'insect' }"    @tap="activeCategory = 'insect'">昆虫</view>
    </view>

    <view class="collection-grid">
      <view
        v-for="card in visibleCards"
        :key="card.id"
        class="collection-card"
        :class="{ locked: !card.unlocked }"
      >
        <text class="c-emoji">{{ card.unlocked ? getIcon(card) : '❓' }}</text>
        <text class="c-name">{{ card.unlocked ? card.name : '???' }}</text>
        <text class="c-desc">{{ card.unlocked ? card.description : '未解锁图鉴' }}</text>
      </view>
    </view>
  </view>
  <CustomTabBar current="collection" />
</template>

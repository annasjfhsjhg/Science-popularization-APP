<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCollectionList } from '../../api/index.js'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'

const activeCategory = ref('all')
const allItems = ref([])
const loading = ref(false)

const CATEGORY_ICON = { astronomy: '🌟', history: '🏺', insect: '🦋' }

const visibleCards = computed(() =>
  activeCategory.value === 'all'
    ? allItems.value
    : allItems.value.filter(c => c.category === activeCategory.value)
)

function getIcon(item) {
  return CATEGORY_ICON[item.category] || '📚'
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getCollectionList()
    if (res.code === 0) allItems.value = res.data || []
  } catch (e) {
    console.error('图鉴加载失败', e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <view class="page-wrap">
    <text class="section-title" style="display:block; text-align:center;">📖 科普图鉴</text>

    <view class="collection-tabs">
      <view class="ctab" :class="{ active: activeCategory === 'all' }"       @tap="activeCategory = 'all'">全部</view>
      <view class="ctab" :class="{ active: activeCategory === 'astronomy' }" @tap="activeCategory = 'astronomy'">天文</view>
      <view class="ctab" :class="{ active: activeCategory === 'history' }"   @tap="activeCategory = 'history'">历史</view>
      <view class="ctab" :class="{ active: activeCategory === 'insect' }"    @tap="activeCategory = 'insect'">昆虫</view>
    </view>

    <view v-if="loading" style="text-align:center; padding: 40rpx;">
      <text>加载中...</text>
    </view>

    <view v-else class="collection-grid">
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

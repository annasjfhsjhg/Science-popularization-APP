<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCollectionList } from '../../api/index.js'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import PixelStatusBar from '../../components/PixelStatusBar.vue'

const activeCategory = ref('all')
const allItems = ref([])
const loading = ref(false)

const CATEGORY_ICON = { astronomy: '🌟', history: '🏺', insect: '🦋' }
const defaultCollections = [
  { id: 1, name: '猎户座', category: 'astronomy', description: '闪亮的猎户座，夜空中最容易辨认。', unlocked: true },
  { id: 2, name: '四羊方尊', category: 'history', description: '商代青铜礼器，雕工精美。', unlocked: false },
  { id: 3, name: '金环蝴蝶', category: 'insect', description: '色彩斑斓的昆虫图鉴。', unlocked: false },
  { id: 4, name: '月球', category: 'astronomy', description: '地球的唯一自然卫星。', unlocked: true }
]

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
    if (res.code === 0 && Array.isArray(res.data) && res.data.length > 0) {
      allItems.value = res.data
    } else {
      allItems.value = defaultCollections
    }
  } catch (e) {
    console.error('图鉴加载失败', e)
    allItems.value = defaultCollections
  } finally {
    loading.value = false
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

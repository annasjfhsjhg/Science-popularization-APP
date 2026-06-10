<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user.js'
import { getDashboard } from '../../api/index.js'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'

const store = useUserStore()
const levelProgress = computed(() => store.getLevelProgress())
const expLeft = computed(() => store.user.nextLevelExp - store.user.exp)

const categories = ref([])
const dailyRecommend = ref(null)

// 分类名称 → 路由页面名（匹配数据库存的中文名）
const CATEGORY_ROUTE = {
  '天文世界': 'astronomy',
  '历史文明': 'history',
  '昆虫自然': 'insect',
}
// 分类名称 → 图标兜底
const CATEGORY_ICON_FALLBACK = {
  '天文世界': '🌌',
  '历史文明': '🏺',
  '昆虫自然': '🦋',
}
// 分类名称 → 副标题兜底
const CATEGORY_SUBTITLE = {
  '天文世界': '星座连线',
  '历史文明': '文物拼图',
  '昆虫自然': '生态探索',
}
// 推荐卡片分类 → 路由
const RECOMMEND_ROUTE = { astronomy: 'astronomy', history: 'history', insect: 'insect' }

function getCategoryIcon(cat) {
  return cat.icon || CATEGORY_ICON_FALLBACK[cat.name] || '🎮'
}

function getCategorySubtitle(cat) {
  return cat.description || CATEGORY_SUBTITLE[cat.name] || '开始探索'
}

function goToCategory(cat) {
  const page = CATEGORY_ROUTE[cat.name]
  if (!page) return
  const tabPages = ['astronomy', 'history', 'collection']
  if (tabPages.includes(page)) {
    uni.switchTab({ url: `/pages/${page}/${page}` })
  } else {
    uni.navigateTo({ url: `/pages/${page}/${page}` })
  }
}

function goToRecommend() {
  const page = dailyRecommend.value
    ? (RECOMMEND_ROUTE[dailyRecommend.value.category] || 'astronomy')
    : 'astronomy'
  uni.switchTab({ url: `/pages/${page}/${page}` })
}

function goTo(name) {
  const tabPages = ['astronomy', 'history', 'collection']
  if (tabPages.includes(name)) {
    uni.switchTab({ url: `/pages/${name}/${name}` })
  } else {
    uni.navigateTo({ url: `/pages/${name}/${name}` })
  }
}

onMounted(async () => {
  try {
    const res = await getDashboard()
    if (res.code === 0) {
      const data = res.data
      store.syncFromBackend(data)
      categories.value = data.categories || []
      dailyRecommend.value = data.dailyRecommend || null
    }
  } catch (e) {
    console.error('首页数据加载失败', e)
  }
})
</script>

<template>
  <view class="page-wrap">
    <view class="home-banner">
      <text class="banner-title">你好，{{ store.user.nickname || '小探险家' }}！</text>
      <text class="banner-sub">今天又是元气满满的探索日～</text>
      <text class="banner-star">★</text>
    </view>

    <view class="level-card">
      <view class="pixel-avatar">{{ store.user.avatar }}</view>
      <view class="level-info">
        <text class="level-name">小科学家 Lv.{{ store.user.level }}</text>
        <view class="pixel-progress">
          <view class="pixel-progress-fill" :style="{ width: levelProgress + '%' }"></view>
        </view>
        <text class="exp-hint">距离升级还差 {{ expLeft }} 经验</text>
      </view>
    </view>

    <text class="section-title">★ 每日推荐</text>
    <view class="recommend-card" @tap="goToRecommend">
      <view class="rec-emoji">
        {{ dailyRecommend ? (CATEGORY_ICON_FALLBACK[dailyRecommend.category] || '🔭') : '🔭' }}
      </view>
      <view>
        <text class="rec-title">
          {{ dailyRecommend ? ('今日探索：' + dailyRecommend.name) : '今日探索：猎户座的秘密' }}
        </text>
        <text class="rec-sub">
          {{ dailyRecommend ? dailyRecommend.description : '点击进入，开启星空之旅！' }}
        </text>
      </view>
    </view>

    <text class="section-title">★ 游戏分类</text>
    <view class="game-grid">
      <!-- 后端分类数据 -->
      <view
        v-for="cat in categories"
        :key="cat.id"
        class="game-card"
        @tap="goToCategory(cat)"
      >
        <view class="game-icon" :class="'icon-' + (CATEGORY_ROUTE[cat.name] || 'blue')">
          {{ getCategoryIcon(cat) }}
        </view>
        <text class="g-title">{{ cat.name }}</text>
        <text class="g-sub">{{ getCategorySubtitle(cat) }}</text>
        <text v-if="cat.totalCount > 0" class="g-progress">
          {{ cat.completedCount }}/{{ cat.totalCount }}
        </text>
      </view>
      <!-- 科普图鉴固定入口（不是游戏分类，不来自后端） -->
      <view class="game-card" @tap="goTo('collection')">
        <view class="game-icon icon-purple">📖</view>
        <text class="g-title">科普图鉴</text>
        <text class="g-sub">收集知识</text>
      </view>
    </view>
  </view>
  <CustomTabBar current="home" />
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '../../stores/user.js'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'

const store = useUserStore()
const activeCategory = ref('all')

const COLLECTION_LIBRARY = [
  { name: '猎户座', category: 'astronomy', icon: '🌟', desc: '星座连线的入门图鉴' },
  { name: '月球',   category: 'astronomy', icon: '🌙', desc: '夜空中最熟悉的伙伴' },
  { name: '土星',   category: 'astronomy', icon: '🪐', desc: '拥有美丽光环的行星' },
  { name: '四羊方尊', category: 'history', icon: '🏺', desc: '商代青铜礼器代表作' },
  { name: '兵马俑',  category: 'history', icon: '🗿', desc: '秦始皇陵地下军团' },
  { name: '青铜鼎',  category: 'history', icon: '⚱️', desc: '古代礼制与铸造工艺的象征' },
  { name: '蝴蝶',   category: 'insect', icon: '🦋', desc: '完全变态发育的经典代表' },
  { name: '蜜蜂',   category: 'insect', icon: '🐝', desc: '勤劳的授粉小能手' },
  { name: '瓢虫',   category: 'insect', icon: '🐞', desc: '会帮植物"吃虫"的益虫' },
]

const unlockedNames = computed(() =>
  new Set(store.collections.filter(c => c.unlocked).map(c => c.name))
)

const visibleCards = computed(() =>
  COLLECTION_LIBRARY.filter(c => activeCategory.value === 'all' || c.category === activeCategory.value)
)
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

    <view class="collection-grid">
      <view
        v-for="card in visibleCards"
        :key="card.name"
        class="collection-card"
        :class="{ locked: !unlockedNames.has(card.name) }"
      >
        <text class="c-emoji">{{ unlockedNames.has(card.name) ? card.icon : '❓' }}</text>
        <text class="c-name">{{ unlockedNames.has(card.name) ? card.name : '???' }}</text>
        <text class="c-desc">{{ unlockedNames.has(card.name) ? card.desc : '未解锁图鉴' }}</text>
      </view>
    </view>
  </view>
  <CustomTabBar current="collection" />
</template>

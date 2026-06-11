<script setup>
import { computed } from 'vue'
import { useUserStore } from '../../stores/user.js'
import { useModal } from '../../composables/useModal.js'
import PixelModal from '../../components/PixelModal.vue'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import PixelStatusBar from '../../components/PixelStatusBar.vue'

const store = useUserStore()
const { showModal } = useModal()

const difficultyText = computed(() => ({
  easy:   '轻松模式：更多提示与引导，适合刚开始探索的小朋友。',
  normal: '普通模式：题目数量适中，提示较平衡。',
  hard:   '挑战模式：题量更大、节奏更快，适合进阶玩家。'
}[store.difficulty] || '普通模式：题目数量适中，提示较平衡。'))

const LEVELS = [
  { key: 'easy',   icon: '🌱', label: '轻松模式', desc: '更多提示，节奏更慢' },
  { key: 'normal', icon: '⚡', label: '普通模式', desc: '推荐给大多数小探险家' },
  { key: 'hard',   icon: '🔥', label: '挑战模式', desc: '题目更多，限时更短' },
]

function setDifficulty(level) {
  store.setDifficulty(level)
  const name = { easy: '轻松模式', normal: '普通模式', hard: '挑战模式' }[level]
  showModal('设置已更新', `已切换到${name}`, '⚙️')
}
</script>

<template>
  <PixelStatusBar />
  <view class="page-wrap">
    <text class="section-title">⚙️ 难度设置</text>

    <view class="pixel-box">
      <text style="display:block; font-size:28rpx; font-weight:bold; margin-bottom:20rpx;">当前难度</text>
      <text class="difficulty-tip">{{ difficultyText }}</text>
    </view>

    <view class="difficulty-list">
      <view
        v-for="level in LEVELS"
        :key="level.key"
        class="difficulty-item"
        :class="{ active: store.difficulty === level.key }"
        @tap="setDifficulty(level.key)"
      >
        <text class="d-title">{{ level.icon }} {{ level.label }}</text>
        <text class="d-desc">{{ level.desc }}</text>
      </view>
    </view>
  </view>
  <PixelModal />
  <CustomTabBar />
</template>

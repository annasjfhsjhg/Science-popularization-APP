<script setup>
import { ref, computed, onMounted } from 'vue'
import { useModal } from '../../composables/useModal.js'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import PixelModal from '../../components/PixelModal.vue'
import PixelStatusBar from '../../components/PixelStatusBar.vue'
import { useUserStore } from '../../stores/user.js'
import { submitGameResult } from '../../api/index.js'

const { showModal } = useModal()
const userStore = useUserStore()
const correctOrder = ['eggs', 'larva', 'pupa', 'adult']
const initialItems = [
  { id: 'eggs', icon: '🥚', label: '卵' },
  { id: 'larva', icon: '🐛', label: '幼虫' },
  { id: 'pupa', icon: '🛡️', label: '蛹' },
  { id: 'adult', icon: '🦋', label: '成虫' }
]
const items = ref(shuffle(initialItems))
const selectedStages = ref([])
const selectedCount = ref(0)
const tip = ref('按正确顺序点击生命周期阶段')

const stageDescriptions = {
  eggs: '卵壳内慢慢孕育新生命。',
  larva: '幼虫时期贪吃长大，准备化蛹。',
  pupa: '蛹内重组身体，变成翩翩蝶。',
  adult: '展翅飞翔，完成生命奇迹。'
}

const selectedLabels = computed(() => {
  return selectedStages.value
    .map(id => initialItems.find(item => item.id === id)?.label || '')
    .filter(Boolean)
    .join(' → ')
})

function shuffle(arr) {
  return arr.slice().sort(() => Math.random() - 0.5)
}

function resetGame() {
  selectedCount.value = 0
  selectedStages.value = []
  tip.value = '按正确顺序点击生命周期阶段'
  items.value = shuffle(initialItems)
}

function goToHome() {
  uni.switchTab({ url: '/pages/home/home' })
}

function handleItemTap(item) {
  if (selectedCount.value >= correctOrder.length) return
  if (item.id !== correctOrder[selectedCount.value]) {
    tip.value = '顺序不对，重新开始！'
    setTimeout(resetGame, 800)
    return
  }

  selectedCount.value += 1
  selectedStages.value.push(item.id)

  if (selectedCount.value === correctOrder.length) {
    const exp = 60
    userStore.addExp(exp)
    const collectionResult = userStore.unlockCollection('昆虫百科')
    const newAchievements = collectionResult.achievements || []

    const parts = [`获得经验值 +${exp}`]
    if (collectionResult.unlocked) {
      parts.push(`解锁图鉴：${collectionResult.collection.name}`)
    }
    if (newAchievements.length > 0) {
      parts.push(`获得成就：${newAchievements.map(a => a.name).join('、')}`)
    }
    if (!collectionResult.unlocked && newAchievements.length === 0) {
      parts.push('已有图鉴/成就，仅获得经验奖励。')
    }

    tip.value = '完成啦！'
    showModal('🎉 完成了！', parts.join('\n'), '🦋')

    submitGameResult({
      gameType: 'insect',
      score: exp,
      timeUsed: 0,
      detail: '昆虫生命周期完成'
    }).catch(() => {})
  } else {
    tip.value = `已完成 ${selectedCount.value} 步：${stageDescriptions[item.id]}`
  }
}

  

function isSelected(item) {
  return selectedStages.value.includes(item.id)
}

onMounted(async () => {
  // 尝试从后端获取昆虫数据（3秒超时）
  try {
    await Promise.race([
      fetch('/api/game/insect-stages', {
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + uni.getStorageSync('token') }
      }).catch(() => {}),
      new Promise((_, reject) => setTimeout(() => reject(new Error('超时')), 3000))
    ])
  } catch (e) {
    console.debug('昆虫游戏数据加载超时，使用本地数据', e)
  }
})
</script>

<template>
  <PixelStatusBar />
  <view class="page-wrap">    <view class="title-row">      <view class="back-btn" @tap="goToHome">◄</view>    <text class="section-title">🦋 生命周期</text>    </view>

    <view class="insect-area">
        <text class="i-tip">{{ tip }}</text>
        <view class="order-status">
          <text class="order-label">当前进度：</text>
          <text class="order-sequence">{{ selectedLabels || '尚未开始' }}</text>
          <view class="pixel-btn small-btn" @tap="resetGame">重置</view>
        </view>
      <view class="insect-list">
        <view
          v-for="item in items"
          :key="item.id"
          class="insect-item"
          :class="{ selected: isSelected(item) }"
          @tap="handleItemTap(item)"
        >
          <text>{{ item.icon }}</text>
          <text style="font-size:24rpx; margin-top:8rpx;">{{ item.label }}</text>
        </view>
      </view>
    </view>

    <view class="knowledge-card">
      <text class="k-title">🦋 蝴蝶的一生</text>
      <text class="k-text">卵 → 幼虫 → 蛹 → 成虫，这种神奇变化叫"完全变态发育"！</text>
    </view>
    <view class="knowledge-card">
      <text class="k-title">🌿 你知道吗？</text>
      <text class="k-text">全世界有超过 18,000 种蝴蝶，最大的翅膀展开可达 30 厘米！</text>
    </view>
  </view>
  <CustomTabBar current="insect" />
  <PixelModal />
</template>

<style scoped>
.insect-area {
  background: #7DC383;
  background-image:
    linear-gradient(#6BB372 2px, transparent 2px),
    linear-gradient(90deg, #6BB372 2px, transparent 2px);
  background-size: 32rpx 32rpx;
  border: 4rpx solid #000;
  box-shadow: 6rpx 6rpx 0 #000;
  padding: 32rpx;
  margin-bottom: 28rpx;
}
.i-tip {
  color: #fff;
  text-align: center;
  margin-bottom: 24rpx;
  font-size: 28rpx;
  text-shadow: 2px 2px 0 #000;
  display: block;
  font-family: Arial, 'Microsoft YaHei', sans-serif;
}
.order-status {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
  background: rgba(255, 255, 255, 0.95);
  border: 4rpx solid #000;
  box-shadow: 6rpx 6rpx 0 #000;
  padding: 20rpx;
  margin-bottom: 24rpx;
}
.order-label {
  font-size: 24rpx;
  font-weight: 700;
  font-family: Arial, 'Microsoft YaHei', sans-serif;
}
.order-sequence {
  flex: 1;
  font-size: 24rpx;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: Arial, 'Microsoft YaHei', sans-serif;
}
.order-actions {
  display: flex;
  gap: 18rpx;
  margin-bottom: 24rpx;
}
.pixel-btn.small-btn {
  padding: 16rpx 20rpx;
  font-size: 24rpx;
}
.insect-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  justify-content: center;
}
.insect-item {
  width: 132rpx;
  height: 132rpx;
  border: 4rpx solid #000;
  box-shadow: 6rpx 6rpx 0 #000;
  background: linear-gradient(135deg, #FFEF0F 0%, #fff 100%);
  transition: transform 0.1s ease, background 0.1s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  text-shadow: 1px 1px 0 #000;
}
.insect-item.selected {
  background: #FFEF0F;
  transform: translate(4px, 4px);
  box-shadow: 0 0 0 #000;
}
.insect-item text {
  font-size: 30rpx;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  border: 3px solid #000;
  box-shadow: 4rpx 4rpx 0 #000;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  background: #FFEF0F;
  cursor: pointer;
  flex-shrink: 0;
}
.back-btn:active {
  transform: translate(4px, 4px);
  box-shadow: 0 0 0 #000;
}
</style>

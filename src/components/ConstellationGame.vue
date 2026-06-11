<script setup>
import { ref, onMounted, nextTick, getCurrentInstance } from 'vue'
import { getGameLevel, submitGameResult } from '../api/index.js'
import { useUserStore } from '../stores/user.js'

const emit = defineEmits(['game-complete'])
const instance = getCurrentInstance()
const userStore = useUserStore()

const tip = ref('点击两颗星把它们连起来！')
const stars = ref([])
const activeStarIds = ref(new Set())
const lines = ref([])
const selected = ref([])
const correctOrder = ref([])
const isPlaying = ref(false)
const canvasWidth = ref(0)
const canvasHeight = ref(0)

const defaultStars = [
  { id: 1, x: 70, y: 50 },
  { id: 2, x: 140, y: 90 },
  { id: 3, x: 210, y: 130 },
  { id: 4, x: 270, y: 100 },
  { id: 5, x: 110, y: 180 },
  { id: 6, x: 180, y: 220 },
  { id: 7, x: 260, y: 240 }
]

onMounted(async () => {
  await initAstronomyGame()
})

async function initAstronomyGame() {
  tip.value = '正在加载星座...'
  isPlaying.value = true
  selected.value = []
  activeStarIds.value = new Set()
  lines.value = []

  try {
    const res = await Promise.race([
      getGameLevel(),
      new Promise(resolve => setTimeout(() => resolve(null), 800))
    ])
    const data = res?.data || res || {}
    stars.value = Array.isArray(data.stars) && data.stars.length ? data.stars : defaultStars
    correctOrder.value = Array.isArray(data.correctOrder) && data.correctOrder.length
      ? data.correctOrder
      : stars.value.map(star => star.id)
  } catch {
    stars.value = defaultStars
    correctOrder.value = defaultStars.map(star => star.id)
  }

  await nextTick()
  await updateCanvasSize()
  normalizeStarPositions()
  tip.value = '点击星星，按顺序连线'
}

function updateCanvasSize() {
  return new Promise(resolve => {
    const query = uni.createSelectorQuery().in(instance.proxy)
    query.select('.star-canvas').boundingClientRect(rect => {
      if (rect) {
        canvasWidth.value = rect.width
        canvasHeight.value = rect.height
      }
      resolve()
    }).exec()
  })
}

function normalizeStarPositions() {
  if (!canvasWidth.value || !canvasHeight.value || stars.value.length === 0) return

  const xs = stars.value.map(star => star.x)
  const ys = stars.value.map(star => star.y)
  const minX = Math.min(...xs)
  const maxX = Math.max(...xs)
  const minY = Math.min(...ys)
  const maxY = Math.max(...ys)

  const margin = 24
  const sourceWidth = maxX - minX
  const sourceHeight = maxY - minY
  const targetWidth = Math.max(canvasWidth.value - margin * 2, sourceWidth)
  const targetHeight = Math.max(canvasHeight.value - margin * 2, sourceHeight)

  const offsetX = (targetWidth - sourceWidth) / 2 - minX + margin
  const offsetY = (targetHeight - sourceHeight) / 2 - minY + margin

  stars.value = stars.value.map(star => ({
    ...star,
    x: star.x + offsetX,
    y: star.y + offsetY
  }))
}

function handleStarClick(id) {
  if (!isPlaying.value) return
  if (selected.value.includes(id)) return

  const expectedId = correctOrder.value[selected.value.length]
  if (id !== expectedId) {
    isPlaying.value = false
    tip.value = '❌ 顺序不对，重新连线！'
    setTimeout(reset, 800)
    return
  }

  selected.value.push(id)
  activeStarIds.value = new Set(selected.value)

  if (selected.value.length > 1) {
    const fromId = selected.value[selected.value.length - 2]
    const fromStar = stars.value.find(star => star.id === fromId)
    const toStar = stars.value.find(star => star.id === id)
    if (fromStar && toStar) {
      lines.value.push({
        x1: fromStar.x + 8,
        y1: fromStar.y + 8,
        x2: toStar.x + 8,
        y2: toStar.y + 8
      })
    }
  }

  if (selected.value.length === correctOrder.value.length) {
    finishGame()
  } else {
    tip.value = `已连 ${selected.value.length} 颗星！`
  }
}

async function finishGame() {
  isPlaying.value = false
  tip.value = '⭐ 太棒了！星座连线完成！'

  const score = 100
  userStore.addExp(score)

  const collectionResult = userStore.unlockCollection('猎户座')
  const newAchievements = collectionResult.achievements || []

  submitGameResult({
    gameType: 'astronomy',
    score,
    timeUsed: 0,
    stars: 3
  }).catch(() => {})

  emit('game-complete', {
    score,
    unlockedCollection: collectionResult.unlocked ? collectionResult.collection.name : null,
    achievements: newAchievements
  })
}

function reset() {
  isPlaying.value = true
  selected.value = []
  activeStarIds.value = new Set()
  lines.value = []
  tip.value = '点击星星，按顺序连线'
}

defineExpose({ reset, initAstronomyGame })
</script>

<template>
  <view class="star-canvas">
    <text class="star-tip">{{ tip }}</text>

    <svg id="lineLayer" class="line-layer" xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" preserveAspectRatio="none">
      <line
        v-for="(line, index) in lines"
        :key="index"
        :x1="line.x1"
        :y1="line.y1"
        :x2="line.x2"
        :y2="line.y2"
        stroke="#FFD700"
        stroke-width="3"
        stroke-linecap="round"
      />
    </svg>

    <view
      v-for="star in stars"
      :key="star.id"
      :id="`star-${star.id}`"
      class="star"
      :class="{ active: activeStarIds.has(star.id) }"
      :style="{ top: star.y + 'px', left: star.x + 'px' }"
      @tap="handleStarClick(star.id)"
    />
  </view>
</template>

<style scoped>
.line-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}
.star {
  z-index: 2;
}
.star-tip {
  z-index: 3;
}
</style>

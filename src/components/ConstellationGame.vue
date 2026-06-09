<script setup>
import { ref, reactive, onMounted, getCurrentInstance, nextTick } from 'vue'
import { getGameLevel, submitGameResult, getAchievementList } from '../api/index.js'

const emit = defineEmits(['game-complete'])
const instance = getCurrentInstance()

const CANVAS_ID = 'constellation-canvas'
const tip = ref('点击两颗星把它们连起来！')

const stars = reactive([
  { id: 1, x: 35,  y: 25  },
  { id: 2, x: 70,  y: 45  },
  { id: 3, x: 105, y: 65  },
  { id: 4, x: 135, y: 50  },
  { id: 5, x: 55,  y: 90  },
  { id: 6, x: 90,  y: 110 },
  { id: 7, x: 130, y: 120 },
])

const activeStarIds = ref(new Set())
// 存储每颗星的像素坐标（来自 SelectorQuery）
const starPositions = ref({})
// 已绘制的连线
const lines = ref([])
let canvasWidth = 0
let canvasHeight = 0
let selected = []
let isPlaying = true
let correctOrder = []

onMounted(async () => {
  try {
    const res = await getGameLevel()
    if (res?.data?.stars?.length) {
      stars.splice(0, stars.length, ...res.data.stars)
    }
    if (res?.data?.correctOrder?.length) {
      correctOrder = res.data.correctOrder
    }
    tip.value = '点击星星，按顺序连线'
  } catch {
    tip.value = '点击两颗星把它们连起来！'
  }

  // 等待 DOM 渲染完成后获取坐标
  await nextTick()
  await getCanvasSize()
  await getStarPositions()
})

function getCanvasSize() {
  return new Promise(resolve => {
    const query = uni.createSelectorQuery().in(instance.proxy)
    query.select('.star-canvas').boundingClientRect(rect => {
      if (rect) {
        canvasWidth = rect.width
        canvasHeight = rect.height
      }
      resolve()
    }).exec()
  })
}

function getStarPositions() {
  return new Promise(resolve => {
    const query = uni.createSelectorQuery().in(instance.proxy)
    let pending = stars.length
    stars.forEach(star => {
      query.select(`#star-${star.id}`).boundingClientRect(rect => {
        if (rect) {
          starPositions.value[star.id] = {
            x: rect.left + rect.width / 2,
            y: rect.top + rect.height / 2
          }
        }
        pending--
        if (pending === 0) resolve()
      })
    })
    query.exec()
  })
}

async function handleStarClick(id) {
  if (!isPlaying) return

  // 如果坐标还没准备好，重新获取一次
  if (Object.keys(starPositions.value).length < stars.length) {
    await getCanvasSize()
    await getStarPositions()
  }

  if (correctOrder.length > 0 && id !== correctOrder[selected.length]) {
    tip.value = '❌ 顺序不对，重新连线！'
    setTimeout(reset, 800)
    return
  }

  selected.push(id)
  activeStarIds.value = new Set(selected)

  if (selected.length > 1) {
    const fromId = selected[selected.length - 2]
    const p1 = starPositions.value[fromId]
    const p2 = starPositions.value[id]
    if (p1 && p2) {
      lines.value.push({ x1: p1.x, y1: p1.y, x2: p2.x, y2: p2.y })
      drawCanvas()
    }
  }

  if (correctOrder.length > 0 && selected.length === correctOrder.length) {
    finishGame()
  } else if (correctOrder.length === 0 && selected.length >= 2) {
    tip.value = `已连 ${selected.length} 颗星！`
  }
}

function drawCanvas() {
  const ctx = uni.createCanvasContext(CANVAS_ID, instance.proxy)
  ctx.clearRect(0, 0, canvasWidth, canvasHeight)
  ctx.setStrokeStyle('#FFD700')
  ctx.setLineWidth(3)
  ctx.beginPath()
  lines.value.forEach(line => {
    ctx.moveTo(line.x1, line.y1)
    ctx.lineTo(line.x2, line.y2)
  })
  ctx.stroke()
  ctx.draw()
}

async function finishGame() {
  isPlaying = false
  tip.value = '⭐ 太棒了！星座连线完成！'
  submitGameResult({ gameType: 'astronomy', score: 100, timeUsed: 0, stars: 3 }).catch(() => {})
  try {
    const list = await getAchievementList()
    emit('game-complete', Array.isArray(list) ? list : [])
  } catch {
    emit('game-complete', [])
  }
}

function reset() {
  isPlaying = true
  selected = []
  activeStarIds.value = new Set()
  lines.value = []
  const ctx = uni.createCanvasContext(CANVAS_ID, instance.proxy)
  ctx.clearRect(0, 0, canvasWidth, canvasHeight)
  ctx.draw()
  tip.value = '点击星星，按顺序连线'
}

defineExpose({ reset })
</script>

<template>
  <view class="star-canvas">
    <text class="star-tip">{{ tip }}</text>

    <!-- canvas 层：绘制连线，不响应点击 -->
    <canvas
      :canvas-id="CANVAS_ID"
      class="constellation-canvas"
    />

    <!-- 星星层：响应点击 -->
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

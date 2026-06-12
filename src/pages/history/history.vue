<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useModal } from '../../composables/useModal.js'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import PixelModal from '../../components/PixelModal.vue'
import PixelStatusBar from '../../components/PixelStatusBar.vue'
import { useUserStore } from '../../stores/user.js'
import { submitGameResult } from '../../api/index.js'

const { showModal } = useModal()
const userStore = useUserStore()
const puzzleImageUrl = '/static/history-puzzle.svg'

const pieces = ref([])
const remainingPieces = ref([])
const placeholderPiece = ref(null)
const gridSlots = reactive(Array.from({ length: 9 }, () => ({ value: null })))
const isDragging = ref(false)
const dragPiece = ref(null)
const dragStyle = reactive({ left: '0px', top: '0px', display: 'none', width: '72px', height: '72px' })
const lastPointer = reactive({ x: NaN, y: NaN })
const topToast = ref('')
const toastVisible = ref(false)
const gridRefs = ref([])
const placeholderRef = ref(null)
let cleanupListeners = null

function shuffle(arr) {
  return arr.slice().sort(() => Math.random() - 0.5)
}

function createPuzzlePieces() {
  return new Promise((resolve, reject) => {
    const image = new Image()
    image.crossOrigin = 'anonymous'
    image.onload = () => {
      const pieceWidth = Math.floor(image.naturalWidth / 3)
      const pieceHeight = Math.floor(image.naturalHeight / 3)
      const result = []

      const pixelScale = 6
      const tempWidth = Math.max(16, Math.floor(pieceWidth / pixelScale))
      const tempHeight = Math.max(16, Math.floor(pieceHeight / pixelScale))

      for (let row = 0; row < 3; row += 1) {
        for (let col = 0; col < 3; col += 1) {
          const smallCanvas = document.createElement('canvas')
          smallCanvas.width = tempWidth
          smallCanvas.height = tempHeight
          const smallCtx = smallCanvas.getContext('2d')
          smallCtx.imageSmoothingEnabled = false
          smallCtx.drawImage(
            image,
            col * pieceWidth,
            row * pieceHeight,
            pieceWidth,
            pieceHeight,
            0,
            0,
            tempWidth,
            tempHeight
          )

          const canvas = document.createElement('canvas')
          canvas.width = pieceWidth
          canvas.height = pieceHeight
          const ctx = canvas.getContext('2d')
          ctx.imageSmoothingEnabled = false
          ctx.drawImage(
            smallCanvas,
            0,
            0,
            tempWidth,
            tempHeight,
            0,
            0,
            pieceWidth,
            pieceHeight
          )

          result.push({
            id: row * 3 + col + 1,
            src: canvas.toDataURL('image/png')
          })
        }
      }
      resolve(result)
    }
    image.onerror = () => reject(new Error('历史拼图图片加载失败'))
    image.src = puzzleImageUrl
  })
}

async function initPuzzle() {
  try {
    pieces.value = await createPuzzlePieces()
  } catch (e) {
    console.error(e)
    pieces.value = []
  }
  resetGame()
}

function fillPlaceholder() {
  if (!placeholderPiece.value && remainingPieces.value.length > 0) {
    placeholderPiece.value = remainingPieces.value.shift()
  }
}

function resetGame() {
  const source = pieces.value.length ? pieces.value : []
  remainingPieces.value = shuffle(source)
  for (let i = 0; i < gridSlots.length; i += 1) {
    gridSlots[i].value = null
  }
  placeholderPiece.value = null
  fillPlaceholder()
}

function showToast(message) {
  topToast.value = message
  toastVisible.value = true
  setTimeout(() => {
    toastVisible.value = false
  }, 1800)
}

function getSlotIndexByPoint(x, y) {
  let px = Number(x)
  let py = Number(y)
  if (!isFinite(px) || !isFinite(py)) {
    px = Number(lastPointer.x)
    py = Number(lastPointer.y)
  }
  if (!isFinite(px) || !isFinite(py)) return -1
  try {
    const target = document.elementFromPoint(px, py)
    if (target) {
      const cell = target.closest ? target.closest('.puzzle-cell') : null
      if (cell) {
        const index = Number(cell.dataset.index)
        if (!Number.isNaN(index)) return index
      }
    }
  } catch (e) {
    // fallback
  }
  for (let i = 0; i < gridRefs.value.length; i += 1) {
    const rect = gridRefs.value[i]?.getBoundingClientRect()
    if (rect && px >= rect.left && px <= rect.right && py >= rect.top && py <= rect.bottom) {
      return i
    }
  }
  return -1
}

function onSlotTap(index) {
  if (placeholderPiece.value && gridSlots[index].value == null) {
    gridSlots[index].value = placeholderPiece.value
    placeholderPiece.value = null
    fillPlaceholder()
    return
  }
  if (!placeholderPiece.value && gridSlots[index].value != null) {
    placeholderPiece.value = gridSlots[index].value
    gridSlots[index].value = null
  }
}

function onPointerDownPiece(event, source, index = null) {
  if (isDragging.value) return
  const pageX = event.clientX
  const pageY = event.clientY
  lastPointer.x = Number(pageX)
  lastPointer.y = Number(pageY)
  event.preventDefault()
  if (source === 'placeholder' && placeholderPiece.value) {
    dragPiece.value = { source, value: placeholderPiece.value }
    placeholderPiece.value = null
    fillPlaceholder()
  } else if (source === 'grid' && gridSlots[index].value != null) {
    dragPiece.value = { source, value: gridSlots[index].value, index }
    gridSlots[index].value = null
  } else {
    return
  }
  isDragging.value = true
  dragStyle.display = 'flex'
  dragStyle.left = `${pageX - 36}px`
  dragStyle.top = `${pageY - 36}px`
  try {
    let sizeRect = null
    if (source === 'grid' && typeof index === 'number' && gridRefs.value[index]) {
      sizeRect = gridRefs.value[index].getBoundingClientRect()
    } else if (gridRefs.value[0]) {
      sizeRect = gridRefs.value[0].getBoundingClientRect()
    }
    if (sizeRect) {
      dragStyle.width = `${Math.round(sizeRect.width)}px`
      dragStyle.height = `${Math.round(sizeRect.height)}px`
    }
  } catch (e) {
    // ignore
  }
}

function onPointerMove(event) {
  if (!isDragging.value) return
  event.preventDefault()
  const cx = Number(event.clientX)
  const cy = Number(event.clientY)
  if (isFinite(cx) && isFinite(cy)) {
    lastPointer.x = cx
    lastPointer.y = cy
    dragStyle.left = `${cx - 36}px`
    dragStyle.top = `${cy - 36}px`
  }
}

function onPointerUp(event) {
  if (!isDragging.value || !dragPiece.value) return
  event.preventDefault()
  const px = Number(event?.clientX)
  const py = Number(event?.clientY)
  const slotIndex = getSlotIndexByPoint(px, py)
  if (slotIndex >= 0 && gridSlots[slotIndex].value == null) {
    gridSlots[slotIndex].value = dragPiece.value.value
  } else {
    if (dragPiece.value.source === 'grid') {
      if (!placeholderPiece.value) {
        placeholderPiece.value = dragPiece.value.value
      } else {
        remainingPieces.value.unshift(dragPiece.value.value)
      }
    } else if (dragPiece.value.source === 'placeholder') {
      if (!placeholderPiece.value) {
        placeholderPiece.value = dragPiece.value.value
      } else {
        remainingPieces.value.unshift(dragPiece.value.value)
      }
    }
  }
  dragPiece.value = null
  isDragging.value = false
  dragStyle.display = 'none'
}

function goToHome() {
  uni.switchTab({ url: '/pages/home/home' })
}

function handleComplete() {
  const emptySlot = gridSlots.some(slot => slot.value == null)
  if (emptySlot) {
    showToast('拼图未完成，请先放满所有碎片')
    return
  }
  const correct = gridSlots.every((slot, index) => slot.value?.id === index + 1)
  if (!correct) {
    showToast('拼图顺序不对，请再检查哦')
    return
  }

  const exp = 80
  userStore.addExp(exp)

  const collectionResult = userStore.unlockCollection('四羊方尊')
  const newAchievements = collectionResult.achievements || []

  const parts = [`获得经验值 +${exp}`]
  if (collectionResult.unlocked) {
    parts.push(`解锁图鉴：${collectionResult.collection.name}`)
  }
  if (newAchievements.length > 0) {
    parts.push(`获得成就：${newAchievements.map(a => a.name).join('、')}`)
  }
  if (!collectionResult.unlocked && newAchievements.length === 0) {
    parts.push('已有图鉴/成就，无额外奖励，只获得经验。')
  }
  const text = parts.join('\n')
  showModal('🏆 拼图完成！', text, '🏺')

  submitGameResult({
    gameType: 'history',
    score: exp,
    timeUsed: 0,
    detail: '四羊方尊拼图完成'
  }).catch(() => {})
}

onMounted(async () => {
  // 尝试从后端获取拼图配置（3秒超时）
  try {
    await Promise.race([
      fetch('/api/game/history-puzzle', {
        method: 'GET',
        headers: { 'Authorization': 'Bearer ' + uni.getStorageSync('token') }
      }).catch(() => {}),
      new Promise((_, reject) => setTimeout(() => reject(new Error('超时')), 3000))
    ])
  } catch (e) {
    console.debug('历史拼图配置加载超时，使用本地数据', e)
  }
  
  initPuzzle()
  const moveHandler = onPointerMove
  const upHandler = onPointerUp
  window.addEventListener('pointermove', moveHandler)
  window.addEventListener('pointerup', upHandler)
  cleanupListeners = () => {
    window.removeEventListener('pointermove', moveHandler)
    window.removeEventListener('pointerup', upHandler)
  }
})

onUnmounted(() => {
  if (cleanupListeners) cleanupListeners()
})
</script>

<template>
  <PixelStatusBar />
  <view class="page-wrap" @pointermove="onPointerMove" @pointerup="onPointerUp">
    <view class="title-row">
      <view class="back-btn" @tap="goToHome">◄</view>
      <text class="section-title">🏺 文物拼图</text>
    </view>

    <view class="puzzle-board">
      <view class="puzzle-grid" ref="boardRef">
        <view
          v-for="(slot, index) in gridSlots"
          :key="index"
          class="puzzle-cell"
          :data-index="index"
          ref="el => gridRefs[index] = el"
          @tap="onSlotTap(index)"
        >
          <view
            v-if="slot.value"
            class="puzzle-piece draggable"
            @pointerdown="event => onPointerDownPiece(event, 'grid', index)"
            :style="{ backgroundImage: 'url(' + slot.value.src + ')' }"
          />
        </view>
      </view>
    </view>

    <view class="puzzle-actions">
      <view class="placeholder-box" ref="placeholderRef">
        <text class="placeholder-hint">剩余 {{ remainingPieces.length + (placeholderPiece ? 1 : 0) }} 张</text>
        <view
          v-if="placeholderPiece"
          class="puzzle-piece placeholder-piece"
          @pointerdown="event => onPointerDownPiece(event, 'placeholder')"
          :style="{ backgroundImage: 'url(' + placeholderPiece.src + ')' }"
        />
      </view>
      <view class="complete-box">
        <view class="pixel-btn complete-btn" @tap="handleComplete">完成拼图</view>
      </view>
    </view>

    <view class="knowledge-card">
      <text class="k-title">🏺 四羊方尊</text>
      <text class="k-text">商代晚期青铜礼器，距今3000多年。四只大卷角羊立于器身四角，是中国青铜艺术巅峰。</text>
    </view>
  </view>
  <view v-if="isDragging && dragPiece" class="drag-preview" :style="[{ backgroundImage: 'url(' + dragPiece.value.src + ')' }, dragStyle]"></view>
  <view v-if="toastVisible" class="top-toast">{{ topToast }}</view>
  <CustomTabBar />
  <PixelModal />
</template>

<style scoped>
.drag-preview {
  position: fixed;
  background: #FFEF0F;
  background-size: cover;
  background-position: center;
  border: 3rpx solid #000;
  box-shadow: 4rpx 4rpx 0 #000;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  pointer-events: none;
  z-index: 1001;
}
.puzzle-board {
  margin-bottom: 24rpx;
}
.puzzle-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0;
  border: 4rpx solid #000;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}
.puzzle-cell {
  width: 100%;
  aspect-ratio: 1 / 1;
  background: #f8f1d3;
  border-right: 2rpx solid #000;
  border-bottom: 2rpx solid #000;
  padding: 0;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
}
.puzzle-cell:nth-child(3n) {
  border-right: none;
}
.puzzle-cell:nth-last-child(-n+3) {
  border-bottom: none;
}
.puzzle-piece,
.placeholder-piece {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FFEF0F;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  image-rendering: pixelated;
  border: 3rpx solid #000;
  box-shadow: 4rpx 4rpx 0 #000;
  box-sizing: border-box;
  font-size: 0;
  touch-action: none;
}
.puzzle-piece:active,
.placeholder-piece:active {
  transform: translate(2px, 2px);
  box-shadow: 0 0 0 #000;
}
.placeholder-box {
  width: 220rpx;
  min-height: 220rpx;
  padding: 16rpx;
  background: #FFEF0F;
  border: 4rpx solid #000;
  box-shadow: 4rpx 4rpx 0 #000;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
}
.puzzle-actions {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 20rpx;
  margin-bottom: 24rpx;
}
.complete-box {
  display: flex;
  align-items: center;
  justify-content: center;
}
.pixel-btn.complete-btn {
  width: 100%;
  max-width: 220rpx;
  text-align: center;
  padding: 24rpx 0;
}
.placeholder-hint {
  text-align: center;
  font-size: 26rpx;
  color: #333;
  font-weight: 700;
  margin-bottom: 16rpx;
}
.top-toast {
  position: fixed;
  left: 50%;
  transform: translateX(-50%);
  top: 130rpx;
  padding: 18rpx 28rpx;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  border-radius: 16rpx;
  z-index: 1000;
  font-size: 26rpx;
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

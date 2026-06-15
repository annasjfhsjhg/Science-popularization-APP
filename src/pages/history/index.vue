<template>
  <view class="history-page">
    <!-- 文物选择 -->
    <scroll-view scroll-x class="artifact-scroll">
      <view class="artifact-tabs">
        <view
          v-for="a in artifacts"
          :key="a.id"
          class="artifact-tab"
          :class="{ active: currentId === a.id }"
          @click="selectArtifact(a.id)"
        >
          <text class="tab-emoji">{{ a.emoji }}</text>
          <text class="tab-name">{{ a.name }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 拼图说明 -->
    <view class="rule-card">
      <text class="rule-text">🎯 点击选中碎片，再点击目标格子放置。拼完 9 块即可完成！</text>
    </view>

    <!-- 目标预览 + 拼图区域 -->
    <view class="puzzle-section">
      <!-- 完成预览 -->
      <view class="preview-area">
        <text class="area-label">目标样式</text>
        <view class="preview-grid">
          <view
            v-for="piece in currentArtifact.pieces"
            :key="'p' + piece.pos"
            class="preview-cell"
            :style="{ background: piece.color }"
          >
            <text class="preview-symbol">{{ piece.symbol }}</text>
          </view>
        </view>
      </view>

      <!-- 拼图板 -->
      <view class="board-area">
        <text class="area-label">拼图板</text>
        <view class="board-grid">
          <view
            v-for="slot in 9"
            :key="'s' + slot"
            class="board-slot"
            :class="{
              filled:          boardSlots[slot - 1] !== null,
              correct:         correctSlots.includes(slot - 1),
              'selected-slot': selectedPiece !== null && boardSlots[slot - 1] === null
            }"
            @click="onSlotClick(slot - 1)"
          >
            <template v-if="boardSlots[slot - 1] !== null">
              <view
                class="placed-piece"
                :style="{ background: boardSlots[slot - 1].color }"
                :class="{ correct: correctSlots.includes(slot - 1) }"
              >
                <text class="piece-sym">{{ boardSlots[slot - 1].symbol }}</text>
                <text class="correct-mark" v-if="correctSlots.includes(slot - 1)">✓</text>
              </view>
            </template>
            <template v-else>
              <text class="slot-num">{{ slot }}</text>
            </template>
          </view>
        </view>
      </view>
    </view>

    <!-- 碎片托盘 -->
    <view class="tray-area">
      <text class="area-label">碎片托盘（{{ trayPieces.length }} 块剩余）</text>
      <scroll-view scroll-x class="tray-scroll">
        <view class="tray-row">
          <view
            v-for="piece in trayPieces"
            :key="'t' + piece.pos"
            class="tray-piece"
            :class="{ selected: selectedPiece && selectedPiece.pos === piece.pos }"
            :style="{ background: piece.color }"
            @click="onTrayClick(piece)"
          >
            <text class="piece-sym">{{ piece.symbol }}</text>
          </view>
        </view>
      </scroll-view>
      <text v-if="selectedPiece" class="select-hint">已选中「{{ selectedPiece.symbol }}」，点击上方空槽放置</text>
    </view>

    <!-- 进度 -->
    <view class="progress-area">
      <text class="prog-label">{{ correctSlots.length }} / 9 正确</text>
      <view class="prog-track">
        <view class="prog-bar" :style="{ width: (correctSlots.length / 9 * 100) + '%' }"></view>
      </view>
    </view>

    <!-- 胜利弹窗 -->
    <view v-if="gameWon" class="modal-mask">
      <view class="win-modal">
        <text class="win-emoji">{{ currentArtifact.emoji }}</text>
        <text class="win-title">{{ currentArtifact.name }} 复原完成！</text>
        <text class="win-story">{{ currentArtifact.story }}</text>
        <view class="reward-row">
          <view class="reward-badge"><text>+9 ⭐</text></view>
          <view class="reward-badge"><text>解锁图鉴</text></view>
        </view>
        <view class="win-btns">
          <button class="btn-secondary" @click="resetGame">重玩</button>
          <button class="btn-primary" @click="nextArtifact">下一件文物</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { gameStore } from '@/store/gameStore'

// ── 文物数据 ──────────────────────────────────────────────────
const artifacts = [
  {
    id: 'bronze',
    name: '青花瓷',
    emoji: '🏺',
    cardId: 'bronze',
    story: '青花瓷是汉族传统名瓷之一，以白底蓝花著称，始创于元代，明清达到鼎盛，是中国陶瓷中最具代表性的品种。',
    pieces: [
      { pos: 0, symbol: '蓝', color: '#1565C0' },
      { pos: 1, symbol: '白', color: '#E3F2FD' },
      { pos: 2, symbol: '蓝', color: '#1565C0' },
      { pos: 3, symbol: '白', color: '#FAFAFA' },
      { pos: 4, symbol: '花', color: '#1976D2' },
      { pos: 5, symbol: '白', color: '#FAFAFA' },
      { pos: 6, symbol: '蓝', color: '#0D47A1' },
      { pos: 7, symbol: '纹', color: '#E3F2FD' },
      { pos: 8, symbol: '蓝', color: '#0D47A1' }
    ]
  },
  {
    id: 'ceramic',
    name: '兵马俑',
    emoji: '🗿',
    cardId: 'ceramic',
    story: '兵马俑是秦始皇陵的陪葬坑，被誉为"世界第八大奇迹"。坑内有真人真马般大小的陶俑数千件，展现了秦军的威武气势。',
    pieces: [
      { pos: 0, symbol: '将', color: '#8D6E63' },
      { pos: 1, symbol: '士', color: '#A1887F' },
      { pos: 2, symbol: '卒', color: '#8D6E63' },
      { pos: 3, symbol: '马', color: '#795548' },
      { pos: 4, symbol: '俑', color: '#6D4C41' },
      { pos: 5, symbol: '战', color: '#795548' },
      { pos: 6, symbol: '秦', color: '#5D4037' },
      { pos: 7, symbol: '阵', color: '#A1887F' },
      { pos: 8, symbol: '威', color: '#5D4037' }
    ]
  },
  {
    id: 'jade',
    name: '铜镜',
    emoji: '🪙',
    cardId: 'jade',
    story: '铜镜是古代用铜制作的镜子，正面光滑、背面有精美纹饰，汉代铜镜工艺最为精湛，是研究古代文化的重要文物。',
    pieces: [
      { pos: 0, symbol: '铜', color: '#4E342E' },
      { pos: 1, symbol: '纹', color: '#6A4F4B' },
      { pos: 2, symbol: '铜', color: '#4E342E' },
      { pos: 3, symbol: '镜', color: '#795548' },
      { pos: 4, symbol: '光', color: '#A1887F' },
      { pos: 5, symbol: '镜', color: '#795548' },
      { pos: 6, symbol: '古', color: '#4E342E' },
      { pos: 7, symbol: '汉', color: '#6A4F4B' },
      { pos: 8, symbol: '宝', color: '#4E342E' }
    ]
  }
]

const currentId     = ref('bronze')
const selectedPiece = ref(null)
const boardSlots    = ref(Array(9).fill(null))
const trayPieces    = ref([])
const gameWon       = ref(false)

const currentArtifact = computed(() => artifacts.find(a => a.id === currentId.value))

const correctSlots = computed(() => {
  const result = []
  boardSlots.value.forEach((p, idx) => {
    if (p && p.pos === idx) result.push(idx)
  })
  return result
})

// 初始化拼图
function initGame() {
  const shuffled = [...currentArtifact.value.pieces].sort(() => Math.random() - 0.5)
  trayPieces.value  = shuffled
  boardSlots.value  = Array(9).fill(null)
  selectedPiece.value = null
  gameWon.value = false
}

watch(currentId, () => initGame(), { immediate: true })

function selectArtifact(id) {
  if (id === currentId.value) return
  currentId.value = id
}

function onTrayClick(piece) {
  if (selectedPiece.value && selectedPiece.value.pos === piece.pos) {
    selectedPiece.value = null
  } else {
    selectedPiece.value = piece
  }
}

function onSlotClick(slotIdx) {
  if (boardSlots.value[slotIdx] !== null) {
    // 从板上取回到托盘
    const piece = boardSlots.value[slotIdx]
    trayPieces.value = [...trayPieces.value, piece]
    boardSlots.value[slotIdx] = null
    if (selectedPiece.value && selectedPiece.value.pos === piece.pos) {
      selectedPiece.value = null
    }
    return
  }

  if (!selectedPiece.value) return

  // 放置
  const newBoard = [...boardSlots.value]
  newBoard[slotIdx] = selectedPiece.value
  trayPieces.value  = trayPieces.value.filter(p => p.pos !== selectedPiece.value.pos)
  boardSlots.value  = newBoard
  selectedPiece.value = null

  // 检查是否完成
  if (trayPieces.value.length === 0) {
    const allCorrect = boardSlots.value.every((p, i) => p && p.pos === i)
    if (allCorrect) {
      setTimeout(() => {
        gameWon.value = true
        onGameComplete()
      }, 300)
    } else {
      // 标出错误并提示
      uni.showToast({ title: '还有碎片位置不对，继续调整！', icon: 'none', duration: 2000 })
    }
  }
}

function onGameComplete() {
  const a = currentArtifact.value
  gameStore.addStars(9)
  gameStore.unlockCard(a.cardId)
  gameStore.completeGame('history')
  if (gameStore.state.gameStats.history_completed >= 1) gameStore.unlockAchievement('history_guardian')
}

function resetGame() {
  gameWon.value = false
  initGame()
}

function nextArtifact() {
  const idx = artifacts.findIndex(a => a.id === currentId.value)
  currentId.value = artifacts[(idx + 1) % artifacts.length].id
  gameWon.value = false
}
</script>

<style scoped>
.history-page {
  background: #FFF8EE;
  min-height: 100vh;
  padding-bottom: 40rpx;
}

/* 文物选择 */
.artifact-scroll { padding: 24rpx 20rpx 12rpx; }

.artifact-tabs {
  display: flex;
  flex-direction: row;
}

.artifact-tab {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  background: #fff;
  border: 3rpx solid #e0d0c0;
  border-radius: 20rpx;
  padding: 16rpx 28rpx;
  margin-right: 16rpx;
  min-width: 120rpx;
  transition: all 0.2s;
}

.artifact-tab.active {
  background: #6B3A2A;
  border-color: #6B3A2A;
}

.artifact-tab.active .tab-name { color: #fff; }

.tab-emoji { font-size: 44rpx; }
.tab-name  { font-size: 24rpx; color: #6B3A2A; margin-top: 6rpx; font-weight: bold; }

/* 说明 */
.rule-card {
  margin: 0 24rpx 16rpx;
  background: #FFF3E0;
  border-radius: 16rpx;
  padding: 16rpx 24rpx;
  border-left: 8rpx solid #FF6B35;
}

.rule-text { font-size: 24rpx; color: #6B3A2A; line-height: 1.5; }

/* 预览 + 拼图 */
.puzzle-section {
  display: flex;
  flex-direction: row;
  padding: 0 20rpx;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.preview-area, .board-area { flex: 1; }

.area-label {
  display: block;
  font-size: 24rpx;
  color: #6B3A2A;
  font-weight: bold;
  margin-bottom: 10rpx;
  text-align: center;
}

.preview-grid, .board-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4rpx;
  background: #d4b89a;
  border-radius: 12rpx;
  padding: 4rpx;
}

.preview-cell {
  aspect-ratio: 1;
  border-radius: 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-symbol { font-size: 20rpx; color: rgba(255,255,255,0.9); font-weight: bold; }

/* 拼图板槽 */
.board-slot {
  aspect-ratio: 1;
  background: #f5e8d6;
  border-radius: 6rpx;
  border: 2rpx dashed #c4a484;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.board-slot.selected-slot {
  border-color: #FF6B35;
  background: rgba(255, 107, 53, 0.1);
}

.slot-num { font-size: 22rpx; color: #c4a484; }

.placed-piece {
  width: 100%;
  height: 100%;
  border-radius: 4rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.placed-piece.correct { outline: 3rpx solid #4CAF50; }

.piece-sym  { font-size: 24rpx; color: rgba(255,255,255,0.95); font-weight: bold; }

.correct-mark {
  position: absolute;
  top: 2rpx;
  right: 4rpx;
  font-size: 18rpx;
  color: #4CAF50;
  font-weight: bold;
}

/* 托盘 */
.tray-area {
  padding: 0 20rpx 16rpx;
}

.tray-scroll { margin-top: 10rpx; }

.tray-row {
  display: flex;
  flex-direction: row;
  padding: 8rpx 0;
}

.tray-piece {
  width: 90rpx;
  height: 90rpx;
  border-radius: 12rpx;
  margin-right: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid transparent;
  flex-shrink: 0;
  cursor: pointer;
  transition: all 0.2s;
}

.tray-piece.selected {
  border-color: #FF6B35;
  transform: scale(1.1);
  box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.4);
}

.select-hint {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #FF6B35;
  font-weight: bold;
}

/* 进度 */
.progress-area {
  padding: 0 24rpx;
}

.prog-label {
  display: block;
  font-size: 26rpx;
  color: #6B3A2A;
  margin-bottom: 10rpx;
}

.prog-track {
  background: #e0d0c0;
  height: 12rpx;
  border-radius: 6rpx;
  overflow: hidden;
}

.prog-bar {
  height: 100%;
  background: linear-gradient(90deg, #FF6B35, #FFB347);
  border-radius: 6rpx;
  transition: width 0.3s ease;
}

/* 胜利弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.65);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.win-modal {
  background: #fff;
  border-radius: 36rpx;
  padding: 50rpx 40rpx;
  margin: 40rpx;
  text-align: center;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.3);
}

.win-emoji  { font-size: 100rpx; display: block; margin-bottom: 16rpx; }
.win-title  { display: block; font-size: 40rpx; font-weight: bold; color: #6B3A2A; margin-bottom: 16rpx; }
.win-story  { display: block; font-size: 26rpx; color: #666; line-height: 1.7; margin-bottom: 28rpx; text-align: left; }

.reward-row {
  display: flex;
  justify-content: center;
  gap: 16rpx;
  margin-bottom: 32rpx;
}

.reward-badge {
  background: #FFF3E0;
  border: 2rpx solid #FF6B35;
  border-radius: 30rpx;
  padding: 8rpx 24rpx;
  color: #FF6B35;
  font-size: 26rpx;
}

.win-btns {
  display: flex;
  gap: 16rpx;
}

.btn-secondary {
  flex: 1;
  background: #f5f5f5;
  color: #666;
  border-radius: 50rpx;
  font-size: 30rpx;
  padding: 20rpx;
  border: none;
}

.btn-primary {
  flex: 1;
  background: linear-gradient(135deg, #FF6B35, #FFB347);
  color: #fff;
  border-radius: 50rpx;
  font-size: 30rpx;
  font-weight: bold;
  padding: 20rpx;
  border: none;
}
</style>

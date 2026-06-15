<template>
  <view class="astro-page">
    <!-- 星座选择 -->
    <scroll-view scroll-x class="const-scroll">
      <view class="const-tabs">
        <view
          v-for="c in constellations"
          :key="c.id"
          class="const-tab"
          :class="{ active: currentId === c.id }"
          @click="selectConstellation(c.id)"
        >
          {{ c.name }}
        </view>
      </view>
    </scroll-view>

    <!-- 游戏区域 -->
    <view class="game-area" :style="{ height: ch + 'px' }">
      <!-- Canvas 星空背景 + 连线 -->
      <canvas
        canvas-id="starCanvas"
        :style="{ width: cw + 'px', height: ch + 'px' }"
        class="star-canvas"
      ></canvas>

      <!-- 可交互星星（绝对定位于 canvas 之上） -->
      <view
        v-for="star in currentConst.stars"
        :key="star.id"
        class="star-hit"
        :style="{
          left: (star.x / 100 * cw - 30) + 'px',
          top:  (star.y / 100 * ch - 30) + 'px'
        }"
        @click="onStarClick(star)"
      >
        <view
          class="star-ring"
          :class="{
            done: completedIds.includes(star.id),
            next: nextStar && nextStar.id === star.id
          }"
        >
          <text class="star-num">{{ star.order }}</text>
        </view>
        <text class="star-label">{{ star.name }}</text>
      </view>

      <!-- 提示框 -->
      <view class="hint-box">
        <text v-if="!gameWon" class="hint-text">
          {{ completedIds.length === 0 ? '点击第 1 颗星开始连线' : `请点击：${nextStar ? nextStar.name : ''}（第 ${completedIds.length + 1} 颗）` }}
        </text>
        <text v-else class="hint-win">🌟 连线完成！</text>
      </view>
    </view>

    <!-- 进度条 -->
    <view class="progress-area">
      <view class="progress-header">
        <text class="progress-label">连线进度</text>
        <text class="progress-count">{{ completedIds.length }} / {{ currentConst.stars.length }}</text>
      </view>
      <view class="progress-track">
        <view
          class="progress-bar"
          :style="{ width: (completedIds.length / currentConst.stars.length * 100) + '%' }"
        ></view>
      </view>
    </view>

    <!-- 星座说明 -->
    <view class="info-card">
      <text class="info-title">关于 {{ currentConst.name }}</text>
      <text class="info-body">{{ currentConst.intro }}</text>
    </view>

    <!-- 胜利弹窗 -->
    <view v-if="gameWon" class="modal-mask">
      <view class="win-modal">
        <text class="win-emoji">🎉</text>
        <text class="win-title">{{ currentConst.name }} 完成！</text>
        <text class="win-story">{{ currentConst.story }}</text>
        <view class="reward-row">
          <view class="reward-badge">
            <text>+{{ currentConst.stars.length }} ⭐</text>
          </view>
          <view class="reward-badge">
            <text>解锁图鉴</text>
          </view>
        </view>
        <view class="win-btns">
          <button class="btn-secondary" @click="resetGame">重玩</button>
          <button class="btn-primary" @click="nextConstellation">下一个</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, getCurrentInstance, watch } from 'vue'
import { gameStore } from '@/store/gameStore'

const { proxy } = getCurrentInstance()

const cw = ref(375)
const ch = ref(375)
let ctx = null

// ── 星座数据 ──────────────────────────────────────────────────
const constellations = [
  {
    id: 'orion',
    name: '猎户座',
    cardId: 'orion',
    intro: '猎户座是冬夜最醒目的星座，三颗排成一线的星构成猎人的腰带，肉眼即可辨认。',
    story: '传说猎人俄里翁是海神波塞冬之子，体格壮硕、射术超群。宙斯将其升至天界成为星座，永远守望苍穹。',
    stars: [
      { id: 1, order: 1, name: '参宿四', x: 32, y: 22 },
      { id: 2, order: 2, name: '参宿五', x: 65, y: 22 },
      { id: 3, order: 3, name: '参宿一', x: 40, y: 48 },
      { id: 4, order: 4, name: '参宿二', x: 50, y: 52 },
      { id: 5, order: 5, name: '参宿三', x: 60, y: 48 },
      { id: 6, order: 6, name: '参宿六', x: 67, y: 75 },
      { id: 7, order: 7, name: '参宿七', x: 30, y: 75 }
    ]
  },
  {
    id: 'bigdipper',
    name: '北斗七星',
    cardId: 'bigdipper',
    intro: '北斗七星是大熊座最亮的七颗星，形似勺子。延长勺口两星连线约五倍可找到北极星。',
    story: '北斗七星在中国古代被称为"帝车"，古人用它辨别方向、划分季节，是农耕文明的天文指南。',
    stars: [
      { id: 1, order: 1, name: '天枢', x: 18, y: 42 },
      { id: 2, order: 2, name: '天璇', x: 30, y: 38 },
      { id: 3, order: 3, name: '天玑', x: 43, y: 36 },
      { id: 4, order: 4, name: '天权', x: 54, y: 40 },
      { id: 5, order: 5, name: '玉衡', x: 63, y: 55 },
      { id: 6, order: 6, name: '开阳', x: 73, y: 63 },
      { id: 7, order: 7, name: '摇光', x: 82, y: 72 }
    ]
  },
  {
    id: 'cassiopeia',
    name: '仙后座',
    cardId: 'cassiopeia',
    intro: '仙后座形如字母 W，全年均可在北方天空中看到，是寻找北极星的另一个参考标志。',
    story: '仙后卡西奥佩娅因夸耀女儿美貌触怒海神，被束缚于天界王座上，日夜绕北极星旋转，永不落地。',
    stars: [
      { id: 1, order: 1, name: '王良一', x: 12, y: 62 },
      { id: 2, order: 2, name: '王良四', x: 30, y: 32 },
      { id: 3, order: 3, name: '策',     x: 50, y: 58 },
      { id: 4, order: 4, name: '阁道三', x: 70, y: 28 },
      { id: 5, order: 5, name: '阁道二', x: 85, y: 56 }
    ]
  }
]

const currentId   = ref('orion')
const completedIds  = ref([])
const completedLines = ref([])
const gameWon     = ref(false)

const currentConst = computed(() => constellations.find(c => c.id === currentId.value))

const nextStar = computed(() => {
  const nextOrder = completedIds.value.length + 1
  return currentConst.value.stars.find(s => s.order === nextOrder) || null
})

// 固定随机背景星点
const bgStars = Array.from({ length: 100 }, () => ({
  x: Math.random(),
  y: Math.random(),
  r: Math.random() * 1.5 + 0.3,
  o: (Math.random() * 0.6 + 0.2).toFixed(2)
}))

// ── 生命周期 ──────────────────────────────────────────────────
onMounted(async () => {
  const info = uni.getSystemInfoSync()
  cw.value = info.windowWidth
  ch.value = Math.round(info.windowWidth * 0.82)
  await new Promise(r => setTimeout(r, 200))
  ctx = uni.createCanvasContext('starCanvas', proxy)
  drawCanvas()
})

watch(currentId, () => resetGame())
watch([completedIds, completedLines], () => drawCanvas(), { deep: true })

// ── 游戏逻辑 ──────────────────────────────────────────────────
function selectConstellation(id) {
  if (id === currentId.value) return
  currentId.value = id
}

function resetGame() {
  completedIds.value  = []
  completedLines.value = []
  gameWon.value = false
  setTimeout(() => drawCanvas(), 80)
}

function onStarClick(star) {
  if (gameWon.value) return
  const expected = nextStar.value
  if (!expected) return

  if (star.id === expected.id) {
    // 正确：添加连线
    const lastId = completedIds.value[completedIds.value.length - 1]
    if (lastId !== undefined) {
      completedLines.value = [...completedLines.value, { from: lastId, to: star.id }]
    }
    completedIds.value = [...completedIds.value, star.id]
    try { uni.vibrateShort({ type: 'light' }) } catch (e) {}

    if (completedIds.value.length === currentConst.value.stars.length) {
      gameWon.value = true
      onGameComplete()
    }
  } else {
    try { uni.vibrateShort({ type: 'heavy' }) } catch (e) {}
    uni.showToast({ title: `请先点击：${expected.name}`, icon: 'none', duration: 1200 })
  }
}

function onGameComplete() {
  const c = currentConst.value
  gameStore.addStars(c.stars.length)
  gameStore.unlockCard(c.cardId)
  gameStore.completeGame('astronomy')
  if (gameStore.state.gameStats.astronomy_completed >= 1) gameStore.unlockAchievement('astro_beginner')
}

function nextConstellation() {
  const idx = constellations.findIndex(c => c.id === currentId.value)
  currentId.value = constellations[(idx + 1) % constellations.length].id
  gameWon.value = false
  resetGame()
}

// ── Canvas 绘制 ────────────────────────────────────────────────
function starPos(starId) {
  const s = currentConst.value.stars.find(s => s.id === starId)
  return { x: s.x / 100 * cw.value, y: s.y / 100 * ch.value }
}

function drawCanvas() {
  if (!ctx) return

  // 背景
  ctx.setFillStyle('#0A0A22')
  ctx.fillRect(0, 0, cw.value, ch.value)

  // 背景小星点
  bgStars.forEach(s => {
    ctx.setFillStyle(`rgba(255,255,255,${s.o})`)
    ctx.beginPath()
    ctx.arc(s.x * cw.value, s.y * ch.value, s.r, 0, Math.PI * 2)
    ctx.fill()
  })

  // 已连接的线
  completedLines.value.forEach(line => {
    const from = starPos(line.from)
    const to   = starPos(line.to)
    ctx.setStrokeStyle('#FFD700')
    ctx.setLineWidth(2.5)
    ctx.setLineCap('round')
    ctx.beginPath()
    ctx.moveTo(from.x, from.y)
    ctx.lineTo(to.x, to.y)
    ctx.stroke()
  })

  // 星座主星
  currentConst.value.stars.forEach(star => {
    const { x, y } = starPos(star.id)
    const done = completedIds.value.includes(star.id)
    const isNext = nextStar.value && nextStar.value.id === star.id

    // 光晕
    if (done || isNext) {
      ctx.setFillStyle(done ? 'rgba(255,215,0,0.25)' : 'rgba(100,200,255,0.2)')
      ctx.beginPath()
      ctx.arc(x, y, 20, 0, Math.PI * 2)
      ctx.fill()
    }

    // 星点
    ctx.setFillStyle(done ? '#FFD700' : (isNext ? '#88DDFF' : '#8888BB'))
    ctx.beginPath()
    ctx.arc(x, y, 7, 0, Math.PI * 2)
    ctx.fill()
  })

  ctx.draw()
}
</script>

<style scoped>
.astro-page {
  background: #0A0A22;
  min-height: 100vh;
  padding-bottom: 40rpx;
}

.const-scroll {
  padding: 24rpx 20rpx 16rpx;
}

.const-tabs {
  display: flex;
  flex-direction: row;
  white-space: nowrap;
}

.const-tab {
  display: inline-flex;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.5);
  padding: 14rpx 36rpx;
  border-radius: 50rpx;
  margin-right: 16rpx;
  font-size: 28rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.1);
  transition: all 0.2s;
}

.const-tab.active {
  background: #FFD700;
  color: #1A1A3E;
  font-weight: bold;
  border-color: #FFD700;
}

/* 游戏区 */
.game-area {
  position: relative;
  overflow: hidden;
}

.star-canvas {
  display: block;
}

.star-hit {
  position: absolute;
  width: 60px;
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 10;
}

.star-ring {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  border: 2px solid rgba(136, 136, 187, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.star-ring.next {
  border-color: #88DDFF;
  background: rgba(136, 221, 255, 0.15);
  box-shadow: 0 0 12px #88DDFF88;
}

.star-ring.done {
  border-color: #FFD700;
  background: rgba(255, 215, 0, 0.15);
}

.star-num {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
  font-weight: bold;
}

.star-label {
  font-size: 18rpx;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 2px;
  white-space: nowrap;
}

/* 提示框 */
.hint-box {
  position: absolute;
  bottom: 20rpx;
  left: 24rpx;
  right: 24rpx;
  background: rgba(0, 0, 0, 0.55);
  border-radius: 16rpx;
  padding: 16rpx 24rpx;
  z-index: 20;
  backdrop-filter: blur(4px);
}

.hint-text {
  color: rgba(255, 255, 255, 0.8);
  font-size: 26rpx;
}

.hint-win {
  color: #FFD700;
  font-size: 30rpx;
  font-weight: bold;
}

/* 进度 */
.progress-area {
  padding: 24rpx 40rpx 0;
}

.progress-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.progress-label,
.progress-count {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.6);
}

.progress-track {
  background: rgba(255, 255, 255, 0.12);
  height: 12rpx;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #FFD700, #FFA500);
  border-radius: 6rpx;
  transition: width 0.35s ease;
}

/* 信息卡 */
.info-card {
  margin: 24rpx;
  background: rgba(255, 255, 255, 0.07);
  border-radius: 20rpx;
  padding: 24rpx 28rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.12);
}

.info-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #FFD700;
  margin-bottom: 12rpx;
}

.info-body {
  display: block;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.7;
}

/* 胜利弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.win-modal {
  background: linear-gradient(145deg, #1A1A3E, #2D2D6B);
  border-radius: 36rpx;
  padding: 50rpx 40rpx;
  margin: 40rpx;
  border: 2rpx solid #FFD700;
  text-align: center;
}

.win-emoji {
  font-size: 100rpx;
  display: block;
  margin-bottom: 20rpx;
}

.win-title {
  display: block;
  font-size: 44rpx;
  font-weight: bold;
  color: #FFD700;
  margin-bottom: 20rpx;
}

.win-story {
  display: block;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.7;
  margin-bottom: 30rpx;
  text-align: left;
}

.reward-row {
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 20rpx;
  margin-bottom: 36rpx;
}

.reward-badge {
  background: rgba(255, 215, 0, 0.15);
  border: 1rpx solid #FFD700;
  border-radius: 30rpx;
  padding: 8rpx 24rpx;
  color: #FFD700;
  font-size: 26rpx;
}

.win-btns {
  display: flex;
  flex-direction: row;
  gap: 20rpx;
}

.btn-secondary {
  flex: 1;
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
  border-radius: 50rpx;
  font-size: 30rpx;
  padding: 20rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.2);
}

.btn-primary {
  flex: 1;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #1A1A3E;
  border-radius: 50rpx;
  font-size: 30rpx;
  font-weight: bold;
  padding: 20rpx;
  border: none;
}
</style>

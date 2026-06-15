<template>
  <view class="insect-page">
    <!-- 关卡切换 -->
    <view class="level-tabs">
      <view
        class="level-tab"
        :class="{ active: level === 1 }"
        @click="switchLevel(1)"
      >🔖 昆虫分类</view>
      <view
        class="level-tab"
        :class="{ active: level === 2 }"
        @click="switchLevel(2)"
      >🔄 生命周期</view>
    </view>

    <!-- ══ 关卡一：昆虫分类 ══ -->
    <template v-if="level === 1">
      <view class="game-area">
        <!-- 分数 / 进度 -->
        <view class="score-row">
          <text class="score-label">得分</text>
          <text class="score-val">{{ score1 }}</text>
          <text class="score-label">进度</text>
          <text class="score-val">{{ round1 }} / {{ total1 }}</text>
          <text class="score-label">❤️</text>
          <text class="score-val">{{ lives }}</text>
        </view>

        <!-- 当前昆虫 -->
        <view v-if="!game1Won" class="insect-show">
          <text class="insect-emoji">{{ current1.emoji }}</text>
          <text class="insect-name">{{ current1.name }}</text>
          <text class="insect-hint">{{ current1.hint }}</text>
        </view>

        <!-- 分类按钮 -->
        <view v-if="!game1Won" class="category-btns">
          <view
            v-for="cat in categories"
            :key="cat.id"
            class="cat-btn"
            :style="{ background: cat.color }"
            :class="{ flash: flashCat === cat.id }"
            @click="classify(cat.id)"
          >
            <text class="cat-icon">{{ cat.icon }}</text>
            <text class="cat-label">{{ cat.name }}</text>
          </view>
        </view>

        <!-- 反馈动画 -->
        <view v-if="feedback" class="feedback-box" :class="feedback">
          <text class="feedback-text">{{ feedback === 'correct' ? '✅ 正确！' : '❌ 错误！' }}</text>
        </view>

        <!-- 关卡一通关 -->
        <view v-if="game1Won" class="level-win">
          <text class="lw-emoji">🎊</text>
          <text class="lw-title">昆虫分类完成！</text>
          <text class="lw-score">得分：{{ score1 }} / {{ total1 }}</text>
          <button class="lw-btn" @click="nextLevel">挑战生命周期 →</button>
          <button class="lw-btn-sec" @click="restartLevel1">再玩一次</button>
        </view>
      </view>
    </template>

    <!-- ══ 关卡二：生命周期排序 ══ -->
    <template v-if="level === 2">
      <view class="game-area">
        <text class="lc-title">请将蝴蝶的生命周期按正确顺序排列</text>

        <!-- 排列区（目标槽） -->
        <view class="order-slots">
          <view
            v-for="(slot, idx) in orderSlots"
            :key="'slot' + idx"
            class="order-slot"
            :class="{
              filled:   slot !== null,
              correct:  lcCorrect.includes(idx),
              selected: selectedStage !== null && slot === null
            }"
            @click="placeStage(idx)"
          >
            <template v-if="slot">
              <text class="stage-emoji">{{ slot.emoji }}</text>
              <text class="stage-name">{{ slot.name }}</text>
              <text v-if="lcCorrect.includes(idx)" class="stage-check">✓</text>
            </template>
            <template v-else>
              <text class="slot-label">第 {{ idx + 1 }} 阶段</text>
            </template>
          </view>
        </view>

        <!-- 阶段卡片托盘 -->
        <view class="stage-tray">
          <text class="tray-title">待放置</text>
          <view class="stage-cards">
            <view
              v-for="stage in stageTray"
              :key="stage.id"
              class="stage-card"
              :class="{ selected: selectedStage && selectedStage.id === stage.id }"
              @click="selectStage(stage)"
            >
              <text class="stage-emoji">{{ stage.emoji }}</text>
              <text class="stage-name">{{ stage.name }}</text>
            </view>
          </view>
          <text v-if="selectedStage" class="select-hint">
            已选「{{ selectedStage.name }}」，点击上方槽位放置
          </text>
        </view>

        <!-- 检查按钮 -->
        <view v-if="stageTray.length === 0 && !game2Won" class="check-row">
          <button class="check-btn" @click="checkOrder">检查答案</button>
        </view>

        <!-- 关卡二通关 -->
        <view v-if="game2Won" class="level-win">
          <text class="lw-emoji">🦋</text>
          <text class="lw-title">太棒了！生命周期答对了！</text>
          <text class="lw-score">解锁蝴蝶图鉴 + 昆虫探索家成就</text>
          <button class="lw-btn" @click="restartLevel2">再玩一次</button>
          <button class="lw-btn-sec" @click="goHome">返回首页</button>
        </view>

        <!-- 错误提示 -->
        <view v-if="lcWrong" class="lc-wrong-hint">
          <text>❌ 顺序有误，点击槽位可取回碎片重新摆放！</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { gameStore } from '@/store/gameStore'

// ── 公用 ──────────────────────────────────────────────────────
const level = ref(1)

function switchLevel(l) {
  level.value = l
  if (l === 1) restartLevel1()
  else restartLevel2()
}

// ══ 关卡一：昆虫分类 ══════════════════════════════════════════
const insectList = [
  { name: '蝴蝶', emoji: '🦋', category: 'fly',   hint: '它有四片彩色翅膀' },
  { name: '蜜蜂', emoji: '🐝', category: 'fly',   hint: '它会采花蜜、会蜇人' },
  { name: '蜻蜓', emoji: '🦗', category: 'fly',   hint: '它的翅膀像网纱' },
  { name: '蚊子', emoji: '🦟', category: 'fly',   hint: '夏天让你睡不着觉' },
  { name: '蚂蚁', emoji: '🐜', category: 'crawl', hint: '它搬运的食物是自身体重的50倍' },
  { name: '蜗牛', emoji: '🐌', category: 'crawl', hint: '它背着一个螺旋形的壳' },
  { name: '毛毛虫', emoji: '🐛', category: 'crawl', hint: '蝴蝶的幼年形态' },
  { name: '甲虫', emoji: '🪲', category: 'crawl', hint: '它有坚硬的甲壳' },
  { name: '萤火虫', emoji: '✨', category: 'fly',  hint: '夜晚会发出绿色荧光' },
  { name: '蟑螂', emoji: '🪳', category: 'crawl', hint: '有很强的生命力和繁殖能力' }
]

const categories = [
  { id: 'fly',   name: '飞行类', icon: '🕊️', color: '#1565C0' },
  { id: 'crawl', name: '爬行类', icon: '🐾', color: '#2E7D32' }
]

const total1   = insectList.length
const lives    = ref(3)
const score1   = ref(0)
const round1   = ref(0)
const game1Won = ref(false)
const feedback = ref(null)  // 'correct' | 'wrong'
const flashCat = ref(null)
let queue = []

const current1 = ref(insectList[0])

function buildQueue() {
  queue = [...insectList].sort(() => Math.random() - 0.5)
  current1.value = queue[0]
}

function restartLevel1() {
  lives.value    = 3
  score1.value   = 0
  round1.value   = 0
  game1Won.value = false
  feedback.value = null
  buildQueue()
}

function classify(catId) {
  if (game1Won.value || feedback.value) return
  const correct = current1.value.category === catId

  feedback.value = correct ? 'correct' : 'wrong'
  flashCat.value = catId

  if (correct) {
    score1.value++
    try { uni.vibrateShort({ type: 'light' }) } catch (e) {}
  } else {
    lives.value--
    try { uni.vibrateShort({ type: 'heavy' }) } catch (e) {}
    if (lives.value <= 0) {
      setTimeout(() => {
        uni.showModal({
          title: '游戏结束',
          content: `得分：${score1.value}，挑战失败，重新来过！`,
          showCancel: false,
          success: () => restartLevel1()
        })
      }, 600)
      return
    }
  }

  setTimeout(() => {
    feedback.value = null
    flashCat.value = null
    round1.value++
    if (round1.value >= total1) {
      game1Won.value = true
      gameStore.completeGame('insect')
      gameStore.addStars(score1.value)
      gameStore.unlockCard('butterfly')
    } else {
      queue.shift()
      current1.value = queue[0]
    }
  }, 700)
}

function nextLevel() {
  level.value = 2
  restartLevel2()
}

// ══ 关卡二：生命周期排序 ══════════════════════════════════════
const lifecycleStages = [
  { id: 1, order: 1, name: '卵',  emoji: '🥚', desc: '蝴蝶在植物叶片上产卵' },
  { id: 2, order: 2, name: '幼虫', emoji: '🐛', desc: '孵化后成为毛毛虫，大量进食' },
  { id: 3, order: 3, name: '蛹',  emoji: '🫘', desc: '毛毛虫结茧变蛹，内部重组' },
  { id: 4, order: 4, name: '成虫', emoji: '🦋', desc: '破茧而出，展翅飞翔' }
]

const orderSlots    = ref([null, null, null, null])
const stageTray     = ref([])
const selectedStage = ref(null)
const game2Won      = ref(false)
const lcWrong       = ref(false)

const lcCorrect = computed(() => {
  const result = []
  orderSlots.value.forEach((s, i) => {
    if (s && s.order === i + 1) result.push(i)
  })
  return result
})

function restartLevel2() {
  orderSlots.value    = [null, null, null, null]
  stageTray.value     = [...lifecycleStages].sort(() => Math.random() - 0.5)
  selectedStage.value = null
  game2Won.value      = false
  lcWrong.value       = false
}

function selectStage(stage) {
  if (selectedStage.value && selectedStage.value.id === stage.id) {
    selectedStage.value = null
  } else {
    selectedStage.value = stage
  }
}

function placeStage(slotIdx) {
  if (orderSlots.value[slotIdx] !== null) {
    // 取回
    const s = orderSlots.value[slotIdx]
    stageTray.value = [...stageTray.value, s]
    orderSlots.value[slotIdx] = null
    lcWrong.value = false
    return
  }
  if (!selectedStage.value) return

  const newSlots = [...orderSlots.value]
  newSlots[slotIdx] = selectedStage.value
  stageTray.value   = stageTray.value.filter(s => s.id !== selectedStage.value.id)
  orderSlots.value  = newSlots
  selectedStage.value = null
  lcWrong.value = false
}

function checkOrder() {
  const allCorrect = orderSlots.value.every((s, i) => s && s.order === i + 1)
  if (allCorrect) {
    game2Won.value = true
    lcWrong.value  = false
    gameStore.completeGame('insect')
    gameStore.addStars(10)
    gameStore.unlockCard('bee')
    gameStore.unlockAchievement('insect_explorer')
  } else {
    lcWrong.value = true
    try { uni.vibrateShort({ type: 'heavy' }) } catch (e) {}
  }
}

function goHome() {
  uni.reLaunch({ url: '/pages/index/index' })
}

// 初始化
restartLevel1()
</script>

<style scoped>
.insect-page {
  background: #F1F8E9;
  min-height: 100vh;
  padding-bottom: 40rpx;
}

/* 关卡切换 */
.level-tabs {
  display: flex;
  flex-direction: row;
  margin: 24rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 8rpx;
  gap: 8rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
}

.level-tab {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  border-radius: 14rpx;
  font-size: 28rpx;
  color: #666;
  transition: all 0.25s;
}

.level-tab.active {
  background: linear-gradient(135deg, #2E7D32, #66BB6A);
  color: #fff;
  font-weight: bold;
}

/* 游戏区 */
.game-area {
  padding: 0 24rpx;
}

/* 分数行 */
.score-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}

.score-label { font-size: 24rpx; color: #888; }
.score-val   { font-size: 34rpx; font-weight: bold; color: #2E7D32; min-width: 40rpx; text-align: center; }

/* 昆虫展示 */
.insect-show {
  background: #fff;
  border-radius: 28rpx;
  padding: 40rpx;
  text-align: center;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.08);
}

.insect-emoji { display: block; font-size: 120rpx; margin-bottom: 20rpx; }
.insect-name  { display: block; font-size: 48rpx; font-weight: bold; color: #2E7D32; margin-bottom: 12rpx; }
.insect-hint  { display: block; font-size: 26rpx; color: #888; font-style: italic; }

/* 分类按钮 */
.category-btns {
  display: flex;
  flex-direction: row;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.cat-btn {
  flex: 1;
  border-radius: 24rpx;
  padding: 30rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: transform 0.15s;
}

.cat-btn:active { transform: scale(0.95); }
.cat-btn.flash  { transform: scale(1.05); opacity: 0.8; }

.cat-icon  { font-size: 56rpx; margin-bottom: 12rpx; }
.cat-label { font-size: 32rpx; font-weight: bold; color: #fff; }

/* 反馈 */
.feedback-box {
  text-align: center;
  padding: 16rpx;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  animation: fadeIn 0.2s ease;
}

.feedback-box.correct { background: #E8F5E9; }
.feedback-box.wrong   { background: #FFEBEE; }
.feedback-text        { font-size: 36rpx; font-weight: bold; }

@keyframes fadeIn { from { opacity: 0; transform: scale(0.8); } to { opacity: 1; transform: scale(1); } }

/* 通关 */
.level-win {
  background: #fff;
  border-radius: 28rpx;
  padding: 50rpx 32rpx;
  text-align: center;
  box-shadow: 0 8rpx 40rpx rgba(0,0,0,0.1);
}

.lw-emoji { display: block; font-size: 100rpx; margin-bottom: 20rpx; }
.lw-title { display: block; font-size: 40rpx; font-weight: bold; color: #2E7D32; margin-bottom: 16rpx; }
.lw-score { display: block; font-size: 28rpx; color: #666; margin-bottom: 32rpx; }

.lw-btn {
  background: linear-gradient(135deg, #2E7D32, #66BB6A);
  color: #fff;
  border-radius: 50rpx;
  font-size: 30rpx;
  font-weight: bold;
  padding: 20rpx 60rpx;
  border: none;
  margin-bottom: 16rpx;
  display: block;
}

.lw-btn-sec {
  background: #f0f0f0;
  color: #666;
  border-radius: 50rpx;
  font-size: 28rpx;
  padding: 18rpx 60rpx;
  border: none;
  display: block;
}

/* ── 关卡二 ── */
.lc-title {
  display: block;
  text-align: center;
  font-size: 30rpx;
  color: #2E7D32;
  font-weight: bold;
  margin-bottom: 28rpx;
  line-height: 1.5;
}

.order-slots {
  display: flex;
  flex-direction: row;
  gap: 12rpx;
  margin-bottom: 28rpx;
}

.order-slot {
  flex: 1;
  min-height: 160rpx;
  background: #fff;
  border-radius: 20rpx;
  border: 3rpx dashed #A5D6A7;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12rpx 8rpx;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.order-slot.selected { border-color: #FF6B35; background: rgba(255,107,53,0.06); }
.order-slot.correct  { border-color: #4CAF50; background: rgba(76,175,80,0.08); }

.slot-label { font-size: 22rpx; color: #A5D6A7; text-align: center; }

.stage-emoji { display: block; font-size: 60rpx; text-align: center; }
.stage-name  { display: block; font-size: 24rpx; color: #333; font-weight: bold; text-align: center; margin-top: 6rpx; }
.stage-check {
  position: absolute;
  top: 6rpx; right: 10rpx;
  font-size: 28rpx; color: #4CAF50; font-weight: bold;
}

/* 托盘 */
.stage-tray {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}

.tray-title { display: block; font-size: 26rpx; color: #888; margin-bottom: 16rpx; }

.stage-cards {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  flex-wrap: wrap;
}

.stage-card {
  flex: 1;
  min-width: 140rpx;
  background: #F1F8E9;
  border: 3rpx solid #A5D6A7;
  border-radius: 20rpx;
  padding: 20rpx 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.2s;
}

.stage-card.selected {
  border-color: #2E7D32;
  background: #E8F5E9;
  transform: scale(1.05);
  box-shadow: 0 4rpx 16rpx rgba(46,125,50,0.3);
}

.select-hint {
  display: block;
  margin-top: 14rpx;
  font-size: 24rpx;
  color: #FF6B35;
  font-weight: bold;
}

/* 检查按钮 */
.check-row { text-align: center; margin-bottom: 20rpx; }

.check-btn {
  background: linear-gradient(135deg, #2E7D32, #66BB6A);
  color: #fff;
  border-radius: 50rpx;
  font-size: 32rpx;
  font-weight: bold;
  padding: 22rpx 80rpx;
  border: none;
}

/* 错误提示 */
.lc-wrong-hint {
  background: #FFEBEE;
  border-radius: 16rpx;
  padding: 16rpx 24rpx;
  margin-top: 12rpx;
  color: #C62828;
  font-size: 26rpx;
  text-align: center;
}
</style>

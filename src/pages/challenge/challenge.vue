<script setup>
import { ref, computed, onMounted } from 'vue'
import { useModal } from '../../composables/useModal.js'
import PixelModal from '../../components/PixelModal.vue'
import PixelStatusBar from '../../components/PixelStatusBar.vue'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import { useUserStore } from '../../stores/user.js'
import { submitGameResult, getDailyQuestions } from '../../api/index.js'

const { showModal } = useModal()
const userStore = useUserStore()

const questions = [
  {
    id: 1,
    title: '哪个阶段是蝴蝶的完全变态最后一步？',
    options: ['卵', '幼虫', '蛹', '成虫'],
    answer: '成虫'
  },
  {
    id: 2,
    title: '猎户座属于什么类型天体？',
    options: ['星座', '行星', '彗星', '卫星'],
    answer: '星座'
  },
  {
    id: 3,
    title: '古代青铜器“四羊方尊”最可能属于哪个朝代？',
    options: ['商代', '唐代', '宋代', '明代'],
    answer: '商代'
  },
  {
    id: 4,
    title: '完全变态昆虫的幼虫阶段通常做什么？',
    options: ['飞行', '觅食', '交配', '产卵'],
    answer: '觅食'
  },
  {
    id: 5,
    title: '“四羊方尊”最早出土于哪个朝代？',
    options: ['商代', '夏代', '汉代', '唐代'],
    answer: '商代'
  }
]

function shuffle(arr) {
  return arr.slice().sort(() => Math.random() - 0.5)
}

const questionSet = ref(shuffle(questions))
const index = ref(0)
const selectedOption = ref('')
const correctCount = ref(0)
const completed = ref(false)
const resultHint = ref('选择一个答案，挑战成功可获得经验。')

const questionCount = computed(() => questionSet.value.length)
const progressText = computed(() => `第 ${index.value + 1} / ${questionCount.value} 题`)

function currentQuestion() {
  return questionSet.value[index.value]
}

function resetChallenge() {
  index.value = 0
  selectedOption.value = ''
  correctCount.value = 0
  completed.value = false
  questionSet.value = shuffle(questions)
  resultHint.value = '选择一个答案，挑战成功可获得经验。'
}

onMounted(() => {
  // 3秒超时，避免挑战页卡
  Promise.race([
    getDailyQuestions(),
    new Promise((_, reject) => setTimeout(() => reject(new Error('超时')), 3000))
  ]).then((res) => {
    // assume backend returns an array of question objects
    if (Array.isArray(res) && res.length) {
      questionSet.value = shuffle(res)
    }
  }).catch(() => {
    // 超时或失败，keep local questions
  })
})

function finishChallenge() {
  const exp = 30 + correctCount.value * 10
  userStore.addExp(exp)
  submitGameResult({
    gameType: 'challenge',
    score: exp,
    timeUsed: 0,
    detail: `每日挑战答对 ${correctCount.value} / ${questionCount.value} 题`
  }).catch(() => {})
  const textLines = [`获得经验值 +${exp}`, `答对 ${correctCount.value} / ${questionCount.value} 题`]
  if (correctCount.value === questionCount.value) {
    textLines.push('完美答题，学习大师！')
  }
  showModal('🏅 挑战完成！', textLines.join('\n'), '🎯')
}

function handleChoose(option) {
  if (completed.value) return
  selectedOption.value = option
  const correct = option === currentQuestion().answer
  if (correct) {
    correctCount.value += 1
    resultHint.value = '答对了！继续下一题。'
  } else {
    resultHint.value = `答案不对，正确答案是：${currentQuestion().answer}`
  }

  if (index.value === questionCount.value - 1) {
    completed.value = true
    finishChallenge()
    return
  }

  index.value += 1
  selectedOption.value = ''
}

function goToHome() {
  uni.switchTab({ url: '/pages/home/home' })
}
</script>

<template>
  <PixelStatusBar />
  <view class="page-wrap">
    <view class="title-row">
      <view class="back-btn" @tap="goToHome">◄</view>
      <text class="section-title">🎯 每日挑战</text>
    </view>

    <view class="challenge-card">
      <text class="challenge-title">{{ currentQuestion().title }}</text>
      <text class="progress-text">{{ progressText }}</text>
      <view class="option-list">
        <view
          v-for="option in currentQuestion().options"
          :key="option"
          class="option-item"
          @tap="handleChoose(option)"
        >
          <text>{{ option }}</text>
        </view>
      </view>
      <view class="hint-box">
        <text>{{ resultHint }}</text>
      </view>
      <view class="button-row">
        <view class="pixel-btn" @tap="resetChallenge">换一题</view>
        <view class="pixel-btn" @tap="resetChallenge">重新开始</view>
      </view>
    </view>

    <view class="knowledge-card">
      <text class="k-title">💡 小提示</text>
      <text class="k-text">挑战题目根据天文、历史与昆虫三大主题随机生成，答对可获得经验。</text>
    </view>
  </view>
  <CustomTabBar />
  <PixelModal />
</template>

<style scoped>
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
.challenge-card {
  background: #FFEF0F;
  border: 4rpx solid #000;
  box-shadow: 6rpx 6rpx 0 #000;
  padding: 30rpx;
  margin-bottom: 24rpx;
}
.challenge-title {
  font-size: 30rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
  display: block;
}
.progress-text {
  font-size: 24rpx;
  color: #333;
  margin-bottom: 18rpx;
  display: block;
}
.option-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18rpx;
  margin-bottom: 24rpx;
}
.option-item {
  background: #fff;
  border: 4rpx solid #000;
  box-shadow: 6rpx 6rpx 0 #000;
  padding: 24rpx;
  text-align: center;
  font-size: 28rpx;
}
.option-item:active {
  transform: translate(4px, 4px);
  box-shadow: 0 0 0 #000;
}
.hint-box {
  background: #fff;
  border: 4rpx solid #000;
  box-shadow: 6rpx 6rpx 0 #000;
  padding: 22rpx;
  margin-bottom: 24rpx;
}
.button-row {
  display: flex;
  gap: 18rpx;
  flex-wrap: wrap;
  justify-content: space-between;
}
.k-title {
  font-size: 28rpx;
  margin-bottom: 12rpx;
}
.k-text {
  font-size: 24rpx;
  line-height: 1.5;
}
</style>

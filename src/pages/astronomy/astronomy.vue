<script setup>
import { ref } from 'vue'
import ConstellationGame from '../../components/ConstellationGame.vue'
import { useModal } from '../../composables/useModal.js'
import PixelModal from '../../components/PixelModal.vue'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import PixelStatusBar from '../../components/PixelStatusBar.vue'

const { showModal } = useModal()
const gameRef = ref(null)

function onGameComplete(result) {
  const score = result?.score ?? 0
  const unlockedCollection = result?.unlockedCollection
  const achievements = Array.isArray(result?.achievements) ? result.achievements : []

  const parts = [`获得积分：+${score}`]
  if (unlockedCollection) {
    parts.push(`解锁图鉴：${unlockedCollection}`)
  }
  if (achievements.length > 0) {
    parts.push(`获得成就：${achievements.map(a => a.name).join('、')}`)
  }
  if (!unlockedCollection && achievements.length === 0) {
    parts.push('本次未解锁新成就，已获得经验值。')
  }

  showModal('⭐ 连线成功！', parts.join('\n'), '🌟')
}

function resetStars() {
  gameRef.value?.reset()
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
      <text class="section-title">🌌 星座连线</text>
    </view>

    <ConstellationGame ref="gameRef" @game-complete="onGameComplete" />

    <view class="knowledge-card">
      <text class="k-title">⭐ 猎户座 Orion</text>
      <text class="k-text">猎户座由七颗明亮的星星组成。其中"参宿四"是颗红超巨星，比太阳大1000倍！</text>
    </view>
    <view class="knowledge-card">
      <text class="k-title">💡 小知识</text>
      <text class="k-text">古希腊神话中，猎户座代表勇敢的猎人俄里翁。点击星星把它们连起来吧！</text>
    </view>
    <view class="knowledge-card small-recommend">
      <text class="k-title">📝 推荐玩法</text>
      <text class="k-text">完成星座连线后，可以继续挑战历史拼图和昆虫生命周期，三种主题一起做更容易记住知识点。</text>
    </view>
    <view class="pixel-btn" style="width:87%; margin-top:6px;" @tap="resetStars">🔄 重新连线</view>
  </view>
  <CustomTabBar />
  <PixelModal />
</template>

<style>
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

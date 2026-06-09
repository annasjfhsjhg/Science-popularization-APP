<script setup>
import { ref } from 'vue'
import ConstellationGame from '../../components/ConstellationGame.vue'
import { useModal } from '../../composables/useModal.js'
import PixelModal from '../../components/PixelModal.vue'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'

const { showModal } = useModal()
const gameRef = ref(null)

function onGameComplete(achievements) {
  const unlocked = achievements.filter(a => a.unlocked)
  if (unlocked.length > 0) {
    showModal('🏆 成就解锁！', unlocked.map(a => `${a.name}：${a.desc}`).join('\n'), '🏆')
  } else {
    showModal('⭐ 完成！', '星座连线完成，继续探索更多吧！', '🌟')
  }
}

function resetStars() {
  gameRef.value?.reset()
}
</script>

<template>
  <view class="page-wrap">
    <view class="game-header">
      <view class="back-btn" @tap="uni.navigateBack()">◀</view>
      <text class="game-title">🌌 星座连线</text>
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
    <view class="pixel-btn" style="width:100%; margin-top:12rpx;" @tap="resetStars">🔄 重新连线</view>
  </view>
  <CustomTabBar current="astronomy" />
  <PixelModal />
</template>

<script setup>
import { useModal } from '../../composables/useModal.js'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import PixelModal from '../../components/PixelModal.vue'

const { showModal } = useModal()

function showAchievement() {
  showModal('🏆 成就达成！', '文物拼图完成\n解锁【文物守护者】', '🏺')
}
</script>

<template>
  <view class="page-wrap">
    <view class="game-header">
      <view class="back-btn" @tap="uni.navigateBack()">◀</view>
      <text class="game-title">🏺 文物拼图</text>
    </view>

    <view class="puzzle-board">
      <view class="puzzle-grid">
        <view class="puzzle-piece">🏺</view>
        <view class="puzzle-piece">🟫</view>
        <view class="puzzle-piece">🟧</view>
        <view class="puzzle-piece">🟧</view>
        <view class="puzzle-piece">🏛️</view>
        <view class="puzzle-piece">🟫</view>
        <view class="puzzle-piece">🟫</view>
        <view class="puzzle-piece">🟧</view>
        <view class="puzzle-piece">🏺</view>
      </view>
    </view>

    <view class="knowledge-card">
      <text class="k-title">🏺 四羊方尊</text>
      <text class="k-text">商代晚期青铜礼器，距今3000多年。四只大卷角羊立于器身四角，是中国青铜艺术巅峰。</text>
    </view>
    <view class="pixel-btn" @tap="showAchievement">完成拼图 🎉</view>
  </view>
  <CustomTabBar current="history" />
  <PixelModal />
</template>

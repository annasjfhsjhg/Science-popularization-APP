<script setup>
import { useModal } from '../composables/useModal.js'
const { state, hideModal } = useModal()
</script>

<template>
  <view v-if="state.visible" class="modal-overlay">
    <view class="modal-content">
      <view class="big-emoji">{{ state.emoji }}</view>
      <text class="modal-title">{{ state.title }}</text>
      <text class="modal-text">{{ state.text }}</text>
      <view class="pixel-btn" @tap="hideModal">继续探索</view>
    </view>
  </view>
</template>

<style>
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background: #FFEEC4;
  border: 4px solid #000;
  box-shadow: 6px 6px 0 #000;
  padding: 48rpx;
  width: 80%;
  max-width: 360px;
  text-align: center;
}

.modal-content .big-emoji {
  font-size: 112rpx;
  background: #FFEF0F;
  border: 3px solid #000;
  width: 160rpx;
  height: 160rpx;
  line-height: 152rpx;
  margin: 0 auto 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-title {
  display: block;
  color: #FF6B35;
  font-size: 32rpx;
  margin-bottom: 20rpx;
  text-shadow: 2px 2px 0 #000;
}

.modal-text {
  display: block;
  color: #000;
  font-size: 26rpx;
  line-height: 1.6;
  margin-bottom: 32rpx;
  white-space: pre-line;
}
</style>

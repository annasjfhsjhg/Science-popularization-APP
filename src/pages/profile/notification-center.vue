<template>
  <view class="page-wrap">
    <view class="game-header">
      <view class="back-btn" @tap="uni.navigateBack()">◀</view>
      <text class="game-title">🔔 消息通知</text>
    </view>

    <view class="pixel-box notice-item">
      <text class="n-title">每日提醒</text>
      <text class="n-text">今天还差 1 次探索即可完成连续学习打卡。</text>
    </view>
    <view class="pixel-box notice-item">
      <text class="n-title">成就更新</text>
      <text class="n-text">你距离"知识达人"成就还差 4 张图鉴。</text>
    </view>
  </view>
</template>

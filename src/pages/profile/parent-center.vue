<template>
  <view class="page-wrap">
    <view class="game-header">
      <view class="back-btn" @tap="uni.navigateBack()">◀</view>
      <text class="game-title">👨‍👩‍👧 家长中心</text>
    </view>

    <view class="pixel-box">
      <text style="display:block; font-size:28rpx; font-weight:bold; margin-bottom:8rpx;">本周专注时长</text>
      <text style="display:block; font-size:26rpx; color:#333;">约 126 分钟，较上周提升 18%</text>
    </view>
    <view class="pixel-box">
      <text style="display:block; font-size:28rpx; font-weight:bold; margin-bottom:8rpx;">推荐陪伴任务</text>
      <text style="display:block; font-size:26rpx; color:#333;">和孩子一起完成 1 次历史文明拼图，讨论文物背后的故事。</text>
    </view>
  </view>
</template>

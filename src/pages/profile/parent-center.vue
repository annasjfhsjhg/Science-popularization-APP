<script setup>
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import PixelStatusBar from '../../components/PixelStatusBar.vue'
</script>

<template>
  <PixelStatusBar />
  <view class="page-wrap">
    <text class="section-title">👨‍👩‍👧 家长中心</text>

    <view class="pixel-box">
      <text style="display:block; font-size:28rpx; font-weight:bold; margin-bottom:8rpx;">本周专注时长</text>
      <text style="display:block; font-size:26rpx; color:#333;">约 126 分钟，较上周提升 18%</text>
    </view>
    <view class="pixel-box">
      <text style="display:block; font-size:28rpx; font-weight:bold; margin-bottom:8rpx;">推荐陪伴任务</text>
      <text style="display:block; font-size:26rpx; color:#333;">和孩子一起完成 1 次历史文明拼图，讨论文物背后的故事。</text>
    </view>
  </view>
  <CustomTabBar />
</template>


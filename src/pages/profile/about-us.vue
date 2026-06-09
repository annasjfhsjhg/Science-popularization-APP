<template>
  <view class="page-wrap">
    <view class="game-header">
      <view class="back-btn" @tap="uni.navigateBack()">◀</view>
      <text class="game-title">ℹ️ 关于我们</text>
    </view>

    <view class="knowledge-card">
      <text class="k-title">科普星球</text>
      <text class="k-text">我们希望通过游戏化学习，让孩子在探索中建立科学兴趣与长期学习动力。</text>
    </view>
    <view class="knowledge-card">
      <text class="k-title">版本信息</text>
      <text class="k-text">演示版本 v1.0.0 · 课程作业前端原型</text>
    </view>
  </view>
</template>

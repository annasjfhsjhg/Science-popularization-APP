<script setup>
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import PixelStatusBar from '../../components/PixelStatusBar.vue'
</script>

<template>
  <PixelStatusBar />
  <view class="page-wrap">


    <view class="knowledge-card">
      <text class="k-title">科普星球</text>
      <text class="k-text">我们希望通过游戏化学习，让孩子在探索中建立科学兴趣与长期学习动力。</text>
    </view>
    <view class="knowledge-card">
      <text class="k-title">版本信息</text>
      <text class="k-text">演示版本 v1.0.0 · 课程作业前端原型</text>
    </view>
  </view>
  <CustomTabBar />
</template>

<script setup>
</script>

<template>
  <view class="page-wrap">
    <view class="game-header">
      <view class="back-btn" @tap="uni.navigateBack()">◀</view>
      <text class="game-title">🦋 生命周期</text>
    </view>

    <view class="insect-area">
      <text class="i-tip">按生命周期顺序排列吧！</text>
      <view class="insect-list">
        <view class="insect-item">🥚</view>
        <view class="insect-item">🐛</view>
        <view class="insect-item">🛡️</view>
        <view class="insect-item">🦋</view>
      </view>
    </view>

    <view class="knowledge-card">
      <text class="k-title">🦋 蝴蝶的一生</text>
      <text class="k-text">卵 → 幼虫 → 蛹 → 成虫，这种神奇变化叫"完全变态发育"！</text>
    </view>
    <view class="knowledge-card">
      <text class="k-title">🌿 你知道吗？</text>
      <text class="k-text">全世界有超过 18,000 种蝴蝶，最大的翅膀展开可达 30 厘米！</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '../../stores/user.js'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'

const store = useUserStore()
const levelProgress = computed(() => store.getLevelProgress())
const expLeft = computed(() => store.user.nextLevelExp - store.user.exp)

function goTo(name) {
  const tabPages = ['astronomy', 'history', 'collection']
  if (tabPages.includes(name)) {
    uni.switchTab({ url: `/pages/${name}/${name}` })
  } else {
    uni.navigateTo({ url: `/pages/${name}/${name}` })
  }
}
</script>

<template>
  <view class="page-wrap">
    <view class="home-banner">
      <text class="banner-title">你好，小探险家！</text>
      <text class="banner-sub">今天又是元气满满的探索日～</text>
      <text class="banner-star">★</text>
    </view>

    <view class="level-card">
      <view class="pixel-avatar">{{ store.user.avatar }}</view>
      <view class="level-info">
        <text class="level-name">小科学家 Lv.{{ store.user.level }}</text>
        <view class="pixel-progress">
          <view class="pixel-progress-fill" :style="{ width: levelProgress + '%' }"></view>
        </view>
        <text class="exp-hint">距离升级还差 {{ expLeft }} 经验</text>
      </view>
    </view>

    <text class="section-title">★ 每日推荐</text>
    <view class="recommend-card" @tap="goTo('astronomy')">
      <view class="rec-emoji">🔭</view>
      <view>
        <text class="rec-title">今日探索：猎户座的秘密</text>
        <text class="rec-sub">点击进入，开启星空之旅！</text>
      </view>
    </view>

    <text class="section-title">★ 游戏分类</text>
    <view class="game-grid">
      <view class="game-card" @tap="goTo('astronomy')">
        <view class="game-icon icon-blue">🌌</view>
        <text class="g-title">天文世界</text>
        <text class="g-sub">星座连线</text>
      </view>
      <view class="game-card" @tap="goTo('history')">
        <view class="game-icon icon-orange">🏺</view>
        <text class="g-title">历史文明</text>
        <text class="g-sub">文物拼图</text>
      </view>
      <view class="game-card" @tap="goTo('insect')">
        <view class="game-icon icon-green">🦋</view>
        <text class="g-title">昆虫自然</text>
        <text class="g-sub">生态探索</text>
      </view>
      <view class="game-card" @tap="goTo('collection')">
        <view class="game-icon icon-purple">📖</view>
        <text class="g-title">科普图鉴</text>
        <text class="g-sub">收集知识</text>
      </view>
    </view>
  </view>
  <CustomTabBar current="home" />
</template>

<script setup>
import { useModal } from '../../composables/useModal.js'
import PixelModal from '../../components/PixelModal.vue'
import CustomTabBar from '../../components/CustomTabBar/CustomTabBar.vue'
import PixelStatusBar from '../../components/PixelStatusBar.vue'

const { showModal } = useModal()
</script>

<template>
  <PixelStatusBar />
  <view class="page-wrap">
    <text class="section-title">🤖 AI 科普助手</text>

    <view class="knowledge-card">
      <text class="k-title">你可以这样问我</text>
      <text class="k-text">1. 为什么月亮有阴晴圆缺？&#10;2. 蝴蝶为什么会蜕变？&#10;3. 古代青铜器是怎么做出来的？</text>
    </view>
    <view class="pixel-box assistant-input">今天想探索哪个知识点？</view>
    <view class="pixel-btn" @tap="showModal('AI 助手上线', '这是课堂演示版入口，后续可接入真实问答 API。', '🤖')">
      开始提问
    </view>
  </view>
  <PixelModal />
  <CustomTabBar />
</template>

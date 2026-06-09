<script setup>
const ACHIEVEMENT_LIST = [
  { id: 'astronomy-novice',  badge: '🌟', name: '天文新手',   desc: '完成第一个星座连线',    progress: 100 },
  { id: 'artifact-guardian', badge: '🏺', name: '文物守护者', desc: '完成 5 个文物拼图',      progress: 60 },
  { id: 'insect-expert',     badge: '🦋', name: '昆虫小专家', desc: '认识 10 种昆虫',         progress: 80 },
  { id: 'knowledge-master',  badge: '🔒', name: '知识达人',   desc: '解锁 50 张图鉴卡片',    progress: 30, locked: true },
  { id: 'explorer-master',   badge: '🔒', name: '探索大师',   desc: '连续登录 30 天',         progress: 20, locked: true },
]
</script>

<template>
  <view class="page-wrap">
    <text class="section-title" style="display:block; text-align:center;">🏆 成就中心</text>

    <view v-for="item in ACHIEVEMENT_LIST" :key="item.id" class="achievement-item">
      <view class="badge" :class="{ locked: item.locked }">{{ item.badge }}</view>
      <view class="achievement-info">
        <text class="a-name">{{ item.name }}</text>
        <text class="a-desc">{{ item.desc }}</text>
        <view class="pixel-progress">
          <view class="pixel-progress-fill" :style="{ width: item.progress + '%' }"></view>
        </view>
      </view>
    </view>
  </view>
</template>

<template>
  <view class="profile-page">
    <!-- 头像 + 信息 -->
    <view class="profile-header">
      <view class="avatar-selector">
        <text class="avatar-display" @click="showAvatarPicker = true">{{ store.state.user.avatar }}</text>
        <view class="avatar-edit-hint" @click="showAvatarPicker = true">更换</view>
      </view>
      <view class="user-info">
        <input
          class="name-input"
          :value="store.state.user.name"
          @blur="onNameChange"
          placeholder="点击修改昵称"
          maxlength="10"
        />
        <text class="user-lv">Lv.{{ store.state.user.level }} · {{ levelTitle }}</text>
      </view>
    </view>

    <!-- 经验条 -->
    <view class="exp-section">
      <view class="exp-row">
        <text class="exp-label">经验值</text>
        <text class="exp-val">{{ store.state.user.exp }} / {{ nextLevelExp }}</text>
      </view>
      <view class="exp-track">
        <view class="exp-fill" :style="{ width: expPercent + '%' }"></view>
      </view>
      <text class="exp-hint">再获得 {{ nextLevelExp - store.state.user.exp }} 经验升级</text>
    </view>

    <!-- 数据卡 -->
    <view class="data-grid">
      <view class="data-card">
        <text class="data-num star">⭐ {{ store.state.user.stars }}</text>
        <text class="data-label">总星星</text>
      </view>
      <view class="data-card">
        <text class="data-num">{{ totalGames }}</text>
        <text class="data-label">完成游戏</text>
      </view>
      <view class="data-card">
        <text class="data-num">{{ unlockedCards }}</text>
        <text class="data-label">解锁图鉴</text>
      </view>
      <view class="data-card">
        <text class="data-num">{{ earnedAch }}</text>
        <text class="data-label">获得成就</text>
      </view>
    </view>

    <!-- 游戏记录 -->
    <view class="section-card">
      <text class="section-title">游戏记录</text>
      <view class="record-row">
        <text class="record-icon">🌟</text>
        <text class="record-label">天文世界</text>
        <text class="record-val">完成 {{ store.state.gameStats.astronomy_completed }} 次</text>
      </view>
      <view class="record-row">
        <text class="record-icon">🏺</text>
        <text class="record-label">历史文明</text>
        <text class="record-val">完成 {{ store.state.gameStats.history_completed }} 次</text>
      </view>
      <view class="record-row">
        <text class="record-icon">🦋</text>
        <text class="record-label">昆虫自然</text>
        <text class="record-val">完成 {{ store.state.gameStats.insect_completed }} 次</text>
      </view>
    </view>

    <!-- 关于 -->
    <view class="section-card">
      <text class="section-title">关于科普星球</text>
      <text class="about-text">《科普星球》是一款面向 5~15 岁儿童的互动式科普教育应用，通过趣味小游戏将天文、历史、生物等知识融入游戏过程，让孩子在"玩中学、学中玩"。</text>
      <text class="app-ver">Version 1.0.0</text>
    </view>

    <!-- 重置 -->
    <view class="danger-zone">
      <button class="reset-btn" @click="confirmReset">重置所有数据</button>
    </view>

    <!-- 头像选择器 -->
    <view v-if="showAvatarPicker" class="modal-mask" @click.self="showAvatarPicker = false">
      <view class="avatar-modal">
        <text class="modal-title">选择头像</text>
        <view class="avatar-grid">
          <text
            v-for="av in avatarOptions"
            :key="av"
            class="avatar-option"
            :class="{ selected: store.state.user.avatar === av }"
            @click="selectAvatar(av)"
          >{{ av }}</text>
        </view>
        <button class="close-btn" @click="showAvatarPicker = false">关闭</button>
      </view>
    </view>

    <view style="height: 150rpx;"></view>
    <CustomTabBar current="profile" />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { gameStore } from '@/store/gameStore'
import CustomTabBar from '@/components/CustomTabBar.vue'

const store = gameStore
const showAvatarPicker = ref(false)

const avatarOptions = ['🧑‍🚀', '👦', '👧', '🧒', '🐣', '🐸', '🦊', '🐼', '🐨', '🦁', '🐯', '🐰']

const levelTitles = ['', '小探索家', '科普达人', '知识猎手', '星际学者', '宇宙博士']
const levelTitle = computed(() => levelTitles[Math.min(store.state.user.level, levelTitles.length - 1)])

const nextLevelExp = computed(() => store.state.user.level * 100)
const expPercent   = computed(() => {
  const base = (store.state.user.level - 1) * 100
  const cur  = store.state.user.exp - base
  return Math.min(100, Math.max(0, (cur / 100) * 100))
})

const totalGames   = computed(() =>
  store.state.gameStats.astronomy_completed +
  store.state.gameStats.history_completed +
  store.state.gameStats.insect_completed
)
const unlockedCards = computed(() => Object.values(store.state.encyclopedia).filter(v => v).length)
const earnedAch     = computed(() => Object.values(store.state.achievements).filter(v => v).length)

function onNameChange(e) {
  const val = e.detail.value.trim()
  if (val) gameStore.updateUser('name', val)
}

function selectAvatar(av) {
  gameStore.updateUser('avatar', av)
  showAvatarPicker.value = false
}

function confirmReset() {
  uni.showModal({
    title: '⚠️ 确认重置',
    content: '将清除所有游戏进度、图鉴、成就数据，此操作不可撤销！',
    confirmColor: '#FF4444',
    success(res) {
      if (res.confirm) {
        gameStore.resetAll()
        uni.showToast({ title: '数据已重置', icon: 'success' })
      }
    }
  })
}
</script>

<style scoped>
.profile-page {
  background: #F0F9FF;
  min-height: 100vh;
}

/* 顶部 */
.profile-header {
  background: linear-gradient(135deg, #4ECDC4, #45B7D1);
  padding: 40rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 30rpx;
}

.avatar-selector {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-display {
  font-size: 100rpx;
  display: block;
}

.avatar-edit-hint {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.2);
  padding: 4rpx 14rpx;
  border-radius: 12rpx;
  margin-top: 6rpx;
}

.user-info { flex: 1; }

.name-input {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  background: transparent;
  border-bottom: 2rpx solid rgba(255, 255, 255, 0.4);
  padding-bottom: 8rpx;
  width: 100%;
  margin-bottom: 12rpx;
}

.user-lv {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
}

/* 经验 */
.exp-section {
  background: #fff;
  margin: 20rpx 24rpx;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.exp-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.exp-label { font-size: 26rpx; color: #666; }
.exp-val   { font-size: 26rpx; color: #4ECDC4; font-weight: bold; }

.exp-track {
  background: #e0f2f1;
  height: 14rpx;
  border-radius: 7rpx;
  overflow: hidden;
  margin-bottom: 10rpx;
}

.exp-fill {
  height: 100%;
  background: linear-gradient(90deg, #4ECDC4, #26C6DA);
  border-radius: 7rpx;
  transition: width 0.4s ease;
}

.exp-hint {
  font-size: 22rpx;
  color: #aaa;
}

/* 数据网格 */
.data-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  padding: 0 24rpx;
  margin-bottom: 20rpx;
}

.data-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx 20rpx;
  text-align: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.data-num {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #4ECDC4;
  margin-bottom: 8rpx;
}

.data-num.star { color: #FFB300; }
.data-label    { display: block; font-size: 24rpx; color: #aaa; }

/* 通用 section 卡 */
.section-card {
  background: #fff;
  margin: 0 24rpx 20rpx;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.section-title {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.record-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.record-row:last-child { border-bottom: none; }

.record-icon  { font-size: 36rpx; margin-right: 16rpx; }
.record-label { flex: 1; font-size: 28rpx; color: #555; }
.record-val   { font-size: 26rpx; color: #4ECDC4; font-weight: bold; }

.about-text {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
  margin-bottom: 16rpx;
}

.app-ver {
  display: block;
  font-size: 22rpx;
  color: #ccc;
  text-align: right;
}

/* 重置 */
.danger-zone {
  padding: 0 24rpx 20rpx;
}

.reset-btn {
  background: #fff;
  color: #FF4444;
  border: 2rpx solid #FF4444;
  border-radius: 50rpx;
  font-size: 28rpx;
  padding: 18rpx 0;
  width: 100%;
}

/* 头像弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 999;
}

.avatar-modal {
  background: #fff;
  width: 100%;
  border-radius: 36rpx 36rpx 0 0;
  padding: 36rpx 40rpx 60rpx;
}

.modal-title {
  display: block;
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 30rpx;
}

.avatar-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16rpx;
  margin-bottom: 32rpx;
}

.avatar-option {
  font-size: 60rpx;
  text-align: center;
  padding: 8rpx;
  border-radius: 16rpx;
  border: 3rpx solid transparent;
  transition: all 0.2s;
}

.avatar-option.selected {
  border-color: #4ECDC4;
  background: #E0F7FA;
}

.close-btn {
  background: #4ECDC4;
  color: #fff;
  border-radius: 50rpx;
  font-size: 30rpx;
  padding: 18rpx 0;
  width: 100%;
  border: none;
}
</style>

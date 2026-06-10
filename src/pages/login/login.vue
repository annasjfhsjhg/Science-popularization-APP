<script setup>
import { ref } from 'vue'
import { login } from '../../api/index.js'

const username = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  // if (loading.value) return
  // loading.value = true
  // try {
  //   const res = await login({ username: username.value, password: password.value })
  //   if (res.code === 0 && res.data?.token) {
  //     uni.setStorageSync('token', res.data.token)
  //     uni.reLaunch({ url: '/pages/home/home' })
  //   } else {
  //     uni.showToast({ title: '登录失败：' + (res.message || '未知错误'), icon: 'none', duration: 2000 })
  //   }
  // } catch {
  //   uni.showToast({ title: '网络错误或后端未启动', icon: 'none', duration: 2000 })
  // } finally {
  //   loading.value = false
  // }
  uni.reLaunch({ url: '/pages/home/home' })
}
</script>

<template>
  <view class="login-page">
    <view class="login-box">
      <view class="big-emoji">🚀</view>
      <text class="login-title">欢迎回来</text>

      <input
        v-model="username"
        type="text"
        placeholder="用户名"
        class="login-input"
      />

      <input
        v-model="password"
        type="password"
        placeholder="密码"
        class="login-input"
        @confirm="handleLogin"
      />

      <view class="pixel-btn login-btn" :class="{ disabled: loading }" @tap="handleLogin">
        {{ loading ? '登录中...' : '登 录' }}
      </view>
    </view>
  </view>
</template>

<style>
.login-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 40rpx;
  background: #FFEEC4;
}
.login-box {
  width: 640rpx;
  padding: 48rpx;
  border: 4px solid #000;
  box-shadow: 6px 6px 0 #000;
  background: #fff;
  text-align: center;
}
.login-box .big-emoji {
  font-size: 112rpx;
  background: #FFEF0F;
  border: 3px solid #000;
  width: 160rpx;
  height: 160rpx;
  line-height: 152rpx;
  margin: 0 auto 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-title {
  display: block;
  font-size: 36rpx;
  color: #000;
  margin-bottom: 40rpx;
}
.login-input {
  width: 90%;
  padding: 16rpx;
  margin-bottom: 24rpx;
  border: 2px solid #000;
  font-family: inherit;
  font-size: 28rpx;
  background: #fff;
  display: block;
}
.login-btn {
  width: 90%;
  padding: 20rpx;
  font-size: 32rpx;
  margin: 0 auto;
}
.login-btn.disabled { opacity: 0.6; }
</style>

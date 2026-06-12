<script setup>
import { ref } from 'vue'
import { register } from '../../api/index.js'

const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const email = ref('')
const loading = ref(false)

async function handleRegister() {
  if (!username.value || !password.value || !confirmPassword.value || !email.value) {
    uni.showToast({ title: '请填写所有字段', icon: 'none', duration: 2000 })
    return
  }

  if (password.value !== confirmPassword.value) {
    uni.showToast({ title: '两次密码不一致', icon: 'none', duration: 2000 })
    return
  }

  if (password.value.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none', duration: 2000 })
    return
  }

  loading.value = true
  try {
    // 3秒超时，避免注册页卡
    const res = await Promise.race([
      register({
        username: username.value,
        password: password.value,
        email: email.value
      }),
      new Promise((_, reject) => setTimeout(() => reject(new Error('请求超时')), 3000))
    ])
    if (res.code === 0) {
      uni.showToast({ title: '注册成功，请登录', icon: 'success', duration: 1500 })
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/login/login' })
      }, 1500)
    } else {
      uni.showToast({ title: '注册失败：' + (res.message || '未知错误'), icon: 'none', duration: 2000 })
    }
  } catch (error) {
    uni.showToast({ title: '网络错误或后端未启动', icon: 'none', duration: 2000 })
  } finally {
    loading.value = false
  }
}

function goToLogin() {
  uni.reLaunch({ url: '/pages/login/login' })
}
</script>

<template>
  <view class="register-page">
    <view class="register-box">
      <view class="big-emoji">📝</view>
      <text class="register-title">创建账号</text>

      <input
        v-model="username"
        type="text"
        placeholder="用户名"
        class="login-input"
      />

      <input
        v-model="email"
        type="email"
        placeholder="邮箱地址"
        class="login-input"
      />

      <input
        v-model="password"
        type="password"
        placeholder="密码"
        class="login-input"
      />

      <input
        v-model="confirmPassword"
        type="password"
        placeholder="确认密码"
        class="login-input"
      />

      <view class="pixel-btn login-btn" :class="{ disabled: loading }" @tap="handleRegister">
        {{ loading ? '注册中...' : '注 册' }}
      </view>

      <text class="login-link" @tap="goToLogin">已有账号？立即登录</text>
    </view>
  </view>
</template>

<style>
.register-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 40rpx;
  background: #FFEEC4;
}
.register-box {
  width: 640rpx;
  padding: 48rpx;
  border: 4px solid #000;
  box-shadow: 6px 6px 0 #000;
  background: #fff;
  text-align: center;
}
.register-box .big-emoji {
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
.register-title {
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
  margin: 0 auto 24rpx;
}
.login-btn.disabled { opacity: 0.6; }
.login-link {
  display: block;
  color: #FF6B35;
  font-size: 24rpx;
  text-decoration: underline;
  cursor: pointer;
}
</style>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const currentTime = ref('--:--')
const batteryLevel = ref(98)
const batteryText = computed(() => `${batteryLevel.value}%`)

function updateTime() {
  const now = new Date()
  const hour = String(now.getHours()).padStart(2, '0')
  const minute = String(now.getMinutes()).padStart(2, '0')
  currentTime.value = `${hour}:${minute}`
}

async function updateBattery() {
  if (typeof navigator !== 'undefined' && navigator.getBattery) {
    try {
      const battery = await navigator.getBattery()
      const setLevel = () => { batteryLevel.value = Math.round(battery.level * 100) }
      setLevel()
      battery.addEventListener('levelchange', setLevel)
      battery.addEventListener('chargingchange', setLevel)
      return () => {
        battery.removeEventListener('levelchange', setLevel)
        battery.removeEventListener('chargingchange', setLevel)
      }
    } catch (e) {
      console.warn('Battery API unavailable', e)
    }
  }
  batteryLevel.value = 98
  return () => {}
}

let timeTimer = null
let cleanupBattery = null

onMounted(async () => {
  updateTime()
  timeTimer = setInterval(updateTime, 60 * 1000)
  cleanupBattery = await updateBattery()
})

onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer)
  if (cleanupBattery) cleanupBattery()
})
</script>

<template>
  <view class="pixel-status-bar">
    <text class="status-item left">{{ currentTime }}</text>
    <text class="status-item title">★ PIXEL ★</text>
    <view class="status-item right">
      <text class="battery-text">{{ batteryText }}</text>
      <text class="battery-icon">🔋</text>
    </view>
  </view>
</template>

<style scoped>
.pixel-status-bar {
  width: 100%;
  min-height: 100rpx;
  padding: 24rpx 30rpx;
  background: #6998EC;
  border-bottom: 4rpx solid #000;
  box-shadow: 0 6rpx 0 #000;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
  color: #FFEEC4;
}
.status-item {
  font-size: 28rpx;
  font-weight: 700;
}
.status-item.title {
  flex: 1;
  text-align: center;
}
.status-item.right {
  display: flex;
  align-items: center;
  gap: 10rpx;
}
.battery-text {
  font-size: 26rpx;
}
.battery-icon {
  font-size: 30rpx;
}
</style>

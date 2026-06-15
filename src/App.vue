<script>
export default {
  onLaunch() {},
  onShow() {},
  onHide() {}
}
</script>

<style lang="scss">
page {
  font-family: -apple-system, 'PingFang SC', 'Helvetica Neue', 'Microsoft YaHei', sans-serif;
  box-sizing: border-box;
  background-color: #F0F9FF;
}

view, text, input, button {
  box-sizing: border-box;
}

button::after {
  border: none;
}

button {
  line-height: normal;
  font-family: inherit;
}

/* 通用动画 */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30rpx); }
  to   { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50%       { transform: scale(1.05); }
}
</style>

<script>
export default {
  onLaunch() {
    const token = uni.getStorageSync('token')
    if (!token) {
      uni.reLaunch({ url: '/pages/login/login' })
    }
    // #ifdef APP-PLUS
    uni.loadFontFace({
      family: 'ZCOOL KuaiLe',
      source: 'url("/static/fonts/ZCOOLKuaiLe.ttf")'
    })
    uni.loadFontFace({
      family: 'Press Start 2P',
      source: 'url("/static/fonts/PressStart2P.ttf")'
    })
    // #endif
  },
  onShow() {},
  onHide() {}
}
</script>

<style>
@import './styles/base.scss';
@import './styles/layout.scss';
@import './styles/components.scss';
</style>

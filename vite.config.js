import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uni()],
  server: {
    // H5 开发时反向代理后端（解决跨域）
    proxy: {
      '/api': {
        target: 'http://10.252.136.168:8080',
        changeOrigin: true
      }
    }
  }
})

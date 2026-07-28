import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const API_TARGET = process.env.VITE_API_TARGET || 'http://localhost:8081'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: API_TARGET, changeOrigin: true },
      '/upload': { target: API_TARGET, changeOrigin: true }
    }
  },
  build: {
    chunkSizeWarningLimit: 1000,
    rollupOptions: {
      output: {
        manualChunks: {
          // three.js 单独分包（仅首页雪花用到，按需加载）
          three: ['three'],
          // Element Plus 单独分包
          element: ['element-plus', '@element-plus/icons-vue'],
          // Vue 全家桶
          vendor: ['vue', 'vue-router', 'pinia', 'axios']
        }
      }
    }
  }
})

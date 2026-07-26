import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const API_TARGET = process.env.VITE_API_TARGET || 'http://localhost:8081'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: API_TARGET,
        changeOrigin: true
      },
      '/upload': {
        target: API_TARGET,
        changeOrigin: true
      }
    }
  }
})

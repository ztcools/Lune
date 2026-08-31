import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    globals: true,
    include: ['src/**/*.{test,spec}.js'],
    // 每个测试文件隔离环境，避免 localStorage / document 状态串扰
    restoreMocks: true,
    clearMocks: true
  }
})

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

const API_TARGET = process.env.VITE_API_TARGET || 'http://localhost:8081'

export default defineConfig({
  plugins: [
    vue(),
    // Element Plus 按需加载：只打包用到的组件，大幅减小 element chunk
    AutoImport({ resolvers: [ElementPlusResolver()] }),
    Components({ resolvers: [ElementPlusResolver()] })
  ],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: API_TARGET, changeOrigin: true },
      '/upload': { target: API_TARGET, changeOrigin: true }
    }
  },
  build: {
    chunkSizeWarningLimit: 1000,
    // 生产环境不生成 sourcemap，防止源码被直接还原查看
    sourcemap: false,
    // 移除构建产物中的 console/debugger，降低被调试的便利
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    rollupOptions: {
      output: {
        manualChunks: {
          // three.js 单独分包（仅首页雪花用到，按需加载）
          three: ['three'],
          // Vue 全家桶
          vendor: ['vue', 'vue-router', 'pinia', 'axios']
          // 注意：Element Plus 走 unplugin 按需加载，不强制整包，
          //       用到的组件会被 Rollup 自动分到各页面 chunk
        }
      }
    }
  }
})

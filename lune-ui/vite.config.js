import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { VitePWA } from 'vite-plugin-pwa'

const API_TARGET = process.env.VITE_API_TARGET || 'http://localhost:8081'
const isProd = process.env.NODE_ENV === 'production'

export default defineConfig({
  plugins: [
    vue(),
    // Element Plus 按需加载：只打包用到的组件，大幅减小 element chunk
    AutoImport({ resolvers: [ElementPlusResolver()] }),
    Components({ resolvers: [ElementPlusResolver()] }),
    // PWA：仅生产构建启用，避免影响 HMR
    isProd && VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'robots.txt'],
      manifest: {
        name: 'Lune - 记录美好生活',
        short_name: 'Lune',
        description: '记录美好生活，分享成长点滴的个人博客',
        theme_color: '#43a047',
        background_color: '#f4f9f4',
        display: 'standalone',
        start_url: '/',
        icons: [
          { src: '/pwa-192x192.png', sizes: '192x192', type: 'image/png' },
          { src: '/pwa-512x512.png', sizes: '512x512', type: 'image/png' }
        ]
      },
      workbox: {
        // 静态资源预缓存
        globPatterns: ['**/*.{js,css,html,svg,png,jpg,jpeg,woff,woff2}'],
        // 单文件最大 3MB
        maximumFileSizeToCacheInBytes: 3 * 1024 * 1024,
        // 运行时缓存
        runtimeCaching: [
          // API 配置（5 分钟）
          {
            urlPattern: /\/api\/site-config\/public/,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'api-config',
              expiration: { maxEntries: 5, maxAgeSeconds: 5 * 60 },
              networkTimeoutSeconds: 5
            }
          },
          // API 列表数据（1 分钟）
          {
            urlPattern: /\/api\/(articles|essays|records|treeholes|wishes|family|diaries|resume|categories|tags)/,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'api-data',
              expiration: { maxEntries: 50, maxAgeSeconds: 60 },
              networkTimeoutSeconds: 8
            }
          },
          // 上传的图片（7 天）
          {
            urlPattern: /\/upload\//,
            handler: 'CacheFirst',
            options: {
              cacheName: 'upload-images',
              expiration: { maxEntries: 200, maxAgeSeconds: 7 * 24 * 3600 }
            }
          },
          // alcy.cc 二次元图床（7 天）
          {
            urlPattern: /^https:\/\/t\.alcy\.cc\//,
            handler: 'CacheFirst',
            options: {
              cacheName: 'alcy-images',
              expiration: { maxEntries: 100, maxAgeSeconds: 7 * 24 * 3600 }
            }
          },
          // jsdelivr CDN（30 天）
          {
            urlPattern: /^https:\/\/cdn\.jsdelivr\.net\//,
            handler: 'CacheFirst',
            options: {
              cacheName: 'cdn-jsdelivr',
              expiration: { maxEntries: 50, maxAgeSeconds: 30 * 24 * 3600 }
            }
          },
          // Google Fonts（30 天）
          {
            urlPattern: /^https:\/\/fonts\.(googleapis|gstatic)\.com\//,
            handler: 'CacheFirst',
            options: {
              cacheName: 'google-fonts',
              expiration: { maxEntries: 30, maxAgeSeconds: 30 * 24 * 3600 }
            }
          }
        ]
      },
      devOptions: { enabled: false }
    })
  ].filter(Boolean),
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

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
        // 静态资源预缓存。预缓存是「每个访客首次访问就全量下载」，
        // 所以只放公开页面真正需要的东西。
        globPatterns: ['**/*.{js,css,html,svg,png,jpg,jpeg}'],
        globIgnores: [
          // 后台管理 chunk（含 ECharts，单独一个就 600 KB）只有站长会用，
          // 却曾被每位访客预缓存。见下方 chunkFileNames 的目录分流。
          'assets/admin/**',
          // three.js 495 KB，只有 PC 首页雪花特效用，且已是 defineAsyncComponent
          // 按需加载 —— 预缓存等于把「按需」两个字作废
          'assets/three-*.js',
          // 字体走下面的运行时缓存：unicode-range 分片意味着浏览器只会下载
          // 实际用到的那几个，预缓存反而把 12 个全拉下来
          '**/*.woff2'
        ],
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
          // 自托管字体（1 年）。预缓存里已排除 woff2，改由此处按需缓存：
          // 只有浏览器真正请求过的分片才进缓存
          {
            urlPattern: /\/assets\/fonts\/.*\.woff2$/,
            handler: 'CacheFirst',
            options: {
              cacheName: 'local-fonts',
              expiration: { maxEntries: 20, maxAgeSeconds: 365 * 24 * 3600 }
            }
          },
          // 中文书法体镜像（30 天）。原先这里配的是 fonts.googleapis/gstatic.com，
          // 而那两个域名在大陆访问不到，等于一条永远命中不了的规则
          {
            urlPattern: /^https:\/\/(fonts|gstatic)\.loli\.net\//,
            handler: 'CacheFirst',
            options: {
              cacheName: 'cjk-display-fonts',
              expiration: { maxEntries: 40, maxAgeSeconds: 30 * 24 * 3600 },
              cacheableResponse: { statuses: [0, 200] }
            }
          },
          // 中国地图 GeoJSON 582 KB，只有后台大盘用，改到就近缓存而非预缓存
          {
            urlPattern: /\/maps\/.*\.json$/,
            handler: 'CacheFirst',
            options: {
              cacheName: 'map-geojson',
              expiration: { maxEntries: 5, maxAgeSeconds: 30 * 24 * 3600 }
            }
          }
          // 说明：/media 下的自托管 MP3 不走 Service Worker。音频播放依赖
          // Range 请求，SW 缓存需额外挂 RangeRequests 插件才不会放回整段响应；
          // 交给 nginx 的长 max-age + 浏览器 HTTP 缓存更稳，见 nginx.prod.conf。
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
        // 后台页面的 chunk 单独放到 assets/admin/ 下，好让 Workbox 的
        // globIgnores 一条 'assets/admin/**' 就能把它们从预缓存里剔掉
        // （访客不需要后台代码，Dashboard 那个 chunk 光 ECharts 就 600 KB）。
        // 这里只改输出路径，不改分包逻辑，避免动 manualChunks 引入回归。
        chunkFileNames(chunkInfo) {
          const id = chunkInfo.facadeModuleId || ''
          return id.includes('/src/admin/')
            ? 'assets/admin/[name]-[hash].js'
            : 'assets/[name]-[hash].js'
        },
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

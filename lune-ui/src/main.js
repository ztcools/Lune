import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { setupSecurity } from './utils/security'
import { scheduleCjkDisplayFonts } from './utils/loadFonts'
import './assets/styles/variables.css'
import './assets/styles/global.css'
import './assets/styles/animations.css'

// 生产环境安全加固（禁用右键/调试快捷键、DevTools 检测）
setupSecurity()

// Element Plus 组件已由 unplugin-vue-components 按需自动注册，
// 无需 app.use(ElementPlus)，大幅减小打包体积。

const app = createApp(App)
app.use(createPinia())
app.use(router)
// Element Plus 图标体积很小，保留全量注册（后台大量使用）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')

// 装饰性中文书法体在首屏之后再注入，避免渲染阻塞（详见 utils/loadFonts.js）
scheduleCjkDisplayFonts()

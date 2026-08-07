<template>
  <div class="landing-page">
    <!-- 飘落花瓣装饰 -->
    <FloatPetals type="petal" :count="16" />

    <!-- 首页背景图片 -->
    <LuneImage
      style="animation: header-effect 2s"
      class="background-image-index"
      :src="coverImage"
      variant="hero"
      lcp
      alt=""
    />

    <!-- 首页文字 -->
    <div class="signature-wall myCenter my-animation-hideToShow">
      <h1 class="playful">
        <span v-for="(char, index) in titleChars" :key="index" :style="{ animationDelay: index * 0.15 + 's' }">{{ char }}</span>
      </h1>
      <div class="printer">
        <h3>
          {{ typedText }}<span class="cursor">|</span>
        </h3>
      </div>

      <!-- 入口门扉：原来并排三个按钮（进入/简历/许愿池），视觉上抢戏又分散注意，
           简历改从导航进、许愿池本来就在导航里，这里只留一道门。 -->
      <div class="gate-wrap">
        <button class="gate" @click="$router.push('/home')">
          <span class="gate-text">入 内</span>
        </button>
      </div>

      <!-- 波浪效果 -->
      <div id="bannerWave1"></div>
      <div id="bannerWave2"></div>

      <!-- 向下滚动箭头 -->
      <div class="scroll-down-icon" @click="scrollDown">
        <el-icon :size="40" color="var(--white)">
          <ArrowDown />
        </el-icon>
      </div>
    </div>

    <!-- 页脚备案：默认隐藏，滚动到页面底部时才显示 -->
    <footer class="landing-footer" :class="{ show: footerVisible }">
      <p class="footer-icp">{{ appStore.webInfo.footer || '© 2024 Lune. All Rights Reserved.' }}</p>
      <p class="footer-beian" v-if="appStore.config.beian_icp">
        <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener">{{ appStore.config.beian_icp }}</a>
      </p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { useAppStore } from '../../stores/app'
import { usePageBackground } from '../../composables/usePageBackground'
import LuneImage from '../../components/LuneImage.vue'
import FloatPetals from '../../components/effects/FloatPetals.vue'

const appStore = useAppStore()

const titleChars = ref([])
const typedText = ref('')
const fullText = computed(() => '一纸浮生，半卷光阴')

// 从 store 获取随机封面，或使用默认图片
const coverImage = usePageBackground('landing')

let typingTimer = null
const footerVisible = ref(false)

function onScrollFooter() {
  // 滚动到接近页面底部时显示备案页脚
  const nearBottom = window.innerHeight + window.scrollY >= document.body.scrollHeight - 80
  footerVisible.value = nearBottom
}

onMounted(() => {
  appStore.fetchConfig()
  titleChars.value = (appStore.webInfo.webTitle || 'Lune').split('')
  window.addEventListener('scroll', onScrollFooter, { passive: true })

  // 打字机效果（渐进打出，停顿后回退）；「减少动态效果」下直接显示整句
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    typedText.value = fullText.value
    return
  }
  let i = 0
  let forward = true
  let holdTicks = 0
  typingTimer = setInterval(() => {
    const text = fullText.value
    if (forward) {
      typedText.value = text.slice(0, i + 1)
      i++
      if (i >= text.length) { forward = false; holdTicks = 20 }
    } else {
      if (holdTicks > 0) { holdTicks--; return }
      typedText.value = text.slice(0, i)
      i--
      if (i <= 0) forward = true
    }
  }, 100)
})

onUnmounted(() => {
  if (typingTimer) clearInterval(typingTimer)
  window.removeEventListener('scroll', onScrollFooter)
})

function scrollDown() {
  window.scrollTo({
    top: window.innerHeight,
    behavior: 'smooth',
  })
}
</script>

<style scoped>
.landing-page {
  min-height: 100vh;
  overflow: hidden;
}

/* ===== 背景图片 ===== */
.background-image-index {
  width: 100vw;
  height: 100vh;
  height: 100dvh;
  position: fixed;
  z-index: -1;
}

.background-image-index::before {
  position: absolute;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.2);
  content: '';
}

.background-image-index-error {
  background-color: var(--lightGreen);
  width: 100vw;
  height: 100vh;
  height: 100dvh;
  position: fixed;
  z-index: -1;
}

/* ===== 签名墙（内容区） ===== */
.signature-wall {
  display: flex;
  flex-direction: column;
  position: relative;
  user-select: none;
  /* 手机地址栏会吃掉 100vh 与可视高度的差值，进入按钮被顶到折叠线以下 */
  height: 100vh;
  height: 100dvh;
  overflow: hidden;
}

/* ===== 动态标题 ===== */
.playful {
  color: var(--white);
  font-size: 40px;
  margin-bottom: 20px;
}

.playful span {
  color: var(--white);
  display: inline-block;
  animation: scatter 1.75s infinite;
}

/* ===== 打字机字幕 ===== */
.printer {
  cursor: pointer;
  color: var(--white);
  background: var(--translucent);
  border-radius: 10px;
  padding: 5px 20px;
  margin-bottom: 30px;
}

.printer h3 {
  font-size: 18px;
  font-weight: 400;
  min-height: 28px;
}

/* 光标 */
.cursor {
  margin-left: 1px;
  animation: hideToShow 0.7s infinite;
  font-weight: 200;
}

/* ===== 入口门扉 ===== */
.gate-wrap {
  display: flex;
  justify-content: center;
  margin-top: 14px;
  z-index: 12;
}

.gate {
  position: relative;
  padding: 17px 54px;
  border-radius: 2px;
  border: 1px solid rgba(255, 255, 255, 0.55);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  cursor: pointer;
  overflow: hidden;
  transition: background 0.45s ease, border-color 0.45s ease, letter-spacing 0.45s ease;
}

.gate-text {
  position: relative;
  z-index: 2;
  color: #fff;
  font-family: var(--calligraphy-font);
  font-size: 21px;
  letter-spacing: 8px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.25);
}

/* hover 时一圈光晕从中心荡开：只动 transform / opacity，不触发重排 */
.gate::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, rgba(255, 255, 255, 0.45), transparent 62%);
  opacity: 0;
  transform: scale(0.35);
  transition: transform 0.7s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.7s ease;
  pointer-events: none;
}
.gate:hover { background: rgba(255, 255, 255, 0.18); border-color: rgba(255, 255, 255, 0.85); }
.gate:hover::after { opacity: 1; transform: scale(1.6); }
.gate:active { transform: scale(0.97); }

/* 门框四角的细线，弱化「按钮」感 */
.gate::before {
  content: '';
  position: absolute;
  inset: 5px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 1px;
  pointer-events: none;
}

/* ===== 波浪效果 ===== */
#bannerWave1 {
  height: 84px;
  background: linear-gradient(180deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.3) 100%);
  position: absolute;
  width: 200%;
  bottom: 0;
  z-index: 10;
  animation: gradientBG 120s linear infinite;
  border-radius: 40% 60% 0 0;
}

#bannerWave2 {
  height: 100px;
  background: linear-gradient(180deg, rgba(255,255,255,0.05) 0%, rgba(255,255,255,0.2) 100%);
  position: absolute;
  width: 400%;
  bottom: 0;
  z-index: 5;
  animation: gradientBG 120s linear infinite reverse;
  border-radius: 50% 40% 0 0;
}

/* ===== 向下滚动箭头 ===== */
.scroll-down-icon {
  position: absolute;
  bottom: 60px;
  z-index: 15;
  cursor: pointer;
  animation: my-shake 1.5s ease-out infinite;
}

/* ===== 页脚备案（默认隐藏，滚到底才显示） ===== */
.landing-footer {
  padding: 18px 20px;
  text-align: center;
  background: rgba(0, 0, 0, 0.28);
  backdrop-filter: blur(8px);
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  opacity: 0;
  transform: translateY(8px);
  transition: all 0.4s ease;
  pointer-events: none;
}
.landing-footer.show {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}
.footer-icp { margin: 0; letter-spacing: 0.5px; }
.footer-beian { margin: 6px 0 0; }
.footer-beian a { color: rgba(255, 255, 255, 0.7); transition: color 0.3s; }
.footer-beian a:hover { color: #fff; }

/* ===== 响应式 ===== */
@media screen and (max-width: 768px) {
  .playful {
    font-size: 35px;
  }

  .printer h3 {
    font-size: 16px;
  }

  .gate { padding: 15px 42px; }
  .gate-text { font-size: 19px; letter-spacing: 6px; }
}

@media (prefers-reduced-motion: reduce) {
  .gate, .gate::after { transition: none; }
  .gate:hover::after { opacity: 0; transform: scale(0.35); }
  .gate:active { transform: none; }
  .cursor { animation: none; }
}
</style>

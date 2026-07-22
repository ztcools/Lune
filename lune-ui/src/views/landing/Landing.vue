<template>
  <div class="landing-page">
    <!-- 首页背景图片 -->
    <el-image
      style="animation: header-effect 2s"
      class="background-image-index"
      v-once
      lazy
      :src="coverImage"
      fit="cover"
    >
      <template #error>
        <div class="background-image-index-error" />
      </template>
    </el-image>

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

      <!-- 动作按钮 -->
      <div class="hero-actions">
        <el-button size="large" round type="primary" @click="$router.push('/home')">进入首页</el-button>
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

    <!-- 页脚 -->
    <footer class="landing-footer">
      <p>{{ appStore.webInfo.footer || '© 2024 Lune. All Rights Reserved.' }}</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { useAppStore } from '../../stores/app'

const appStore = useAppStore()

const titleChars = ref([])
const typedText = ref('')
const fullText = computed(() => '记录美好生活，分享成长点滴 ✨')

// 从 store 获取随机封面，或使用默认图片
const coverImage = computed(() => {
  const bg = appStore.webInfo.backgroundImage
  if (bg) return bg
  try {
    const covers = JSON.parse(appStore.webInfo.randomCover || '[]')
    if (covers.length > 0) {
      return covers[Math.floor(Math.random() * covers.length)]
    }
  } catch (e) { /* ignore */ }
  const idx = Math.floor(Math.random() * 11) + 1
  return `/assets/背景${idx}.jpg`
})

let typingTimer = null

onMounted(() => {
  // 标题拆分
  titleChars.value = (appStore.webInfo.webTitle || 'Lune').split('')

  // 打字机效果（渐进打出，然后回退）
  let i = 0
  let forward = true
  typingTimer = setInterval(() => {
    const text = fullText.value
    if (forward) {
      typedText.value = text.slice(0, i + 1)
      i++
      if (i >= text.length) {
        forward = false
        setTimeout(() => {}, 2000)
      }
    } else {
      typedText.value = text.slice(0, i)
      i--
      if (i <= 0) {
        forward = true
      }
    }
  }, 100)
})

onUnmounted(() => {
  if (typingTimer) clearInterval(typingTimer)
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
  position: fixed;
  z-index: -1;
}

/* ===== 签名墙（内容区） ===== */
.signature-wall {
  display: flex;
  flex-direction: column;
  position: relative;
  user-select: none;
  height: 100vh;
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

/* ===== 动作按钮 ===== */
.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 10px;
  z-index: 15;
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

/* ===== 页脚 ===== */
.landing-footer {
  background: var(--gradientBG);
  background-size: 400% 400%;
  animation: gradientBG 10s ease infinite;
  padding: 20px;
  text-align: center;
  color: var(--white);
  font-size: 14px;
}

/* ===== 响应式 ===== */
@media screen and (max-width: 768px) {
  .playful {
    font-size: 35px;
  }

  .printer h3 {
    font-size: 16px;
  }
}
</style>

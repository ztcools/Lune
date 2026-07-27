<template>
  <div class="family-page">
    <!-- Sakura background -->
    <SakuraFall :count="80" :fallSpeed="1.0" :wind="0.4" />

    <!-- Hero Banner -->
    <div class="hero-section">
      <div class="bg-image" :style="{ backgroundImage: `url(${familyHeroBg})` }" v-if="familyHeroBg" />
      <div class="hero-overlay" />
      <!-- Meteors -->
      <div class="meteors-container">
        <span v-for="i in 15" :key="i" class="meteor" :style="meteorStyle(i)" />
      </div>
      <div class="hero-content">
        <!-- Avatars + Lightning Connection -->
        <div class="couple-area">
          <div class="person-card">
            <el-avatar class="couple-avatar" shape="square" :size="100" :src="family.manCover || defaultMan" @click="sendHeart('left')" />
            <div class="couple-name">{{ family.manName || '他' }}</div>
          </div>

          <!-- Lightning + Beam connector -->
          <div class="connector-wrap">
            <!-- Flying heart -->
            <Transition name="heart-fly">
              <div v-if="flyingHeart" class="flying-heart" :class="flyingHeart.from">❤️</div>
            </Transition>
            <svg class="lightning-svg" viewBox="0 0 120 160" width="120" height="160">
              <defs>
                <filter id="glow">
                  <feGaussianBlur stdDeviation="2.5" result="blur" />
                  <feMerge><feMergeNode in="blur" /><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge>
                </filter>
              </defs>
              <path :d="lightningPath" stroke="#ffe082" stroke-width="2.5" fill="none" filter="url(#glow)" />
              <path :d="lightningPath" stroke="#fff" stroke-width="1" fill="none" opacity="0.7" />
            </svg>
            <!-- Beam rays -->
            <div class="beam-rays">
              <div class="beam-ray" v-for="i in 8" :key="i" :style="{ transform: `rotate(${i*45}deg)` }" />
            </div>
          </div>

          <div class="person-card">
            <el-avatar class="couple-avatar" shape="square" :size="100" :src="family.womanCover || defaultWoman" @click="sendHeart('right')" />
            <div class="couple-name">{{ family.womanName || '她' }}</div>
          </div>
        </div>

        <!-- Timer -->
        <div class="timer-row">
          <span>第</span><span class="timer-num">{{ timing.year }}</span><span>年</span>
          <span class="timer-num">{{ timing.month }}</span><span>月</span>
          <span class="timer-num">{{ timing.day }}</span><span>日</span>
          <span class="timer-num">{{ timing.hour }}</span><span>时</span>
          <span class="timer-num">{{ timing.minute }}</span><span>分</span>
          <span class="timer-num">{{ timing.second }}</span><span>秒</span>
        </div>
        <div v-if="family.countdownTime" class="countdown-text">{{ family.countdownTitle }}: {{ countdownText }}</div>
      </div>
    </div>

    <!-- Tab Navigation -->
    <div class="tab-nav">
      <button :class="{ active: activeTab === 'painting' }" @click="activeTab = 'painting'">世界名画</button>
      <button :class="{ active: activeTab === 'blessing' }" @click="activeTab = 'blessing'">祝福板</button>
      <button :class="{ active: activeTab === 'diary' }" @click="activeTab = 'diary'">点点滴滴</button>
    </div>

    <!-- Tab: 世界名画 -->
    <div v-show="activeTab === 'painting'" class="painting-card">
      <div class="painting-inner">
        <div class="painting-frame">
          <el-image src="" class="painting-img" alt="世界名画" v-if="false" />
          <div class="painting-vignette" />
          <div class="painting-label">世界名画</div>
        </div>
      </div>
    </div>

    <!-- Tab: 祝福板 -->
    <div v-show="activeTab === 'blessing'" class="blessing-section">
      <div class="blessing-carousel-wrap">
        <div class="blessing-track" :style="carouselStyle">
          <div v-for="(b, i) in blessingLoop" :key="i" class="blessing-slide">
            <el-avatar :size="44" :src="b.avatar || randomAvatar(i)">
              <el-icon :size="22"><UserFilled /></el-icon>
            </el-avatar>
            <div class="blessing-slide-body">
              <span v-if="b.username" class="blessing-slide-name">{{ b.username }}</span>
              <span class="blessing-slide-text">{{ b.content }}</span>
              <span class="blessing-slide-time">{{ timeAgo(b.createTime) }}</span>
            </div>
          </div>
          <div v-for="(b, i) in blessingLoop" :key="'dup'+i" class="blessing-slide">
            <el-avatar :size="44" :src="b.avatar || randomAvatar(i)">
              <el-icon :size="22"><UserFilled /></el-icon>
            </el-avatar>
            <div class="blessing-slide-body">
              <span v-if="b.username" class="blessing-slide-name">{{ b.username }}</span>
              <span class="blessing-slide-text">{{ b.content }}</span>
              <span class="blessing-slide-time">{{ timeAgo(b.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="blessing-input-bar">
        <div class="blessing-input-wrap">
          <el-input v-model="blessingText" placeholder="💌 写下祝福..." size="large" class="blessing-input"
            @keyup.enter="submitBlessing" />
          <button class="blessing-send-btn" :disabled="!blessingText.trim()" @click="submitBlessing">
            <svg viewBox="0 0 24 24" width="20" height="20"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" fill="currentColor"/></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Tab: 烂皮书 (点点滴滴) -->
    <div v-show="activeTab === 'diary'" class="diary-book">
      <div class="book-container">
        <!-- Left page -->
        <div class="book-page book-left" :key="'l'+currentPage">
          <div class="page-content">
            <div class="page-record-time">{{ formatFullDate(leftPage?.recordTime) }}</div>
            <img v-if="firstDiaryImage(leftPage)" :src="firstDiaryImage(leftPage)" class="page-image" />
            <div class="page-text" v-html="leftPage?.content"></div>
            <div class="page-number">{{ leftPage?.pageOrder || currentPage * 2 - 1 }}</div>
          </div>
        </div>
        <!-- Right page -->
        <div class="book-page book-right" :key="'r'+currentPage">
          <div class="page-content">
            <div class="page-record-time">{{ formatFullDate(rightPage?.recordTime) }}</div>
            <img v-if="firstDiaryImage(rightPage)" :src="firstDiaryImage(rightPage)" class="page-image" />
            <div class="page-text" v-html="rightPage?.content"></div>
            <div class="page-number">{{ rightPage?.pageOrder || currentPage * 2 }}</div>
          </div>
        </div>
      </div>

      <!-- Page navigation -->
      <div class="book-nav">
        <button :disabled="currentPage <= 1" @click="prevPage">◂ 上一页</button>
        <span class="book-page-indicator">{{ currentPage }} / {{ totalPages }}</span>
        <button :disabled="currentPage >= totalPages" @click="nextPage">下一页 ▸</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { familyApi, diaryApi, commentApi } from '../../api/modules'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import { requireLogin } from '../../composables/useAuth'
import { usePageBackground } from '../../composables/usePageBackground'
import { UserFilled } from '@element-plus/icons-vue'
import SakuraFall from '../../components/SakuraFall.vue'

const familyHeroBg = usePageBackground('familyHero')
const familyContentBg = usePageBackground('familyContent')

function randomAvatar(i) { return '' }
function randomNick() { const nicks=['小星星','月亮船','阳光','微风','彩虹糖','云朵','海浪','樱花','蒲公英','小太阳']; return nicks[Math.floor(Math.random()*nicks.length)] }

const userStore = useUserStore()
const defaultMan = ''
const defaultWoman = ''

// Family
const family = ref({})
let timerInterval = null
const timing = reactive({ year:0, month:0, day:0, hour:0, minute:0, second:0 })
const countdownText = ref('')

// Tabs
const activeTab = ref('painting')

// Lightning: 定时刷新，避免 computed 中 Math.random() 导致频繁重渲染
const lightningPath = ref(generateLightning())
function generateLightning() {
  const segs = 8, dy = 160 / segs
  let path = 'M60,0'
  for (let i = 1; i <= segs; i++) {
    const x = 60 + (Math.random() - 0.5) * 50 + (i % 2 ? 15 : -15)
    path += ` L${x},${i * dy}`
  }
  return path
}
let lightningTimer = null

// Blessings
const blessingList = ref([])
const blessingText = ref('')
const blessingPosting = ref(false)
const blessingLoop = computed(() => blessingList.value.length > 0 ? blessingList.value : defaultBlessings)
const carouselStyle = computed(() => {
  const count = blessingLoop.value.length || 1
  return { animationDuration: `${Math.max(count * 4, 10)}s` }
})

// Diary book
const diaryPages = ref([])
const currentPage = ref(1)
const totalPages = computed(() => Math.max(1, Math.ceil(diaryPages.value.length / 2)))
const leftPage = computed(() => diaryPages.value[(currentPage.value - 1) * 2] || null)
const rightPage = computed(() => diaryPages.value[(currentPage.value - 1) * 2 + 1] || null)

// Default blessings
const defaultBlessings = [
  { id:1, username:'小星星', avatar:'', content:'祝你们永远幸福！💕', createTime: new Date().toISOString() },
  { id:2, username:'月亮', avatar:'', content:'岁月静好，与君偕老 🌙', createTime: new Date().toISOString() },
  { id:3, username:'阳光', avatar:'', content:'看到你们就觉得爱情真美好 ☀️', createTime: new Date().toISOString() },
]

const meteorStyles = ref(Array.from({ length: 15 }, () => generateMeteorStyle()))
function generateMeteorStyle() {
  const top = Math.random() * 100, left = Math.random() * 100
  const dur = 2 + Math.random() * 4, delay = Math.random() * 5
  return { top: `${top}%`, left: `${left}%`, animationDuration: `${dur}s`, animationDelay: `${delay}s` }
}
function meteorStyle(i) { return meteorStyles.value[i - 1] || {} }

onMounted(async () => {
  await fetchFamily()
  fetchBlessings()
  fetchDiaries()
  lightningTimer = setInterval(() => { lightningPath.value = generateLightning() }, 3000)
})
onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
  if (lightningTimer) clearInterval(lightningTimer)
})

async function fetchFamily() {
  try {
    const data = await familyApi.list()
    if (data && (Array.isArray(data) ? data.length > 0 : data.id)) {
      family.value = Array.isArray(data) ? data[0] : data
      startTimer()
    }
  } catch (e) { /* silent */ }
}

function startTimer() {
  if (timerInterval) clearInterval(timerInterval)
  const tick = () => {
    if (!family.value.timing) return
    const start = new Date(family.value.timing).getTime()
    const diff = Math.max(0, Math.floor((Date.now() - start) / 1000))
    timing.second = diff % 60; timing.minute = Math.floor(diff/60) % 60
    timing.hour = Math.floor(diff/3600) % 24; timing.day = Math.floor(diff/86400)
    timing.month = Math.floor(timing.day / 30); timing.year = Math.floor(timing.day / 365)
    if (family.value.countdownTime) {
      const cd = new Date(family.value.countdownTime).getTime() - Date.now()
      if (cd <= 0) countdownText.value = '已到来!'
      else {
        const d = Math.floor(cd/86400000), h = Math.floor((cd%86400000)/3600000)
        const m = Math.floor((cd%3600000)/60000), s = Math.floor((cd%60000)/1000)
        countdownText.value = `${d}天${h}时${m}分${s}秒`
      }
    }
  }
  tick(); timerInterval = setInterval(tick, 1000)
}

async function fetchBlessings() {
  try {
    const data = await commentApi.list({ type: 'love', page: 1, size: 50 })
    blessingList.value = data?.records || data || []
  } catch (e) { /* silent */ }
}

async function submitBlessing() {
  if (!blessingText.value.trim()) return
  if (!requireLogin()) return
  blessingPosting.value = true
  try {
    await commentApi.create({ content: blessingText.value, type: 'love', sourceId: 0 })
    ElMessage.success('祝福发送成功')
    blessingText.value = ''
    fetchBlessings()
  } catch (e) { ElMessage.error('发送失败') }
  finally { blessingPosting.value = false }
}

async function fetchDiaries() {
  try {
    const data = await diaryApi.list({ page: 1, size: 100 })
    diaryPages.value = data?.records || []
  } catch (e) { /* silent */ }
}

function prevPage() { if (currentPage.value > 1) currentPage.value-- }
function nextPage() { if (currentPage.value < totalPages.value) currentPage.value++ }

function firstDiaryImage(page) {
  if (!page?.images) return null
  try { const arr = JSON.parse(page.images); return arr[0] || null } catch { return null }
}
function timeAgo(d) {
  if (!d) return ''
  const diff = Date.now() - new Date(d).getTime()
  if (diff < 6e4) return '刚刚'
  if (diff < 36e5) return Math.floor(diff/6e4)+'分钟前'
  if (diff < 864e5) return Math.floor(diff/36e5)+'小时前'
  return Math.floor(diff/864e5)+'天前'
}
// Flying heart animation
const flyingHeart = ref(null)
function sendHeart(from) {
  flyingHeart.value = { from }
  setTimeout(() => { flyingHeart.value = null }, 1200)
}


function formatFullDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', { year:'numeric', month:'long', day:'numeric' })
}
</script>

<style scoped>
/* ====== 字体：Ma Shan Zheng + Liu Jian Mao Cao ====== */
/* 使用系统回退字体，如需自托管请将 woff2 放入 public/assets/fonts/ */

/* ====== Hero ====== */
.hero-section {
  position: relative; min-height: 28vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(180deg, #2d1b2e 0%, #4a2c3f 40%, #7a4b5c 70%, #c8a08c 100%);
  overflow: hidden;
}
.hero-overlay {
  position: absolute; inset: 0;
  background: radial-gradient(ellipse at center, transparent 40%, rgba(0,0,0,0.3) 100%);
  z-index: 2;
}
.hero-content {
  position: relative; z-index: 3; display: flex; flex-direction: column; align-items: center; gap: 10px;
}

/* Couple area */
.couple-area { display: flex; align-items: center; gap: 8px; }
.person-card { display: flex; flex-direction: column; align-items: center; gap: 12px; }
.couple-avatar {
  border-radius: 22px !important;
  border: 3px solid rgba(255,255,255,0.3) !important;
  box-shadow: 0 0 30px rgba(255,200,150,0.3);
}
.couple-name { color: #fff; font-size: 17px; font-weight: 600; letter-spacing: 1px; }

/* Lightning connector */
.connector-wrap { position: relative; width: 80px; height: 90px; display: flex; align-items: center; justify-content: center; }
.lightning-svg { position: absolute; z-index: 2; }
.beam-rays { position: absolute; width: 100px; height: 100px; }
.beam-ray {
  position: absolute; top: 50%; left: 50%; width: 2px; height: 60px;
  background: linear-gradient(to top, rgba(255,220,150,0), rgba(255,220,150,0.6), rgba(255,220,150,0));
  transform-origin: bottom center; margin-top: -60px;
  animation: beamPulse 2s ease-in-out infinite;
}
.beam-ray:nth-child(2n) { animation-delay: 0.5s; }
.beam-ray:nth-child(3n) { animation-delay: 1s; }
@keyframes beamPulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}
/* Flying heart animation */
.flying-heart {
  position: absolute; font-size: 28px; z-index: 20; pointer-events: none;
  top: 60px; left: 50%; transform: translateX(-50%);
  animation: heartFly 1.2s ease-out forwards;
}
.flying-heart.left { animation-name: heartFlyLeft; }
@keyframes heartFly {
  0% { opacity: 1; transform: translateX(-50%) translateY(0) scale(0.5); }
  50% { opacity: 1; transform: translateX(50%) translateY(-40px) scale(1.3); }
  100% { opacity: 0; transform: translateX(80%) translateY(-60px) scale(0.3); }
}
@keyframes heartFlyLeft {
  0% { opacity: 1; transform: translateX(-50%) translateY(0) scale(0.5); }
  50% { opacity: 1; transform: translateX(-150%) translateY(-40px) scale(1.3); }
  100% { opacity: 0; transform: translateX(-200%) translateY(-60px) scale(0.3); }
}

/* Timer */
.timer-row { color: #ffe8d0; font-size: 16px; letter-spacing: 1px; }
.timer-num { font-size: 26px; font-weight: 700; color: #fff; margin: 0 2px; font-family: 'Ma Shan Zheng', cursive; }
.countdown-text { color: #ffccaa; font-size: 13px; letter-spacing: 1px; }

/* ====== Tab Nav ====== */
.tab-nav {
  display: flex; justify-content: center; gap: 16px; padding: 14px 12px;
  background: rgba(255,255,255,0.8); backdrop-filter: blur(8px);
  position: sticky; top: 0; z-index: 10;
}
.tab-nav button {
  padding: 10px 30px; border: 2px solid #d4a574; background: transparent;
  color: #6b4c3b; border-radius: 28px; font-size: 17px; cursor: pointer;
  transition: all 0.3s; font-family: 'Ma Shan Zheng', cursive; letter-spacing: 1px;
}
.tab-nav button.active,
.tab-nav button:hover { background: #6b4c3b; color: #fff; border-color: #6b4c3b; }

/* ====== World Painting Card ====== */
.painting-card { display: flex; justify-content: center; padding: 20px 16px 20px; min-height: 62vh; }
.painting-inner { max-width: 800px; width: 100%; }
.painting-frame {
  position: relative; border-radius: 16px; overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1), 0 0 0 8px #f5e6d3, 0 0 0 10px #c8a882, 0 0 0 18px #f5e6d3;
}
.painting-img { width: 100%; min-height: 55vh; max-height: 65vh; object-fit: cover; display: block; filter: sepia(0.2) brightness(0.95); }
.painting-vignette {
  position: absolute; inset: 0;
  box-shadow: inset 0 0 80px rgba(0,0,0,0.4);
  pointer-events: none;
}
.painting-label {
  position: absolute; bottom: 16px; left: 50%; transform: translateX(-50%);
  color: #f5e6d3; font-size: 20px; letter-spacing: 4px;
  font-family: 'Ma Shan Zheng', cursive;
  text-shadow: 0 2px 8px rgba(0,0,0,0.5);
}

/* ====== Blessing Board ====== */
.blessing-section { padding: 16px 16px 20px; max-width: 760px; margin: 0 auto; min-height: 60vh; }
.blessing-carousel-wrap { overflow: hidden; margin-bottom: 16px; border-radius: 18px; background: linear-gradient(135deg, rgba(255,248,240,0.8), rgba(255,240,245,0.8)); height: 52vh; }
.blessing-track {
  display: flex; flex-direction: column; gap: 6px;
  animation: blessScroll var(--dur, 20s) linear infinite;
  padding: 14px 0;
}
.blessing-track:hover { animation-play-state: paused; }
@keyframes blessScroll { 0% { transform: translateY(0); } 100% { transform: translateY(-50%); } }
.blessing-slide {
  display: flex; align-items: flex-start; gap: 12px; padding: 12px 18px;
  border-bottom: 1px solid rgba(0,0,0,0.04); flex-shrink: 0;
  transition: background 0.3s;
}
.blessing-slide:hover { background: rgba(255,107,157,0.05); }
.blessing-slide-body { flex: 1; min-width: 0; }
.blessing-slide-name { font-weight: 700; font-size: 14px; color: #e8734a; margin-right: 8px; }
.blessing-slide-text { font-size: 15px; color: #444; line-height: 1.5; }
.blessing-slide-time { display: block; font-size: 11px; color: #ccc; margin-top: 3px; }
.blessing-input-bar { padding: 8px 0; }
.blessing-input-wrap {
  display: flex; gap: 10px; align-items: center;
  background: linear-gradient(135deg, #fff5f7, #fff0f5, #fef5e7);
  border-radius: 30px; padding: 6px 6px 6px 18px;
  box-shadow: 0 2px 16px rgba(255,107,157,0.12);
  border: 2px solid transparent;
  transition: all 0.3s;
}
.blessing-input-wrap:focus-within {
  border-color: #ff6b9d;
  box-shadow: 0 4px 24px rgba(255,107,157,0.2);
}
.blessing-input :deep(.el-input__wrapper) {
  border-radius: 24px; background: transparent; box-shadow: none !important;
  padding: 0;
}
.blessing-input :deep(.el-input__inner) { font-size: 15px; }
.blessing-send-btn {
  width: 46px; height: 46px; border-radius: 50%; border: none;
  background: linear-gradient(135deg, #ff6b9d, #ff8e53); color: #fff;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all 0.3s; flex-shrink: 0;
  box-shadow: 0 3px 12px rgba(255,107,157,0.3);
}
.blessing-send-btn:hover:not(:disabled) { transform: scale(1.1); box-shadow: 0 6px 24px rgba(255,107,157,0.5); }
.blessing-send-btn:disabled { opacity: 0.4; cursor: default; }

/* ====== Diary Book ====== */
.diary-book { display: flex; flex-direction: column; align-items: center; padding: 24px 16px 20px; min-height: 60vh; }
.book-container {
  display: flex; gap: 0; max-width: 860px; width: 100%;
  perspective: 1500px;
  background: linear-gradient(to right, #e8d5c0 0%, #f0e0cc 2%, #faf3e8 4%, #faf3e8 96%, #f0e0cc 98%, #e8d5c0 100%);
  border-radius: 8px; box-shadow: 0 8px 40px rgba(0,0,0,0.15), inset 0 0 30px rgba(0,0,0,0.05);
  min-height: 55vh;
}
.book-page {
  flex: 1; padding: 18px 20px;
  font-family: 'Ma Shan Zheng', 'Liu Jian Mao Cao', 'KaiTi', cursive;
  position: relative;
}
.book-left { border-right: 1px solid rgba(0,0,0,0.08); }
.book-right { border-left: 1px solid rgba(0,0,0,0.08); }
.page-content {
  position: relative; height: 100%;
  background: repeating-linear-gradient(transparent, transparent 33px, rgba(0,0,0,0.04) 33px, rgba(0,0,0,0.04) 34px);
}
.page-record-time {
  font-size: 14px; color: #8b7355; margin-bottom: 10px;
  border-bottom: 1px dashed #c8b898; padding-bottom: 6px;
}
.page-image {
  width: 100%; max-height: 200px; object-fit: cover; border-radius: 4px;
  margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.page-text { font-size: 18px; line-height: 34px; color: #3a2a1a; }
.page-text :deep(p) { margin: 0 0 17px 0; text-indent: 2em; }
.page-number {
  position: absolute; bottom: 0; right: 0;
  font-size: 16px; color: #b8a080; font-style: italic;
}
.book-nav {
  display: flex; align-items: center; gap: 24px; margin-top: 24px;
}
.book-nav button {
  padding: 8px 20px; border: 1px solid #c8a882; background: #faf3e8;
  border-radius: 20px; cursor: pointer; font-family: 'Ma Shan Zheng', cursive;
  font-size: 16px; color: #6b4c3b; transition: all 0.3s;
}
.book-nav button:hover:not(:disabled) { background: #6b4c3b; color: #fff; }
.book-nav button:disabled { opacity: 0.4; cursor: default; }
.book-page-indicator { font-size: 15px; color: #8b7355; font-family: 'Ma Shan Zheng', cursive; }

/* Page turn animation */
.book-page { transition: transform 0.5s ease, opacity 0.5s ease; }
.book-left { transform-origin: right center; }
.book-right { transform-origin: left center; }

/* ====== Responsive ====== */
@media screen and (max-width: 768px) {
  .couple-avatar { width: 100px !important; height: 100px !important; border-radius: 16px !important; }
  .connector-wrap { width: 60px; height: 100px; }
  .lightning-svg { width: 60px; height: 100px; }
  .beam-rays { width: 50px; height: 50px; }
  .beam-ray { height: 30px; }
  .timer-row { font-size: 14px; }
  .timer-num { font-size: 24px; }
  .book-container { flex-direction: column; min-height: auto; }
  .book-left { border-right: none; border-bottom: 1px solid rgba(0,0,0,0.08); }
  .book-right { border-left: none; border-top: 1px solid rgba(0,0,0,0.08); }
  .painting-frame { box-shadow: 0 0 0 4px #f5e6d3, 0 0 0 6px #c8a882; }
  .painting-label { font-size: 20px; letter-spacing: 4px; }
  .meteors-container { display: none; }
}
/* ====== Meteor Shooting Stars ====== */
.meteors-container { position: absolute; inset: 0; overflow: hidden; z-index: 1; pointer-events: none; }
.meteor {
  position: absolute; width: 2px; height: 70px;
  background: linear-gradient(to top, rgba(255,255,255,0), rgba(255,255,255,0.8));
  border-radius: 1px; opacity: 0;
  animation: meteorFall linear infinite;
  transform: rotate(-35deg);
}
.meteor::after {
  content: ''; position: absolute; top: 0; left: 0;
  width: 2px; height: 20px;
  background: linear-gradient(to top, rgba(255,255,255,0), #fff);
  border-radius: 50%;
}
@keyframes meteorFall {
  0% { opacity: 0; transform: rotate(-35deg) translateY(-100px) translateX(0); }
  5% { opacity: 1; }
  15% { opacity: 0; }
  100% { opacity: 0; transform: rotate(-35deg) translateY(400px) translateX(-100px); }
}
.tab-content { position: relative; overflow: hidden; }
/* bg-image overlay */
.bg-image { position: absolute; inset: 0; background-size: cover; background-position: center; z-index: 0; }
.content-bg { position: absolute; inset: 0; background-size: cover; background-position: center; z-index: 0; opacity: 0.15; }
</style>

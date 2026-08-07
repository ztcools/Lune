<template>
  <div class="family-page">
    <!-- 樱花雨背景 -->
    <SakuraFall :count="50" :fallSpeed="0.9" :wind="0.3" />
    <!-- 漂浮爱心 -->
    <div class="love-float-layer" aria-hidden="true">
      <span v-for="h in floatHearts" :key="h.id" class="float-heart" :style="h.style">{{ h.char }}</span>
    </div>

    <!-- ===== Hero：情侣互动区 ===== -->
    <div class="love-hero">
      <div class="bg-image" :style="{ backgroundImage: `url(${familyHeroBg})` }" v-if="familyHeroBg" />
      <div class="love-hero-overlay" />
      <!-- 闪烁星星 -->
      <div class="twinkle-layer">
        <span v-for="s in stars" :key="s.id" class="twinkle-star" :style="s.style">✦</span>
      </div>

      <div class="love-hero-content">
        <!-- 情侣头像 + 爱心连线 -->
        <div class="couple-area">
          <div class="person-card" @click="burstHearts('left')">
            <div class="avatar-ring-wrap">
              <el-avatar class="couple-avatar" :size="104" :src="family.manCover || defaultMan" />
              <div class="avatar-halo"></div>
            </div>
            <div class="couple-name">{{ family.manName || '他' }}</div>
          </div>

          <!-- 中间爱心连接 -->
          <div class="love-connector">
            <div class="heartbeat-wrap" @click="burstHearts('center')">
              <span class="big-heart">💗</span>
              <div class="heart-pulse"></div>
            </div>
            <svg class="love-line" viewBox="0 0 200 60" width="200" height="60">
              <path d="M5 30 Q 50 5, 100 30 T 195 30" stroke="rgba(255,182,193,0.8)" stroke-width="2.5" fill="none" stroke-dasharray="6 6" class="dash-anim" />
            </svg>
          </div>

          <div class="person-card" @click="burstHearts('right')">
            <div class="avatar-ring-wrap">
              <el-avatar class="couple-avatar" :size="104" :src="family.womanCover || defaultWoman" />
              <div class="avatar-halo pink"></div>
            </div>
            <div class="couple-name">{{ family.womanName || '她' }}</div>
          </div>
        </div>

        <!-- 爱心爆发粒子 -->
        <transition-group name="burst" tag="div" class="burst-layer">
          <span v-for="p in burstParticles" :key="p.id" class="burst-heart" :style="p.style">{{ p.char }}</span>
        </transition-group>

        <!-- 在一起天数 -->
        <div class="together-card">
          <div class="together-label">我们已经在一起</div>
          <div class="together-days">
            <span class="days-num">{{ togetherDays }}</span>
            <span class="days-unit">天</span>
          </div>
          <div class="together-detail">
            {{ timing.year > 0 ? timing.year + '年 ' : '' }}{{ timing.month % 12 }}个月 {{ timing.day % 30 }}天
            {{ String(timing.hour).padStart(2, '0') }}:{{ String(timing.minute).padStart(2, '0') }}:{{ String(timing.second).padStart(2, '0') }}
          </div>
          <div v-if="family.countdownTime" class="countdown-text">💝 {{ family.countdownTitle }} · {{ countdownText }}</div>
        </div>
      </div>

      <!-- 藤蔓装饰 -->
      <div class="vine vine-left">🌿</div>
      <div class="vine vine-right">🌿</div>
    </div>

    <!-- ===== Tab 导航 ===== -->
    <div class="tab-nav">
      <button :class="{ active: activeTab === 'painting' }" @click="activeTab = 'painting'"><span>🖼️</span> 世界名画</button>
      <button :class="{ active: activeTab === 'blessing' }" @click="activeTab = 'blessing'"><span>💌</span> 祝福板</button>
      <button :class="{ active: activeTab === 'diary' }" @click="activeTab = 'diary'"><span>📖</span> 点点滴滴</button>
    </div>

    <!-- ===== Tab: 世界名画 ===== -->
    <div v-show="activeTab === 'painting'" class="painting-card">
      <div class="painting-inner">
        <div class="painting-frame">
          <LuneImage :src="paintingUrl" class="painting-img" variant="hero" alt="世界名画" />
          <div class="painting-vignette" />
          <div class="painting-label">世界名画 · 我们的爱</div>
        </div>
      </div>
    </div>

    <!-- ===== Tab: 祝福板 ===== -->
    <div v-show="activeTab === 'blessing'" class="blessing-section">
      <div class="blessing-board">
        <div class="blessing-grid">
          <div v-for="(b, i) in blessingLoop" :key="i" class="blessing-note" :class="'note-' + (i % 5)">
            <div class="note-pin">📌</div>
            <div class="note-text">{{ b.content }}</div>
            <div class="note-footer">
              <span class="note-name">{{ b.username || anonNick(b, i) }}</span>
              <span class="note-time">{{ timeAgo(b.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="blessing-input-bar">
        <div class="blessing-input-wrap">
          <el-input v-model="blessingText" placeholder="💌 写下你的祝福..." size="large" class="blessing-input"
            @keyup.enter="submitBlessing" />
          <button class="blessing-send-btn" :disabled="!blessingText.trim()" @click="submitBlessing">
            <svg viewBox="0 0 24 24" width="20" height="20"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" fill="currentColor"/></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- ===== Tab: 烂皮书（点点滴滴）===== -->
    <div v-show="activeTab === 'diary'" class="diary-book">
      <div class="book-container">
        <div class="book-spine"></div>
        <!-- 左页 -->
        <div class="book-page book-left" :key="'l'+currentPage">
          <div class="page-texture"></div>
          <div class="page-content">
            <div class="page-record-time">{{ formatFullDate(leftPage?.recordTime) }}</div>
            <img v-if="firstDiaryImage(leftPage)" :src="firstDiaryImage(leftPage)" class="page-image" />
            <div class="page-text" v-html="leftPage?.content || emptyPageText"></div>
            <div class="page-number">{{ currentPage * 2 - 1 }}</div>
          </div>
        </div>
        <!-- 右页 -->
        <div class="book-page book-right" :key="'r'+currentPage">
          <div class="page-texture"></div>
          <div class="page-content">
            <div class="page-record-time">{{ formatFullDate(rightPage?.recordTime) }}</div>
            <img v-if="firstDiaryImage(rightPage)" :src="firstDiaryImage(rightPage)" class="page-image" />
            <div class="page-text" v-html="rightPage?.content || emptyPageText"></div>
            <div class="page-number">{{ currentPage * 2 }}</div>
          </div>
        </div>
      </div>
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
import LuneImage from '../../components/LuneImage.vue'
import SakuraFall from '../../components/SakuraFall.vue'

const familyHeroBg = usePageBackground('familyHero')
const familyContentBg = usePageBackground('familyContent')
const paintingUrl = familyContentBg

// 匿名祝福的展示昵称。必须是纯函数：模板里每次渲染都会调它，而在一起时长的
// 秒表每秒触发一次重渲染 —— 用 Math.random() 的话昵称会一秒换一次。
// 按祝福 id（无 id 则按下标）取模，同一条祝福永远是同一个昵称。
const ANON_NICKS = ['小星星','月亮船','阳光','微风','彩虹糖','云朵','海浪','樱花','蒲公英','小太阳']
function anonNick(b, i) { return ANON_NICKS[(Number(b?.id) || i) % ANON_NICKS.length] }

const userStore = useUserStore()
const defaultMan = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><rect fill="#7ba7d9" width="100" height="100"/><circle cx="50" cy="38" r="18" fill="#fff" opacity="0.9"/><ellipse cx="50" cy="85" rx="28" ry="18" fill="#fff" opacity="0.9"/></svg>')
const defaultWoman = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><rect fill="#f0a8c0" width="100" height="100"/><circle cx="50" cy="38" r="18" fill="#fff" opacity="0.9"/><path d="M28 65 Q50 55 72 65 L72 88 L28 88 Z" fill="#fff" opacity="0.9"/></svg>')

const family = ref({})
let timerInterval = null
const timing = reactive({ year:0, month:0, day:0, hour:0, minute:0, second:0 })
const countdownText = ref('')
const togetherDays = computed(() => {
  if (!family.value.timing) return 0
  return Math.max(0, Math.floor((Date.now() - new Date(family.value.timing).getTime()) / 86400000))
})

const activeTab = ref('blessing')
const emptyPageText = '<p style="text-align:center;color:#c8b8a8;margin-top:40px">这一页等待被书写…</p>'

// 漂浮爱心
const floatHearts = ref([])
// 星星
const stars = ref([])
// 爆发粒子
const burstParticles = ref([])
let burstId = 0

// 祝福
const blessingList = ref([])
const blessingText = ref('')
const blessingPosting = ref(false)
const blessingLoop = computed(() => blessingList.value.length > 0 ? blessingList.value : defaultBlessings)

// 日记
const diaryPages = ref([])
const currentPage = ref(1)
const totalPages = computed(() => Math.max(1, Math.ceil(diaryPages.value.length / 2)))
const leftPage = computed(() => diaryPages.value[(currentPage.value - 1) * 2] || null)
const rightPage = computed(() => diaryPages.value[(currentPage.value - 1) * 2 + 1] || null)

const defaultBlessings = [
  { id:1, username:'小星星', content:'祝你们永远幸福！💕', createTime: new Date().toISOString() },
  { id:2, username:'月亮', content:'岁月静好，与君偕老 🌙', createTime: new Date().toISOString() },
  { id:3, username:'阳光', content:'看到你们就觉得爱情真美好 ☀️', createTime: new Date().toISOString() },
  { id:4, username:'微风', content:'愿你们的爱情甜甜蜜蜜 🍬', createTime: new Date().toISOString() },
  { id:5, username:'彩虹', content:'百年好合，早生贵子 🌈', createTime: new Date().toISOString() },
  { id:6, username:'云朵', content:'执子之手，与子偕老 ☁️', createTime: new Date().toISOString() },
]

function initDecorations() {
  const isMobile = window.innerWidth < 768
  const heartChars = ['💕','💗','💖','💝','❤️']
  floatHearts.value = Array.from({ length: isMobile ? 5 : 10 }, (_, i) => ({
    id: i,
    char: heartChars[i % heartChars.length],
    style: {
      left: Math.random() * 100 + '%',
      fontSize: (14 + Math.random() * 16) + 'px',
      animationDuration: (8 + Math.random() * 8) + 's',
      animationDelay: (-Math.random() * 10) + 's',
      opacity: (0.4 + Math.random() * 0.4).toFixed(2)
    }
  }))
  stars.value = Array.from({ length: isMobile ? 9 : 18 }, (_, i) => ({
    id: i,
    style: {
      left: Math.random() * 100 + '%',
      top: Math.random() * 70 + '%',
      fontSize: (8 + Math.random() * 12) + 'px',
      animationDelay: (Math.random() * 4) + 's',
      animationDuration: (1.5 + Math.random() * 2) + 's'
    }
  }))
}

function burstHearts(from) {
  const chars = ['❤️','💕','💗','💖','💓','💞']
  const rect = document.querySelector('.love-hero-content')?.getBoundingClientRect()
  const cx = rect ? rect.width / 2 : 200
  for (let i = 0; i < 14; i++) {
    const angle = (Math.PI * 2 * i) / 14 + Math.random() * 0.5
    const dist = 60 + Math.random() * 90
    const id = burstId++
    burstParticles.value.push({
      id,
      char: chars[Math.floor(Math.random() * chars.length)],
      style: {
        left: cx + 'px',
        top: '90px',
        '--tx': Math.cos(angle) * dist + 'px',
        '--ty': Math.sin(angle) * dist + 'px',
        fontSize: (14 + Math.random() * 14) + 'px'
      }
    })
    setTimeout(() => {
      burstParticles.value = burstParticles.value.filter(p => p.id !== id)
    }, 1400)
  }
}

onMounted(async () => {
  initDecorations()
  await fetchFamily()
  fetchBlessings()
  fetchDiaries()
})
onUnmounted(() => { if (timerInterval) clearInterval(timerInterval) })

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
    ElMessage.success('祝福已送达 💕')
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
function formatFullDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', { year:'numeric', month:'long', day:'numeric' })
}
</script>

<style scoped>
/* ===== 整体 ===== */
.family-page { position: relative; min-height: 100vh; background: linear-gradient(180deg, #fff5f7 0%, #ffeef2 50%, #fff8f0 100%); overflow-x: hidden; }

/* 漂浮爱心 */
.love-float-layer { position: fixed; inset: 0; pointer-events: none; z-index: 1; overflow: hidden; }
.float-heart { position: absolute; top: -6%; animation: floatUp linear infinite; will-change: transform; }
@keyframes floatUp {
  0% { transform: translateY(0) translateX(0) rotate(0deg); opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { transform: translateY(-110vh) translateX(30px) rotate(25deg); opacity: 0; }
}

/* ===== Hero ===== */
.love-hero { position: relative; min-height: 64vh; display: flex; align-items: center; justify-content: center; overflow: hidden; background: linear-gradient(160deg, #ffd6e0 0%, #ffc2d4 30%, #e0aaff 70%, #c8b6ff 100%); }
.love-hero .bg-image { position: absolute; inset: 0; background-size: cover; background-position: center; opacity: 0.35; z-index: 0; }
.love-hero-overlay { position: absolute; inset: 0; background: radial-gradient(ellipse at center, rgba(255,255,255,0.1), rgba(255,182,213,0.25)); z-index: 1; }

.twinkle-layer { position: absolute; inset: 0; z-index: 1; pointer-events: none; }
.twinkle-star { position: absolute; color: #fff; animation: twinkle ease-in-out infinite; text-shadow: 0 0 6px #fff, 0 0 12px #ffd6e8; }
@keyframes twinkle { 0%, 100% { opacity: 0.2; transform: scale(0.8); } 50% { opacity: 1; transform: scale(1.3); } }

.love-hero-content { position: relative; z-index: 3; display: flex; flex-direction: column; align-items: center; gap: 34px; padding: 90px 20px 70px; }

/* 情侣区 */
.couple-area { display: flex; align-items: center; gap: 4px; }
.person-card { display: flex; flex-direction: column; align-items: center; gap: 14px; cursor: pointer; transition: transform 0.3s; }
.person-card:hover { transform: scale(1.05); }
.person-card:active { transform: scale(0.95); }
.avatar-ring-wrap { position: relative; }
.couple-avatar { border-radius: 50% !important; border: 4px solid rgba(255,255,255,0.9) !important; box-shadow: 0 8px 32px rgba(255,107,157,0.4); position: relative; z-index: 2; }
.avatar-halo { position: absolute; inset: -10px; border-radius: 50%; border: 3px dashed rgba(255,255,255,0.6); animation: spinSlow 12s linear infinite; }
.avatar-halo.pink { border-color: rgba(255,182,193,0.8); animation-direction: reverse; }
@keyframes spinSlow { to { transform: rotate(360deg); } }
.couple-name { color: #fff; font-size: 19px; font-weight: 600; letter-spacing: 2px; font-family: var(--calligraphy-font); text-shadow: 0 2px 10px rgba(255,107,157,0.6); }

/* 爱心连接 */
.love-connector { position: relative; width: 130px; display: flex; align-items: center; justify-content: center; }
.love-line { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 130px; opacity: 0.7; }
.dash-anim { animation: dashMove 2.5s linear infinite; }
@keyframes dashMove { to { stroke-dashoffset: -24; } }
.heartbeat-wrap { position: relative; cursor: pointer; z-index: 3; }
.big-heart { font-size: 46px; display: block; animation: heartbeat 1.4s ease-in-out infinite; filter: drop-shadow(0 4px 12px rgba(255,20,100,0.5)); }
@keyframes heartbeat { 0%, 100% { transform: scale(1); } 25% { transform: scale(1.22); } 45% { transform: scale(1); } 60% { transform: scale(1.15); } }
.heart-pulse { position: absolute; inset: -14px; border-radius: 50%; border: 3px solid rgba(255,107,157,0.5); animation: pulseRing 1.4s ease-out infinite; }
@keyframes pulseRing { 0% { transform: scale(0.7); opacity: 1; } 100% { transform: scale(1.6); opacity: 0; } }

/* 爱心爆发 */
.burst-layer { position: absolute; inset: 0; pointer-events: none; z-index: 10; }
.burst-heart { position: absolute; animation: burst 1.4s ease-out forwards; will-change: transform; }
@keyframes burst {
  0% { transform: translate(0, 0) scale(0.4); opacity: 1; }
  100% { transform: translate(var(--tx), var(--ty)) scale(1.2); opacity: 0; }
}

/* 在一起天数卡片 */
.together-card { background: rgba(255,255,255,0.28); backdrop-filter: blur(16px); border-radius: 32px; padding: 26px 48px; text-align: center; border: 1.5px solid rgba(255,255,255,0.5); box-shadow: 0 12px 40px rgba(255,107,157,0.25); }
.together-label { color: #fff; font-size: 16px; letter-spacing: 3px; font-family: var(--calligraphy-font); text-shadow: 0 1px 6px rgba(255,107,157,0.5); }
.together-days { display: flex; align-items: baseline; justify-content: center; gap: 8px; margin: 8px 0; }
.days-num { font-size: 64px; font-weight: 700; color: #fff; font-family: var(--trendy-font); text-shadow: 0 4px 20px rgba(255,20,100,0.5); line-height: 1; }
.days-unit { font-size: 24px; color: #ffe0ea; font-family: var(--calligraphy-font); }
.together-detail { color: #fff; font-size: 15px; letter-spacing: 1px; opacity: 0.95; font-family: var(--trendy-font); }
.countdown-text { margin-top: 10px; color: #fff; font-size: 14px; background: rgba(255,107,157,0.3); border-radius: 16px; padding: 6px 16px; display: inline-block; }

/* 藤蔓 */
.vine { position: absolute; font-size: 60px; opacity: 0.5; z-index: 2; animation: vineSway 6s ease-in-out infinite; }
.vine-left { left: -10px; bottom: 20px; transform: rotate(45deg); }
.vine-right { right: -10px; top: 60px; transform: rotate(-135deg); animation-delay: 3s; }
@keyframes vineSway { 0%, 100% { transform: rotate(45deg) translateY(0); } 50% { transform: rotate(50deg) translateY(-8px); } }
.vine-right { animation-name: vineSwayR; }
@keyframes vineSwayR { 0%, 100% { transform: rotate(-135deg) translateY(0); } 50% { transform: rotate(-130deg) translateY(-8px); } }

/* ===== Tab 导航 ===== */
.tab-nav { display: flex; justify-content: center; gap: 14px; padding: 18px 12px; position: sticky; top: 56px; z-index: 20; }
.tab-nav button { display: inline-flex; align-items: center; gap: 7px; padding: 11px 26px; border: none; background: rgba(255,255,255,0.75); backdrop-filter: blur(14px); color: #d6336c; border-radius: 30px; font-size: 16px; cursor: pointer; transition: all 0.35s cubic-bezier(0.34,1.56,0.64,1); font-family: var(--calligraphy-font); letter-spacing: 1px; box-shadow: 0 4px 16px rgba(255,107,157,0.15); }
.tab-nav button span { font-size: 18px; }
.tab-nav button:hover { transform: translateY(-3px) scale(1.04); box-shadow: 0 8px 24px rgba(255,107,157,0.3); }
.tab-nav button.active { background: linear-gradient(135deg, #ff6b9d, #ff8e9e); color: #fff; box-shadow: 0 8px 24px rgba(255,107,157,0.45); }

/* ===== 世界名画 ===== */
.painting-card { display: flex; justify-content: center; padding: 24px 18px 30px; min-height: 62vh; }
.painting-inner { max-width: 820px; width: 100%; }
.painting-frame { position: relative; border-radius: 20px; overflow: hidden; box-shadow: 0 6px 16px rgba(0,0,0,0.12), 0 0 0 10px #fff0f3, 0 0 0 13px #ffb6c9, 0 0 0 24px #fff0f3; }
.painting-img { width: 100%; min-height: 56vh; max-height: 66vh; object-fit: cover; display: block; }
.painting-fallback { width: 100%; min-height: 56vh; background: linear-gradient(135deg, #ffe0ec, #ffc2d4, #e0aaff, #ffd6e0); background-size: 400% 400%; animation: gradientBG 8s ease infinite; }
.painting-vignette { position: absolute; inset: 0; box-shadow: inset 0 0 80px rgba(214,51,108,0.25); pointer-events: none; }
.painting-label { position: absolute; bottom: 18px; left: 50%; transform: translateX(-50%); color: #fff; font-size: 22px; letter-spacing: 4px; font-family: var(--calligraphy-font); text-shadow: 0 2px 10px rgba(214,51,108,0.7); background: rgba(214,51,108,0.35); padding: 6px 24px; border-radius: 20px; backdrop-filter: blur(6px); }

/* ===== 祝福板 ===== */
.blessing-section { padding: 20px 18px 26px; max-width: 900px; margin: 0 auto; min-height: 60vh; }
.blessing-board { background: linear-gradient(135deg, #fff8e7, #ffeef5); border-radius: 28px; padding: 30px; box-shadow: inset 0 2px 20px rgba(214,51,108,0.08), 0 8px 32px rgba(255,107,157,0.12); border: 3px solid #ffd6e0; margin-bottom: 20px; }
.blessing-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px; }
.blessing-note { position: relative; background: #fffbe7; border-radius: 6px 20px 6px 20px; padding: 20px 18px 14px; box-shadow: 0 4px 14px rgba(0,0,0,0.1); transition: all 0.35s cubic-bezier(0.34,1.56,0.64,1); }
.blessing-note:hover { transform: translateY(-6px) rotate(-2deg) scale(1.03); box-shadow: 0 10px 28px rgba(255,107,157,0.25); z-index: 2; }
.note-0 { background: #fff9e6; }
.note-1 { background: #ffeef5; }
.note-2 { background: #e8f7ff; }
.note-3 { background: #f0ffe9; }
.note-4 { background: #f3eaff; }
.note-pin { position: absolute; top: -10px; left: 50%; transform: translateX(-50%); font-size: 18px; filter: drop-shadow(0 2px 3px rgba(0,0,0,0.2)); }
.note-text { font-size: 15px; color: #4a3a3a; line-height: 1.7; font-family: var(--handwriting-font); min-height: 44px; }
.note-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; padding-top: 8px; border-top: 1px dashed rgba(0,0,0,0.1); }
.note-name { font-size: 13px; font-weight: 700; color: #d6336c; font-family: var(--calligraphy-font); }
.note-time { font-size: 11px; color: #c0a0a0; }

.blessing-input-bar { padding: 4px 0; }
.blessing-input-wrap { display: flex; gap: 10px; align-items: center; background: #fff; border-radius: 32px; padding: 7px 7px 7px 20px; box-shadow: 0 4px 20px rgba(255,107,157,0.18); border: 2px solid transparent; transition: all 0.3s; }
.blessing-input-wrap:focus-within { border-color: #ff6b9d; box-shadow: 0 6px 28px rgba(255,107,157,0.3); }
.blessing-input :deep(.el-input__wrapper) { border-radius: 26px; background: transparent; box-shadow: none !important; padding: 0; }
.blessing-send-btn { width: 48px; height: 48px; border-radius: 50%; border: none; background: linear-gradient(135deg, #ff6b9d, #ff8e53); color: #fff; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.3s; flex-shrink: 0; box-shadow: 0 4px 14px rgba(255,107,157,0.4); }
.blessing-send-btn:hover:not(:disabled) { transform: scale(1.12) rotate(8deg); box-shadow: 0 8px 26px rgba(255,107,157,0.6); }
.blessing-send-btn:disabled { opacity: 0.4; cursor: default; }

/* ===== 烂皮书 ===== */
.diary-book { display: flex; flex-direction: column; align-items: center; padding: 26px 18px 30px; min-height: 62vh; }
.book-container { position: relative; display: flex; max-width: 880px; width: 100%; perspective: 1600px; border-radius: 14px; box-shadow: 0 20px 60px rgba(120,70,40,0.35), 0 4px 16px rgba(120,70,40,0.2); min-height: 58vh; background: linear-gradient(90deg, #d4b896 0%, #e8d5b5 3%, #f5ecd8 6%, #f9f2e0 50%, #f5ecd8 94%, #e8d5b5 97%, #d4b896 100%); }
.book-spine { position: absolute; left: 50%; top: 0; bottom: 0; width: 8px; transform: translateX(-50%); background: linear-gradient(90deg, rgba(120,80,40,0.4), rgba(120,80,40,0.1), rgba(120,80,40,0.4)); z-index: 3; box-shadow: 0 0 12px rgba(120,80,40,0.3); }
.book-page { flex: 1; padding: 28px 30px; position: relative; overflow: hidden; }
.book-left { border-radius: 14px 0 0 14px; transform-origin: right center; }
.book-right { border-radius: 0 14px 14px 0; transform-origin: left center; }
.page-texture { position: absolute; inset: 0; pointer-events: none; opacity: 0.5; background:
  radial-gradient(ellipse at 20% 30%, rgba(180,150,110,0.12), transparent 50%),
  radial-gradient(ellipse at 80% 70%, rgba(180,150,110,0.1), transparent 50%),
  radial-gradient(ellipse at 50% 90%, rgba(160,130,90,0.08), transparent 40%),
  repeating-linear-gradient(0deg, transparent, transparent 34px, rgba(150,120,80,0.06) 34px, rgba(150,120,80,0.06) 35px);
}
.book-left .page-texture { box-shadow: inset -18px 0 24px -14px rgba(120,80,40,0.35); }
.book-right .page-texture { box-shadow: inset 18px 0 24px -14px rgba(120,80,40,0.35); }
.page-content { position: relative; height: 100%; z-index: 2; }
.page-record-time { font-size: 15px; color: #a08050; margin-bottom: 14px; border-bottom: 2px dashed #d4b896; padding-bottom: 8px; font-family: var(--handwriting-font); letter-spacing: 1px; }
.page-image { width: 100%; max-height: 210px; object-fit: cover; border-radius: 6px; margin-bottom: 18px; box-shadow: 0 3px 12px rgba(120,80,40,0.25); border: 3px solid #fff; }
.page-text { font-size: 17px; line-height: 35px; color: #5a4530; font-family: var(--handwriting-font); }
.page-text :deep(p) { margin: 0 0 18px 0; text-indent: 2em; }
.page-number { position: absolute; bottom: 0; right: 0; font-size: 15px; color: #b8986a; font-family: var(--handwriting-font); font-style: italic; }
.book-nav { display: flex; align-items: center; gap: 24px; margin-top: 28px; }
.book-nav button { padding: 10px 24px; border: none; background: linear-gradient(135deg, #ffb6c9, #ffc2d4); border-radius: 24px; cursor: pointer; font-family: var(--calligraphy-font); font-size: 16px; color: #8a2a4a; transition: all 0.3s; box-shadow: 0 4px 14px rgba(255,107,157,0.25); letter-spacing: 1px; }
.book-nav button:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 8px 22px rgba(255,107,157,0.4); }
.book-nav button:disabled { opacity: 0.4; cursor: default; }
.book-page-indicator { font-size: 16px; color: #a08050; font-family: var(--calligraphy-font); letter-spacing: 1px; }

/* ===== 响应式 ===== */
@media screen and (max-width: 768px) {
  .love-hero { min-height: 52vh; }
  .couple-avatar { width: 76px !important; height: 76px !important; }
  .love-connector { width: 60px; }
  .love-line { width: 60px; }
  .big-heart { font-size: 32px; }
  .days-num { font-size: 40px; }
  .together-card { padding: 18px 22px; border-radius: 16px; }
  .vine { font-size: 36px; }
  .book-container { flex-direction: column; }
  .book-spine { left: 0; right: 0; top: 50%; bottom: auto; width: auto; height: 8px; transform: translateY(-50%); }
  .tab-nav { top: 52px; gap: 6px; }
  .tab-nav button { padding: 8px 14px; font-size: 13px; }
  .book-nav button { padding: 8px 18px; font-size: 14px; }
  .book-page-indicator { font-size: 14px; }
}
</style>

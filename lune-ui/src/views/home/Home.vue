<template>
  <div>
    <!-- Hero background image -->
    <el-image
      style="animation: header-effect 2s"
      class="background-image-index"
      v-once
      lazy
      :src="bgImage"
      fit="cover"
    >
      <div slot="error" class="image-slot background-image-index-error"></div>
    </el-image>

    <!-- PixelSnow full-page overlay -->
    <PixelSnow
      color="#ffffff"
      :flakeSize="0.023"
      :minFlakeSize="1.25"
      :pixelResolution="500"
      :speed="2.3"
      :density="0.45"
      :direction="180"
      :brightness="3.0"
      :depthFade="14.5"
      :farPlane="15"
      variant="snowflake"
      class-name="full-page-snow"
    />

    <!-- Hero text overlay -->
    <div class="signature-wall myCenter my-animation-hideToShow">
      <h1 class="playful">
        <span v-for="(a, index) in titleChars" :key="index">{{ a }}</span>
      </h1>
      <div class="printer" @click="getRandomPoem">
        <h3>{{ printerText }}<span class="cursor">|</span></h3>
      </div>
      <div id="bannerWave1"></div>
      <div id="bannerWave2"></div>
      <div class="scroll-arrow myCenter" @click="scrollToContent">
        <svg viewBox="0 0 1024 1024" width="40" height="40">
          <path d="M512 714.666667c-8.533333 0-17.066667-2.133333-23.466667-8.533334l-256-256c-12.8-12.8-12.8-32 0-44.8 12.8-12.8 32-12.8 44.8 0l234.666667 234.666667 234.666667-234.666667c12.8-12.8 32-12.8 44.8 0 12.8 12.8 12.8 32 0 44.8l-256 256c-6.4 6.4-14.933333 8.533333-23.466667 8.533334z" fill="#ffffff"></path>
        </svg>
      </div>
    </div>

    <!-- Content area -->
    <div class="page-container-wrap">
      <!-- Blurred background layer -->
      <div class="bg-blur-layer" v-if="contentBgImage">
        <img :src="contentBgImage" alt="" class="bg-blur-img" />
      </div>

      <div class="page-container">
        <!-- Left sidebar -->
        <div class="aside-content" v-if="showAside">
          <div class="myAside-container">
            <!-- Info card -->
            <div class="card-content1 glass-card shadow-box">
              <el-avatar class="user-avatar" :size="120" :src="appStore.ownerInfo.avatar || appStore.webInfo.avatar || '/assets/头像1.jpg'">
                {{ (appStore.ownerInfo.nickname || appStore.webInfo.webName || 'L').charAt(0) }}
              </el-avatar>
              <div class="web-name">{{ appStore.ownerInfo.nickname || appStore.webInfo.webName || 'Lune' }}</div>
              <div class="web-bio"><span class="motto-text">时刻保持思考！</span></div>
              <div class="web-info">
                <div class="blog-info-box">
                  <span>文章</span>
                  <span class="blog-info-num">{{ total }}</span>
                </div>
                <div class="blog-info-box">
                  <span>分类</span>
                  <span class="blog-info-num">{{ categories.length }}</span>
                </div>
                <div class="blog-info-box">
                  <span>访问量</span>
                  <span class="blog-info-num">{{ appStore.webInfo.historyAllCount || 0 }}</span>
                </div>
              </div>
              <a class="collection-btn" @click="$router.push('/treehole')">
                <i class="el-icon-star-off" style="margin-right: 2px"></i>朋友圈
              </a>
            </div>

            <!-- BGM Player -->
            <div class="music-card glass-card shadow-box">
              <div class="music-title">🎵 来听首小曲</div>
              <div class="music-player">
                <div class="music-lyrics" v-if="currentLyric">
                  <div class="lyric-line" :class="{ active: currentLyric === lyrics[lyricIdx] }">{{ lyrics[lyricIdx] || '' }}</div>
                  <div class="lyric-line">{{ lyrics[lyricIdx + 1] || '' }}</div>
                </div>
                <div class="music-song-name">{{ currentSong.name }}</div>
                <div class="music-controls">
                  <button class="music-play-btn" @click="toggleMusic">
                    <svg v-if="!musicPlaying" viewBox="0 0 24 24" width="18" height="18"><path d="M8 5v14l11-7z" fill="currentColor"/></svg>
                    <svg v-else viewBox="0 0 24 24" width="18" height="18"><path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z" fill="currentColor"/></svg>
                  </button>
                  <div class="music-progress" @click="seekMusic">
                    <div class="music-progress-fill" :style="{ width: musicProgress + '%' }" />
                  </div>
                </div>
                <div class="music-song-artist">{{ currentSong.artist }}</div>
              </div>
            </div>

            <!-- Recommended articles -->
            <div v-if="recommendArticles.length > 0" class="recommend-card glass-card shadow-box">
              <div class="card-content2-title"><span>🔥 推荐文章</span></div>
              <div v-for="(article, index) in recommendArticles" :key="'rec' + index" @click="readerArticleId = article.id">
                <div class="aside-post-detail">
                  <div class="aside-post-image">
                    <el-image lazy class="my-el-image" :src="article.cover || '/assets/背景1.jpg'" fit="cover">
                      <div slot="error" class="image-slot"><div class="error-aside-image">{{ article.title }}</div></div>
                    </el-image>
                  </div>
                  <div class="aside-post-title">{{ article.title }}</div>
                </div>
                <div class="aside-post-date"><i class="el-icon-date" style="color: var(--greyFont)"></i>{{ formatDate(article.createTime) }}</div>
              </div>
            </div>

            <!-- Category quick-browse -->
            <div class="selectSort">
              <div
                v-for="(cat, index) in categories.slice(0, 6)"
                @click="selectSort(cat)"
                :key="'cat' + index"
                :style="{ background: sortColors[index % sortColors.length] }"
                class="sort-card shadow-box-mini"
              >
                <div>速览</div>
                <div class="sort-name">{{ cat.name }}</div>
                <div style="font-weight: bold; margin-top: 15px; white-space: nowrap; text-overflow: ellipsis; overflow: hidden">
                  {{ cat.description || '点击浏览' + cat.name + '相关文章' }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Main article area -->
        <div class="recent-posts">
          <!-- Notice bar -->
          <div class="announcement glass-card" v-if="notices.length > 0">
            <span style="color: var(--themeBackground); font-size: 22px; margin: auto 0; animation: scale 0.8s ease-in-out infinite;">📢</span>
            <div>
              <div v-for="(notice, idx) in notices" :key="'notice' + idx">{{ notice }}</div>
            </div>
          </div>

          <!-- Default: articles grouped by category -->
          <div v-show="indexType === 1">
            <div v-for="(cat, idx) in categories" :key="'group' + idx">
              <div v-if="groupedArticles[cat.id] && groupedArticles[cat.id].length > 0">
                <div class="sort-article-first">
                  <div>
                    <svg viewBox="0 0 1024 1024" width="20" height="20" style="vertical-align: -2px; margin-bottom: -2px">
                      <path d="M367.36 482.304H195.9936c-63.3344 0-114.6368-51.3536-114.6368-114.6368V196.2496c0-63.3344 51.3536-114.6368 114.6368-114.6368h171.4176c63.3344 0 114.6368 51.3536 114.6368 114.6368V367.616c0 63.3344-51.3536 114.688-114.688 114.688zM367.36 938.752H195.9936c-63.3344 0-114.6368-51.3536-114.6368-114.6368v-171.4176c0-63.3344 51.3536-114.6368 114.6368-114.6368h171.4176c63.3344 0 114.6368 51.3536 114.6368 114.6368v171.4176c0 63.3344-51.3536 114.6368-114.688 114.6368zM828.672 938.752h-171.4176c-63.3344 0-114.6368-51.3536-114.6368-114.6368v-171.4176c0-63.3344 51.3536-114.6368 114.6368-114.6368h171.4176c63.3344 0 114.6368 51.3536 114.6368 114.6368v171.4176c0 63.3344-51.3024 114.6368-114.6368 114.6368zM828.672 482.304h-171.4176c-63.3344 0-114.6368-51.3536-114.6368-114.6368V196.2496c0-63.3344 51.3536-114.6368 114.6368-114.6368h171.4176c63.3344 0 114.6368 51.3536 114.6368 114.6368V367.616c0 63.3344-51.3024 114.688-114.6368 114.688z" fill="#FF623E"></path>
                    </svg>
                    {{ cat.name }}
                  </div>
                </div>
                <!-- Grid article cards -->
                <div class="article-grid">
                  <div
                    v-for="article in groupedArticles[cat.id]"
                    :key="article.id"
                    class="article-card shadow-box"
                    @click="readerArticleId = article.id"
                  >
                    <div class="article-cover-wrap">
                      <el-image lazy class="article-cover-img" :src="article.cover || '/assets/背景1.jpg'" fit="cover">
                        <div slot="error" class="image-slot article-cover-error">{{ article.title }}</div>
                      </el-image>
                    </div>
                    <div class="article-body">
                      <h3 class="article-title">{{ article.title }}</h3>
                      <p class="article-summary">{{ article.summary || (article.content || '').substring(0, 120) }}</p>
                      <div class="article-meta">
                        <span>📅 {{ formatDate(article.createTime) }}</span>
                        <span>👁 {{ article.viewCount || 0 }}</span>
                        <span>❤️ {{ article.likeCount || 0 }}</span>
                        <span>💬 {{ article._cc || 0 }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Filtered: flat grid -->
          <div v-show="indexType === 2">
            <div v-if="filteredArticles.length === 0" style="text-align: center; padding: 80px 0;">
              <el-empty description="暂无文章" />
            </div>
            <div class="article-grid">
              <div
                v-for="article in filteredArticles"
                :key="article.id"
                class="article-card shadow-box"
                @click="readerArticleId = article.id"
              >
                <div class="article-cover-wrap">
                  <el-image lazy class="article-cover-img" :src="article.cover || '/assets/背景1.jpg'" fit="cover">
                    <div slot="error" class="image-slot article-cover-error">{{ article.title }}</div>
                  </el-image>
                </div>
                <div class="article-body">
                  <h3 class="article-title">{{ article.title }}</h3>
                  <p class="article-summary">{{ article.summary || (article.content || '').substring(0, 120) }}</p>
                  <div class="article-meta">
                    <span>📅 {{ formatDate(article.createTime) }}</span>
                    <span>👁 {{ article.viewCount || 0 }}</span>
                    <span>❤️ {{ article.likeCount || 0 }}</span>
                    <span>💬 {{ article._cc || 0 }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="pagination-wrap">
              <div @click="pageArticles()" class="pagination" v-if="pagination.total !== filteredArticles.length">下一页</div>
              <div v-else style="user-select: none; color: var(--greyFont)">~~到底啦~~</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Article reader overlay -->
    <ArticleReader
      v-if="readerArticleId"
      :key="readerArticleId"
      :articleId="readerArticleId"
      @close="readerArticleId = null"
      @commented="onArticleCommented"
      @liked="onArticleLiked"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '../../stores/app'
import { useUserStore } from '../../stores/user'
import { articleApi, categoryApi } from '../../api/modules'
import { usePageBackground } from '../../composables/usePageBackground'
import PixelSnow from '../../components/PixelSnow/PixelSnow.vue'
import ArticleReader from '../../components/ArticleReader.vue'

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const titleChars = ref([])
const printerText = ref('')
const fullPrinterText = ref('你看对面的青山多漂亮')

const bgImage = usePageBackground('homeHero')
const contentBgImage = usePageBackground('homeContent')

const notices = computed(() => safeJsonParse(appStore.webInfo.notices, []))

const indexType = ref(1)
const showAside = ref(true)
const readerArticleId = ref(null)
const sortColors = ['#FF623E', '#51C492', '#F9DB88', '#5362f6', '#e485f8', '#ff9c55']

const articles = ref([])
const allArticles = ref([])
const filteredArticles = ref([])
const categories = ref([])
const recommendArticles = ref([])

const pagination = reactive({
  current: 1, size: 10, total: 0, sortId: null
})
const total = ref(0)

const groupedArticles = computed(() => {
  const groups = {}
  for (const article of allArticles.value) {
    const catId = article.categoryId
    if (catId) {
      if (!groups[catId]) groups[catId] = []
      groups[catId].push(article)
    }
  }
  return groups
})

// Music player with Web Audio melody generator
const musicPlaying = ref(false)
const musicProgress = ref(0)
const currentSong = ref(null)
const currentLyric = ref('')
const lyricIdx = ref(0)
let audioCtx = null
let progressTimer = null

// Pentatonic scale melodies (C D E G A)
const pentatonic = [262, 294, 330, 392, 440, 523, 587, 659, 784, 880]
const songList = [
  { name:'晴天', artist:'周杰伦 · 纯音版', notes:[0,2,3,5,7,8,7,5,3,2,0,1,2,3,4,5,3,2,0,2,3,5,7], lyrics:['故事的小黄花 从出生那年就飘着','童年的荡秋千 随记忆一直晃到现在','Re So So Si Do Si La','So La Si Si Si Si La Si La So','吹着前奏 望着天空','我想起花瓣试着掉落','为你翘课的那一天 花落的那一天'] },
  { name:'起风了', artist:'买辣椒也用券 · 纯音版', notes:[5,3,2,0,1,2,3,5,6,7,8,7,5,3,2,0,2,3,5,3,2,0,1,2], lyrics:['这一路上走走停停 顺着少年漂流的痕迹','迈出车站的前一刻 竟有些犹豫','不禁笑这近乡情怯 仍无可避免','而长野的天 依旧那么暖 风吹起了从前','从前初识这世间 万般流连','看着天边似在眼前','也甘愿赴汤蹈火去走它一遍'] }
]
currentSong.value = songList[0]
const lyrics = computed(() => currentSong.value?.lyrics || [])

function initAudio() {
  if (!audioCtx) audioCtx = new (window.AudioContext || window.webkitAudioContext)()
}

function toggleMusic() {
  if (musicPlaying.value) { stopMusic() }
  else { playMusic() }
}

function playMusic() {
  initAudio()
  stopMusic()
  if (audioCtx.state === 'suspended') audioCtx.resume()
  const song = currentSong.value
  const notes = song.notes
  const noteLen = 0.5 // seconds per note
  const totalDur = notes.length * noteLen

  const now = audioCtx.currentTime
  notes.forEach((ni, i) => {
    const osc = audioCtx.createOscillator()
    const g = audioCtx.createGain()
    osc.type = 'triangle'
    osc.frequency.value = pentatonic[ni % pentatonic.length]
    g.gain.setValueAtTime(0, now + i * noteLen)
    g.gain.linearRampToValueAtTime(0.07, now + i * noteLen + 0.02)
    g.gain.setValueAtTime(0.07, now + (i + 1) * noteLen - 0.05)
    g.gain.linearRampToValueAtTime(0, now + (i + 1) * noteLen)
    osc.connect(g); g.connect(audioCtx.destination)
    osc.start(now + i * noteLen); osc.stop(now + (i + 1) * noteLen)
  })

  musicPlaying.value = true
  musicProgress.value = 0
  lyricIdx.value = 0
  currentLyric.value = lyrics.value[0] || ''

  clearInterval(progressTimer)
  progressTimer = setInterval(() => {
    if (!audioCtx || !musicPlaying.value) return
    const elapsed = audioCtx.currentTime - now
    const pct = Math.min(elapsed / totalDur, 1)
    musicProgress.value = pct * 100
    const newIdx = Math.min(Math.floor(pct * lyrics.value.length), lyrics.value.length - 1)
    if (newIdx !== lyricIdx.value) { lyricIdx.value = newIdx; currentLyric.value = lyrics.value[newIdx] || '' }
    if (pct >= 1) {
      const idx = (songList.indexOf(currentSong.value) + 1) % songList.length
      currentSong.value = songList[idx]; stopMusic(); playMusic()
    }
  }, 250)
}

function stopMusic() {
  clearInterval(progressTimer)
  musicPlaying.value = false
  // AudioContext oscillators auto-stop via scheduled stop()
}

function seekMusic(e) {
  stopMusic()
  playMusic()
}

// Add article for admin
const showAddArticle = computed(() => userStore.isAdmin)

function onArticleLiked({ articleId, likeCount }) {
  const update = (arr) => arr.forEach(a => { if (a.id === articleId) a.likeCount = likeCount })
  update(allArticles.value)
  update(filteredArticles.value)
  recommendArticles.value.forEach(a => { if (a.id === articleId) a.likeCount = likeCount })
}
function onArticleCommented() {
  const aid = readerArticleId.value
  const upd = (arr) => arr.forEach(a => { if (a.id === aid) a._cc = (a._cc || 0) + 1 })
  upd(allArticles.value)
  upd(filteredArticles.value)
}

function safeJsonParse(str, fallback) {
  if (!str) return fallback
  try { return JSON.parse(str) } catch (e) { return fallback }
}
function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN')
}
function startTypewriter() {
  const text = fullPrinterText.value
  let i = 0, forward = true, pauseCount = 0
  const PAUSE_FRAMES = 30
  setInterval(() => {
    if (forward) {
      printerText.value = text.slice(0, i + 1); i++
      if (i > text.length) { forward = false; pauseCount = 0 }
    } else if (pauseCount < PAUSE_FRAMES) { pauseCount++ }
    else { printerText.value = text.slice(0, i); i--; if (i <= 0) forward = true }
  }, 120)
}

const poemPhrases = [
  '你看对面的青山多漂亮', '人生若只如初见，何事秋风悲画扇',
  '愿我如星君如月，夜夜流光相皎洁', '云想衣裳花想容，春风拂槛露华浓',
  '山有木兮木有枝，心悦君兮君不知', '世间安得双全法，不负如来不负卿',
  '春风得意马蹄疾，一日看尽长安花', '众里寻他千百度，蓦然回首，那人却在灯火阑珊处'
]
function getRandomPoem() {
  fullPrinterText.value = poemPhrases[Math.floor(Math.random() * poemPhrases.length)]
}

async function selectSort(cat) {
  pagination.sortId = cat.id; pagination.current = 1; pagination.size = 10
  filteredArticles.value = []
  await getArticles()
  indexType.value = 1
  nextTick(() => {
    document.querySelector('.recent-posts')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}
async function pageArticles() { pagination.current++; await getArticles() }
async function getArticles() {
  try {
    const params = { page: pagination.current, size: pagination.size }
    if (pagination.sortId) params.categoryId = pagination.sortId
    const data = await articleApi.list(params)
    if (data && data.records) {
      filteredArticles.value = [...filteredArticles.value, ...data.records]
      pagination.total = data.total
    }
  } catch (e) { console.error('Failed to fetch articles:', e) }
}
async function fetchAllArticles() {
  try {
    const data = await articleApi.list({ page: 1, size: 500 })
    if (data && data.records) { allArticles.value = data.records; total.value = data.total || data.records.length }
  } catch (e) { console.error('Failed to fetch all articles:', e) }
}
async function fetchRecommendArticles() {
  try {
    const data = await articleApi.list({ page: 1, size: 5 })
    if (data && data.records) recommendArticles.value = data.records
  } catch (e) { console.error('Failed to fetch recommended articles:', e) }
}
async function fetchArticleComments() {
  try {
    const { commentApi } = await import('../../api/modules')
    const data = await commentApi.list({ page: 1, size: 1000 })
    const records = data?.records || data || []
    const counts = {}
    records.forEach(c => { const aid = c.articleId; if (aid && aid > 0) counts[aid] = (counts[aid] || 0) + 1 })
    allArticles.value.forEach(a => { a._cc = counts[a.id] || 0 })
    filteredArticles.value.forEach(a => { a._cc = counts[a.id] || 0 })
  } catch (e) { /* silent */ }
}
async function fetchCategories() {
  try { categories.value = await categoryApi.list('article') }
  catch (e) { console.error('Failed to fetch categories:', e) }
}
function scrollToContent() {
  const target = document.querySelector('.page-container-wrap')
  if (target) window.scrollTo({ top: target.offsetTop, behavior: 'smooth' })
}

onMounted(async () => {
  titleChars.value = (appStore.webInfo.webTitle || 'Lune').split('')
  startTypewriter()
  await Promise.all([fetchAllArticles(), fetchCategories(), fetchRecommendArticles(), fetchArticleComments()])
})
</script>

<style scoped>
/* ============================
   Hero Banner
   ============================ */
.background-image-index {
  width: 100vw;
  height: 50vh;
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
  height: 50vh;
  position: fixed;
  z-index: -1;
}

.signature-wall {
  display: flex;
  flex-direction: column;
  position: relative;
  user-select: none;
  height: 50vh;
  overflow: hidden;
}
.myCenter { display: flex; align-items: center; justify-content: center; }

.playful { color: var(--white); font-size: 40px; text-align: center; }
.playful span { display: inline-block; animation: scatter 1.75s infinite; }
.playful span:nth-child(2n) { animation-delay: 0.3s; }
.playful span:nth-child(3n) { animation-delay: 0.15s; }
.playful span:nth-child(5n) { animation-delay: 0.4s; }
.playful span:nth-child(7n) { animation-delay: 0.25s; }

.printer {
  cursor: pointer; color: var(--white); background: var(--translucent);
  border-radius: 10px; padding: 0 10px; margin-top: 12px;
}
.printer h3 { margin: 0; font-size: 18px; font-weight: normal; line-height: 2; }
.cursor { margin-left: 1px; animation: hideToShow 0.7s infinite; font-weight: 200; }

#bannerWave1 {
  height: 84px; position: absolute; width: 200%; bottom: 0; z-index: 10;
  background: var(--gradientBG); background-size: 400% 400%;
  animation: gradientBG 120s linear infinite;
  border-radius: 45% 55% 0 0 / 100% 100% 0 0; opacity: 0.8;
}
#bannerWave2 {
  height: 100px; position: absolute; width: 400%; bottom: 0; z-index: 5;
  background: var(--gradualRed); background-size: 400% 400%;
  animation: gradientBG 120s linear infinite reverse;
  border-radius: 50% 40% 0 0 / 100% 100% 0 0; opacity: 0.5;
}
.scroll-arrow { position: absolute; bottom: 40px; z-index: 15; cursor: pointer; animation: my-shake 1.5s ease-out infinite; }

/* Full-page PixelSnow overlay */
:deep(.full-page-snow) {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  z-index: 50;
  pointer-events: none;
}

/* ============================
   Content Area
   ============================ */
.page-container-wrap {
  position: relative;
}
/* Blurred background image layer */
.bg-blur-layer {
  position: fixed;
  top: 50vh;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  pointer-events: none;
}
.bg-blur-layer::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.55);
}
.bg-blur-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: blur(6px);
  transform: scale(1.05);
}
.page-container {
  display: flex;
  justify-content: center;
  width: 100%;
  padding: 0 10px 40px;
  margin: 0 auto;
  flex-direction: row;
  max-width: 1650px;
  position: relative;
  z-index: 2;
}
.recent-posts { width: 72%; }

/* ============================
   Sidebar
   ============================ */
.aside-content {
  width: calc(28% - 36px);
  user-select: none;
  margin-top: 40px;
  margin-right: 36px;
  max-width: 370px;
}
.myAside-container > div:not(:last-child) { margin-bottom: 24px; }
.selectSort > div:not(:last-child) { margin-bottom: 20px; }

.glass-card {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.card-content1 {
  display: flex; flex-direction: column; align-items: center;
  border-radius: 16px; padding: 24px 16px 20px;
  transition: all 0.3s ease;
}
.card-content1:hover { background: rgba(255, 255, 255, 0.85); box-shadow: 0 8px 32px rgba(0,0,0,0.08); }
.user-avatar { margin-bottom: 12px; }
.web-name {
  font-size: 26px; font-weight: 700;
  background: linear-gradient(135deg, #ff6b6b, #ffa07a);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text; margin-bottom: 6px;
}
.web-bio { font-size: 13px; color: var(--greyFont); margin-bottom: 18px; text-align: center; }
.motto-text {
  font-size: 14px; font-weight: 600; letter-spacing: 2px;
  background: linear-gradient(90deg, #ff6b6b, #ffa07a, #ffd700, #ff6b6b);
  background-size: 300% 100%;
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: mottoShine 3s ease-in-out infinite;
}
@keyframes mottoShine {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}
.web-info { width: 100%; display: flex; justify-content: space-around; margin-bottom: 18px; }
.blog-info-box { display: flex; flex-direction: column; align-items: center; color: var(--articleGreyFontColor); font-size: 13px; }
.blog-info-num { margin-top: 8px; font-size: 22px; font-weight: 700; color: var(--articleFontColor); }

.collection-btn {
  position: relative; margin-top: 4px; background: var(--lightGreen); cursor: pointer;
  width: 65%; height: 36px; border-radius: 1rem; text-align: center; line-height: 36px;
  color: var(--white); overflow: hidden; z-index: 1; text-decoration: none; display: inline-block;
}
.collection-btn::before {
  background: var(--gradualRed); position: absolute; top: 0; right: 0; bottom: 0; left: 0;
  content: ""; transform: scaleX(0); transform-origin: 0;
  transition: transform 0.5s ease-out; transition-timing-function: cubic-bezier(0.45, 1.64, 0.47, 0.66);
  border-radius: 1rem; z-index: -1;
}
.collection-btn:hover::before { transform: scaleX(1); }

.music-card { padding: 18px 16px; border-radius: 22px; animation: hideToShow 1s ease-in-out; }
.music-title { color: #ff6b9d; font-size: 16px; font-weight: 800; margin-bottom: 10px; letter-spacing: 1.5px; font-family: 'Ma Shan Zheng', 'KaiTi', cursive; text-align: center; }
.music-player { display: flex; flex-direction: column; gap: 8px; }
.music-lyrics { text-align: center; min-height: 44px; width: 100%; }
.lyric-line { font-size: 12px; color: #bbb; line-height: 1.6; font-style: italic; font-weight: 600; transition: all 0.4s ease; }
.lyric-line.active { color: #ff4757; font-size: 15px; transform: scale(1.05); font-weight: 700; }
.music-song-name { font-size: 12px; font-weight: 600; color: #666; text-align: center; }
.music-song-artist { font-size: 11px; color: #bbb; text-align: center; }
.music-controls { display: flex; align-items: center; gap: 10px; }
.music-play-btn {
  width: 32px; height: 32px; border-radius: 50%; border: 2px solid #ff6b9d;
  background: linear-gradient(135deg, #fff0f5, #ffe0ec); color: #ff6b9d;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1); flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(255,107,157,0.2);
}
.music-play-btn:hover {
  background: #ff6b9d; color: #fff;
  transform: scale(1.2);
  box-shadow: 0 4px 16px rgba(255,107,157,0.4);
}
.music-progress { flex: 1; height: 4px; background: #f0e0e8; border-radius: 2px; cursor: pointer; overflow: hidden; }
.music-progress-fill { height: 100%; background: linear-gradient(90deg, #ff6b9d, #ffa07a); border-radius: 2px; transition: width 0.3s linear; }

.recommend-card { padding: 20px; border-radius: 14px; animation: hideToShow 1s ease-in-out; }
.card-content2-title { font-size: 16px; margin-bottom: 16px; color: var(--lightGreen); font-weight: 700; }
.aside-post-detail { display: flex; cursor: pointer; }
.aside-post-image { width: 40%; min-height: 50px; border-radius: 6px; margin-right: 8px; overflow: hidden; position: relative; }
.my-el-image { width: 100%; height: 100%; }
.error-aside-image { background: var(--themeBackground); color: var(--white); padding: 10px; text-align: center; width: 100%; height: 100%; font-size: 12px; }
.aside-post-title { width: 60%; white-space: nowrap; text-overflow: ellipsis; overflow: hidden; color: var(--articleFontColor); font-size: 13px; }
.aside-post-date { margin-top: 8px; margin-bottom: 16px; color: var(--greyFont); font-size: 12px; }

.sort-card {
  position: relative; padding: 10px 25px 15px; border-radius: 14px;
  animation: hideToShow 1s ease-in-out; cursor: pointer; color: var(--white); transition: transform 0.2s;
}
.sort-card:hover { transform: scale(1.03); }
.sort-name { font-weight: bold; font-size: 25px; margin-top: 15px; white-space: nowrap; text-overflow: ellipsis; overflow: hidden; }
.sort-name:after { top: 74px; width: 22px; left: 26px; height: 2px; background: var(--white); content: ""; border-radius: 1px; position: absolute; }

/* ============================
   Article Cards — Grid
   ============================ */
.announcement {
  padding: 20px 24px; border: 1px solid rgba(0,0,0,0.06); color: var(--greyFont);
  border-radius: 14px; display: flex; margin: 30px auto 28px;
  background: rgba(255,255,255,0.55); backdrop-filter: blur(8px);
}
.announcement div div { margin-left: 20px; line-height: 30px; }

.sort-article-first {
  margin: 30px auto 16px; display: flex; justify-content: space-between; font-size: 16px;
  font-weight: 600; color: var(--articleFontColor); border-bottom: 1px solid rgba(0,0,0,0.08); padding-bottom: 8px;
}
.article-more { cursor: pointer; transition: all 0.3s; font-size: 14px; color: var(--greyFont); }
.article-more:hover { color: var(--lightGreen); font-weight: 700; transform: scale(1.1); }

/* 3-column article grid */
.article-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.article-card {
  background: transparent;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.35s ease;
  border: 1px solid transparent;
  display: flex;
  flex-direction: column;
}
.article-card:hover {
  transform: translateY(-3px);
  background: rgba(255, 255, 255, 0.45);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.07);
  border-color: rgba(0, 0, 0, 0.04);
}
.article-cover-wrap {
  width: 100%;
  height: 200px;
  flex-shrink: 0;
  overflow: hidden;
}
.article-cover-img { width: 100%; height: 100%; }
.article-cover-error { background: var(--themeBackground); color: var(--white); text-align: center; padding: 20px; height: 100%; display: flex; align-items: center; justify-content: center; }
.article-body { padding: 16px 18px 18px; display: flex; flex-direction: column; flex: 1; }
.article-title {
  font-size: 17px; margin: 0 0 8px; color: var(--articleFontColor);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-weight: 600;
}
.article-summary {
  color: var(--articleGreyFontColor); font-size: 13px; flex: 1; margin: 0;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-height: 1.5;
}
.article-meta { display: flex; gap: 14px; color: var(--greyFont); font-size: 12px; margin-top: 10px; }

/* ============================
   Pagination
   ============================ */
.pagination-wrap { display: flex; justify-content: center; margin-top: 40px; }
.pagination {
  padding: 13px 15px; border: 1px solid rgba(0,0,0,0.1); border-radius: 3rem;
  color: var(--greyFont); width: 100px; user-select: none; cursor: pointer; text-align: center;
  background: rgba(255,255,255,0.5); backdrop-filter: blur(8px);
}
.pagination:hover { border: 1px solid var(--themeBackground); color: var(--themeBackground); box-shadow: 0 0 8px rgba(255,165,0,0.2); }

/* ============================
   Utility
   ============================ */
.shadow-box { box-shadow: 0 2px 16px rgba(0,0,0,0.05); }
.shadow-box-mini { box-shadow: 0 1px 8px rgba(0,0,0,0.06); }
.image-slot { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; }

/* ============================
   Responsive
   ============================ */
@media screen and (max-width: 1200px) {
  .recent-posts { width: 100%; }
  .page-container { width: 100%; }
  .article-grid { grid-template-columns: repeat(2, 1fr); }
}
@media screen and (max-width: 1000px) {
  .page-container { flex-direction: column; }
  .aside-content { width: 100%; max-width: unset; margin: 40px auto 0; }
}
@media screen and (max-width: 768px) {
  h1 { font-size: 35px; }
  .playful { font-size: 35px; }
  .article-grid { grid-template-columns: 1fr; }
  .article-cover-wrap { height: 200px; }
}
</style>

<template>
  <div class="snap-container">
    <!-- ===== Section 1: Hero ===== -->
    <section class="snap-section hero-section">
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
    </section>

    <!-- ===== Section 2: Content with panoramic background ===== -->
    <section class="snap-section content-section">
      <el-image
        class="content-bg-image"
        v-once
        lazy
        :src="contentBgImage"
        fit="cover"
      >
        <div slot="error" class="image-slot content-bg-fallback"></div>
      </el-image>
      <div class="content-bg-overlay"></div>

      <div class="page-container-wrap">
        <div class="page-container">
        <!-- Left sidebar: personal profile -->
        <aside class="aside-content" v-if="showAside">
          <!-- Glass morphism info card -->
          <div class="card-content1 glass-card shadow-box">
            <el-avatar class="user-avatar" :size="144" :src="appStore.webInfo.avatar || '/assets/头像1.jpg'" />
            <div class="web-name">{{ appStore.webInfo.webName || 'Lune' }}</div>
            <div class="web-bio">记录美好生活，分享成长点滴 ✨</div>
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

          <!-- Search box -->
          <div class="search-box-card glass-card shadow-box">
            <div class="search-title">搜索</div>
            <div class="search-row">
              <input
                class="ais-SearchBox-input"
                v-model="articleSearch"
                placeholder="搜索文章"
                maxlength="32"
                @keyup.enter="selectArticle"
              />
              <div class="ais-SearchBox-submit" @click="selectArticle">
                <svg style="margin-top: 3.5px; margin-left: 18px" viewBox="0 0 1024 1024" width="20" height="20">
                  <path d="M51.2 508.8c0 256.8 208 464.8 464.8 464.8s464.8-208 464.8-464.8-208-464.8-464.8-464.8-464.8 208-464.8 464.8z" fill="#51C492"></path>
                  <path d="M772.8 718.4c48-58.4 76.8-132.8 76.8-213.6 0-186.4-151.2-337.6-337.6-337.6-186.4 0-337.6 151.2-337.6 337.6 0 186.4 151.2 337.6 337.6 337.6 81.6 0 156-28.8 213.6-76.8L856 896l47.2-47.2-130.4-130.4zM512 776c-149.6 0-270.4-121.6-270.4-271.2S363.2 233.6 512 233.6c149.6 0 271.2 121.6 271.2 271.2C782.4 654.4 660.8 776 512 776z" fill="#FFFFFF"></path>
                </svg>
              </div>
            </div>
          </div>

          <!-- Recommended articles -->
          <div v-if="recommendArticles.length > 0" class="recommend-card glass-card shadow-box">
            <div class="card-content2-title">
              <span>🔥 推荐文章</span>
            </div>
            <div
              v-for="(article, index) in recommendArticles"
              :key="'rec' + index"
              @click="$router.push('/article/' + article.id)"
            >
              <div class="aside-post-detail">
                <div class="aside-post-image">
                  <el-image lazy class="my-el-image" :src="article.cover || '/assets/背景1.jpg'" fit="cover">
                    <div slot="error" class="image-slot">
                      <div class="error-aside-image">{{ article.title }}</div>
                    </div>
                  </el-image>
                </div>
                <div class="aside-post-title">{{ article.title }}</div>
              </div>
              <div class="aside-post-date">
                <i class="el-icon-date" style="color: var(--greyFont)"></i>{{ formatDate(article.createTime) }}
              </div>
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
        </aside>

        <!-- Middle: main article area -->
        <main class="recent-posts">
          <!-- Notice bar -->
          <div class="announcement glass-card" v-if="notices.length > 0">
            <span style="color: var(--themeBackground); font-size: 22px; margin: auto 0; animation: scale 0.8s ease-in-out infinite;">📢</span>
            <div>
              <div v-for="(notice, idx) in notices" :key="'notice' + idx">{{ notice }}</div>
            </div>
          </div>

          <!-- Default view: articles grouped by category -->
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
                  <div class="article-more" @click="selectSort(cat)">
                    <svg viewBox="0 0 1024 1024" width="20" height="20" style="vertical-align: -2px; margin-bottom: -2px">
                      <path d="M347.3 897.3H142.2c-30.8 0-51.4-31.7-38.9-59.9l136.1-306.1c4.9-11 4.9-23.6 0-34.6L103.3 190.6c-12.5-28.2 8.1-59.9 38.9-59.9h205.1c16.8 0 32.1 9.9 38.9 25.3l151.4 340.7c4.9 11 4.9 23.6 0 34.6L386.3 872.1c-6.9 15.3-22.1 25.2-39 25.2z" fill="#009F72"></path>
                      <path d="M730.4 897.3H525.3c-30.8 0-51.4-31.7-38.9-59.9l136.1-306.1c4.9-11 4.9-23.6 0-34.6L486.4 190.6c-12.5-28.2 8.1-59.9 38.9-59.9h205.1c16.8 0 32.1 9.9 38.9 25.3l151.4 340.7c4.9 11 4.9 23.6 0 34.6L769.3 872.1c-6.8 15.3-22.1 25.2-38.9 25.2z" fill="#F9DB88"></path>
                    </svg>
                    MORE
                  </div>
                </div>
                <div class="sort-article-list">
                  <div
                    v-for="article in groupedArticles[cat.id].slice(0, 4)"
                    :key="article.id"
                    class="article-card shadow-box"
                    @click="$router.push('/article/' + article.id)"
                  >
                    <div class="article-cover-wrap">
                      <el-image lazy class="article-cover-img" :src="article.cover || '/assets/背景1.jpg'" fit="cover">
                        <div slot="error" class="image-slot article-cover-error">{{ article.title }}</div>
                      </el-image>
                    </div>
                    <div class="article-body">
                      <h3 class="article-title">{{ article.title }}</h3>
                      <p class="article-summary">{{ article.summary || (article.content || '').substring(0, 150) }}</p>
                      <div class="article-meta">
                        <span>📅 {{ formatDate(article.createTime) }}</span>
                        <span>👁 {{ article.viewCount || 0 }}</span>
                        <span>❤️ {{ article.likeCount || 0 }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Filtered view: flat article list -->
          <div v-show="indexType === 2">
            <div v-if="filteredArticles.length === 0" style="text-align: center; padding: 80px 0;">
              <el-empty description="暂无文章" />
            </div>
            <div
              v-for="article in filteredArticles"
              :key="article.id"
              class="article-card shadow-box"
              @click="$router.push('/article/' + article.id)"
            >
              <div class="article-cover-wrap">
                <el-image lazy class="article-cover-img" :src="article.cover || '/assets/背景1.jpg'" fit="cover">
                  <div slot="error" class="image-slot article-cover-error">{{ article.title }}</div>
                </el-image>
              </div>
              <div class="article-body">
                <h3 class="article-title">{{ article.title }}</h3>
                <p class="article-summary">{{ article.summary || (article.content || '').substring(0, 150) }}</p>
                <div class="article-meta">
                  <span>📅 {{ formatDate(article.createTime) }}</span>
                  <span>👁 {{ article.viewCount || 0 }}</span>
                  <span>❤️ {{ article.likeCount || 0 }}</span>
                </div>
              </div>
            </div>
            <div class="pagination-wrap">
              <div @click="pageArticles()" class="pagination" v-if="pagination.total !== filteredArticles.length">
                下一页
              </div>
              <div v-else style="user-select: none; color: var(--greyFont)">
                ~~到底啦~~
              </div>
            </div>
          </div>
        </main>

        <!-- Right: 3D interactive scene -->
        <aside class="three-aside">
          <ThreeScene />
        </aside>
      </div>
    </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '../../stores/app'
import { articleApi, categoryApi } from '../../api/modules'
import ThreeScene from '../../components/ThreeCharacter/ThreeScene.vue'

const router = useRouter()
const appStore = useAppStore()

// Hero state
const titleChars = ref([])
const printerText = ref('')
const fullPrinterText = ref('你看对面的青山多漂亮')

// Background image: use store value or fallback
const bgImage = computed(() => {
  const bg = appStore.webInfo.backgroundImage
  if (bg) return bg
  const covers = safeJsonParse(appStore.webInfo.randomCover, [])
  const idx = Math.floor(Math.random() * 11) + 1
  return covers.length > 0 ? covers[Math.floor(Math.random() * covers.length)] : `/assets/背景${idx}.jpg`
})

// Content section background (different from hero)
const contentBgImage = computed(() => {
  const covers = safeJsonParse(appStore.webInfo.randomCover, [])
  if (covers.length > 1) {
    return covers[Math.floor(Math.random() * covers.length)]
  }
  const idx = (Math.floor(Math.random() * 5) + 6) % 11 + 1
  return `/assets/背景${idx}.jpg`
})

// Notice parsing
const notices = computed(() => {
  return safeJsonParse(appStore.webInfo.notices, [])
})

// Article index view mode: 1 = grouped by category, 2 = flat filtered list
const indexType = ref(1)
const showAside = ref(true)
const articleSearch = ref('')

// Category colors
const sortColors = ['#FF623E', '#51C492', '#F9DB88', '#5362f6', '#e485f8', '#ff9c55']

// Data
const articles = ref([])
const allArticles = ref([])
const filteredArticles = ref([])
const categories = ref([])
const recommendArticles = ref([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
  searchKey: '',
  sortId: null,
  articleSearch: ''
})

const total = ref(0)

// Grouped articles by category for default view
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
  let i = 0
  let forward = true
  let pauseCount = 0
  const PAUSE_FRAMES = 30

  setInterval(() => {
    if (forward) {
      printerText.value = text.slice(0, i + 1)
      i++
      if (i > text.length) {
        forward = false
        pauseCount = 0
      }
    } else if (pauseCount < PAUSE_FRAMES) {
      pauseCount++
    } else {
      printerText.value = text.slice(0, i)
      i--
      if (i <= 0) {
        forward = true
      }
    }
  }, 120)
}

const poemPhrases = [
  '你看对面的青山多漂亮',
  '人生若只如初见，何事秋风悲画扇',
  '愿我如星君如月，夜夜流光相皎洁',
  '云想衣裳花想容，春风拂槛露华浓',
  '山有木兮木有枝，心悦君兮君不知',
  '世间安得双全法，不负如来不负卿',
  '春风得意马蹄疾，一日看尽长安花',
  '众里寻他千百度，蓦然回首，那人却在灯火阑珊处'
]

function getRandomPoem() {
  const idx = Math.floor(Math.random() * poemPhrases.length)
  fullPrinterText.value = poemPhrases[idx]
}

async function selectSort(cat) {
  pagination.sortId = cat.id
  pagination.current = 1
  pagination.size = 10
  pagination.searchKey = ''
  pagination.articleSearch = ''
  filteredArticles.value = []
  articleSearch.value = ''
  await getArticles()
  nextTick(() => {
    indexType.value = 2
    document.querySelector('.recent-posts')?.scrollIntoView({
      behavior: 'smooth', block: 'start', inline: 'nearest'
    })
  })
}

async function selectArticle() {
  if (!articleSearch.value.trim()) return
  pagination.searchKey = ''
  pagination.sortId = null
  pagination.articleSearch = articleSearch.value.trim()
  pagination.current = 1
  pagination.size = 10
  filteredArticles.value = []
  await getArticles()
  nextTick(() => {
    indexType.value = 2
    document.querySelector('.recent-posts')?.scrollIntoView({
      behavior: 'smooth', block: 'start', inline: 'nearest'
    })
  })
}

async function pageArticles() {
  pagination.current++
  await getArticles()
}

async function getArticles() {
  try {
    const params = { page: pagination.current, size: pagination.size }
    if (pagination.sortId) params.categoryId = pagination.sortId
    if (pagination.articleSearch) params.keyword = pagination.articleSearch
    const data = await articleApi.list(params)
    if (data && data.records) {
      filteredArticles.value = [...filteredArticles.value, ...data.records]
      pagination.total = data.total
    }
  } catch (e) {
    console.error('Failed to fetch articles:', e)
  }
}

async function fetchAllArticles() {
  try {
    const data = await articleApi.list({ page: 1, size: 100 })
    if (data && data.records) {
      allArticles.value = data.records
      total.value = data.total
    }
  } catch (e) {
    console.error('Failed to fetch all articles:', e)
  }
}

async function fetchRecommendArticles() {
  try {
    const data = await articleApi.list({ page: 1, size: 5 })
    if (data && data.records) {
      recommendArticles.value = data.records
    }
  } catch (e) {
    console.error('Failed to fetch recommended articles:', e)
  }
}

async function fetchCategories() {
  try {
    categories.value = await categoryApi.list('article')
  } catch (e) {
    console.error('Failed to fetch categories:', e)
  }
}

function scrollToContent() {
  const container = document.querySelector('.snap-container')
  if (container) {
    container.scrollBy({ top: window.innerHeight, behavior: 'smooth' })
  }
}

onMounted(async () => {
  titleChars.value = (appStore.webInfo.webTitle || 'Lune').split('')
  startTypewriter()
  document.body.style.overflow = 'hidden'
  await Promise.all([
    fetchAllArticles(),
    fetchCategories(),
    fetchRecommendArticles()
  ])
})

onUnmounted(() => {
  document.body.style.overflow = ''
})
</script>

<style scoped>
/* ============================
   Snap Scroll Container
   ============================ */
.snap-container {
  height: 100vh;
  overflow-y: scroll;
  scroll-snap-type: y mandatory;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.snap-container::-webkit-scrollbar {
  display: none;
}

.snap-section {
  scroll-snap-align: start;
  scroll-snap-stop: always;
  position: relative;
  overflow: hidden;
}

/* ============================
   Hero Section — 100vh
   ============================ */
.hero-section {
  height: 100vh;
}

.background-image-index {
  width: 100vw;
  height: 100vh;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 0;
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
  position: absolute;
  top: 0;
  left: 0;
  z-index: 0;
}

/* ============================
   Content Section Background
   ============================ */
.content-section {
  min-height: 100vh;
}

.content-bg-image {
  width: 100vw;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 0;
}

.content-bg-image::before {
  position: absolute;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.25);
  content: '';
}

.content-bg-fallback {
  background: linear-gradient(180deg, #c9e8ff 0%, #e8f4fd 40%, #d4f0d4 80%, #b8e0b8 100%);
  width: 100vw;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 0;
}

.content-bg-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 1;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
  pointer-events: none;
}

.myCenter {
  display: flex;
  align-items: center;
  justify-content: center;
}

.signature-wall {
  display: flex;
  flex-direction: column;
  position: relative;
  user-select: none;
  height: 100vh;
  overflow: hidden;
  z-index: 1;
}

.playful {
  color: var(--white);
  font-size: 40px;
  text-align: center;
}

.playful span {
  display: inline-block;
  animation: scatter 1.75s infinite;
}

.playful span:nth-child(2n) { animation-delay: 0.3s; }
.playful span:nth-child(3n) { animation-delay: 0.15s; }
.playful span:nth-child(5n) { animation-delay: 0.4s; }
.playful span:nth-child(7n) { animation-delay: 0.25s; }

.printer {
  cursor: pointer;
  color: var(--white);
  background: var(--translucent);
  border-radius: 10px;
  padding-left: 10px;
  padding-right: 10px;
  margin-top: 12px;
}

.printer h3 {
  margin: 0;
  font-size: 18px;
  font-weight: normal;
  line-height: 2;
}

.cursor {
  margin-left: 1px;
  animation: hideToShow 0.7s infinite;
  font-weight: 200;
}

/* Waves */
#bannerWave1 {
  height: 84px;
  position: absolute;
  width: 200%;
  bottom: 0;
  z-index: 10;
  background: var(--gradientBG);
  background-size: 400% 400%;
  animation: gradientBG 120s linear infinite;
  border-radius: 45% 55% 0 0 / 100% 100% 0 0;
  opacity: 0.8;
}

#bannerWave2 {
  height: 100px;
  position: absolute;
  width: 400%;
  bottom: 0;
  z-index: 5;
  background: var(--gradualRed);
  background-size: 400% 400%;
  animation: gradientBG 120s linear infinite reverse;
  border-radius: 50% 40% 0 0 / 100% 100% 0 0;
  opacity: 0.5;
}

.scroll-arrow {
  position: absolute;
  bottom: 40px;
  z-index: 15;
  cursor: pointer;
  animation: my-shake 1.5s ease-out infinite;
}

/* ============================
   Content Area — 3-column Grid
   ============================ */
.page-container-wrap {
  position: relative;
  z-index: 2;
  min-height: 100vh;
  padding-bottom: 40px;
}

.page-container {
  display: grid;
  grid-template-columns: 320px minmax(700px, 1fr) 380px;
  gap: 20px;
  max-width: 1800px;
  width: 100%;
  padding: 24px 16px 40px;
  margin: 0 auto;
  align-items: start;
}

/* ============================
   Left Sidebar — Glass Morphism
   ============================ */
.aside-content {
  user-select: none;
  position: sticky;
  top: 80px;
}

.aside-content > div {
  margin-bottom: 24px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.selectSort > div:not(:last-child) {
  margin-bottom: 20px;
}

/* Info card */
.card-content1 {
  display: flex;
  flex-direction: column;
  align-items: center;
  border-radius: 20px;
  padding: 28px 20px 24px;
  transition: all 0.3s ease;
}

.card-content1:hover {
  background: rgba(255, 255, 255, 0.85);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.user-avatar {
  margin-bottom: 16px;
}

.web-name {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #ff6b6b, #ffa07a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.web-bio {
  font-size: 13px;
  color: var(--greyFont);
  margin-bottom: 20px;
  text-align: center;
}

.web-info {
  width: 100%;
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  margin-bottom: 20px;
}

.blog-info-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-around;
  color: var(--articleGreyFontColor);
  font-size: 13px;
}

.blog-info-num {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 700;
  color: var(--articleFontColor);
}

.collection-btn {
  position: relative;
  margin-top: 4px;
  background: var(--lightGreen);
  cursor: pointer;
  width: 70%;
  height: 38px;
  border-radius: 1rem;
  text-align: center;
  line-height: 38px;
  color: var(--white);
  overflow: hidden;
  z-index: 1;
  text-decoration: none;
  display: inline-block;
  font-size: 15px;
}

.collection-btn::before {
  background: var(--gradualRed);
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  content: "";
  transform: scaleX(0);
  transform-origin: 0;
  transition: transform 0.5s ease-out;
  transition-timing-function: cubic-bezier(0.45, 1.64, 0.47, 0.66);
  border-radius: 1rem;
  z-index: -1;
}

.collection-btn:hover::before {
  transform: scaleX(1);
}

/* Search box */
.search-box-card {
  padding: 18px;
  border-radius: 16px;
  animation: hideToShow 1s ease-in-out;
}

.search-title {
  color: var(--lightGreen);
  font-size: 19px;
  font-weight: 700;
  margin-bottom: 12px;
}

.search-row {
  display: flex;
}

.ais-SearchBox-input {
  padding: 0 14px;
  height: 34px;
  width: calc(100% - 50px);
  outline: 0;
  border: 2px solid var(--lightGreen);
  border-right: 0;
  border-radius: 40px 0 0 40px;
  color: var(--maxGreyFont);
  background: rgba(255, 255, 255, 0.6);
}

.ais-SearchBox-submit {
  height: 34px;
  width: 50px;
  border: 2px solid var(--lightGreen);
  border-left: 0;
  border-radius: 0 40px 40px 0;
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
}

/* Recommended articles */
.recommend-card {
  padding: 22px;
  border-radius: 16px;
  animation: hideToShow 1s ease-in-out;
}

.card-content2-title {
  font-size: 17px;
  margin-bottom: 18px;
  color: var(--lightGreen);
  font-weight: 700;
}

.aside-post-detail {
  display: flex;
  cursor: pointer;
}

.aside-post-image {
  width: 42%;
  min-height: 55px;
  border-radius: 8px;
  margin-right: 10px;
  overflow: hidden;
  position: relative;
}

.my-el-image {
  width: 100%;
  height: 100%;
}

.error-aside-image {
  background: var(--themeBackground);
  color: var(--white);
  padding: 10px;
  text-align: center;
  width: 100%;
  height: 100%;
  font-size: 12px;
}

.aside-post-title {
  width: 58%;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  color: var(--articleFontColor);
  font-size: 14px;
}

.aside-post-date {
  margin-top: 8px;
  margin-bottom: 18px;
  color: var(--greyFont);
  font-size: 12px;
}

/* Category quick-browse cards */
.sort-card {
  position: relative;
  padding: 14px 25px 18px;
  border-radius: 14px;
  animation: hideToShow 1s ease-in-out;
  cursor: pointer;
  color: var(--white);
  transition: transform 0.2s;
}

.sort-card:hover {
  transform: scale(1.03);
}

.sort-name {
  font-weight: bold;
  font-size: 26px;
  margin-top: 14px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.sort-name:after {
  top: 76px;
  width: 24px;
  left: 26px;
  height: 2px;
  background: var(--white);
  content: "";
  border-radius: 1px;
  position: absolute;
}

/* ============================
   Main Article Area
   ============================ */
.recent-posts {
  padding-top: 8px;
}

.announcement {
  padding: 20px 24px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  color: var(--greyFont);
  border-radius: 16px;
  display: flex;
  margin-bottom: 28px;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(8px);
}

.announcement div div {
  margin-left: 20px;
  line-height: 30px;
}

.sort-article-first {
  margin: 28px auto 18px;
  display: flex;
  justify-content: space-between;
  font-size: 17px;
  font-weight: 600;
  color: var(--articleFontColor);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  padding-bottom: 8px;
}

.article-more {
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: var(--greyFont);
}

.article-more:hover {
  color: var(--lightGreen);
  font-weight: 700;
  transform: scale(1.1);
}

.sort-article-list {
  display: flex;
  flex-direction: column;
}

.article-card {
  background: transparent;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 22px;
  cursor: pointer;
  transition: all 0.35s ease;
  display: flex;
  border: 1px solid transparent;
}

.article-card:hover {
  transform: translateY(-3px);
  background: rgba(255, 255, 255, 0.45);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.07);
  border-color: rgba(0, 0, 0, 0.04);
}

.article-cover-wrap {
  width: 240px;
  min-height: 160px;
  flex-shrink: 0;
  overflow: hidden;
}

.article-cover-img {
  width: 100%;
  height: 100%;
}

.article-cover-error {
  background: var(--themeBackground);
  color: var(--white);
  text-align: center;
  padding: 20px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.article-body {
  padding: 20px 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.article-title {
  font-size: 20px;
  margin: 0 0 10px;
  color: var(--articleFontColor);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}

.article-summary {
  color: var(--articleGreyFontColor);
  font-size: 15px;
  flex: 1;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.6;
}

.article-meta {
  display: flex;
  gap: 18px;
  color: var(--greyFont);
  font-size: 13px;
  margin-top: 12px;
}

/* ============================
   Right: 3D Scene Container
   ============================ */
.three-aside {
  position: sticky;
  top: 80px;
  height: calc(100vh - 100px);
  min-height: 620px;
  border-radius: 20px;
  overflow: hidden;
}

/* ============================
   Pagination
   ============================ */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.pagination {
  padding: 13px 15px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 3rem;
  color: var(--greyFont);
  width: 100px;
  user-select: none;
  cursor: pointer;
  text-align: center;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(8px);
}

.pagination:hover {
  border: 1px solid var(--themeBackground);
  color: var(--themeBackground);
  box-shadow: 0 0 8px rgba(255, 165, 0, 0.2);
}

/* ============================
   Utility
   ============================ */
.shadow-box {
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.05);
}

.shadow-box-mini {
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
}

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

/* ============================
   Responsive
   ============================ */
@media screen and (max-width: 1400px) {
  .page-container {
    grid-template-columns: 300px minmax(600px, 1fr) 280px;
    gap: 18px;
  }
  .three-aside {
    min-height: 500px;
  }
}

@media screen and (max-width: 1200px) {
  .page-container {
    grid-template-columns: 280px 1fr;
    gap: 18px;
  }
  .three-aside {
    display: none;
  }
}

@media screen and (max-width: 900px) {
  .page-container {
    grid-template-columns: 1fr;
    width: 100%;
    padding: 16px 12px 30px;
  }
  .aside-content {
    position: static;
    order: 2;
  }
  .recent-posts {
    order: 1;
  }
}

@media screen and (max-width: 768px) {
  h1 { font-size: 35px; }
  .playful { font-size: 35px; }

  .article-card {
    flex-direction: column;
  }
  .article-cover-wrap {
    width: 100%;
    height: 200px;
  }
  .article-body {
    padding: 16px;
  }
  .article-title {
    font-size: 17px;
  }
}
</style>

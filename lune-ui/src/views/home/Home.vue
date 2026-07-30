<template>
  <div>
    <!-- Hero background image -->
    <el-image
      style="animation: header-effect 2s"
      class="background-image-index"
      lazy
      :src="bgImage"
      fit="cover"
    >
      <div slot="error" class="image-slot background-image-index-error"></div>
    </el-image>

    <!-- PixelSnow full-page overlay（移动端降级为轻量 CSS 雪花） -->
    <PixelSnow
      v-if="!appStore.mobile"
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
    <FloatPetals v-else type="snow" :count="12" />

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
      <!-- 透明内容背景（QQ空间风） -->
      <PageBg :image="contentBgImage" variant="green" />

      <div class="page-container">
        <!-- Left sidebar -->
        <div class="aside-content" v-if="showAside">
          <div class="myAside-container">
            <!-- Info card -->
            <div class="card-content1 glass-card shadow-box">
              <el-avatar class="user-avatar" :size="120" :src="appStore.ownerInfo.avatar">
                {{ (appStore.ownerInfo.nickname || 'L').charAt(0) }}
              </el-avatar>
              <div class="web-name">{{ appStore.ownerInfo.nickname || 'Lune' }}</div>
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
                  <span>获赞</span>
                  <span class="blog-info-num">{{ totalLikes }}</span>
                </div>
              </div>
              <a class="collection-btn" @click="$router.push('/treehole')">
                <i class="el-icon-star-off" style="margin-right: 2px"></i>朋友圈
              </a>
            </div>

            <!-- BGM Player（后台可配置真实音频歌单） -->
            <MusicPlayer :fallback="fallbackSongs" />

            <!-- Recommended articles -->
            <div v-if="recommendArticles.length > 0" class="recommend-card glass-card shadow-box">
              <div class="card-content2-title"><span>🔥 推荐文章</span></div>
              <div v-for="(article, index) in recommendArticles" :key="'rec' + index" @click="readerArticleId = article.id">
                <div class="aside-post-detail">
                  <div class="aside-post-image">
                    <el-image lazy class="my-el-image" :src="article.cover || ''" fit="cover">
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
                      <el-image lazy class="article-cover-img" :src="article.cover || ''" fit="cover">
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
            <!-- 筛选态必须有出口：进了分类视图没有「返回全部」就只能刷新页面 -->
            <div class="filter-bar">
              <span class="filter-label">分类：{{ currentSortName }}</span>
              <span class="filter-reset" @click="clearSort()">← 返回全部</span>
            </div>
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
                  <el-image lazy class="article-cover-img" :src="article.cover || ''" fit="cover">
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
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, defineAsyncComponent } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '../../stores/app'
import { useUserStore } from '../../stores/user'
import { articleApi, categoryApi } from '../../api/modules'
import { usePageBackground } from '../../composables/usePageBackground'
// PixelSnow 依赖 three.js（约 500KB），按需异步加载：仅 PC 端渲染时才会下载
const PixelSnow = defineAsyncComponent(() => import('../../components/PixelSnow/PixelSnow.vue'))
import ArticleReader from '../../components/ArticleReader.vue'
import PageBg from '../../components/PageBg.vue'
import MusicPlayer from '../../components/MusicPlayer.vue'
import FloatPetals from '../../components/effects/FloatPetals.vue'

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
// 侧边栏「获赞」总数，来自 GET /api/articles/total-likes
const totalLikes = ref(0)
// 文章ID -> 评论数。留成状态而不是就地赋值，是为了「下一页」追加进来的
// 文章也能补上评论数 —— 否则筛选列表翻页后新加载的卡片评论数恒为 0
const commentCounts = ref({})

const currentSortName = computed(() => {
  const cat = categories.value.find(c => c.id === pagination.sortId)
  return cat ? cat.name : '全部'
})

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

// 兜底歌单（后台未配置真实音频时显示）
const fallbackSongs = []
let typewriterTimer = null

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
  // 连 commentCounts 一起加，否则之后翻页追加进来的同一篇文章会带回旧的评论数
  commentCounts.value[aid] = (commentCounts.value[aid] || 0) + 1
  const upd = (arr) => arr.forEach(a => { if (a.id === aid) a._cc = commentCounts.value[aid] })
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
  clearInterval(typewriterTimer)
  typewriterTimer = setInterval(() => {
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
  // 必须切到 2：分类结果渲染在 indexType === 2 的扁平网格里。
  // 这里原先写的是 1，于是点分类卡片只是滚动一下，筛选结果和它的「下一页」永远看不见。
  indexType.value = 2
  nextTick(() => {
    document.querySelector('.recent-posts')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}
function clearSort() {
  pagination.sortId = null; pagination.current = 1; pagination.total = 0
  filteredArticles.value = []
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
      // 追加的同时补评论数：评论数是单独一次聚合查询拿的，不跟文章分页走
      data.records.forEach(a => { a._cc = commentCounts.value[a.id] || 0 })
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
    // 计数下推到 SQL（GET /api/comments/counts）。原先是拉 size=1000 的评论到浏览器自己数：
    // 把所有评论正文发给每个访客，且评论总数超过 1000 之后开始静默少算。
    commentCounts.value = (await commentApi.counts('article')) || {}
    allArticles.value.forEach(a => { a._cc = commentCounts.value[a.id] || 0 })
    filteredArticles.value.forEach(a => { a._cc = commentCounts.value[a.id] || 0 })
  } catch (e) { console.error('Failed to fetch comment counts:', e) }
}
async function fetchCategories() {
  try { categories.value = await categoryApi.list('article') }
  catch (e) { console.error('Failed to fetch categories:', e) }
}
async function fetchTotalLikes() {
  try { totalLikes.value = await articleApi.totalLikes() ?? 0 }
  catch (e) { console.error('Failed to fetch total likes:', e) }
}
function scrollToContent() {
  const target = document.querySelector('.page-container-wrap')
  if (target) window.scrollTo({ top: target.offsetTop, behavior: 'smooth' })
}

onMounted(async () => {
  titleChars.value = (appStore.webInfo.webTitle || 'Lune').split('')
  startTypewriter()
  // fetchArticleComments 要往 allArticles 的元素上写 _cc，必须等文章先到位。
  // 原先五个请求一起塞进 Promise.all，评论数常常写在空数组上，卡片评论数恒为 0。
  await Promise.all([fetchAllArticles(), fetchCategories(), fetchRecommendArticles(), fetchTotalLikes()])
  await fetchArticleComments()
})
onUnmounted(() => {
  if (typewriterTimer) clearInterval(typewriterTimer)
})
</script>

<style scoped>
/* ============================
   Hero Banner
   ============================ */
.background-image-index {
  width: 100vw;
  height: 50vh;
  position: absolute;
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
  position: absolute;
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
  font-size: 18px; margin: 0 0 8px; color: var(--articleFontColor);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-weight: 700;
  font-family: var(--trendy-font); letter-spacing: 0.5px;
  transition: color 0.3s;
}
.article-card:hover .article-title { color: var(--nature-green); }
.article-summary {
  color: var(--articleGreyFontColor); font-size: 14px; flex: 1; margin: 0;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-height: 1.6;
  font-family: var(--article-font);
}
.article-meta { display: flex; gap: 14px; color: var(--greyFont); font-size: 12px; margin-top: 10px; font-family: var(--trendy-font); }

/* ============================
   Filter bar (分类筛选态)
   ============================ */
.filter-bar {
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
  padding: 12px 18px; margin-bottom: 18px; border-radius: 14px;
  background: rgba(255,255,255,0.55); backdrop-filter: blur(8px);
  border: 1px solid rgba(0,0,0,0.05);
}
.filter-label { font-family: var(--trendy-font); color: var(--fontColor); font-weight: 600; }
.filter-reset {
  font-size: 13px; color: var(--greyFont); cursor: pointer; user-select: none;
  padding: 5px 12px; border-radius: 2rem; transition: color 0.25s, background 0.25s;
}
.filter-reset:hover { color: var(--themeBackground); background: rgba(0,0,0,0.03); }

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
  h1 { font-size: 28px; }
  .playful { font-size: 28px; }
  .article-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  .article-card {
    border-radius: 12px;
  }
  .article-cover-wrap { height: 160px; }
  .article-body { padding: 12px 14px 14px; }
  .article-title { font-size: 16px; margin-bottom: 6px; }
  .article-summary { font-size: 13px; line-height: 1.55; -webkit-line-clamp: 2; }
  .article-meta { font-size: 11px; gap: 10px; margin-top: 8px; }
  .pagination-wrap { margin-top: 24px; }
}
@media screen and (max-width: 400px) {
  h1 { font-size: 24px; }
  .playful { font-size: 24px; }
  .article-cover-wrap { height: 150px; }
}
</style>

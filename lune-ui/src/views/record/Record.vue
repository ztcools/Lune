<template>
  <div class="record-page">
    <!-- ===== Hero ===== -->
    <div class="hero-banner">
      <div class="bg-image" :style="{ backgroundImage: `url(${bannerImage})` }"></div>
      <div class="bg-overlay"></div>
      <div class="hero-info">
        <h1>光阴集</h1>
        <p class="hero-subtitle">记录生活的每一个精彩瞬间</p>
      </div>
      <div class="hero-wave"></div>
    </div>

    <!-- ===== 分类导航 ===== -->
    <div class="cat-nav-wrap">
      <div class="cat-nav">
        <button
          class="cat-pill"
          :class="{ active: activeCategoryId === null }"
          @click="switchCategory(null)"
        >
          <span class="cat-pill-icon">🌈</span> 全部
        </button>
        <button
          v-for="(cat, index) in categoryList"
          :key="cat.id"
          class="cat-pill"
          :class="{ active: activeCategoryId === cat.id }"
          :style="{ '--pill-color': catColors[index % catColors.length] }"
          @click="switchCategory(cat.id)"
        >
          <span class="cat-pill-icon">{{ catIcons[index % catIcons.length] }}</span> {{ cat.name }}
        </button>
      </div>
    </div>

    <!-- ===== 记录瀑布流 ===== -->
    <div class="feed-container">
      <PageBg :image="contentBg" variant="blue" />

      <transition-group name="card-list" tag="div" class="feed-grid" v-if="recordList.length > 0">
        <div v-for="item in recordList" :key="item.id" class="feed-card">
          <!-- 封面媒体 -->
          <div class="feed-media" v-if="item.mediaList.length || item.cover">
            <template v-if="item.mediaList.length">
              <div :class="'fgrid fgrid-' + Math.min(item.mediaList.length, 3)">
                <div v-for="(m, mi) in item.mediaList.slice(0, 3)" :key="mi" class="fgrid-item">
                  <el-image
                    v-if="m.type === 'image'" :src="m.url" fit="cover" class="fgrid-img"
                    :preview-src-list="item.imageList" :initial-index="item.imageList.indexOf(m.url)" lazy />
                  <video v-else :src="m.url" controls class="fgrid-video" preload="metadata"></video>
                </div>
                <div v-if="item.mediaList.length > 3" class="fgrid-more">+{{ item.mediaList.length - 3 }}</div>
              </div>
            </template>
            <el-image v-else-if="item.cover" :src="item.cover" fit="cover" class="feed-cover"
              :preview-src-list="[item.cover]" lazy />
          </div>

          <!-- 文本 -->
          <div class="feed-body">
            <div class="feed-text" v-if="item.content" v-html="item.contentHtml"></div>

            <!-- 底部：作者 + 分类 + 时间 -->
            <div class="feed-footer">
              <div class="feed-author">
                <el-avatar :size="30" :src="item.avatar || appStore.ownerInfo.avatar" class="feed-avatar">
                  {{ (item.nickname || item.username || 'L').charAt(0) }}
                </el-avatar>
                <span class="feed-nickname">{{ item.nickname || item.username || 'Lune' }}</span>
              </div>
              <div class="feed-meta">
                <span class="feed-cat" v-if="item.categoryName">{{ item.categoryName }}</span>
                <span class="feed-time">{{ formatRelative(item.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </transition-group>

      <div v-else class="empty-wrap"><el-empty description="暂无记录，去后台添加一条吧 🌱" /></div>

      <div class="pagination-wrap" v-if="totalCount > recordList.length">
        <button class="load-more-btn" @click="loadMore">加载更多</button>
      </div>
      <div v-else-if="recordList.length > 0" class="pagination-wrap">
        <span class="pagination-end">— THE END —</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { recordApi, categoryApi } from '../../api/modules'
import { usePageBackground } from '../../composables/usePageBackground'
import { useAppStore } from '../../stores/app'
import PageBg from '../../components/PageBg.vue'

const appStore = useAppStore()

const recordList = ref([])
const categoryList = ref([])
const activeCategoryId = ref(null)
const page = ref(1)
const pageSize = 12
const totalCount = ref(0)

const bannerImage = usePageBackground('recordHero')
const contentBg = usePageBackground('recordContent')
const catColors = ['#66bb6a', '#ffa726', '#42a5f5', '#ec407a', '#ab47bc', '#26c6da', '#9ccc65', '#ff7043']
const catIcons = ['📚', '🎮', '✈️', '🍜', '🎵', '📷', '🏃', '🎨']

onMounted(() => { fetchCategories(); fetchRecords() })

async function fetchCategories() {
  try { categoryList.value = await categoryApi.list('record') || [] } catch (e) { /* silent */ }
}

async function fetchRecords() {
  try {
    const params = { page: page.value, size: pageSize }
    if (activeCategoryId.value) params.categoryId = activeCategoryId.value
    const data = await recordApi.list(params)
    if (data && data.records) {
      data.records.forEach((r) => {
        r.contentHtml = processContent(r.content)
        r.mediaList = parseMedia(r.media)
        r.imageList = (r.mediaList || []).filter(m => m.type === 'image').map(m => m.url)
      })
      recordList.value = recordList.value.concat(data.records)
      totalCount.value = data.total
    }
  } catch (e) { /* silent */ }
}

function processContent(text) {
  if (!text) return ''
  return text.replace(/\n{2,}/g, '<div style="height:8px"></div>').replace(/\n/g, '<br/>')
}

function parseMedia(mediaJson) {
  if (!mediaJson) return []
  try { const arr = JSON.parse(mediaJson); return Array.isArray(arr) ? arr.filter(m => m.type && m.url) : [] } catch (e) { return [] }
}

function switchCategory(catId) {
  activeCategoryId.value = catId
  page.value = 1
  recordList.value = []
  fetchRecords()
}

function loadMore() {
  if (totalCount.value > recordList.value.length) { page.value++; fetchRecords() }
}

function formatRelative(d) {
  if (!d) return ''
  const diff = Date.now() - new Date(d).getTime()
  const s = Math.floor(diff / 1000), m = Math.floor(s / 60), h = Math.floor(m / 60), days = Math.floor(h / 24)
  if (s < 60) return '刚刚'
  if (m < 60) return `${m}分钟前`
  if (h < 24) return `${h}小时前`
  if (days < 30) return `${days}天前`
  if (days < 365) return `${Math.floor(days / 30)}个月前`
  return `${Math.floor(days / 365)}年前`
}
</script>

<style scoped>
/* ===== Hero ===== */
.hero-banner { position: relative; height: 34vh; min-height: 230px; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.bg-image { position: absolute; inset: 0; background-size: cover; background-position: center; }
.bg-overlay { position: absolute; inset: 0; background: linear-gradient(135deg, rgba(66,165,245,0.4), rgba(102,187,106,0.3)); }
.hero-info { position: relative; z-index: 2; text-align: center; color: #fff; }
.hero-info h1 { font-family: var(--trendy-font); font-size: 44px; font-weight: 700; margin: 0 0 10px; letter-spacing: 10px; text-shadow: 0 4px 20px rgba(0,0,0,0.3); }
.hero-info p { font-size: 16px; opacity: 0.95; letter-spacing: 2px; }
.hero-wave { position: absolute; bottom: -2px; left: 0; width: 100%; height: 56px; background: var(--background); border-radius: 50% 50% 0 0 / 100% 100% 0 0; z-index: 3; }

/* ===== 分类导航 ===== */
.cat-nav-wrap { position: sticky; top: 60px; z-index: 40; display: flex; justify-content: center; padding: 16px 12px 6px; }
.cat-nav { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; background: rgba(255,255,255,0.75); backdrop-filter: blur(18px); border-radius: 40px; padding: 12px 18px; box-shadow: var(--card-shadow); border: var(--card-border); max-width: 100%; }
.cat-pill { display: inline-flex; align-items: center; gap: 6px; padding: 9px 20px; border: none; border-radius: 26px; background: #f2f4f2; color: #6a7a6a; font-size: 14px; font-weight: 600; font-family: var(--trendy-font); cursor: pointer; transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1); }
.cat-pill-icon { font-size: 15px; transition: transform 0.3s; }
.cat-pill:hover { transform: translateY(-3px) scale(1.05); background: #e6eee6; }
.cat-pill:hover .cat-pill-icon { transform: scale(1.3) rotate(-10deg); }
.cat-pill.active { background: var(--pill-color, var(--nature-green)); color: #fff; box-shadow: 0 6px 18px rgba(0,0,0,0.18); transform: translateY(-2px); }

/* ===== 瀑布流 ===== */
.feed-container { position: relative; max-width: 1200px; margin: 0 auto; padding: 24px 20px 70px; overflow: hidden; }
.feed-grid { position: relative; z-index: 1; display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 24px; }

.card-list-enter-active { transition: all 0.5s ease; }
.card-list-enter-from { opacity: 0; transform: translateY(30px) scale(0.96); }

.feed-card { background: rgba(255,255,255,0.85); backdrop-filter: blur(16px); border-radius: var(--card-radius); overflow: hidden; box-shadow: var(--card-shadow); border: var(--card-border); transition: all 0.4s cubic-bezier(0.34,1.4,0.64,1); display: flex; flex-direction: column; }
.feed-card:hover { transform: translateY(-7px) scale(1.015); box-shadow: var(--card-shadow-hover); }

/* ===== 媒体 ===== */
.feed-media { overflow: hidden; }
.feed-cover { width: 100%; height: 210px; transition: transform 0.5s; cursor: pointer; }
.feed-card:hover .feed-cover { transform: scale(1.06); }
.fgrid { display: grid; gap: 3px; }
.fgrid-1 { grid-template-columns: 1fr; }
.fgrid-2 { grid-template-columns: 1fr 1fr; }
.fgrid-3 { grid-template-columns: 1fr 1fr 1fr; }
.fgrid-item { position: relative; overflow: hidden; }
.fgrid-1 .fgrid-img, .fgrid-1 .fgrid-video { height: 220px; }
.fgrid-2 .fgrid-img, .fgrid-2 .fgrid-video { height: 150px; }
.fgrid-3 .fgrid-img, .fgrid-3 .fgrid-video { height: 110px; }
.fgrid-img { width: 100%; height: 100%; object-fit: cover; cursor: pointer; transition: transform 0.4s; }
.fgrid-img:hover { transform: scale(1.08); }
.fgrid-video { width: 100%; height: 100%; object-fit: cover; background: #000; }
.fgrid-more { position: absolute; right: 3px; bottom: 3px; background: rgba(0,0,0,0.55); color: #fff; font-size: 12px; font-weight: 600; padding: 3px 10px; border-radius: 12px; }

/* ===== 文本与底部 ===== */
.feed-body { padding: 18px 20px 16px; display: flex; flex-direction: column; flex: 1; }
.feed-text { font-size: 15px; line-height: 1.85; color: #3a4a3a; word-break: break-word; margin-bottom: 16px; flex: 1; }
.feed-footer { display: flex; align-items: center; justify-content: space-between; gap: 10px; padding-top: 14px; border-top: 1px dashed rgba(129,199,132,0.35); }
.feed-author { display: flex; align-items: center; gap: 8px; min-width: 0; }
.feed-avatar { border: 2px solid #fff; box-shadow: 0 2px 6px rgba(0,0,0,0.1); flex-shrink: 0; }
.feed-nickname { font-family: var(--trendy-font); font-size: 14px; font-weight: 700; color: #2e5a2e; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.feed-meta { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.feed-cat { font-size: 12px; font-weight: 600; padding: 4px 12px; border-radius: 14px; background: var(--nature-green-pale); color: var(--nature-green-dark); }
.feed-time { font-size: 12px; color: #b0c0b0; }

/* ===== 分页 ===== */
.pagination-wrap { text-align: center; padding: 36px 0 20px; position: relative; z-index: 1; }
.load-more-btn { padding: 11px 34px; border: none; border-radius: 28px; background: var(--nature-gradient); color: #fff; font-size: 15px; font-weight: 600; font-family: var(--trendy-font); cursor: pointer; box-shadow: 0 6px 18px rgba(76,175,80,0.35); transition: all 0.3s; }
.load-more-btn:hover { transform: translateY(-3px); box-shadow: 0 10px 26px rgba(76,175,80,0.5); }
.pagination-end { color: #b0c0b0; font-size: 14px; letter-spacing: 2px; }

.empty-wrap { position: relative; z-index: 1; padding: 60px 0; }

@media screen and (max-width: 768px) {
  .hero-info h1 { font-size: 28px; letter-spacing: 5px; }
  .cat-nav-wrap { top: 56px; padding: 10px 8px 4px; }
  .cat-nav { gap: 6px; padding: 10px 12px; }
  .cat-pill { padding: 7px 14px; font-size: 13px; }
  .feed-grid { grid-template-columns: 1fr; gap: 18px; }
  .feed-container { padding: 16px 12px 50px; }
}
</style>

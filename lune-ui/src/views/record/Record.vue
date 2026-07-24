<template>
  <div class="record-page">
    <!-- ====== Hero Banner ====== -->
    <div class="hero-banner">
      <div class="bg-image" :style="{ backgroundImage: `url(${bannerImage})` }"></div>
      <div class="bg-overlay"></div>
      <div class="hero-info">
        <h1>记录</h1>
        <p>记录生活的每一个精彩瞬间 ✨</p>
      </div>
    </div>

    <!-- ====== Category Tags ====== -->
    <div class="tags-bar">
      <div class="tags-wrap">
        <span
          class="tag-chip"
          :class="{ 'tag-active': activeCategoryId === null }"
          @click="switchCategory(null)"
        >全部</span>
        <span
          v-for="(cat, index) in categoryList"
          :key="cat.id"
          class="tag-chip"
          :class="{ 'tag-active': activeCategoryId === cat.id }"
          :style="{ '--tag-color': tagColors[index % tagColors.length] }"
          @click="switchCategory(cat.id)"
        >{{ cat.name }}</span>
      </div>
    </div>

    <!-- ====== Feed ====== -->
    <div class="feed-container">
      <div class="feed-list" v-if="recordList.length > 0">
        <div
          v-for="item in recordList"
          :key="item.id"
          class="feed-card shadow-box"
        >
          <!-- Header: avatar + nickname -->
          <div class="feed-header">
            <img
              class="feed-avatar"
              :src="appStore.webInfo.avatar || '/assets/头像1.jpg'"
              alt="avatar"
            />
            <span class="feed-nickname">{{ userStore.nickname || appStore.webInfo.webName || 'Lune' }}</span>
          </div>

          <!-- Text content -->
          <div class="feed-text" v-if="item.content" v-html="item.contentHtml"></div>

          <!-- Media: fallback cover image -->
          <div class="feed-media" v-if="item.mediaList.length > 0 || item.cover">
            <!-- JSON media list -->
            <template v-if="item.mediaList.length > 0">
              <!-- Mixed: images grid + video -->
              <div :class="'media-grid media-grid-' + Math.min(item.mediaList.length, 9)">
                <div
                  v-for="(m, mi) in item.mediaList.slice(0, 9)"
                  :key="mi"
                  class="media-item"
                >
                  <!-- Image -->
                  <el-image
                    v-if="m.type === 'image'"
                    :src="m.url"
                    fit="cover"
                    class="media-img"
                    :preview-src-list="item.imageList"
                    :initial-index="item.imageList.indexOf(m.url)"
                  />
                  <!-- Video -->
                  <video
                    v-else-if="m.type === 'video'"
                    :src="m.url"
                    controls
                    class="media-video"
                    preload="metadata"
                  ></video>
                </div>
              </div>
              <!-- +N overlay -->
              <div
                v-if="item.mediaList.length > 9"
                class="media-more"
                @click="expandedMedia = expandedMedia === item.id ? null : item.id"
              >
                +{{ item.mediaList.length - 9 }}
              </div>
            </template>

            <!-- Legacy cover fallback -->
            <el-image
              v-else-if="item.cover && item.mediaList.length === 0"
              :src="item.cover"
              fit="cover"
              class="media-cover"
              :preview-src-list="[item.cover]"
            />
          </div>

          <!-- Footer: category + time -->
          <div class="feed-footer">
            <span class="feed-cat" v-if="item.categoryName">{{ item.categoryName }}</span>
            <span class="feed-time">{{ formatRelative(item.createTime) }}</span>
          </div>
        </div>
      </div>

      <div v-else style="text-align:center;padding:100px 0;">
        <el-empty description="暂无记录" />
      </div>

      <!-- Load More -->
      <div class="pagination-wrap" v-if="totalCount > recordList.length">
        <div class="load-more-btn" @click="loadMore">查看更多</div>
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
import { useAppStore } from '../../stores/app'
import { useUserStore } from '../../stores/user'

const appStore = useAppStore()
const userStore = useUserStore()

// --- state ---
const recordList = ref([])
const categoryList = ref([])
const activeCategoryId = ref(null)
const page = ref(1)
const pageSize = 10
const totalCount = ref(0)
const expandedMedia = ref(null)

// --- constants ---
const bannerImage = ref('/assets/背景3.jpg')
const tagColors = [
  '#FF7744', '#e73c7e', '#23a6d5', '#39c5bb',
  '#ff4b2b', '#9370db', '#FF9D3A', '#67c23a'
]

// --- lifecycle ---
onMounted(() => {
  if (appStore.webInfo.backgroundImage) {
    bannerImage.value = appStore.webInfo.backgroundImage
  }
  fetchCategories()
  fetchRecords()
})

// --- methods ---
async function fetchCategories() {
  try {
    const data = await categoryApi.list('record')
    categoryList.value = data || []
  } catch (e) { /* silent */ }
}

async function fetchRecords() {
  try {
    const params = { page: page.value, size: pageSize }
    if (activeCategoryId.value) {
      params.categoryId = activeCategoryId.value
    }
    const data = await recordApi.list(params)
    if (data && data.records) {
      // Process each record
      data.records.forEach((r) => {
        r.contentHtml = processContent(r.content)
        r.mediaList = parseMedia(r.media)
        r.imageList = (r.mediaList || [])
          .filter(m => m.type === 'image')
          .map(m => m.url)
      })
      recordList.value = recordList.value.concat(data.records)
      totalCount.value = data.total
    }
  } catch (e) { /* silent */ }
}

function processContent(text) {
  if (!text) return ''
  return text
    .replace(/\n{2,}/g, '<div style="height:10px"></div>')
    .replace(/\n/g, '<br/>')
}

function parseMedia(mediaJson) {
  if (!mediaJson) return []
  try {
    const arr = JSON.parse(mediaJson)
    if (Array.isArray(arr)) return arr.filter(m => m.type && m.url)
    return []
  } catch (e) {
    return []
  }
}

function switchCategory(catId) {
  activeCategoryId.value = catId
  page.value = 1
  recordList.value = []
  fetchRecords()
}

function loadMore() {
  if (totalCount.value > recordList.value.length) {
    page.value++
    fetchRecords()
  }
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
/* ====== Hero Banner ====== */
.hero-banner {
  position: relative;
  height: 30vh;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.bg-image { position: absolute; inset: 0; background-size: cover; background-position: center; }
.bg-overlay { position: absolute; inset: 0; background: var(--mask); }
.hero-info { position: relative; z-index: 2; text-align: center; color: var(--white); }
.hero-info h1 {
  font-family: 'Noto Sans SC', var(--globalFont), sans-serif;
  font-size: 42px;
  font-weight: 700;
  font-style: italic;
  margin: 0 0 10px;
  letter-spacing: 6px;
}
.hero-info p {
  font-size: 16px;
  font-weight: 500;
  font-style: italic;
  opacity: 0.85;
}

/* ====== Tags Bar ====== */
.tags-bar {
  background: var(--background);
  padding: 20px 0 0;
}
.tags-wrap {
  max-width: 760px;
  margin: 0 auto;
  padding: 0 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}
.tag-chip {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  font-family: 'Noto Sans SC', var(--globalFont), sans-serif;
  color: #888;
  background: #f2f2f2;
  cursor: pointer;
  user-select: none;
  transition: all 0.25s;
  border: 1.5px solid transparent;
}
.tag-chip:hover {
  transform: scale(1.08);
  color: #555;
}
.tag-active {
  color: #fff !important;
  background: var(--tag-color, #FF7744) !important;
  border-color: var(--tag-color, #FF7744);
  box-shadow: 0 2px 10px rgba(255, 119, 68, 0.3);
}

/* ====== Feed ====== */
.feed-container {
  max-width: 760px;
  margin: 0 auto;
  padding: 20px 16px 60px;
  background: var(--background);
  min-height: 400px;
}

.feed-card {
  background: transparent;
  border-radius: 14px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid transparent;
  transition: all 0.35s ease;
}
.feed-card:hover {
  background: rgba(255, 255, 255, 0.45);
  border-color: rgba(0, 0, 0, 0.04);
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.07);
}

/* ====== Feed Header ====== */
.feed-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
}
.feed-avatar {
  width: 53px;
  height: 53px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}
.feed-nickname {
  font-family: 'Noto Sans SC', var(--globalFont), sans-serif;
  font-size: 20px;
  font-weight: 700;
  font-style: italic;
  color: #3d5a99;
  letter-spacing: 1.5px;
}

/* ====== Feed Text ====== */
.feed-text {
  font-family: 'Noto Sans SC', var(--globalFont), sans-serif;
  font-size: 17px;
  font-weight: 600;
  font-style: italic;
  line-height: 2;
  letter-spacing: 0.5px;
  color: #1a1a1a;
  margin-bottom: 14px;
  word-break: break-word;
}

/* ====== Media ====== */
.feed-media {
  margin-bottom: 14px;
  border-radius: 10px;
  overflow: hidden;
}

/* Legacy cover fallback */
.media-cover {
  width: 100%;
  max-height: 400px;
  border-radius: 10px;
}

/* Media grid */
.media-grid {
  display: grid;
  gap: 4px;
  border-radius: 10px;
  overflow: hidden;
}

/* 1 item: full-width large */
.media-grid-1 {
  grid-template-columns: 1fr;
}
.media-grid-1 .media-img {
  height: 360px;
}

/* 2 items: side by side */
.media-grid-2 {
  grid-template-columns: 1fr 1fr;
}
.media-grid-2 .media-img {
  height: 280px;
}

/* 3 items: 1 big + 2 small (L-shaped) */
.media-grid-3 {
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 240px 240px;
}
.media-grid-3 .media-item:first-child {
  grid-row: 1 / 3;
}
.media-grid-3 .media-item:first-child .media-img {
  height: 100%;
}

/* 4 items: 2x2 */
.media-grid-4 {
  grid-template-columns: 1fr 1fr;
}
.media-grid-4 .media-img {
  height: 200px;
}

/* 5-9 items: 3 columns */
.media-grid-5,
.media-grid-6,
.media-grid-7,
.media-grid-8,
.media-grid-9 {
  grid-template-columns: repeat(3, 1fr);
}
.media-grid-5 .media-img,
.media-grid-6 .media-img,
.media-grid-7 .media-img,
.media-grid-8 .media-img,
.media-grid-9 .media-img {
  height: 180px;
}

.media-item {
  position: relative;
  overflow: hidden;
}
.media-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.3s;
}
.media-img:hover {
  transform: scale(1.03);
}

.media-video {
  width: 100%;
  max-height: 380px;
  border-radius: 10px;
  background: #000;
  outline: none;
}

.media-more {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

/* ====== Feed Footer ====== */
.feed-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.feed-cat {
  font-size: 12px;
  font-weight: 600;
  font-family: 'Noto Sans SC', var(--globalFont), sans-serif;
  padding: 4px 12px;
  border-radius: 12px;
  background: #f0f0f0;
  color: #888;
}
.feed-time {
  font-size: 13px;
  color: #c0c0c0;
  font-weight: 400;
  margin-left: auto;
}

/* ====== Pagination ====== */
.pagination-wrap { text-align: center; padding: 36px 0 20px; }
.load-more-btn {
  display: inline-block;
  padding: 10px 32px;
  border: 1px solid #ddd;
  border-radius: 24px;
  color: #666;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Noto Sans SC', var(--globalFont), sans-serif;
}
.load-more-btn:hover { background: #f7f7f7; border-color: #bbb; }
.pagination-end { color: #ddd; font-size: 14px; letter-spacing: 2px; }

/* ====== Utility ====== */
.shadow-box { box-shadow: 0 2px 16px rgba(0,0,0,0.05); }

/* ====== Responsive ====== */
@media screen and (max-width: 768px) {
  .hero-info h1 { font-size: 30px; letter-spacing: 4px; }
  .hero-info p { font-size: 14px; }
  .feed-container { padding: 16px 8px 40px; }
  .feed-card { padding: 14px; }
  .feed-avatar { width: 44px; height: 44px; border-radius: 6px; }
  .feed-nickname { font-size: 17px; }
  .feed-text { font-size: 15px; line-height: 1.85; }
  .media-grid-1 .media-img { height: 260px; }
  .media-grid-2 .media-img { height: 200px; }
  .media-grid-3 { grid-template-rows: 180px 180px; }
  .media-grid-4 .media-img { height: 150px; }
  .media-grid-5 .media-img,
  .media-grid-6 .media-img,
  .media-grid-7 .media-img,
  .media-grid-8 .media-img,
  .media-grid-9 .media-img { height: 130px; }
}
</style>

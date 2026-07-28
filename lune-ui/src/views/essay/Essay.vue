<template>
  <div class="essay-page">
    <!-- ===== Hero Banner ===== -->
    <div class="hero-banner">
      <div class="bg-image" :style="{ backgroundImage: `url(${bannerImage})` }"></div>
      <div class="bg-overlay"></div>
      <div class="hero-info">
        <h1>随笔随心</h1>
        <p class="hero-subtitle">随心所悦，记录生活点滴 ✍️</p>
      </div>
      <div class="hero-wave"></div>
    </div>

    <!-- ===== 朋友圈动态流 ===== -->
    <div class="moments-container">
      <PageBg :image="contentBg" variant="green" />

      <div class="moments-list" v-if="essayList.length > 0">
        <div v-for="essay in essayList" :key="essay.id" class="moment-card">
          <!-- 头部：头像 + 昵称 + 时间 -->
          <div class="moment-head">
            <el-avatar class="moment-avatar" :size="50" :src="essay.avatar">
              {{ (essay.nickname || essay.username || 'L').charAt(0) }}
            </el-avatar>
            <div class="moment-head-info">
              <span class="moment-username">{{ essay.nickname || essay.username || 'Lune' }}</span>
              <span class="moment-time">{{ formatRelative(essay.createTime) }}<span v-if="essay.location" class="moment-loc"> · 📍{{ essay.location }}</span></span>
            </div>
            <span
              v-if="userStore.isLoggedIn && userStore.user?.userId === essay.userId"
              class="moment-delete"
              @click="handleDelete(essay.id)"
              title="删除"
            >🗑️</span>
          </div>

          <!-- 正文 -->
          <div class="moment-text" v-html="essay.contentHtml"></div>

          <!-- 媒体：朋友圈九宫格 -->
          <div class="moment-media" v-if="essay.mediaList.length">
            <div :class="'mgrid mgrid-' + Math.min(essay.mediaList.length, 9)">
              <div v-for="(m, mi) in essay.mediaList.slice(0, 9)" :key="mi" class="mgrid-item">
                <el-image
                  v-if="m.type === 'image'"
                  :src="m.url" fit="cover" class="mgrid-img"
                  :preview-src-list="essay.imageList"
                  :initial-index="essay.imageList.indexOf(m.url)"
                  lazy
                />
                <video v-else :src="m.url" controls class="mgrid-video" preload="metadata"></video>
              </div>
            </div>
          </div>

          <!-- 心情/天气标签 -->
          <div class="moment-tags" v-if="essay.weather || essay.mood">
            <span v-if="essay.weather" class="moment-tag">{{ essay.weather }}</span>
            <span v-if="essay.mood" class="moment-tag mood">{{ essay.mood }}</span>
          </div>

          <!-- 操作栏 -->
          <div class="moment-actions">
            <button class="action-btn comment-toggle" @click.stop="toggleComment(essay)">
              <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              <span>{{ essay._cc || 0 }}</span>
            </button>
          </div>

          <!-- 评论区 -->
          <transition name="expand">
            <div v-if="expandedEssayId === essay.id" class="moment-comments">
              <div class="comments-list">
                <div v-if="!essayComments.length" class="comment-empty">暂无评论，来说点什么吧～</div>
                <div v-for="c in essayComments" :key="c.id" class="comment-item">
                  <el-avatar :size="30" :src="c.avatar" class="c-avatar" @click.stop="showMiniProfile(c, $event)">
                    {{ (c.nickname || c.username || '匿').charAt(0) }}
                  </el-avatar>
                  <div class="c-body">
                    <div class="c-top">
                      <span class="c-nick">{{ c.nickname || c.username || '匿名' }}</span>
                      <span class="c-time">{{ formatRelative(c.createTime) }}</span>
                    </div>
                    <div class="c-text">{{ c.content }}</div>
                  </div>
                </div>
              </div>
              <div class="comment-input-bar">
                <el-input v-model="commentText[essay.id]" placeholder="说点什么..." size="small" class="c-input"
                  @keyup.enter="submitComment(essay)">
                  <template #suffix>
                    <el-button type="primary" size="small" round :disabled="!(commentText[essay.id] || '').trim()" @click="submitComment(essay)">发送</el-button>
                  </template>
                </el-input>
              </div>
            </div>
          </transition>
        </div>
      </div>

      <div v-else class="empty-wrap"><el-empty description="还没有随笔，来写第一条吧 ✨" /></div>

      <!-- 加载更多 -->
      <div class="pagination-wrap" v-if="total > essayList.length">
        <button class="load-more-btn" @click="loadMore">查看更多</button>
      </div>
      <div v-else-if="essayList.length > 0" class="pagination-wrap">
        <span class="pagination-end">— THE END —</span>
      </div>
    </div>

    <!-- 发布 FAB（管理员） -->
    <div v-if="userStore.isAdmin" class="add-fab" @click="dialogVisible = true" title="发随笔">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.4" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>
    </div>

    <!-- 发布对话框 -->
    <el-dialog v-model="dialogVisible" title="✍️ 发随笔" width="480px" destroy-on-close :close-on-click-modal="false" center>
      <div class="dialog-body">
        <textarea v-model="essayContent" placeholder="记录此刻的想法..." maxlength="1000" class="dialog-textarea" rows="5"></textarea>
        <MediaEditor v-model="essayMedia" class="dialog-media" />
        <div class="dialog-meta">
          <el-select v-model="essayWeather" placeholder="天气" size="small" clearable class="meta-select">
            <el-option v-for="w in ['晴','多云','阴','雨','雪','风']" :key="w" :label="w" :value="w" />
          </el-select>
          <el-select v-model="essayMood" placeholder="心情" size="small" clearable class="meta-select">
            <el-option v-for="m in ['开心','难过','平静','兴奋','疲惫','期待']" :key="m" :label="m" :value="m" />
          </el-select>
          <el-input v-model="essayLocation" placeholder="位置" size="small" clearable class="meta-loc" maxlength="50" />
        </div>
        <div class="dialog-actions">
          <span class="char-count">{{ essayContent.length }}/1000</span>
          <el-button type="primary" round @click="submitEssay" :loading="posting" :disabled="!essayContent.trim()">发布</el-button>
        </div>
      </div>
    </el-dialog>

    <MiniProfileCard :userId="miniProfile.userId" :position="miniProfile.position" :show="miniProfile.show" @close="miniProfile.show = false" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { essayApi, commentApi } from '../../api/modules'
import { usePageBackground } from '../../composables/usePageBackground'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { requireLogin } from '../../composables/useAuth'
import MiniProfileCard from '../../components/MiniProfileCard.vue'
import MediaEditor from '../../admin/MediaEditor.vue'
import PageBg from '../../components/PageBg.vue'

const userStore = useUserStore()

const essayList = ref([])
const expandedEssayId = ref(null)
const essayComments = ref([])
const commentText = ref({})
const pagination = ref({ current: 1, size: 10 })
const total = ref(0)
const dialogVisible = ref(false)
const essayContent = ref('')
const essayMedia = ref('')
const essayWeather = ref('')
const essayMood = ref('')
const essayLocation = ref('')
const posting = ref(false)
const bannerImage = usePageBackground('essayHero')
const contentBg = usePageBackground('essayContent')

const miniProfile = reactive({ show: false, userId: null, position: { x: 0, y: 0 } })
function showMiniProfile(c, event) {
  if (!c.userId) return
  const rect = event.target.getBoundingClientRect()
  miniProfile.userId = c.userId
  miniProfile.position = { x: rect.left + rect.width / 2, y: rect.top }
  miniProfile.show = true
}

onMounted(() => fetchEssays())

function parseMedia(json) {
  if (!json) return []
  try { const a = JSON.parse(json); return Array.isArray(a) ? a.filter(m => m && m.url) : [] } catch { return [] }
}

async function fetchEssays(reset = false) {
  try {
    const data = await essayApi.list({ page: pagination.value.current, size: 10 })
    if (data && data.records) {
      data.records.forEach((c) => {
        let html = c.content || ''
        html = html.replace(/\n{2,}/g, '<div style="height:10px"></div>').replace(/\n/g, '<br/>')
        c.contentHtml = html
        c.mediaList = parseMedia(c.media)
        c.imageList = c.mediaList.filter(m => m.type === 'image').map(m => m.url)
        if (c.weather) c.weather = getWeatherEmoji(c.weather)
        if (c.mood) c.mood = getMoodEmoji(c.mood)
      })
      if (reset) { pagination.value.current = 1; essayList.value = []; expandedEssayId.value = null }
      essayList.value = essayList.value.concat(data.records)
      total.value = data.total
      fetchCommentCounts()
    }
  } catch (e) { /* silent */ }
}

async function fetchCommentCounts() {
  try {
    const data = await commentApi.list({ type: 'essay', page: 1, size: 500 })
    const records = data?.records || (Array.isArray(data) ? data : [])
    const counts = {}
    records.forEach(c => { const sid = c.sourceId || c.articleId; if (sid) counts[sid] = (counts[sid] || 0) + 1 })
    essayList.value.forEach(e => { e._cc = counts[e.id] || 0 })
  } catch (e) { /* silent */ }
}

function stripEmoji(s) { return s ? s.replace(/^[\u{1F000}-\u{1FFFF}]\S*\s*/u, '') : s }
function getWeatherEmoji(w) {
  const clean = stripEmoji(w)
  const map = { '晴': '☀️ 晴', '多云': '⛅ 多云', '阴': '☁️ 阴', '雨': '🌧️ 雨', '雪': '❄️ 雪', '风': '💨 风' }
  return map[clean] || clean
}
function getMoodEmoji(m) {
  const clean = stripEmoji(m)
  const map = { '开心': '😊 开心', '难过': '😢 难过', '平静': '😌 平静', '兴奋': '🤩 兴奋', '疲惫': '😫 疲惫', '期待': '✨ 期待' }
  return map[clean] || clean
}

function loadMore() { if (total.value > essayList.value.length) { pagination.value.current++; fetchEssays() } }
function toggleComment(essay) {
  if (expandedEssayId.value === essay.id) { expandedEssayId.value = null; essayComments.value = [] }
  else { expandedEssayId.value = essay.id; fetchComments(essay.id) }
}
async function fetchComments(sourceId) {
  try {
    const data = await commentApi.list({ sourceId, type: 'essay' })
    essayComments.value = data?.records || data || []
    if (expandedEssayId.value) {
      const target = essayList.value.find(e => e.id === expandedEssayId.value)
      if (target) target._cc = essayComments.value.length
    }
  } catch (e) { essayComments.value = [] }
}
async function submitComment(essay) {
  const text = (commentText.value[essay.id] || '').trim()
  if (!text) return
  if (!requireLogin()) return
  try {
    await commentApi.create({ content: text, sourceId: essay.id, type: 'essay' })
    commentText.value[essay.id] = ''
    fetchComments(essay.id)
  } catch (e) { ElMessage.error('评论失败') }
}
async function submitEssay() {
  if (!essayContent.value.trim()) return
  posting.value = true
  try {
    await essayApi.create({
      content: essayContent.value,
      media: essayMedia.value || null,
      weather: essayWeather.value || null,
      mood: essayMood.value || null,
      location: essayLocation.value || null
    })
    ElMessage.success('发布成功')
    essayContent.value = ''; essayMedia.value = ''; essayWeather.value = ''; essayMood.value = ''; essayLocation.value = ''
    dialogVisible.value = false
    await fetchEssays(true)
  } catch (e) { ElMessage.error('发布失败') } finally { posting.value = false }
}
async function handleDelete(id) {
  if (!requireLogin()) return
  try {
    await ElMessageBox.confirm('确认删除这条随笔？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning', center: true })
    await essayApi.delete(id)
    fetchEssays(true)
  } catch (e) { /* cancelled */ }
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
.hero-banner { position: relative; height: 36vh; min-height: 240px; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.bg-image { position: absolute; inset: 0; background-size: cover; background-position: center; }
.bg-overlay { position: absolute; inset: 0; background: linear-gradient(135deg, rgba(67,160,71,0.42), rgba(255,183,77,0.28)); }
.hero-info { position: relative; z-index: 2; text-align: center; color: #fff; }
.hero-info h1 { font-family: var(--trendy-font); font-size: 44px; font-weight: 700; margin: 0 0 10px; letter-spacing: 8px; text-shadow: 0 4px 20px rgba(0,0,0,0.3); }
.hero-info p { font-size: 16px; opacity: 0.95; letter-spacing: 2px; }
.hero-wave { position: absolute; bottom: -2px; left: 0; width: 100%; height: 56px; background: var(--background); border-radius: 50% 50% 0 0 / 100% 100% 0 0; z-index: 3; }

/* ===== 动态流 ===== */
.moments-container { position: relative; max-width: 700px; margin: 0 auto; padding: 10px 16px 70px; overflow: hidden; }
.moments-list { position: relative; z-index: 1; display: flex; flex-direction: column; gap: 24px; }

.moment-card { background: rgba(255,255,255,0.85); backdrop-filter: blur(16px); border-radius: var(--card-radius); padding: 22px 24px; box-shadow: var(--card-shadow); border: var(--card-border); transition: all 0.35s ease; }
.moment-card:hover { transform: translateY(-3px); box-shadow: var(--card-shadow-hover); }

.moment-head { display: flex; align-items: center; gap: 14px; margin-bottom: 14px; }
.moment-avatar { flex-shrink: 0; border: 2px solid #fff; box-shadow: 0 3px 10px rgba(0,0,0,0.12); transition: transform 0.3s; }
.moment-card:hover .moment-avatar { transform: scale(1.06); }
.moment-head-info { display: flex; flex-direction: column; gap: 2px; flex: 1; min-width: 0; }
.moment-username { font-family: var(--trendy-font); font-size: 18px; font-weight: 700; color: #2e5a2e; letter-spacing: 0.5px; }
.moment-time { font-size: 12px; color: #a0b0a0; }
.moment-loc { color: #b0c0b0; }
.moment-delete { cursor: pointer; font-size: 16px; opacity: 0.4; transition: all 0.25s; padding: 4px; }
.moment-delete:hover { opacity: 1; transform: scale(1.2); }

.moment-text { font-size: 16px; line-height: 1.9; color: #2a3a2a; margin-bottom: 14px; word-break: break-word; letter-spacing: 0.3px; }

/* ===== 九宫格媒体 ===== */
.moment-media { margin-bottom: 14px; }
.mgrid { display: grid; gap: 5px; border-radius: 16px; overflow: hidden; }
.mgrid-1 { grid-template-columns: 1fr; max-width: 78%; }
.mgrid-1 .mgrid-img, .mgrid-1 .mgrid-video { max-height: 340px; }
.mgrid-2, .mgrid-4 { grid-template-columns: 1fr 1fr; max-width: 82%; }
.mgrid-3, .mgrid-5, .mgrid-6, .mgrid-7, .mgrid-8, .mgrid-9 { grid-template-columns: repeat(3, 1fr); }
.mgrid-item { position: relative; overflow: hidden; }
.mgrid-2 .mgrid-img, .mgrid-4 .mgrid-img { height: 200px; }
.mgrid-3 .mgrid-img, .mgrid-5 .mgrid-img, .mgrid-6 .mgrid-img, .mgrid-7 .mgrid-img, .mgrid-8 .mgrid-img, .mgrid-9 .mgrid-img { height: 130px; }
.mgrid-img { width: 100%; height: 100%; object-fit: cover; cursor: pointer; transition: transform 0.35s; }
.mgrid-img:hover { transform: scale(1.06); }
.mgrid-video { width: 100%; max-height: 340px; background: #000; border-radius: 8px; }

/* ===== 标签 ===== */
.moment-tags { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 14px; }
.moment-tag { font-size: 13px; font-weight: 600; padding: 5px 14px; border-radius: 16px; background: var(--nature-green-pale); color: var(--nature-green-dark); border: 1px solid rgba(129,199,132,0.35); }
.moment-tag.mood { background: #fff3e0; color: #e67e22; border-color: #ffe0b2; }

/* ===== 操作 ===== */
.moment-actions { display: flex; gap: 16px; }
.action-btn { display: inline-flex; align-items: center; gap: 6px; padding: 7px 16px; border: none; border-radius: 22px; background: var(--nature-green-pale); color: var(--nature-green-dark); font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.3s; }
.action-btn:hover { transform: translateY(-2px); background: var(--nature-green-light); color: #fff; }

/* ===== 评论 ===== */
.expand-enter-active, .expand-leave-active { transition: all 0.3s ease; max-height: 500px; overflow: hidden; }
.expand-enter-from, .expand-leave-to { opacity: 0; max-height: 0; }
.moment-comments { margin-top: 14px; background: rgba(232,245,233,0.5); border-radius: 16px; padding: 14px 16px; }
.comments-list { max-height: 260px; overflow-y: auto; }
.comment-item { display: flex; gap: 10px; padding: 8px 0; }
.c-avatar { flex-shrink: 0; cursor: pointer; transition: all 0.2s; }
.c-avatar:hover { transform: scale(1.15); }
.c-body { flex: 1; min-width: 0; }
.c-top { display: flex; align-items: center; gap: 8px; }
.c-nick { font-size: 13px; font-weight: 700; color: #2e5a2e; }
.c-time { font-size: 11px; color: #a0b0a0; }
.c-text { font-size: 14px; color: #3a4a3a; line-height: 1.5; word-break: break-word; }
.comment-empty { font-size: 13px; color: #b0c0b0; text-align: center; padding: 14px 0; }
.comment-input-bar { margin-top: 10px; }
.c-input :deep(.el-input__wrapper) { border-radius: 20px; background: #fff; }

/* ===== FAB ===== */
.add-fab { position: fixed; bottom: 60px; right: 30px; width: 58px; height: 58px; border-radius: 50%; background: var(--nature-gradient); box-shadow: 0 6px 20px rgba(76,175,80,0.45); display: flex; align-items: center; justify-content: center; cursor: pointer; z-index: 50; transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1); }
.add-fab:hover { transform: scale(1.12) rotate(90deg); box-shadow: 0 10px 28px rgba(76,175,80,0.6); }

/* ===== 分页 ===== */
.pagination-wrap { text-align: center; padding: 36px 0 20px; position: relative; z-index: 1; }
.load-more-btn { padding: 11px 34px; border: none; border-radius: 28px; background: var(--nature-gradient); color: #fff; font-size: 15px; font-weight: 600; font-family: var(--trendy-font); cursor: pointer; box-shadow: 0 6px 18px rgba(76,175,80,0.35); transition: all 0.3s; }
.load-more-btn:hover { transform: translateY(-3px); box-shadow: 0 10px 26px rgba(76,175,80,0.5); }
.pagination-end { color: #b0c0b0; font-size: 14px; letter-spacing: 2px; }

/* ===== 对话框 ===== */
.dialog-textarea { width: 100%; border: 2px solid #e0eee0; border-radius: 16px; padding: 14px; font-size: 15px; resize: vertical; outline: none; background: #fbfdfb; color: var(--fontColor); box-sizing: border-box; font-family: var(--globalFont); transition: border-color 0.3s; }
.dialog-textarea:focus { border-color: var(--nature-green-light); }
.dialog-media { margin-top: 14px; }
.dialog-meta { display: flex; gap: 10px; margin-top: 14px; }
.meta-select { width: 110px; }
.meta-loc { flex: 1; }
.dialog-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 16px; }
.char-count { font-size: 13px; color: #a0b0a0; }

.empty-wrap { position: relative; z-index: 1; padding: 60px 0; }

@media screen and (max-width: 600px) {
  .moments-container { padding: 0 10px 50px; }
  .hero-info h1 { font-size: 28px; letter-spacing: 4px; }
  .moment-card { padding: 16px 18px; }
  .moment-username { font-size: 16px; }
  .moment-text { font-size: 15px; }
  .mgrid-1 { max-width: 100%; }
  .mgrid-2, .mgrid-4 { max-width: 100%; }
  .add-fab { bottom: 26px; right: 16px; width: 50px; height: 50px; }
  .dialog-meta { flex-wrap: wrap; }
}
</style>

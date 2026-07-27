<template>
  <div class="essay-page">
    <!-- Hero Banner -->
    <div class="hero-banner">
      <div class="bg-image" :style="{ backgroundImage: `url(${bannerImage})` }"></div>
      <div class="bg-overlay"></div>
      <div class="hero-info">
        <h1>随笔随心</h1>
        <p class="hero-subtitle">随心所悦，记录生活点滴 ✍️</p>
      </div>
    </div>

    <!-- Moments-style feed -->
    <div class="moments-container">
      <div class="bg-image content-bg" :style="{ backgroundImage: `url(${contentBg})` }" v-if="contentBg" />
      <div class="moments-list" v-if="essayList.length > 0">
        <div
          v-for="essay in essayList"
          :key="essay.id"
          class="moment-item"
        >
          <!-- Avatar — square with rounded corners -->
          <div class="moment-avatar">
            <el-avatar
              class="avatar-square"
              :size="53"
              :src="essay.avatar"
            >{{ (essay.nickname || essay.username || 'L').charAt(0) }}</el-avatar>
          </div>

          <!-- Content -->
          <div class="moment-body">
            <div class="moment-header">
              <span class="moment-username">{{ essay.nickname || essay.username || 'Lune' }}</span>
              <span class="moment-time">{{ formatRelative(essay.createTime) }}</span>
            </div>

            <div class="moment-text" v-html="essay.content"></div>

            <!-- Tags -->
            <div class="moment-tags" v-if="essay.weather || essay.mood">
              <span v-if="essay.weather" class="moment-tag">{{ essay.weather }}</span>
              <span v-if="essay.mood" class="moment-tag mood">{{ essay.mood }}</span>
            </div>

            <!-- Actions -->
            <div class="moment-actions">
              <div class="moment-comment-toggle" @click.stop="toggleComment(essay)">
                <svg viewBox="0 0 1024 1024" width="18" height="18" style="vertical-align: -3px; margin-right: 4px; pointer-events: none;">
                  <path d="M512 82.464153c-244.63772 0-442.955484 171.85302-442.955484 383.832945 0 125.44199 69.434395 236.8258 176.814008 306.863946-0.502443 56.870242 0.00307 168.373779 0.00307 168.373779s107.36938-70.083172 159.796426-102.527095c34.066887 7.272637 69.684082 11.135618 106.34198 11.135618 244.63772 0 442.955484-171.85302 442.955484-383.846248C954.955484 254.318196 756.63772 82.464153 512 82.464153z" fill="#999"/>
                </svg>
                <span style="pointer-events: none;">{{ essay._cc || 0 }}</span>
              </div>
              <span
                v-if="userStore.isLoggedIn && userStore.user?.userId === essay.userId"
                class="moment-delete"
                @click="handleDelete(essay.id)"
              >
                <svg viewBox="0 0 1024 1024" width="15" height="15" style="vertical-align: -2px;" fill="#bbb">
                  <path d="M921.1392 155.392h-270.592v-48.2816c0-22.7328-18.432-41.1648-41.1648-41.1648H426.3424a41.1648 41.1648 0 0 0-41.1648 41.1648v48.2816H110.6432c-14.1312 0-25.6 11.4688-25.6 25.6s11.4688 25.6 25.6 25.6h810.496c14.1312 0 25.6-11.4688 25.6-25.6s-11.4688-25.6-25.6-25.6zM170.8032 260.0448v592.8448c0 50.8928 41.2672 92.16 92.16 92.16h500.6848c50.8928 0 92.16-41.2672 92.16-92.16V260.0448H170.8032z m249.1392 462.7968c0 14.1312-11.4688 25.6-25.6 25.6s-25.6-11.4688-25.6-25.6V443.0848c0-14.1312 11.4688-25.6 25.6-25.6s25.6 11.4688 25.6 25.6v279.7568z m243.1488 0c0 14.1312-11.4688 25.6-25.6 25.6s-25.6-11.4688-25.6-25.6V443.0848c0-14.1312 11.4688-25.6 25.6-25.6s25.6 11.4688 25.6 25.6v279.7568z"/>
                </svg>
              </span>
            </div>

            <!-- Inline comments (Douyin style) -->
            <div v-if="expandedEssayId === essay.id" class="moment-comments">
              <div class="comments-list">
                <div v-if="!essayComments.length" class="comment-empty">暂无评论，来说点什么吧</div>
                <div v-for="c in essayComments" :key="c.id" class="comment-item">
                  <el-avatar :size="32" :src="c.avatar" class="c-avatar" @click.stop="showMiniProfile(c, $event)">
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
                <el-input
                  v-model="commentText[essay.id]"
                  placeholder="说点什么..."
                  size="small"
                  class="c-input"
                  @keyup.enter="submitComment(essay)"
                >
                  <template #suffix>
                    <el-button type="primary" size="small" round
                      :disabled="!(commentText[essay.id] || '').trim()"
                      @click="submitComment(essay)">发送</el-button>
                  </template>
                </el-input>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else style="text-align:center; padding:100px 0;">
        <el-empty description="还没有随笔，来写第一条吧 ✨" />
      </div>

      <!-- Pagination -->
      <div class="pagination-wrap" v-if="total > essayList.length">
        <div class="load-more-btn" @click="loadMore">查看更多</div>
      </div>
      <div v-else-if="essayList.length > 0" class="pagination-wrap">
        <span class="pagination-end">— THE END —</span>
      </div>
    </div>

    <!-- FAB Add button (admin) -->
    <div v-if="userStore.isAdmin" class="add-fab" @click="dialogVisible = true">
      <svg width="26" height="26" viewBox="0 0 1024 1024">
        <path d="M989.866667 512c0 263.918933-213.947733 477.866667-477.866667 477.866667S34.133333 775.918933 34.133333 512 248.081067 34.133333 512 34.133333s477.866667 213.947733 477.866667 477.866667z" fill="#FF7744"/>
        <path d="M512 221.866667A51.2 51.2 0 0 1 563.2 273.066667v187.733333H750.933333a51.2 51.2 0 0 1 0 102.4h-187.733333V750.933333a51.2 51.2 0 0 1-102.4 0v-187.733333H273.066667a51.2 51.2 0 0 1 0-102.4h187.733333V273.066667A51.2 51.2 0 0 1 512 221.866667z" fill="#FFFFFF"/>
      </svg>
    </div>

    <!-- Add Dialog -->
    <el-dialog v-model="dialogVisible" title="发随笔" width="440px" destroy-on-close :close-on-click-modal="false" center>
      <div class="dialog-body">
        <textarea
          v-model="essayContent"
          placeholder="记录此刻的想法..."
          maxlength="1000"
          class="dialog-textarea"
          rows="6"
        ></textarea>
        <div class="dialog-actions">
          <span class="char-count">{{ essayContent.length }}/1000</span>
          <el-button type="primary" @click="submitEssay" :loading="posting">发布</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
  <MiniProfileCard
    :userId="miniProfile.userId"
    :position="miniProfile.position"
    :show="miniProfile.show"
    @close="miniProfile.show = false"
  />
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { essayApi, commentApi } from '../../api/modules'
import { usePageBackground } from '../../composables/usePageBackground'
import { useUserStore } from '../../stores/user'
import { useAppStore } from '../../stores/app'
import { ElMessage, ElMessageBox } from 'element-plus'
import { requireLogin } from '../../composables/useAuth'
import MiniProfileCard from '../../components/MiniProfileCard.vue'

const userStore = useUserStore()
const appStore = useAppStore()

const essayList = ref([])
const expandedEssayId = ref(null)
const essayComments = ref([])
const commentText = ref({})
const pagination = ref({ current: 1, size: 10 })
const total = ref(0)
const dialogVisible = ref(false)
const essayContent = ref('')
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

onMounted(() => {
  fetchEssays()
})

async function fetchEssays(reset = false) {
  try {
    const data = await essayApi.list({ page: pagination.value.current, size: 10 })
    if (data && data.records) {
      data.records.forEach((c) => {
        if (c.content) {
          c.content = c.content.replace(/\n{2,}/g, '<div style="height:10px"></div>')
          c.content = c.content.replace(/\n/g, '<br/>')
        }
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
    if (!records.length) return
    const counts = {}
    records.forEach(c => {
      const sid = c.sourceId || c.articleId
      if (sid) counts[sid] = (counts[sid] || 0) + 1
    })
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

function loadMore() {
  if (total.value > essayList.value.length) { pagination.value.current++; fetchEssays() }
}
function toggleComment(essay) {
  if (expandedEssayId.value === essay.id) { expandedEssayId.value = null; essayComments.value = [] }
  else { expandedEssayId.value = essay.id; fetchComments(essay.id) }
}
async function fetchComments(sourceId) {
  try { const data = await commentApi.list({ sourceId: sourceId, type: 'essay' }); essayComments.value = data?.records || data || []
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
    await essayApi.create({ content: essayContent.value })
    ElMessage.success('发布成功')
    essayContent.value = ''; dialogVisible.value = false
    await fetchEssays(true)
  } catch (e) { ElMessage.error('发布失败') }
  finally { posting.value = false }
}
async function handleDelete(id) {
  if (!requireLogin()) return
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning', center: true })
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
/* ====== Hero Banner ====== */
.hero-banner {
  position: relative;
  height: 30vh;
  min-height: 180px;
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
  margin: 0 0 10px;
  letter-spacing: 6px;
  font-style: italic;
}
.hero-info p { font-size: 15px; opacity: 0.85; font-style: italic; }

/* ====== Moments Feed ====== */
.moments-container {
  max-width: 680px;
  margin: 0 auto;
  padding: 0 12px 60px;
}

.moment-item {
  display: flex;
  padding: 22px 0;
  border-bottom: 1px solid #eee;
}

/* Square avatar with smooth rounded corners */
.moment-avatar {
  flex-shrink: 0;
  margin-right: 14px;
}
.avatar-square {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
  display: block;
}

.moment-body {
  flex: 1;
  min-width: 0;
}

.moment-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 8px;
}
.moment-username {
  font-family: 'Noto Sans SC', var(--globalFont), sans-serif;
  font-size: 19px;
  font-weight: 700;
  color: #3d5a99;
  letter-spacing: 1.5px;
  font-style: italic;
}
.moment-time {
  font-size: 13px;
  color: #c0c0c0;
  font-weight: 400;
}

.moment-text {
  font-family: 'Noto Sans SC', var(--globalFont), sans-serif;
  font-size: 17px;
  font-weight: 600;
  line-height: 2;
  letter-spacing: 0.5px;
  color: #1a1a1a;
  margin-bottom: 14px;
  word-break: break-word;
  font-style: italic;
}

.moment-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 10px;
  margin-bottom: 16px;
  margin-top: 4px;
}
.moment-tag {
  font-size: 14px;
  font-weight: 600;
  padding: 5px 14px;
  border-radius: 14px;
  background: #f2f2f2;
  color: #777;
  font-family: 'Noto Sans SC', var(--globalFont), sans-serif;
}
.moment-tag.mood { background: #fff3e0; color: #e67e22; }

.moment-actions {
  display: flex;
  align-items: center;
  gap: 18px;
  font-size: 14px;
}
.moment-comment-toggle {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #999;
  font-size: 14px;
  transition: color 0.2s;
}
.moment-comment-toggle:hover { color: #576b95; }
.moment-delete { cursor: pointer; opacity: 0.5; transition: opacity 0.2s; }
.moment-delete:hover { opacity: 1; }

/* ====== Comments ====== */
.moment-comments { margin-top: 10px; background: #fafafa; border-radius: 12px; padding: 12px 14px; }
.comments-list { max-height: 240px; overflow-y: auto; }
.comment-item { display: flex; gap: 10px; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.comment-item:last-child { border-bottom: none; }
.c-avatar { flex-shrink: 0; cursor: pointer; transition: all 0.2s ease; }
.c-avatar:hover { transform: scale(1.15); box-shadow: 0 0 0 3px rgba(76,175,80,0.25); }
.c-body { flex: 1; min-width: 0; }
.c-top { display: flex; align-items: center; gap: 8px; margin-bottom: 2px; }
.c-nick { font-size: 13px; font-weight: 600; color: #576b95; }
.c-time { font-size: 11px; color: #bbb; }
.c-text { font-size: 14px; color: #333; line-height: 1.5; word-break: break-word; }
.comment-empty { font-size: 13px; color: #ccc; text-align: center; padding: 16px 0; }
.comment-input-bar { margin-top: 10px; }
.c-input :deep(.el-input__wrapper) { border-radius: 20px; background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }

/* ====== FAB ====== */
.add-fab {
  position: fixed;
  bottom: 56px;
  right: 28px;
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 3px 16px rgba(0,0,0,0.14);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 50;
  transition: all 0.25s;
}
.add-fab:hover { transform: scale(1.12); box-shadow: 0 6px 24px rgba(0,0,0,0.18); }
.add-fab:active { transform: scale(0.93); }

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
}
.load-more-btn:hover { background: #f7f7f7; border-color: #bbb; }
.pagination-end { color: #ddd; font-size: 14px; letter-spacing: 2px; }

/* ====== Dialog ====== */
.dialog-textarea {
  width: 100%;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 14px;
  font-size: 15px;
  font-weight: 500;
  resize: vertical;
  outline: none;
  background: #fafafa;
  color: var(--fontColor);
  box-sizing: border-box;
  font-family: 'Noto Sans SC', var(--globalFont), sans-serif;
}
.dialog-textarea:focus { border-color: #ff7744; background: #fff; }
.dialog-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }
.char-count { font-size: 13px; color: var(--greyFont); }

@media screen and (max-width: 600px) {
  .moments-container { padding: 0 6px 40px; }
  .hero-info h1 { font-size: 30px; letter-spacing: 4px; }
  .hero-info p { font-size: 15px; }
  .moment-item { padding: 16px 0; }
  .avatar-square { width: 40px; height: 40px; border-radius: 6px; }
  .moment-username { font-size: 17px; }
  .moment-text { font-size: 15px; line-height: 1.85; }
  .add-fab { bottom: 24px; right: 14px; width: 48px; height: 48px; }
}
.moments-container { position: relative; overflow: hidden; }
.moment-feed { position: relative; overflow: hidden; }
.bg-image.content-bg { position: absolute; inset: 0; background-size: cover; background-position: center; z-index: 0; opacity: 0.12; }
</style>

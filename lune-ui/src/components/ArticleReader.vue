<template>
  <Teleport to="body">
    <Transition name="reader">
      <div
        v-if="visible"
        class="reader-overlay"
        @click.self="close"
      >
        <div class="reader-layout" :class="{ 'has-comments': showComments }">
          <!-- ============ Main Card ============ -->
          <div class="reader-card" ref="cardRef">
            <button class="reader-close" @click="close" title="关闭">&times;</button>

            <!-- Loading -->
            <div v-if="loading" class="reader-loading myCenter">
              <el-icon class="is-loading" :size="36"><Loading /></el-icon>
            </div>

            <!-- Error -->
            <div v-else-if="error" class="reader-error myCenter">
              <el-empty description="文章加载失败" :image-size="60" />
              <el-button type="primary" @click="fetchArticle">重新加载</el-button>
            </div>

            <!-- Article -->
            <div v-else-if="article" class="paper-sheet">
              <div class="paper-holes">
                <span v-for="i in 3" :key="i" class="paper-hole" />
              </div>
              <div class="paper-header">
                <h2 class="paper-title">{{ article.title }}</h2>
              </div>
              <div class="paper-content" v-html="article.content" />
              <div class="paper-signature">
                <span class="sig-date">{{ formatDate(article.createTime) }}</span>
                <span class="sig-author">{{ authorName }}</span>
              </div>
            </div>
          </div>

          <!-- Side actions: beside card when no comments -->
          <div v-if="!showComments" class="side-actions">
            <div class="side-line" />
            <button class="side-btn" @click.stop="toggleLike" :class="{ liked: isLiked }">
              <svg viewBox="0 0 24 24" width="22" height="22" class="side-icon">
                <path v-if="!isLiked" d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="none" stroke="currentColor" stroke-width="1.5"/>
                <path v-else d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="#ff4757" stroke="#ff4757" stroke-width="1.5"/>
              </svg>
              <span class="side-count">{{ likeCount }}</span>
            </button>
            <div class="side-line" />
            <button class="side-btn" @click.stop="toggleComments">
              <svg viewBox="0 0 24 24" width="22" height="22" class="side-icon">
                <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z" fill="none" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <span class="side-count">{{ commentTotal }}</span>
            </button>
            <div class="side-line" />
          </div>

          <!-- ============ Comments open: connector + panel ============ -->
          <div v-if="showComments" class="comment-connector">
            <div class="connector-dot connector-dot-top" />
            <div class="connector-line" />
            <div class="connector-dot connector-dot-bot" />
          </div>

          <div v-if="showComments" class="comment-panel-col">
            <Transition name="comment-panel">
              <div v-if="showComments" class="comment-panel">
                <div class="comment-panel-header">
                  <h3>评论 ({{ commentTotal }})</h3>
                  <button class="comment-panel-close" @click="showComments = false">&times;</button>
                  </div>

              <!-- Comment list -->
              <div class="comment-list" ref="commentListRef">
                <div v-if="commentLoading" class="myCenter" style="padding:40px">
                  <el-icon class="is-loading" :size="24"><Loading /></el-icon>
                </div>

                <div v-else-if="comments.length === 0" style="text-align:center;padding:40px;color:#999">
                  暂无评论，来说点什么吧
                </div>

                <div v-else v-for="item in comments" :key="item.id" class="comment-item">
                  <el-avatar :size="36" :src="item.avatar" class="comment-avatar" @click.stop="showMiniProfile(item, $event)">
                    {{ (item.nickname || item.username || '匿').charAt(0) }}
                  </el-avatar>
                  <div class="comment-body">
                    <div class="comment-top">
                      <span class="comment-nick">{{ item.nickname || item.username || '匿名' }}</span>
                      <span class="comment-time">{{ timeAgo(item.createTime) }}</span>
                    </div>
                    <div class="comment-text">{{ item.content }}</div>
                    <div class="comment-actions-row">
                      <button class="c-action" @click="startReply(item)">
                        <svg viewBox="0 0 24 24" width="14" height="14"><path d="M10 9V5l-7 7 7 7v-4.1c5 0 8.5 1.6 11 5.1-1-5-4-10-11-11z" fill="currentColor"/></svg>
                        回复
                      </button>
                      <button class="c-action" @click="likeComment(item)">
                        <svg viewBox="0 0 24 24" width="14" height="14"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="currentColor"/></svg>
                        {{ item._likes || 0 }}
                      </button>
                    </div>

                    <!-- Replies -->
                    <div v-if="item.children && item.children.length" class="replies-wrap">
                      <div v-for="reply in item.children" :key="reply.id" class="reply-item">
                        <span class="reply-nick">{{ reply.nickname || reply.username || '匿名' }}</span>
                        <span v-if="reply.replyToUsername" class="reply-to"> 回复 @{{ reply.replyToUsername }}</span>
                        <span class="reply-colon">：</span>
                        <span class="reply-text">{{ reply.content }}</span>
                        <span class="reply-time-inline">{{ timeAgo(reply.createTime) }}</span>
                        <button class="reply-reply-btn" @click="startReply(item, reply)">回复</button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Comment input bar (Douyin-style) -->
              <div class="comment-input-bar">
                <el-input
                  v-model="commentText"
                  :placeholder="replyTarget ? '回复 @' + replyTarget.username + '...' : '说点什么...'"
                  size="large"
                  class="comment-input"
                  @keyup.enter="submitComment"
                >
                  <template #suffix>
                    <el-button
                      type="primary"
                      :disabled="!commentText.trim()"
                      :loading="submitting"
                      @click="submitComment"
                      size="small"
                      round
                    >
                      发送
                    </el-button>
                  </template>
                </el-input>
                <span v-if="replyTarget" class="reply-cancel" @click="cancelReply">取消回复</span>
              </div>
            </div>
          </Transition>

          <!-- Side actions below comment panel -->
          <div class="side-actions side-actions-below">
            <div class="side-line" />
            <button class="side-btn" @click.stop="toggleLike" :class="{ liked: isLiked }">
              <svg viewBox="0 0 24 24" width="22" height="22" class="side-icon">
                <path v-if="!isLiked" d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="none" stroke="currentColor" stroke-width="1.5"/>
                <path v-else d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="#ff4757" stroke="#ff4757" stroke-width="1.5"/>
              </svg>
              <span class="side-count">{{ likeCount }}</span>
            </button>
            <div class="side-line" />
            <button class="side-btn" @click.stop="toggleComments">
              <svg viewBox="0 0 24 24" width="22" height="22" class="side-icon">
                <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z" fill="none" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <span class="side-count">{{ commentTotal }}</span>
            </button>
            <div class="side-line" />
          </div>
        </div>
      </div>
    </div>
  </Transition>
  </Teleport>
  <MiniProfileCard
    :userId="miniProfile.userId"
    :position="miniProfile.position"
    :show="miniProfile.show"
    @close="miniProfile.show = false"
  />
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick, onUnmounted } from 'vue'
import { Loading, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { requireLogin } from '../composables/useAuth'
import request from '../api/request'
import { articleApi, commentApi } from '../api/modules'
import { useUserStore } from '../stores/user'
import { useAppStore } from '../stores/app'
import MiniProfileCard from './MiniProfileCard.vue'

const props = defineProps({
  articleId: { type: Number, required: true }
})

const emit = defineEmits(['close', 'liked', 'commented'])

const appStore = useAppStore()
const userStore = useUserStore()

// --- Article state ---
const article = ref(null)
const loading = ref(false)
const error = ref(false)
const visible = ref(false)
const cardRef = ref(null)

// --- Like state ---
const isLiked = ref(false)
const likeCount = ref(0)

// --- Comment state ---
const showComments = ref(false)
const comments = ref([])
const commentTotal = ref(0)
const commentLoading = ref(false)
const commentText = ref('')
const submitting = ref(false)
const replyTarget = ref(null)
const commentListRef = ref(null)

const miniProfile = reactive({ show: false, userId: null, position: { x: 0, y: 0 } })
function showMiniProfile(item, event) {
  if (!item.userId) return
  const rect = event.target.getBoundingClientRect()
  miniProfile.userId = item.userId
  miniProfile.position = { x: rect.left + rect.width / 2, y: rect.top }
  miniProfile.show = true
}

const authorName = computed(() => appStore.webInfo.webName || 'Lune')

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', { year:'numeric', month:'2-digit', day:'2-digit' })
}

function timeAgo(d) {
  if (!d) return ''
  const diff = Date.now() - new Date(d).getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff/60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff/3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff/86400000) + '天前'
  return formatDate(d)
}

function close() {
  visible.value = false
  showComments.value = false
  document.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
  setTimeout(() => emit('close'), 350)
}

async function fetchArticle() {
  if (!props.articleId) return
  loading.value = true; error.value = false; article.value = null
  try {
    const data = await articleApi.getById(props.articleId)
    if (data) {
      article.value = data
      likeCount.value = data.likeCount || 0
      restoreLike()
    } else error.value = true
  } catch (e) { error.value = true }
  finally { loading.value = false }
}

async function fetchComments() {
  if (!props.articleId) return
  commentLoading.value = true
  try {
    const data = await commentApi.list({ articleId: props.articleId, page: 1, size: 50 })
    if (data) {
      const flat = data.records || []
      const parents = flat.filter(c => !c.parentId || c.parentId === 0)
      const replies = flat.filter(c => c.parentId && c.parentId > 0)
      parents.forEach(p => {
        p.children = replies.filter(r => r.parentId === p.id)
      })
      comments.value = parents
      commentTotal.value = data.total || 0
    }
  } catch (e) { /* silent */ }
  finally { commentLoading.value = false }
}

function toggleLike() {
  if (!requireLogin()) return
  const delta = isLiked.value ? -1 : 1
  isLiked.value = !isLiked.value
  likeCount.value += delta
  const key = 'liked_' + props.articleId
  if (isLiked.value) localStorage.setItem(key, '1')
  else localStorage.removeItem(key)
  request.patch(`/articles/${props.articleId}/like?delta=${delta}`).catch(() => {})
  emit('liked', { articleId: props.articleId, likeCount: likeCount.value })
}
// Restore like state on load
function restoreLike() {
  const key = 'liked_' + props.articleId
  if (localStorage.getItem(key)) isLiked.value = true
}

function toggleComments() {
  showComments.value = !showComments.value
  if (showComments.value && comments.value.length === 0) fetchComments()
}

function startReply(parent, reply) {
  const target = reply || parent
  replyTarget.value = {
    id: parent.id,
    userId: target.userId,
    username: target.username || target.nickname || '匿名'
  }
  showComments.value = true
  if (comments.value.length === 0) fetchComments()
}

function cancelReply() { replyTarget.value = null }

async function submitComment() {
  const text = commentText.value.trim()
  if (!text) return
  const user = userStore.user
  if (!requireLogin()) return
  submitting.value = true
  try {
    const payload = {
      articleId: props.articleId,
      content: text
    }
    if (replyTarget.value) {
      payload.parentId = replyTarget.value.id
      payload.replyTo = replyTarget.value.userId
      payload.replyToUsername = replyTarget.value.username
    }
    await commentApi.create(payload)
    commentText.value = ''
    replyTarget.value = null
    ElMessage.success('评论成功')
    commentTotal.value++
    emit('commented')
    await fetchComments()
  } catch (e) { ElMessage.error('评论失败') }
  finally { submitting.value = false }
}

function likeComment(item) {
  item._likes = (item._likes || 0) + 1
}

// Escape key
function onKeydown(e) { if (e.key === 'Escape') close() }

// Watch articleId → auto open
watch(() => props.articleId, async (id) => {
  if (id) {
    document.removeEventListener('keydown', onKeydown)
    document.addEventListener('keydown', onKeydown)
    document.body.style.overflow = 'hidden'
    await fetchArticle()
    fetchComments()
    await nextTick()
    visible.value = true
  }
}, { immediate: true })

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})
</script>

<style scoped>
/* ============================
   Overlay
   ============================ */
.reader-overlay {
  position: fixed;
  inset: 0;
  z-index: 999;
  background: rgba(0,0,0,0.5);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

/* ============================
   Layout (card + optional comments)
   ============================ */
.reader-layout {
  display: flex;
  align-items: stretch;
  gap: 32px;
  max-width: 1400px;
  width: 100%;
  height: 75vh;
  max-height: 82vh;
  justify-content: center;
}
.reader-layout.has-comments { gap: 0; }

/* ============================
   Card
   ============================ */
.reader-card {
  position: relative;
  width: 100%;
  max-width: 860px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border-radius: 28px;
  overflow: visible;
}

/* When comments open, card and comment panel share equal width & height */
.reader-layout.has-comments .reader-card {
  max-width: none;
  flex: 1 1 0;
  min-width: 0;
}
.reader-layout.has-comments .comment-panel-col {
  flex: 1 1 0;
  min-width: 0;
}

/* ============================
   Close button (on card top-right)
   ============================ */
.reader-close {
  position: absolute;
  top: -14px;
  right: -14px;
  z-index: 20;
  background: #fff;
  border: 2px solid #e8e0d0;
  color: #8a7a6a;
  font-size: 22px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s;
  line-height: 1;
  padding: 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.reader-close:hover {
  background: #f56c6c;
  color: #fff;
  border-color: #f56c6c;
  transform: rotate(90deg);
}

/* ============================
   Paper Sheet
   ============================ */
.paper-sheet {
  background-color: #fdfaf4;
  background-image:
    repeating-linear-gradient(
      to bottom,
      transparent,
      transparent 37px,
      #e8e0d0 37px,
      #e8e0d0 38px
    );
  border-radius: 28px;
  padding: 56px 56px 40px 72px;
  position: relative;
  min-height: 560px;
  max-height: 75vh;
  overflow-y: auto;

  box-shadow:
    0 2px 4px rgba(0,0,0,0.06),
    0 6px 20px rgba(0,0,0,0.08),
    0 16px 48px rgba(0,0,0,0.12);

  border-left: 2px solid rgba(102, 187, 106, 0.35);
  font-family: var(--globalFont);
}

.paper-sheet::-webkit-scrollbar { width: 5px }
.paper-sheet::-webkit-scrollbar-track { background: transparent; margin: 24px 0 }
.paper-sheet::-webkit-scrollbar-thumb { background: #d0c8b8; border-radius: 1em }

/* Binder holes */
.paper-holes {
  position: absolute;
  top: 28px;
  left: 22px;
  display: flex;
  flex-direction: column;
  gap: 36px;
}
.paper-hole {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #f0ebe0, #d8d0c0);
  box-shadow: inset 0 1px 2px rgba(0,0,0,0.1);
}

/* ============================
   Paper Header
   ============================ */
.paper-header {
  text-align: center;
  margin-bottom: 28px;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(0,0,0,0.06);
}
.paper-title {
  font-size: 28px;
  font-weight: 700;
  color: #3a3228;
  margin: 0;
  line-height: 1.5;
  letter-spacing: 1.5px;
}

/* ============================
   Paper Content
   ============================ */
.paper-content {
  font-size: 19px;
  line-height: 38px; /* aligns with 38px ruled lines */
  color: #3a3228;
  word-break: break-word;
}
.paper-content :deep(p) { margin: 0 0 38px 0; text-indent: 2em; }
.paper-content :deep(img) { max-width: 100%; border-radius: 6px; margin: 20px 0; display: block; }
.paper-content :deep(pre) {
  background: rgba(0,0,0,0.03); border: 1px solid rgba(0,0,0,0.06);
  border-radius: 6px; padding: 18px 22px; overflow-x: auto;
  margin: 0 0 38px 0; line-height: 1.6; font-size: 16px;
}
.paper-content :deep(code) { font-size: 14px; font-family: 'JetBrains Mono','Consolas',monospace; }
.paper-content :deep(blockquote) {
  border-left: 3px solid #d4a574; padding: 10px 18px; margin: 0 0 38px 0;
  background: rgba(212,165,116,0.08); border-radius: 0 6px 6px 0; font-style: italic;
}
.paper-content :deep(h1), .paper-content :deep(h2),
.paper-content :deep(h3), .paper-content :deep(h4) {
  margin: 0 0 20px 0; line-height: 1.4; color: #2a2218;
}
.paper-content :deep(h2) { font-size: 23px; border-bottom: 1px solid rgba(0,0,0,0.06); padding-bottom: 10px; }
.paper-content :deep(h3) { font-size: 20px; }
.paper-content :deep(ul), .paper-content :deep(ol) { margin: 0 0 38px 0; padding-left: 2em; }
.paper-content :deep(li) { line-height: 38px; }
.paper-content :deep(a) { color: #8b6914; border-bottom: 1px dotted #c9a84c; text-decoration: none; }
.paper-content :deep(table) { width: 100%; border-collapse: collapse; margin: 0 0 36px 0; }
.paper-content :deep(th), .paper-content :deep(td) {
  border: 1px solid #ddd6c8; padding: 8px 12px; text-align: left; line-height: 1.8;
}
.paper-content :deep(th) { background: rgba(0,0,0,0.02); }

/* ============================
   Signature
   ============================ */
.paper-signature {
  margin-top: 52px; padding-top: 24px;
  text-align: right; display: flex; flex-direction: column; align-items: flex-end; gap: 8px;
}
.sig-date { font-size: 15px; color: #8a8070; letter-spacing: 0.5px; }
.sig-author {
  font-size: 17px; color: #5a5040; font-weight: 600; position: relative;
}
.sig-author::before {
  content: ''; display: inline-block; width: 48px; height: 1px;
  background: #c8b898; margin-right: 14px; vertical-align: middle;
}

/* ============================
   Comment Panel Column (panel + actions below)
   ============================ */
.comment-panel-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex-shrink: 0;
}

/* ============================
   Side Action Bar
   ============================ */
.side-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  z-index: 10;
  padding: 0 4px;
  flex-shrink: 0;
  align-self: center;
}
.side-actions-below {
  flex-direction: row;
  justify-content: center;
  gap: 20px;
  padding: 6px 0;
}
.side-actions-below .side-line { width: 1px; height: 18px; }
.side-line {
  width: 20px;
  height: 1.5px;
  background: rgba(255,255,255,0.5);
  border-radius: 1px;
}
.side-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
  color: rgba(255,255,255,0.9);
  cursor: pointer;
  padding: 10px 10px;
  border-radius: 14px;
  transition: all 0.2s;
  backdrop-filter: blur(4px);
}
.side-btn:hover {
  color: #fff;
  background: rgba(255,255,255,0.22);
  transform: scale(1.08);
  border-color: rgba(255,255,255,0.35);
}
.side-btn.liked .side-icon { color: #ff4757; }
.side-count { font-size: 12px; font-weight: 600; min-width: 18px; text-align: center; }

/* ============================
   Comment Connector (line between card & panel)
   ============================ */
.comment-connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0;
  width: 20px;
  flex-shrink: 0;
  z-index: 5;
}
.connector-line {
  width: 2px;
  flex: 1;
  min-height: 60px;
  background: linear-gradient(to bottom, transparent, rgba(200,180,150,0.5) 15%, rgba(200,180,150,0.5) 85%, transparent);
  border-radius: 1px;
}
.connector-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(200,180,150,0.5);
  flex-shrink: 0;
}

/* ============================
   Comment Panel
   ============================ */
.comment-panel {
  width: 100%;
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 28px;
  display: flex;
  flex-direction: column;
  height: 100%;
  box-shadow: 0 4px 32px rgba(0,0,0,0.12);
  overflow: hidden;
  font-family: var(--globalFont);
}
.comment-panel-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 18px 20px; border-bottom: 1px solid #f0f0f0; flex-shrink: 0;
}
.comment-panel-header h3 { margin: 0; font-size: 17px; font-weight: 600; color: #333; }
.comment-panel-close {
  background: none; border: none; font-size: 24px; color: #999; cursor: pointer;
  padding: 0 4px; line-height: 1;
}
.comment-panel-close:hover { color: #333; }

/* --- Comment List --- */
.comment-list {
  flex: 1; overflow-y: auto; padding: 12px 16px;
}
.comment-list::-webkit-scrollbar { width: 4px }
.comment-list::-webkit-scrollbar-thumb { background: #e0e0e0; border-radius: 2px }

/* --- Comment Item --- */
.comment-item { display: flex; gap: 10px; padding: 14px 0; border-bottom: 1px solid #f5f5f5; }
.comment-avatar { flex-shrink: 0; cursor: pointer; transition: all 0.2s ease; }
.comment-avatar:hover { transform: scale(1.12); box-shadow: 0 0 0 3px rgba(76,175,80,0.25); }
.comment-body { flex: 1; min-width: 0; }
.comment-top { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.comment-nick { font-size: 13px; font-weight: 600; color: #555; }
.comment-time { font-size: 11px; color: #bbb; }
.comment-text { font-size: 15px; line-height: 1.6; color: #333; word-break: break-word; }
.comment-actions-row { display: flex; gap: 16px; margin-top: 6px; }
.c-action {
  display: flex; align-items: center; gap: 3px;
  background: none; border: none; color: #999; font-size: 12px;
  cursor: pointer; padding: 2px 0; transition: color 0.2s;
}
.c-action:hover { color: #666; }

/* --- Replies --- */
.replies-wrap {
  margin-top: 8px; padding: 8px 12px; background: #f9f9f9; border-radius: 8px;
}
.reply-item { padding: 5px 0; font-size: 14px; line-height: 1.6; }
.reply-nick { font-weight: 600; color: #666; }
.reply-to { color: #999; }
.reply-colon { color: #999; }
.reply-text { color: #333; }
.reply-time-inline { font-size: 11px; color: #bbb; margin-left: 6px; }
.reply-reply-btn {
  background: none; border: none; color: #999; font-size: 11px;
  cursor: pointer; padding: 0; margin-left: 6px;
}
.reply-reply-btn:hover { color: #666; }

/* --- Comment Input Bar --- */
.comment-input-bar {
  flex-shrink: 0; padding: 12px 16px; border-top: 1px solid #f0f0f0;
  background: #fafafa;
}
.comment-input :deep(.el-input__wrapper) {
  border-radius: 24px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.reply-cancel {
  display: block; text-align: center; margin-top: 6px;
  font-size: 12px; color: #999; cursor: pointer;
}
.reply-cancel:hover { color: #666; }

/* ============================
   Loading & Error
   ============================ */
.reader-loading { min-height: 400px; color: #b8a888; }
.reader-error { min-height: 400px; flex-direction: column; gap: 16px; }

/* ============================
   Transitions
   ============================ */
.reader-enter-active { transition: opacity 0.35s cubic-bezier(0.22,0.61,0.36,1); }
.reader-leave-active { transition: opacity 0.25s cubic-bezier(0.55,0.06,0.68,0.19); }
.reader-enter-from, .reader-leave-to { opacity: 0; }

.reader-enter-active .reader-card {
  animation: card-in 0.45s cubic-bezier(0.22,0.61,0.36,1) both;
}
.reader-leave-active .reader-card {
  animation: card-out 0.25s cubic-bezier(0.55,0.06,0.68,0.19) both;
}

@keyframes card-in {
  0% { opacity: 0; transform: scale(0.8) translateY(40px); }
  100% { opacity: 1; transform: scale(1) translateY(0); }
}
@keyframes card-out {
  0% { opacity: 1; transform: scale(1); }
  100% { opacity: 0; transform: scale(0.9); }
}

/* Comment panel transition */
.comment-panel-enter-active { transition: all 0.35s cubic-bezier(0.22,0.61,0.36,1); }
.comment-panel-leave-active { transition: all 0.25s cubic-bezier(0.55,0.06,0.68,0.19); }
.comment-panel-enter-from { opacity: 0; transform: translateX(-20px); }
.comment-panel-leave-to { opacity: 0; transform: translateX(-20px); }

/* ============================
   Mobile Responsive
   ============================ */
@media screen and (max-width: 900px) {
  .reader-overlay { padding: 8px 6px; align-items: flex-start; }
  .reader-layout { flex-direction: column; height: auto; max-height: 97vh; max-width: 100%; gap: 0; }
  .reader-layout.has-comments { justify-content: flex-start; gap: 0; }

  .reader-card {
    max-width: 100% !important;
    border-radius: 18px;
  }

  .reader-close { top: -6px; right: -6px; width: 32px; height: 32px; font-size: 18px; }

  /* Side actions → horizontal row */
  .side-actions {
    flex-direction: row;
    justify-content: center;
    margin-top: 8px;
    gap: 24px;
    padding: 4px 0;
  }
  .side-actions-below { gap: 24px; margin-top: 4px; }
  .side-line { width: 1px; height: 16px; background: rgba(0,0,0,0.15); }
  .side-btn {
    color: #666;
    background: rgba(0,0,0,0.04);
    border-color: rgba(0,0,0,0.08);
  }
  .side-btn:hover { background: rgba(0,0,0,0.08); }
  .comment-panel-col { width: 100%; }

  /* Connector hidden on mobile */
  .comment-connector { display: none; }

  /* Paper */
  .paper-sheet {
    padding: 32px 18px 24px 28px;
    border-radius: 18px;
    max-height: 65vh;
    border-left: 1.5px solid rgba(210,70,50,0.2);
    font-size: 16px;
    background-image:
      repeating-linear-gradient(transparent, transparent 33px, #e8e0d0 33px, #e8e0d0 34px);
  }
  .paper-holes { left: 12px; gap: 28px; top: 24px; }
  .paper-hole { width: 8px; height: 8px; }
  .paper-title { font-size: 20px; }
  .paper-content { font-size: 16px; line-height: 34px; }
  .paper-content :deep(p) { margin-bottom: 34px; }

  /* Comment panel → below */
  .comment-panel {
    width: 100%;
    border-radius: 0 0 18px 18px;
    max-height: 45vh;
    box-shadow: 0 -4px 20px rgba(0,0,0,0.1);
    margin-top: 4px;
  }
  .comment-panel-enter-from,
  .comment-panel-leave-to { opacity: 0; transform: translateY(20px); }
}
</style>

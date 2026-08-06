<template>
  <Teleport to="body">
    <Transition name="reader">
      <div v-if="visible" class="reader-overlay" @click.self="close">
        <div class="reader-layout" :class="{ 'has-comments': showComments }">
          <!-- ============ Article Card ============ -->
          <div class="reader-card" ref="cardRef">
            <button class="reader-close" @click="close" title="关闭">&times;</button>
            <div v-if="loading" class="reader-loading myCenter">
              <el-icon class="is-loading" :size="36"><Loading /></el-icon>
            </div>
            <div v-else-if="error" class="reader-error myCenter">
              <el-empty description="文章加载失败" :image-size="60" />
              <el-button type="primary" @click="fetchArticle">重新加载</el-button>
            </div>
            <div v-else-if="article" class="paper-sheet">
              <div class="paper-holes"><span v-for="i in 3" :key="i" class="paper-hole" /></div>
              <div class="paper-header"><h2 class="paper-title">{{ article.title }}</h2></div>
              <div class="paper-content" v-html="article.content" />
              <div class="paper-signature">
                <span class="sig-date">{{ formatDate(article.createTime) }}</span>
                <span class="sig-author">{{ authorName }}</span>
              </div>
            </div>
          </div>

          <!-- ============ Desktop: sidebar + inline comments ============ -->
          <template v-if="!isMobile">
            <!-- Vertical sidebar (no comments) -->
            <div v-if="!showComments" class="side-actions">
              <ActionButtons :liked="isLiked" :like-count="likeCount" :comment-total="commentTotal"
                @toggle-like="toggleLike" @toggle-comments="toggleComments" />
            </div>
            <!-- Connector + inline panel (comments open) -->
            <template v-if="showComments">
              <div class="comment-connector">
                <div class="connector-dot connector-dot-top" />
                <div class="connector-line" />
                <div class="connector-dot connector-dot-bot" />
              </div>
              <div class="comment-panel-col">
                <Transition name="comment-panel">
                  <div v-if="showComments" class="comment-panel">
                    <CommentSection
                      :items="comments" :loading="commentLoading" :total="commentTotal"
                      :input-text="commentText" :reply-target="replyTarget" :submitting="submitting"
                      @close="showComments = false" @show-mini="showMiniProfile"
                      @start-reply="startReply" @like-comment="likeComment"
                      @submit="submitComment" @cancel-reply="cancelReply"
                      @update:input-text="commentText = $event"
                    />
                  </div>
                </Transition>
              </div>
              <!-- Horizontal actions below panel -->
              <div class="side-actions side-actions-h">
                <ActionButtons :liked="isLiked" :like-count="likeCount" :comment-total="commentTotal"
                  @toggle-like="toggleLike" @toggle-comments="toggleComments" />
              </div>
            </template>
          </template>

          <!-- ============ Mobile: horizontal actions below card ============ -->
          <div v-if="isMobile && !showComments" class="side-actions-mobile">
            <ActionButtons :liked="isLiked" :like-count="likeCount" :comment-total="commentTotal"
              @toggle-like="toggleLike" @toggle-comments="toggleComments" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- ============ Mobile: TikTok-style bottom sheet ============ -->
  <Teleport to="body">
    <Transition name="comment-sheet">
      <div v-if="visible && showComments && isMobile" class="comment-sheet-mask" @click.self="showComments = false">
        <div class="comment-sheet-panel">
          <div class="comment-sheet-handle" />
          <CommentSection
            :items="comments" :loading="commentLoading" :total="commentTotal"
            :input-text="commentText" :reply-target="replyTarget" :submitting="submitting"
            @close="showComments = false" @show-mini="showMiniProfile"
            @start-reply="startReply" @like-comment="likeComment"
            @submit="submitComment" @cancel-reply="cancelReply"
            @update:input-text="commentText = $event"
          />
        </div>
      </div>
    </Transition>
  </Teleport>

  <MiniProfileCard
    :userId="miniProfile.userId" :position="miniProfile.position"
    :show="miniProfile.show" @close="miniProfile.show = false"
  />
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { requireLogin } from '../composables/useAuth'
import request from '../api/request'
import { articleApi, commentApi } from '../api/modules'
import { useUserStore } from '../stores/user'
import { useAppStore } from '../stores/app'
import MiniProfileCard from './MiniProfileCard.vue'
import CommentSection from './CommentSection.vue'
import ActionButtons from './ActionButtons.vue'

const props = defineProps({ articleId: { type: Number, required: true } })
const emit = defineEmits(['close', 'liked', 'commented'])

const appStore = useAppStore()
const userStore = useUserStore()

const article = ref(null)
const loading = ref(false)
const error = ref(false)
const visible = ref(false)
const cardRef = ref(null)

const isLiked = ref(false)
const likeCount = ref(0)

const showComments = ref(false)
const comments = ref([])
const commentTotal = ref(0)
const commentLoading = ref(false)
const commentText = ref('')
const submitting = ref(false)
const replyTarget = ref(null)

const isMobile = ref(false)
let mobileMql = null

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
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
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
    if (data) { article.value = data; likeCount.value = data.likeCount || 0; restoreLike() }
    else error.value = true
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
      parents.forEach(p => { p.children = replies.filter(r => r.parentId === p.id) })
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

function restoreLike() {
  if (localStorage.getItem('liked_' + props.articleId)) isLiked.value = true
}

function toggleComments() {
  showComments.value = !showComments.value
  if (showComments.value && comments.value.length === 0) fetchComments()
}

function startReply(parent, reply) {
  const target = reply || parent
  replyTarget.value = { id: parent.id, userId: target.userId, username: target.username || target.nickname || '匿名' }
  showComments.value = true
  if (comments.value.length === 0) fetchComments()
}

function cancelReply() { replyTarget.value = null }

async function submitComment() {
  const text = commentText.value.trim()
  if (!text) return
  if (!requireLogin()) return
  submitting.value = true
  try {
    const payload = { articleId: props.articleId, content: text }
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

function likeComment(item) { item._likes = (item._likes || 0) + 1 }

function onKeydown(e) { if (e.key === 'Escape') close() }

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

onMounted(() => {
  mobileMql = window.matchMedia('(max-width: 900px)')
  isMobile.value = mobileMql.matches
  mobileMql.addEventListener('change', (e) => { isMobile.value = e.matches })
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
  if (mobileMql) mobileMql.removeEventListener('change', () => {})
})
</script>

<style scoped>
/* ============================
   Overlay
   ============================ */
.reader-overlay {
  position: fixed; inset: 0; z-index: 999;
  background: rgba(0,0,0,0.5);
  backdrop-filter: blur(6px); -webkit-backdrop-filter: blur(6px);
  display: flex; align-items: center; justify-content: center;
  padding: 24px;
}

/* ============================
   Layout
   ============================ */
.reader-layout {
  display: flex; align-items: stretch; gap: 32px;
  max-width: 1400px; width: 100%;
  height: 75vh; max-height: 82vh;
  justify-content: center;
}
.reader-layout.has-comments { gap: 0; }

/* ============================
   Card
   ============================ */
.reader-card {
  position: relative; width: 100%; max-width: 860px;
  flex-shrink: 0; display: flex; flex-direction: column;
  border-radius: 28px; overflow: visible;
}
.reader-layout.has-comments .reader-card { max-width: none; flex: 1 1 0; min-width: 0; }
.reader-layout.has-comments .comment-panel-col { flex: 1 1 0; min-width: 0; }

/* ============================
   Close button
   ============================ */
.reader-close {
  position: absolute; top: -14px; right: -14px; z-index: 20;
  background: #fff; border: 2px solid #e8e0d0; color: #8a7a6a;
  font-size: 22px; width: 40px; height: 40px; border-radius: 50%;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all 0.25s; line-height: 1; padding: 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.reader-close:hover { background: #f56c6c; color: #fff; border-color: #f56c6c; transform: rotate(90deg); }

/* ============================
   Paper Sheet
   ============================ */
.paper-sheet {
  background-color: #fdfaf4;
  background-image: repeating-linear-gradient(transparent, transparent 37px, #e8e0d0 37px, #e8e0d0 38px);
  border-radius: 28px; padding: 56px 56px 40px 72px;
  position: relative; min-height: 560px; max-height: 75vh; overflow-y: auto;
  box-shadow: 0 2px 4px rgba(0,0,0,0.06), 0 6px 20px rgba(0,0,0,0.08), 0 16px 48px rgba(0,0,0,0.12);
  border-left: 2px solid rgba(102,187,106,0.35);
  font-family: var(--globalFont);
}
.paper-sheet::-webkit-scrollbar { width: 5px }
.paper-sheet::-webkit-scrollbar-track { background: transparent; margin: 24px 0 }
.paper-sheet::-webkit-scrollbar-thumb { background: #d0c8b8; border-radius: 1em }

.paper-holes { position: absolute; top: 28px; left: 22px; display: flex; flex-direction: column; gap: 36px; }
.paper-hole { width: 10px; height: 10px; border-radius: 50%; background: radial-gradient(circle at 30% 30%, #f0ebe0, #d8d0c0); box-shadow: inset 0 1px 2px rgba(0,0,0,0.1); }

.paper-header { text-align: center; margin-bottom: 28px; padding-bottom: 18px; border-bottom: 1px solid rgba(0,0,0,0.06); }
.paper-title { font-size: 28px; font-weight: 700; color: #3a3228; margin: 0; line-height: 1.5; letter-spacing: 1.5px; }

.paper-content { font-size: 19px; line-height: 38px; color: #3a3228; word-break: break-word; }
.paper-content :deep(p) { margin: 0 0 38px 0; text-indent: 2em; }
.paper-content :deep(img) { max-width: 100%; border-radius: 6px; margin: 20px 0; display: block; }
.paper-content :deep(pre) { background: rgba(0,0,0,0.03); border: 1px solid rgba(0,0,0,0.06); border-radius: 6px; padding: 18px 22px; overflow-x: auto; margin: 0 0 38px 0; line-height: 1.6; font-size: 16px; }
.paper-content :deep(code) { font-size: 14px; font-family: 'JetBrains Mono','Consolas',monospace; }
.paper-content :deep(blockquote) { border-left: 3px solid #d4a574; padding: 10px 18px; margin: 0 0 38px 0; background: rgba(212,165,116,0.08); border-radius: 0 6px 6px 0; font-style: italic; }
.paper-content :deep(h1), .paper-content :deep(h2), .paper-content :deep(h3), .paper-content :deep(h4) { margin: 0 0 20px 0; line-height: 1.4; color: #2a2218; }
.paper-content :deep(h2) { font-size: 23px; border-bottom: 1px solid rgba(0,0,0,0.06); padding-bottom: 10px; }
.paper-content :deep(h3) { font-size: 20px; }
.paper-content :deep(ul), .paper-content :deep(ol) { margin: 0 0 38px 0; padding-left: 2em; }
.paper-content :deep(li) { line-height: 38px; }
.paper-content :deep(a) { color: #8b6914; border-bottom: 1px dotted #c9a84c; text-decoration: none; }
.paper-content :deep(table) { width: 100%; border-collapse: collapse; margin: 0 0 36px 0; }
.paper-content :deep(th), .paper-content :deep(td) { border: 1px solid #ddd6c8; padding: 8px 12px; text-align: left; line-height: 1.8; }
.paper-content :deep(th) { background: rgba(0,0,0,0.02); }

.paper-signature { margin-top: 52px; padding-top: 24px; text-align: right; display: flex; flex-direction: column; align-items: flex-end; gap: 8px; }
.sig-date { font-size: 15px; color: #8a8070; letter-spacing: 0.5px; }
.sig-author { font-size: 17px; color: #5a5040; font-weight: 600; position: relative; }
.sig-author::before { content: ''; display: inline-block; width: 48px; height: 1px; background: #c8b898; margin-right: 14px; vertical-align: middle; }

/* ============================
   Comment panel column (desktop)
   ============================ */
.comment-panel-col { display: flex; flex-direction: column; gap: 12px; flex-shrink: 0; }

/* ============================
   Comment Panel wrapper (desktop inline)
   ============================ */
.comment-panel {
  width: 100%; flex: 1; min-width: 0;
  background: #fff; border-radius: 28px;
  display: flex; flex-direction: column; height: 100%;
  box-shadow: 0 4px 32px rgba(0,0,0,0.12); overflow: hidden;
  font-family: var(--globalFont);
}

/* ============================
   Side Action Bar (desktop vertical)
   ============================ */
.side-actions {
  display: flex; flex-direction: column; align-items: center;
  gap: 8px; z-index: 10; padding: 0 4px; flex-shrink: 0; align-self: center;
}
.side-actions-h { flex-direction: row; justify-content: center; gap: 20px; padding: 6px 0; }
.side-actions-h :deep(.side-line) { width: 1px; height: 18px; }

/* ============================
   Comment Connector
   ============================ */
.comment-connector {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 0; width: 20px; flex-shrink: 0; z-index: 5;
}
.connector-line {
  width: 2px; flex: 1; min-height: 60px;
  background: linear-gradient(to bottom, transparent, rgba(200,180,150,0.5) 15%, rgba(200,180,150,0.5) 85%, transparent);
  border-radius: 1px;
}
.connector-dot { width: 6px; height: 6px; border-radius: 50%; background: rgba(200,180,150,0.5); flex-shrink: 0; }

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
.reader-enter-active .reader-card { animation: card-in 0.45s cubic-bezier(0.22,0.61,0.36,1) both; }
.reader-leave-active .reader-card { animation: card-out 0.25s cubic-bezier(0.55,0.06,0.68,0.19) both; }
@keyframes card-in { 0% { opacity: 0; transform: scale(0.8) translateY(40px); } 100% { opacity: 1; transform: scale(1) translateY(0); } }
@keyframes card-out { 0% { opacity: 1; transform: scale(1); } 100% { opacity: 0; transform: scale(0.9); } }
.comment-panel-enter-active { transition: all 0.35s cubic-bezier(0.22,0.61,0.36,1); }
.comment-panel-leave-active { transition: all 0.25s cubic-bezier(0.55,0.06,0.68,0.19); }
.comment-panel-enter-from { opacity: 0; transform: translateX(-20px); }
.comment-panel-leave-to { opacity: 0; transform: translateX(-20px); }

/* ============================
   Mobile Responsive (≤900px)
   ============================ */
@media screen and (max-width: 900px) {
  /* overlay 盖过 TabBar(z-index:1000) */
  .reader-overlay { z-index: 1001; padding: 0; }

  .reader-layout {
    flex-direction: column; max-width: 100%; gap: 0; display: flex;
  }
  /* 仅文章：上下均等留白，margin:auto 居中 */
  .reader-layout:not(.has-comments) { height: auto; max-height: 85vh; margin: auto; padding: 24px 12px; width: 100%; }
  /* 评论展开：顶部对齐无内边距，下方留给弹出层 */
  .reader-layout.has-comments { height: 40vh; margin: 0; }

  .reader-card {
    max-width: 100% !important; border-radius: 18px;
    width: 100%;
  }
  .reader-layout:not(.has-comments) .reader-card { flex: 0 1 auto; max-height: 70vh; }
  .reader-layout.has-comments .reader-card { flex: 1 1 auto; overflow: hidden; }

  .reader-close { top: -6px; right: -6px; width: 32px; height: 32px; font-size: 18px; }

  .side-actions-mobile {
    display: flex; flex-direction: row; justify-content: center;
    gap: 24px; padding: 8px 0 4px; flex-shrink: 0;
  }
  .side-actions-mobile :deep(.side-line) { width: 1px; height: 16px; background: rgba(0,0,0,0.15); }
  .side-actions-mobile :deep(.side-btn) {
    color: #666; background: rgba(0,0,0,0.04); border-color: rgba(0,0,0,0.08);
  }

  .paper-sheet {
    padding: 28px 16px 20px 24px; border-radius: 18px;
    overflow-y: auto;
    border-left: 1.5px solid rgba(210,70,50,0.2); font-size: 16px;
    background-image: repeating-linear-gradient(transparent, transparent 33px, #e8e0d0 33px, #e8e0d0 34px);
  }
  .reader-layout:not(.has-comments) .paper-sheet { max-height: 58vh; }
  .reader-layout.has-comments .paper-sheet { max-height: 100%; }
  .paper-holes { left: 10px; gap: 28px; top: 22px; }
  .paper-hole { width: 8px; height: 8px; }
  .paper-title { font-size: 20px; }
  .paper-content { font-size: 16px; line-height: 34px; }
  .paper-content :deep(p) { margin-bottom: 34px; }

  .comment-panel-col { display: none; }
  .comment-connector { display: none; }
  .side-actions { display: none; }
}

/* ============================
   Mobile Bottom Sheet
   ============================ */
.comment-sheet-mask {
  position: fixed; inset: 0; z-index: 2000;
  display: flex; align-items: flex-end;
}
.comment-sheet-panel {
  width: 100%; height: 60vh; background: #fff;
  border-radius: 20px 20px 0 0;
  display: flex; flex-direction: column; overflow: hidden;
  box-shadow: 0 -4px 32px rgba(0,0,0,0.15);
  padding-bottom: env(safe-area-inset-bottom, 0px);
}
.comment-sheet-handle { width: 40px; height: 4px; background: #d1d1d6; border-radius: 2px; margin: 10px auto 6px; flex-shrink: 0; }
.comment-sheet-panel :deep(.comment-panel-header) { padding: 12px 20px 14px; }
.comment-sheet-panel :deep(.comment-list) { flex: 1 1 auto; overflow-y: auto; padding: 0 16px; }
.comment-sheet-panel :deep(.comment-input-bar) { flex-shrink: 0; padding: 10px 16px; }

.comment-sheet-enter-active, .comment-sheet-leave-active { transition: opacity 0.3s ease; }
.comment-sheet-enter-active .comment-sheet-panel, .comment-sheet-leave-active .comment-sheet-panel { transition: transform 0.35s cubic-bezier(0.32,0.72,0,1); }
.comment-sheet-enter-from, .comment-sheet-leave-to { opacity: 0; }
.comment-sheet-enter-from .comment-sheet-panel, .comment-sheet-leave-to .comment-sheet-panel { transform: translateY(100%); }
@media (prefers-reduced-motion: reduce) {
  .comment-sheet-enter-active, .comment-sheet-leave-active,
  .comment-sheet-enter-active .comment-sheet-panel, .comment-sheet-leave-active .comment-sheet-panel { transition: none; }
}
</style>

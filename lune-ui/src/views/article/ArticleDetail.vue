<template>
  <div>
    <div v-if="article" class="article-page">
      <!-- Hero Banner -->
      <div class="article-head my-animation-slide-top">
        <el-image
          class="article-cover-image"
          v-once
          lazy
          :src="article.cover || article.articleCover || randomCover"
          fit="cover"
        >
          <template #error>
            <div class="article-cover-fallback"></div>
          </template>
        </el-image>
        <div class="article-cover-mask"></div>

        <!-- Article info overlay -->
        <div class="article-info-container">
          <div class="article-title">{{ article.title || article.articleTitle }}</div>
          <div class="article-info">
            <!-- Author -->
            <svg viewBox="0 0 1024 1024" width="14" height="14" class="info-svg">
              <path
                d="M510.4 65.5l259.7 0 0 266.9c0 147.5-116.2 266.9-259.7 266.9-143.4 0-259.7-119.5-259.7-266.9 0.1-147.5 116.3-266.9 259.7-266.9z"
                fill="#FF9FCF" />
              <path
                d="M698.4 525.2l-13 0c53-48.4 86.5-117.8 86.5-195.2 0-10.2-0.7-20.3-1.8-30.2C613.8 377.5 438.6 444.9 266 437.7c15 33.4 36.7 63.1 63.5 87.5l-5.3 0c-122.6 0-225.5 88.1-248.8 204.1C340 677.2 597.7 609.2 862.2 585.7c-44.3-37.6-101.5-60.5-163.8-60.5z"
                fill="#FF83BB" />
              <path
                d="M862.2 585.7C597.7 609.2 340 677.2 75.4 729.3c-3.2 16.1-5 32.6-5 49.6 0 99.8 81.7 181.5 181.5 181.5l518.6 0c99.8 0 181.5-81.7 181.5-181.5 0.1-77.2-35-146.5-89.8-193.2z"
                fill="#FF5390" />
            </svg>
            <span>&nbsp;{{ article.author || article.username || '佚名' }}</span>
            <span class="info-sep">·</span>

            <!-- Date -->
            <svg viewBox="0 0 1024 1024" width="14" height="14" class="info-svg">
              <path d="M512 512m-512 0a512 512 0 1 0 1024 0 512 512 0 1 0-1024 0Z" fill="#409EFF" />
              <path
                d="M654.2 256c-17 0-28.4 11.4-28.4 28.4v56.9c0 17.1 11.4 28.5 28.4 28.5s28.5-11.4 28.5-28.5v-56.9c0-17-11.4-28.4-28.5-28.4zM369.8 256c-17.1 0-28.5 11.4-28.5 28.4v56.9c0 17.1 11.4 28.5 28.5 28.5s28.4-11.4 28.4-28.5v-56.9c0-17-11.3-28.4-28.4-28.4z"
                fill="#FFFFFF" />
              <path
                d="M725.3 312.9h-14.2v28.4c0 31.3-25.6 56.9-56.9 56.9s-56.9-25.6-56.9-56.9v-28.4h-170.6v28.4c0 31.3-25.6 56.9-56.9 56.9s-56.9-25.6-56.9-56.9v-28.4h-14.2c-22.8 0-42.7 19.9-42.7 42.7v341.3c0 22.8 19.9 42.7 42.7 42.7h426.6c22.8 0 42.7-19.9 42.7-42.7V355.6c0-22.8-19.9-42.7-42.7-42.7z"
                fill="#FFFFFF" />
            </svg>
            <span>&nbsp;{{ formatDate(article.createTime) }}</span>
            <span class="info-sep">·</span>

            <!-- Views -->
            <svg viewBox="0 0 1024 1024" width="14" height="14" class="info-svg">
              <path d="M14.656 512a497.344 497.344 0 1 0 994.688 0 497.344 497.344 0 1 0-994.688 0z" fill="#FF0000" />
              <path
                d="M374.976 872.64c-48.3-100-22.6-157.4 14.4-211.4 40.5-59 51.1-117.6 51.1-117.6s31.7 41.4 19.1 106c56.2-62.7 66.8-162.1 58.3-200.4C645.2 538.4 699.5 730.7 626 873 1016 652.5 723.1 323.2 672.3 285.9c16.9 37.3 20.1 100-14.1 130.5-58-219.8-201.7-265-201.7-265 17 113.5-61.8 237.4-137.3 330.2-2.8-45.1-5.6-76.5-29.5-119.8-5.3 82.2-68.4 149.3-85.3 231.4-23 111.6 17.2 193.2 170.6 279.5z"
                fill="#FFFFFF" />
            </svg>
            <span>&nbsp;{{ article.viewCount || 0 }}</span>
            <span class="info-sep">·</span>

            <!-- Comments -->
            <svg viewBox="0 0 1024 1024" width="14" height="14" class="info-svg">
              <path
                d="M113.8 291.8v449.2a29 29 0 0 0 28.9 29h252.9v90.5l160.6-90.5h252.9a29 29 0 0 0 29-29V291.8a29 29 0 0 0-29-29h-665.6a29 29 0 0 0-29.7 29z"
                fill="#FFDEAD" />
              <path
                d="M619 632.3l101.9-35.1-131.8-76.1 29.9 111.2zM891.9 149a61.4 61.4 0 0 0-84.1 22.5l-20 34.2 106.7 61.6 20-34.2a61.8 61.8 0 0 0-22.6-84.1z"
                fill="#69BAF9" />
              <path d="M775.3 198.8l131.7 76-186 322.2-131.7-76z" fill="#F7FBFF" />
              <path
                d="M265.9 417.6h229a17.1 17.1 0 1 0 0-34.1h-229a17.1 17.1 0 1 0 0 34.1zM265.9 533.5h229a17.1 17.1 0 0 0 0-34.1h-229a17.1 17.1 0 0 0 0 34.1z"
                fill="#3D3D63" />
            </svg>
            <span>&nbsp;{{ commentTotal }}</span>
            <span class="info-sep">·</span>

            <!-- Likes -->
            <svg viewBox="0 0 1024 1024" width="14" height="14" class="info-svg">
              <path
                d="M510.7 348.8S340.1 48.8 134.2 254.7C-97.6 486.6 510.7 913.4 510.7 913.4s616.1-419 376.4-658.7C691 60 510.7 348.8 510.7 348.8z"
                fill="#FF713C" />
            </svg>
            <span>&nbsp;{{ article.likeCount || 0 }}</span>
          </div>
        </div>
      </div>

      <!-- Article Content Area -->
      <div class="article-body-wrap">
        <div class="article-container my-animation-slide-bottom">
          <!-- Article content -->
          <div v-html="articleContent" class="entry-content"></div>

          <!-- Update time -->
          <div v-if="article.updateTime" class="article-update-time">
            <span>文章最后更新于 {{ formatDate(article.updateTime) }}</span>
          </div>

          <!-- Categories / Tags -->
          <div v-if="article.categories || article.sort" class="article-categories">
            <span>
              {{ categoryLabel }}
            </span>
          </div>

          <!-- Author blockquote -->
          <blockquote class="article-blockquote">
            <div>作者：{{ article.author || article.username || '佚名' }}</div>
            <div>
              <span>版权&许可请详阅 </span>
              <span class="copyright-link" @click="copyrightVisible = true">版权声明</span>
            </div>
          </blockquote>
        </div>
      </div>

      <!-- Comment Section -->
      <div class="comment-wrap" v-if="article.commentStatus !== false">
        <div class="comment-container">
          <h3 class="comment-heading">
            <svg viewBox="0 0 1024 1024" width="20" height="20" class="info-svg">
              <path
                d="M113.8 291.8v449.2a29 29 0 0 0 28.9 29h252.9v90.5l160.6-90.5h252.9a29 29 0 0 0 29-29V291.8a29 29 0 0 0-29-29h-665.6a29 29 0 0 0-29.7 29z"
                fill="#FFDEAD" />
            </svg>
            &nbsp;评论 ({{ commentTotal }})
          </h3>

          <!-- Comment Form -->
          <div class="comment-form">
            <div class="comment-form-header">
              <el-avatar
                v-if="userStore.user"
                :size="36"
                :src="userStore.user.avatar"
                class="comment-form-avatar"
              >{{ (userStore.nickname || '?').charAt(0) }}</el-avatar>
              <span v-if="replyTarget" class="reply-target">
                回复 @{{ replyTarget.username }}&nbsp;
                <el-button :icon="Close" circle size="small" @click="cancelReply" />
              </span>
            </div>
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              :placeholder="replyTarget ? `回复 @${replyTarget.username}...` : '写下你的评论...'"
              maxlength="500"
              show-word-limit
            />
            <div class="comment-form-actions">
              <el-button type="primary" @click="submitComment" :loading="submitting" :disabled="!commentContent.trim()">
                发表评论
              </el-button>
            </div>
          </div>

          <!-- Comment List -->
          <div v-if="comments.length > 0" class="comment-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-main">
                <el-avatar :size="40" :src="comment.avatar" class="comment-avatar">
                  {{ (comment.nickname || comment.username || `U`).charAt(0) }}
                </el-avatar>
                <div class="comment-body">
                  <div class="comment-meta">
                    <span class="comment-username">{{ comment.nickname || comment.username || `用户${comment.userId}` }}</span>
                    <span class="comment-time">{{ formatDateTime(comment.createTime) }}</span>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                  <div class="comment-actions">
                    <el-button text size="small" @click="startReply(comment)">
                      <el-icon><ChatLineSquare /></el-icon>&nbsp;回复
                    </el-button>
                  </div>

                  <!-- Replies -->
                  <div v-if="comment.children && comment.children.length > 0" class="replies-wrap">
                    <div v-for="reply in comment.children" :key="reply.id" class="reply-item">
                      <el-avatar :size="32" :src="reply.avatar" class="reply-avatar">
                        {{ (reply.nickname || reply.username || 'R').charAt(0) }}
                      </el-avatar>
                      <div class="reply-body">
                        <div class="reply-meta">
                          <span class="reply-username">{{ reply.nickname || reply.username || `用户${reply.userId}` }}</span>
                          <span v-if="reply.replyToUsername" class="reply-to">
                            &nbsp;回复 @{{ reply.replyToUsername }}
                          </span>
                          <span class="reply-time">{{ formatDateTime(reply.createTime) }}</span>
                        </div>
                        <div class="reply-content">{{ reply.content }}</div>
                        <div class="comment-actions">
                          <el-button text size="small" @click="startReply(comment, reply)">
                            <el-icon><ChatLineSquare /></el-icon>&nbsp;回复
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Pagination -->
          <div v-if="commentTotal > pageSize" class="comment-pagination myCenter">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="commentTotal"
              layout="prev, pager, next"
              background
              @current-change="fetchComments"
            />
          </div>

          <!-- Empty -->
          <div v-if="!loading && comments.length === 0" style="text-align: center; padding: 40px 0;">
            <el-empty description="暂无评论，快来抢沙发吧" />
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="article-footer">
        <p class="footer-text">{{ appStore.webInfo.footer || 'Lune' }}</p>
      </div>
    </div>

    <!-- Loading -->
    <div v-else-if="loading" style="text-align: center; padding: 120px 0;">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
    </div>

    <!-- Not found -->
    <div v-else style="text-align: center; padding: 120px 0;">
      <el-empty description="文章不存在或已被删除" />
    </div>

    <!-- Copyright Dialog -->
    <el-dialog
      v-model="copyrightVisible"
      title="版权声明"
      width="80%"
      :append-to-body="true"
      center
    >
      <div class="copyright-dialog-body">
        <el-avatar shape="square" :size="35" :src="appStore.ownerInfo.avatar" />
        <div class="copyright-text">
          <p>{{ appStore.webInfo.webName }}的网站版权声明：</p>
          <ul>
            <li>{{ appStore.webInfo.webName }}提供的所有文章、展示的图片素材等内容部分来源于互联网平台，仅供学习参考。如有侵犯您的版权，请联系{{ appStore.webInfo.webName }}负责人。</li>
            <li>{{ appStore.webInfo.webName }}不保证网站内容的全部准确性、安全性和完整性，请您在阅读、下载及使用过程中自行确认。</li>
            <li>未经{{ appStore.webInfo.webName }}允许，不得盗链、盗用本站内容和资源。</li>
            <li>{{ appStore.webInfo.webName }}中的文章的版权仅归原作者所有。</li>
          </ul>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Loading, Close, ChatLineSquare } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { requireLogin } from '../../composables/useAuth'
import { usePageBackground } from '../../composables/usePageBackground'
import { articleApi, commentApi } from '../../api/modules'
import { useAppStore } from '../../stores/app'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const article = ref(null)
const articleContent = ref('')
const loading = ref(true)
const copyrightVisible = ref(false)

// Comments
const comments = ref([])
const commentTotal = ref(0)
const commentContent = ref('')
const submitting = ref(false)
const currentPage = ref(1)
const pageSize = 10
const replyTarget = ref(null)

// 文章无封面时的兜底背景（使用站点配置的通用内容背景，由 composable 随机选取）
const randomCover = usePageBackground('homeContent')

const categoryLabel = computed(() => {
  if (!article.value) return ''
  const cat = article.value.category || article.value.sort
  const tag = article.value.tags || article.value.label
  const catName = typeof cat === 'object' ? (cat.name || cat.sortName) : cat
  const tagName = typeof tag === 'object' ? (tag.name || tag.labelName) : (Array.isArray(tag) ? tag.map(t => typeof t === 'object' ? t.name : t).join(' / ') : tag)
  const parts = [catName, tagName].filter(Boolean)
  return parts.join(' > ')
})

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

function formatDateTime(d) {
  if (!d) return ''
  const date = new Date(d)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

async function fetchArticle() {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const data = await articleApi.getById(id)
    if (data) {
      article.value = data
      // Handle content: may be plain HTML or markdown-converted
      articleContent.value = data.content || data.articleContent || ''
      await fetchComments()
    }
  } catch (e) {
    article.value = null
  } finally {
    loading.value = false
  }
}

async function fetchComments() {
  const articleId = route.params.id
  if (!articleId) return
  try {
    const data = await commentApi.list({
      articleId: Number(articleId),
      page: currentPage.value,
      size: pageSize
    })
    if (data) {
      comments.value = data.records || []
      commentTotal.value = data.total || 0
    }
  } catch (e) {
    // silently handle
  }
}

async function submitComment() {
  const content = commentContent.value.trim()
  if (!content) return
  if (!requireLogin()) return
  submitting.value = true
  try {
    const payload = {
      articleId: Number(route.params.id),
      content: content
    }
    if (replyTarget.value) {
      payload.parentId = replyTarget.value.id
      payload.replyTo = replyTarget.value.userId
      payload.replyToUsername = replyTarget.value.username || replyTarget.value.nickname
    }
    await commentApi.create(payload)
    commentContent.value = ''
    replyTarget.value = null
    ElMessage.success('评论成功')
    await fetchComments()
  } catch (e) {
    ElMessage.error('评论失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

function startReply(parent, reply) {
  const target = reply || parent
  replyTarget.value = {
    id: parent.id,
    userId: target.userId,
    username: target.username || target.nickname || `用户${target.userId}`
  }
  // Scroll to comment form
  const form = document.querySelector('.comment-form')
  if (form) form.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

function cancelReply() {
  replyTarget.value = null
}

onMounted(() => {
  fetchArticle()
})
</script>

<style scoped>
/* ========== Hero Banner ========== */
.article-head {
  height: 40vh;
  min-height: 240px;
  position: relative;
  overflow: hidden;
}

.article-cover-image {
  width: 100%;
  height: 100%;
  position: absolute;
  inset: 0;
}

.article-cover-image :deep(img) {
  object-fit: cover;
}

.article-cover-fallback {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.article-cover-mask {
  position: absolute;
  width: 100%;
  height: 100%;
  background-color: var(--miniMask, rgba(0, 0, 0, 0.35));
  content: "";
}

/* ========== Article Info Overlay ========== */
.article-info-container {
  position: absolute;
  bottom: 15px;
  left: 20%;
  color: var(--white, #fff);
  max-width: 60%;
}

.article-title {
  font-size: 28px;
  margin-bottom: 15px;
  line-height: 1.3;
}

.article-info {
  font-size: 14px;
  user-select: none;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.info-svg {
  vertical-align: -2px;
}

.info-sep {
  margin: 0 5px;
}

/* ========== Article Content ========== */
.article-body-wrap {
  background: var(--background);
}

.article-container {
  max-width: 780px;
  margin: 0 auto;
  padding: 40px 20px;
  background: var(--cardBg, #fff);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-top: -30px;
  position: relative;
  z-index: 5;
}

.entry-content {
  line-height: 1.9;
  font-size: 16px;
  color: var(--articleFontColor);
  word-break: break-word;
}

.entry-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 16px 0;
}

.entry-content :deep(pre) {
  background: var(--codeBg, #f5f5f5);
  border-radius: 8px;
  padding: 16px;
  overflow-x: auto;
  margin: 16px 0;
}

.entry-content :deep(code) {
  font-size: 14px;
}

.entry-content :deep(blockquote) {
  border-left: 3px solid var(--themeBackground);
  padding: 8px 16px;
  margin: 16px 0;
  background: var(--azure, #f0f7ff);
  border-radius: 0 4px 4px 0;
}

.entry-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.entry-content :deep(th),
.entry-content :deep(td) {
  border: 1px solid var(--borderColor, #e5e5e5);
  padding: 8px 12px;
  text-align: left;
}

.entry-content :deep(th) {
  background: var(--codeBg, #f5f5f5);
}

.entry-content :deep(h1),
.entry-content :deep(h2),
.entry-content :deep(h3),
.entry-content :deep(h4) {
  margin: 24px 0 12px;
  line-height: 1.4;
}

/* ========== Update Time ========== */
.article-update-time {
  color: var(--greyFont);
  font-size: 12px;
  margin: 20px 0;
  user-select: none;
}

/* ========== Categories ========== */
.article-categories {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.article-categories span {
  padding: 3px 10px;
  background-color: var(--themeBackground);
  border-radius: 5px;
  font-size: 14px;
  color: var(--white, #fff);
  transition: all 0.3s;
  margin-right: 25px;
  cursor: pointer;
  user-select: none;
}

.article-categories span:hover {
  background-color: var(--red, #f56c6c);
}

/* ========== Blockquote ========== */
.article-blockquote {
  line-height: 2;
  border-left: 0.2rem solid var(--blue, #409EFF);
  padding: 10px 1rem;
  background-color: var(--azure, #f0f7ff);
  border-radius: 4px;
  margin: 0 0 40px 0;
  user-select: none;
  color: var(--textColor);
}

.copyright-link {
  color: #38f;
  cursor: pointer;
}

/* ========== Comment Section ========== */
.comment-wrap {
  background: var(--background);
  padding-bottom: 40px;
}

.comment-container {
  max-width: 780px;
  margin: 0 auto;
  padding: 24px 20px;
}

.comment-heading {
  font-size: 20px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
}

.comment-form {
  margin-bottom: 30px;
}

.comment-form-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
}

.comment-form-avatar {
  flex-shrink: 0;
}

.reply-target {
  font-size: 13px;
  color: var(--themeBackground);
  display: flex;
  align-items: center;
}

.comment-form-actions {
  margin-top: 10px;
  text-align: right;
}

/* ========== Comment List ========== */
.comment-list {
  margin-top: 10px;
}

.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--borderColor, #eee);
}

.comment-main {
  display: flex;
  gap: 12px;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.comment-username {
  font-weight: 600;
  font-size: 14px;
  color: var(--themeBackground);
}

.comment-time {
  font-size: 12px;
  color: var(--greyFont);
}

.comment-content {
  font-size: 15px;
  line-height: 1.6;
  color: var(--articleFontColor);
  margin-bottom: 6px;
  word-break: break-word;
}

.comment-actions {
  margin-bottom: 4px;
}

/* ========== Replies ========== */
.replies-wrap {
  margin-top: 12px;
  padding-left: 16px;
  border-left: 2px solid var(--borderColor, #eee);
}

.reply-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
}

.reply-avatar {
  flex-shrink: 0;
}

.reply-body {
  flex: 1;
  min-width: 0;
}

.reply-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  flex-wrap: wrap;
}

.reply-username {
  font-weight: 600;
  font-size: 13px;
  color: var(--themeBackground);
}

.reply-to {
  font-size: 13px;
  color: var(--greyFont);
}

.reply-time {
  font-size: 12px;
  color: var(--greyFont);
}

.reply-content {
  font-size: 14px;
  line-height: 1.6;
  color: var(--articleFontColor);
  word-break: break-word;
}

/* ========== Pagination ========== */
.comment-pagination {
  margin-top: 24px;
}

/* ========== Footer ========== */
.article-footer {
  background: var(--background);
  text-align: center;
  padding: 30px 20px;
}

.footer-text {
  color: var(--greyFont);
  font-size: 13px;
}

/* ========== Copyright Dialog ========== */
.copyright-dialog-body {
  display: flex;
  align-items: flex-start;
  flex-direction: column;
  align-items: center;
}

.copyright-text {
  color: var(--textColor);
  line-height: 2.5;
  padding: 10px 30px;
  font-size: 15px;
}

.copyright-text ul {
  padding-left: 20px;
}

/* ========== Responsive ========== */
@media screen and (max-width: 768px) {
  .article-info-container {
    left: 20px;
    max-width: calc(100% - 60px);
  }

  .article-title {
    font-size: 22px;
  }

  .article-info {
    font-size: 12px;
  }

  .article-container {
    margin-top: -20px;
    padding: 24px 16px;
    border-radius: 8px;
  }

  .comment-container {
    padding: 16px;
  }
}

@media screen and (max-width: 480px) {
  .article-head {
    height: 30vh;
    min-height: 200px;
  }

  .article-title {
    font-size: 18px;
  }
}
</style>

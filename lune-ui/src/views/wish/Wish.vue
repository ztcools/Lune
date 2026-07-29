<template>
  <div class="wish-page">
    <!-- ===== Hero ===== -->
    <div class="hero-banner">
      <div class="bg-image" :style="{ backgroundImage: `url(${heroBg})` }"></div>
      <div class="bg-overlay"></div>
      <div class="hero-info">
        <h1 class="hero-title">许愿池</h1>
        <p class="hero-subtitle">说出你想要的小工具，也许就帮你实现了呢 ✨</p>
        <button class="wish-fab" @click="openCreate">
          <span class="wish-fab-icon">🌠</span> 许下心愿
        </button>
      </div>
      <div class="hero-wave"></div>
    </div>

    <!-- ===== 许愿列表（按点赞排行）===== -->
    <div class="wish-content">
      <PageBg :image="contentBg" variant="blue" />

      <div class="wish-list" v-if="wishList.length">
        <div v-for="(w, i) in wishList" :key="w.id" class="wish-card">
          <div class="wish-rank" :class="'rank-' + (i + 1)" v-if="i < 3">{{ ['🥇', '🥈', '🥉'][i] }}</div>
          <div class="wish-rank-plain" v-else>#{{ i + 1 + (page - 1) * size }}</div>

          <div class="wish-main">
            <div class="wish-head">
              <el-avatar :size="42" :src="w.avatar" class="wish-avatar">{{ (w.nickname || w.username || '匿').charAt(0) }}</el-avatar>
              <div class="wish-head-info">
                <span class="wish-nick">{{ w.nickname || w.username || '匿名' }}</span>
                <span class="wish-time">{{ formatRelative(w.createTime) }}</span>
              </div>
            </div>
            <h3 class="wish-title">{{ w.title }}</h3>
            <p class="wish-text" v-if="w.content">{{ w.content }}</p>

            <div class="wish-actions">
              <button class="like-btn" :class="{ liked: w.liked }" @click="toggleLike(w)">
                <svg viewBox="0 0 24 24" width="18" height="18" :fill="w.liked ? '#ff5b7f' : 'none'" :stroke="w.liked ? '#ff5b7f' : 'currentColor'" stroke-width="2">
                  <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                </svg>
                <span>{{ w.likeCount || 0 }}</span>
              </button>
              <button class="comment-btn" @click="toggleComment(w)">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                <span>{{ w.commentCount || 0 }}</span>
              </button>
            </div>

            <!-- 评论区 -->
            <transition name="expand">
              <div v-if="expandedId === w.id" class="wish-comments">
                <div class="wc-list">
                  <div v-if="!comments.length" class="wc-empty">还没有评论，来抢沙发～</div>
                  <div v-for="c in comments" :key="c.id" class="wc-item">
                    <el-avatar :size="28" :src="c.avatar">{{ (c.nickname || c.username || '匿').charAt(0) }}</el-avatar>
                    <div class="wc-body">
                      <div class="wc-top">
                        <span class="wc-nick">{{ c.nickname || c.username || '匿名' }}</span>
                        <span class="wc-time">{{ formatRelative(c.createTime) }}</span>
                      </div>
                      <div class="wc-text">{{ c.content }}</div>
                    </div>
                  </div>
                </div>
                <div class="wc-input">
                  <el-input v-model="commentText" placeholder="友善评论，温暖许愿～" size="small"
                    @keyup.enter="submitComment(w)">
                    <template #suffix>
                      <el-button type="primary" size="small" round :disabled="!commentText.trim()" @click="submitComment(w)">发送</el-button>
                    </template>
                  </el-input>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>

      <el-empty v-else description="许愿池空空如也，来许第一个心愿吧 🌠" />

      <div class="load-more" v-if="total > wishList.length">
        <button class="load-btn" @click="loadMore">加载更多心愿</button>
      </div>
    </div>

    <!-- ===== 许愿对话框 ===== -->
    <el-dialog v-model="createVisible" title="🌠 许下心愿" width="480px" destroy-on-close :close-on-click-modal="false" center>
      <div class="create-body">
        <el-input v-model="form.title" maxlength="60" placeholder="心愿标题：想要一个什么样的工具？" show-word-limit class="create-title" />
        <el-input v-model="form.content" type="textarea" :rows="5" maxlength="500" show-word-limit
          placeholder="描述一下具体需求吧，越详细越容易被实现哦～" class="create-content" />
        <div class="create-tip">💡 登录后即可许愿，心愿会公开展示并支持大家点赞</div>
      </div>
      <template #footer>
        <el-button @click="createVisible = false" round>取消</el-button>
        <el-button type="primary" :loading="posting" :disabled="!form.title.trim()" @click="submitWish" round>提交心愿</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { wishApi, commentApi } from '../../api/modules'
import { usePageBackground } from '../../composables/usePageBackground'
import { useUserStore } from '../../stores/user'
import { requireLogin } from '../../composables/useAuth'
import { ElMessage } from 'element-plus'
import PageBg from '../../components/PageBg.vue'

const userStore = useUserStore()
const heroBg = usePageBackground('wishHero')
const contentBg = usePageBackground('wishContent')

const wishList = ref([])
const page = ref(1)
const size = 10
const total = ref(0)

const expandedId = ref(null)
const comments = ref([])
const commentText = ref('')

const createVisible = ref(false)
const posting = ref(false)
const form = reactive({ title: '', content: '' })

onMounted(() => fetchWishes())

async function fetchWishes() {
  try {
    const data = await wishApi.list({ page: page.value, size })
    if (data?.records) {
      wishList.value = wishList.value.concat(data.records)
      total.value = data.total
    }
  } catch (e) { /* silent */ }
}

function loadMore() { page.value++; fetchWishes() }

function openCreate() {
  if (!requireLogin()) return
  createVisible.value = true
}

async function submitWish() {
  if (!form.title.trim()) return
  posting.value = true
  try {
    await wishApi.create({ title: form.title.trim(), content: form.content.trim() })
    ElMessage.success('心愿已投入许愿池 🌠')
    form.title = ''; form.content = ''; createVisible.value = false
    page.value = 1; wishList.value = []; fetchWishes()
  } catch (e) { ElMessage.error('许愿失败') }
  finally { posting.value = false }
}

async function toggleLike(w) {
  if (!requireLogin()) return
  try {
    const count = await wishApi.toggleLike(w.id)
    w.likeCount = count
    w.liked = !w.liked
  } catch (e) { ElMessage.error('操作失败') }
}

function toggleComment(w) {
  if (expandedId.value === w.id) { expandedId.value = null; comments.value = [] }
  else { expandedId.value = w.id; fetchComments(w.id) }
}

async function fetchComments(sourceId) {
  try {
    const data = await commentApi.list({ sourceId, type: 'wish' })
    comments.value = data?.records || data || []
    const target = wishList.value.find(x => x.id === sourceId)
    if (target) target.commentCount = comments.value.length
  } catch (e) { comments.value = [] }
}

async function submitComment(w) {
  const text = commentText.value.trim()
  if (!text) return
  if (!requireLogin()) return
  try {
    await commentApi.create({ content: text, sourceId: w.id, type: 'wish' })
    commentText.value = ''
    fetchComments(w.id)
  } catch (e) { ElMessage.error('评论失败') }
}

function formatRelative(d) {
  if (!d) return ''
  const diff = Date.now() - new Date(d).getTime()
  const s = Math.floor(diff / 1000), m = Math.floor(s / 60), h = Math.floor(m / 60), days = Math.floor(h / 24)
  if (s < 60) return '刚刚'
  if (m < 60) return `${m}分钟前`
  if (h < 24) return `${h}小时前`
  if (days < 30) return `${days}天前`
  return `${Math.floor(days / 30)}个月前`
}
</script>

<style scoped>
/* ===== Hero ===== */
.hero-banner { position: relative; height: 44vh; min-height: 320px; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.bg-image { position: absolute; inset: 0; background-size: cover; background-position: center; }
.bg-overlay { position: absolute; inset: 0; background: linear-gradient(135deg, rgba(35,166,213,0.4), rgba(155,89,182,0.3)); }
.hero-info { position: relative; z-index: 2; text-align: center; color: #fff; }
.hero-title { font-family: var(--trendy-font); font-size: 46px; font-weight: 700; letter-spacing: 10px; margin: 0 0 12px; text-shadow: 0 4px 24px rgba(0,0,0,0.3); }
.hero-subtitle { font-size: 16px; opacity: 0.95; margin: 0 0 26px; letter-spacing: 1px; }
.hero-wave { position: absolute; bottom: -2px; left: 0; width: 100%; height: 60px; background: var(--background); border-radius: 50% 50% 0 0 / 100% 100% 0 0; z-index: 3; }

.wish-fab { display: inline-flex; align-items: center; gap: 8px; padding: 14px 34px; border: none; border-radius: 40px; background: linear-gradient(135deg, #ffb74d, #ff8a65); color: #fff; font-size: 17px; font-weight: 700; font-family: var(--trendy-font); cursor: pointer; box-shadow: 0 8px 24px rgba(255,138,101,0.45); transition: all 0.35s cubic-bezier(0.34,1.56,0.64,1); }
.wish-fab:hover { transform: translateY(-4px) scale(1.05); box-shadow: 0 12px 32px rgba(255,138,101,0.6); }
.wish-fab-icon { font-size: 20px; animation: twinkle 1.6s ease-in-out infinite; }
@keyframes twinkle { 0%,100% { transform: scale(1) rotate(0); } 50% { transform: scale(1.2) rotate(15deg); } }

/* ===== 内容区 ===== */
.wish-content { position: relative; max-width: 720px; margin: 0 auto; padding: 30px 18px 70px; overflow: hidden; }
.wish-list { position: relative; z-index: 1; display: flex; flex-direction: column; gap: 22px; }

.wish-card { position: relative; display: flex; gap: 14px; background: rgba(255,255,255,0.82); backdrop-filter: blur(16px); border-radius: var(--card-radius); padding: 26px; box-shadow: var(--card-shadow); border: var(--card-border); transition: all 0.35s; }
.wish-card:hover { transform: translateY(-4px); box-shadow: var(--card-shadow-hover); }
.wish-rank { font-size: 34px; flex-shrink: 0; line-height: 1; }
.wish-rank-plain { flex-shrink: 0; width: 44px; height: 44px; border-radius: 50%; background: var(--nature-green-pale); color: var(--nature-green-dark); font-weight: 700; display: flex; align-items: center; justify-content: center; font-size: 15px; }
.wish-main { flex: 1; min-width: 0; }
.wish-head { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.wish-avatar { border: 2px solid #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
.wish-head-info { display: flex; flex-direction: column; }
.wish-nick { font-weight: 700; color: #2e5a2e; font-size: 15px; }
.wish-time { font-size: 12px; color: #a0b0a0; }
.wish-title { font-family: var(--trendy-font); font-size: 20px; font-weight: 700; color: #2e5a2e; margin: 0 0 8px; }
.wish-text { font-size: 14px; color: #5a6a5a; line-height: 1.8; margin: 0 0 14px; white-space: pre-wrap; }

.wish-actions { display: flex; gap: 14px; }
.like-btn, .comment-btn { display: inline-flex; align-items: center; gap: 6px; padding: 8px 18px; border: none; border-radius: 24px; background: var(--nature-green-pale); color: var(--nature-green-dark); font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.3s; }
.like-btn:hover, .comment-btn:hover { transform: translateY(-2px) scale(1.05); }
.like-btn.liked { background: linear-gradient(135deg, #ff8a9e, #ff5b7f); color: #fff; box-shadow: 0 4px 14px rgba(255,91,127,0.4); }

/* ===== 评论 ===== */
.expand-enter-active, .expand-leave-active { transition: all 0.3s ease; max-height: 500px; overflow: hidden; }
.expand-enter-from, .expand-leave-to { opacity: 0; max-height: 0; }
.wish-comments { margin-top: 16px; background: rgba(232,245,233,0.5); border-radius: 16px; padding: 16px; }
.wc-list { display: flex; flex-direction: column; gap: 12px; margin-bottom: 12px; }
.wc-empty { text-align: center; color: #9ab89a; font-size: 13px; padding: 8px 0; }
.wc-item { display: flex; gap: 10px; }
.wc-body { flex: 1; }
.wc-top { display: flex; gap: 10px; align-items: baseline; }
.wc-nick { font-size: 13px; font-weight: 700; color: #2e5a2e; }
.wc-time { font-size: 11px; color: #a0b0a0; }
.wc-text { font-size: 13px; color: #4a5a4a; line-height: 1.6; margin-top: 2px; }

.load-more { text-align: center; margin-top: 30px; position: relative; z-index: 1; }
.load-btn { padding: 12px 36px; border: none; border-radius: 30px; background: var(--nature-gradient); color: #fff; font-size: 15px; font-weight: 600; font-family: var(--trendy-font); cursor: pointer; box-shadow: 0 6px 20px rgba(76,175,80,0.35); transition: all 0.3s; }
.load-btn:hover { transform: translateY(-3px); box-shadow: 0 10px 28px rgba(76,175,80,0.5); }

/* ===== 对话框 ===== */
.create-body { display: flex; flex-direction: column; gap: 16px; }
.create-tip { font-size: 12px; color: #9ab89a; }

@media screen and (max-width: 768px) {
  .hero-title { font-size: 26px; letter-spacing: 3px; }
  .wish-card { padding: 14px 14px; gap: 8px; border-radius: 12px; }
  .wish-rank { font-size: 22px; }
  .wish-fab { padding: 12px 24px; font-size: 15px; }
}
</style>

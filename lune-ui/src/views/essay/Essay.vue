<template>
  <div class="essay-page">
    <!-- Hero Banner -->
    <div class="hero-banner myCenter">
      <el-image
        class="banner-image"
        v-once
        lazy
        :src="bannerImage"
        fit="cover"
      >
        <template #error>
          <div class="image-slot"></div>
        </template>
      </el-image>
      <div class="essay-title">
        <div class="essay-title-main">✍️随笔随心</div>
        <div class="essay-title-sub">随心所悦，记录生活点滴</div>
      </div>
      <!-- Add button: admin only -->
      <div
        v-if="userStore.isAdmin"
        class="essay-add-btn"
        @click="dialogVisible = true"
      >
        <svg width="32" height="32" viewBox="0 0 1024 1024">
          <path d="M0 0h1024v1024H0V0z" fill="#202425" opacity=".01"></path>
          <path
            d="M989.866667 512c0 263.918933-213.947733 477.866667-477.866667 477.866667S34.133333 775.918933 34.133333 512 248.081067 34.133333 512 34.133333s477.866667 213.947733 477.866667 477.866667z"
            fill="#FF7744"
          ></path>
          <path
            d="M512 221.866667A51.2 51.2 0 0 1 563.2 273.066667v187.733333H750.933333a51.2 51.2 0 0 1 0 102.4h-187.733333V750.933333a51.2 51.2 0 0 1-102.4 0v-187.733333H273.066667a51.2 51.2 0 0 1 0-102.4h187.733333V273.066667A51.2 51.2 0 0 1 512 221.866667z"
            fill="#FFFFFF"
          ></path>
        </svg>
      </div>
    </div>

    <!-- Content Container: overlapping the banner -->
    <div class="essay-container">
      <div class="essay-content">
        <div class="essay-list">
          <div
            v-for="(essay, index) in essayList"
            :key="essay.id"
            class="essay-item"
          >
            <div class="essay-item-inner">
              <div class="essay-avatar-col">
                <el-avatar
                  shape="square"
                  :size="36"
                  :src="essay.avatar || appStore.webInfo.avatar"
                ></el-avatar>
              </div>
              <div class="essay-body-col">
                <div class="essay-user-row">
                  <span class="essay-username">{{ essay.username || '随笔' }}</span>
                  <span class="essay-lv">{{ essay.createTimeLv || 'NEW' }}</span>
                </div>
                <div class="essay-content-text">
                  <span v-html="essay.content"></span>
                </div>
                <div class="essay-footer-row">
                  <div class="essay-left-actions">
                    <span class="essay-time">{{ formatRelative(essay.createTime) }}</span>
                    <span
                      v-if="userStore.isLoggedIn && userStore.user?.userId === essay.userId"
                      class="essay-delete"
                      @click="handleDelete(essay.id)"
                    >
                      <svg viewBox="0 0 1024 1024" width="16" height="16" style="vertical-align: -4px;">
                        <path
                          d="M921.1392 155.392h-270.592v-48.2816c0-22.7328-18.432-41.1648-41.1648-41.1648H426.3424a41.1648 41.1648 0 0 0-41.1648 41.1648v48.2816H110.6432c-14.1312 0-25.6 11.4688-25.6 25.6s11.4688 25.6 25.6 25.6h810.496c14.1312 0 25.6-11.4688 25.6-25.6s-11.4688-25.6-25.6-25.6zM170.8032 260.0448v592.8448c0 50.8928 41.2672 92.16 92.16 92.16h500.6848c50.8928 0 92.16-41.2672 92.16-92.16V260.0448H170.8032z m249.1392 462.7968c0 14.1312-11.4688 25.6-25.6 25.6s-25.6-11.4688-25.6-25.6V443.0848c0-14.1312 11.4688-25.6 25.6-25.6s25.6 11.4688 25.6 25.6v279.7568z m243.1488 0c0 14.1312-11.4688 25.6-25.6 25.6s-25.6-11.4688-25.6-25.6V443.0848c0-14.1312 11.4688-25.6 25.6-25.6s25.6 11.4688 25.6 25.6v279.7568z"
                          fill="#FF623E"
                        ></path>
                      </svg>
                    </span>
                  </div>
                  <div
                    class="essay-comment-btn"
                    @click="toggleComment(essay)"
                  >
                    <span class="comment-count-num">{{ essay.commentCount || 0 }}</span>
                    <span>
                      <svg viewBox="0 0 1024 1024" width="20" height="20" style="vertical-align: -4px;">
                        <path
                          d="M512 82.464153c-244.63772 0-442.955484 171.85302-442.955484 383.832945 0 125.44199 69.434395 236.8258 176.814008 306.863946-0.502443 56.870242 0.00307 168.373779 0.00307 168.373779s107.36938-70.083172 159.796426-102.527095c34.066887 7.272637 69.684082 11.135618 106.34198 11.135618 244.63772 0 442.955484-171.85302 442.955484-383.846248C954.955484 254.318196 756.63772 82.464153 512 82.464153z"
                          fill="#04b00f"
                        ></path>
                      </svg>
                    </span>
                  </div>
                </div>
              </div>
            </div>
            <!-- Inline Comments -->
            <div
              v-if="currentEssay && essay.id === currentEssay.id"
              class="essay-comment-inline"
            >
              <div class="comment-section">
                <div
                  v-for="c in essayComments"
                  :key="c.id"
                  class="comment-row"
                >
                  <el-avatar :size="28" :src="c.avatar" shape="square" />
                  <div class="comment-body">
                    <span class="comment-user">{{ c.username }}</span>
                    <span class="comment-text">{{ c.content }}</span>
                    <span class="comment-time">{{ formatRelative(c.createTime) }}</span>
                  </div>
                </div>
                <div v-if="!essayComments.length" class="comment-empty">暂无评论</div>
                <div class="comment-input-row">
                  <el-input
                    v-model="commentText[essay.id]"
                    placeholder="写下你的评论..."
                    size="small"
                    @keyup.enter="submitComment(essay)"
                  />
                  <el-button size="small" type="success" @click="submitComment(essay)">发送</el-button>
                </div>
              </div>
            </div>
            <hr class="essay-divider" />
          </div>
        </div>

        <!-- Pagination -->
        <div class="pagination-wrap">
          <div
            v-if="total > essayList.length"
            class="pagination-btn"
            @click="loadMore"
          >
            下一页
          </div>
          <div v-else class="pagination-end">
            ~~到底啦~~
          </div>
        </div>
      </div>
    </div>

    <!-- Add Dialog -->
    <el-dialog
      v-model="dialogVisible"
      title="随笔"
      width="40%"
      :before-close="handleDialogClose"
      destroy-on-close
      :close-on-click-modal="false"
      center
    >
      <div>
        <div class="dialog-radio-wrap">
          <el-radio-group v-model="isPublic">
            <el-radio-button :value="true">公开</el-radio-button>
            <el-radio-button :value="false">私密</el-radio-button>
          </el-radio-group>
        </div>
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
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { essayApi, commentApi } from '../../api/modules'
import { useUserStore } from '../../stores/user'
import { useAppStore } from '../../stores/app'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const appStore = useAppStore()

// --- state ---
const essayList = ref([])
const currentEssay = ref(null)
const essayComments = ref([])
const commentText = ref({})
const pagination = ref({ current: 1, size: 10, total: 0 })
const total = ref(0)
const dialogVisible = ref(false)
const isPublic = ref(true)
const essayContent = ref('')
const posting = ref(false)
const pageSize = 10

// --- computed-like ---
const bannerImage = ref('/assets/背景2.jpg')

// --- lifecycle ---
onMounted(() => {
  if (appStore.webInfo.backgroundImage) {
    bannerImage.value = appStore.webInfo.backgroundImage
  }
  fetchEssays()
})

// --- methods ---
async function fetchEssays(reset = false) {
  try {
    const data = await essayApi.list({ page: pagination.value.current, size: pageSize })
    if (data && data.records) {
      data.records.forEach((c) => {
        if (c.content) {
          c.content = c.content.replace(/\n{2,}/g, '<div style="height:12px"></div>')
          c.content = c.content.replace(/\n/g, '<br/>')
        }
      })
      if (reset) {
        pagination.value.current = 1
        essayList.value = []
        currentEssay.value = null
      }
      essayList.value = essayList.value.concat(data.records)
      total.value = data.total
    }
  } catch (e) {
    // silent
  }
}

function loadMore() {
  if (total.value > essayList.value.length) {
    pagination.value.current = pagination.value.current + 1
    fetchEssays()
  }
}

function toggleComment(essay) {
  if (currentEssay.value && currentEssay.value.id === essay.id) {
    currentEssay.value = null
    essayComments.value = []
  } else {
    currentEssay.value = essay
    fetchComments(essay.id)
  }
}

async function fetchComments(sourceId) {
  try {
    const data = await commentApi.list({ source: sourceId, type: 'essay' })
    essayComments.value = data?.records || data || []
  } catch (e) {
    essayComments.value = []
  }
}

async function submitComment(essay) {
  const text = (commentText.value[essay.id] || '').trim()
  if (!text) return
  if (!userStore.isLoggedIn) {
    ElMessage.error('请先登录！')
    return
  }
  try {
    await commentApi.create({ content: text, source: essay.id, type: 'essay' })
    commentText.value[essay.id] = ''
    ElMessage.success('评论成功')
    fetchComments(essay.id)
  } catch (e) {
    ElMessage.error('评论失败')
  }
}

async function submitEssay() {
  if (!essayContent.value.trim()) return
  posting.value = true
  try {
    await essayApi.create({ content: essayContent.value, isPublic: isPublic.value })
    ElMessage.success('发布成功')
    essayContent.value = ''
    dialogVisible.value = false
    await fetchEssays(true)
  } catch (e) {
    ElMessage.error('发布失败')
  } finally {
    posting.value = false
  }
}

async function handleDelete(id) {
  if (!userStore.isLoggedIn) {
    ElMessage.error('请先登录！')
    return
  }
  try {
    await ElMessageBox.confirm('确认删除？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success',
      center: true
    })
    await essayApi.delete(id)
    ElMessage.success('删除成功!')
    await fetchEssays(true)
  } catch (e) {
    if (e !== 'cancel') {
      // cancelled ok
    }
  }
}

function handleDialogClose() {
  dialogVisible.value = false
}

function formatRelative(d) {
  if (!d) return ''
  const now = Date.now()
  const diff = now - new Date(d).getTime()
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  if (days < 365) return `${Math.floor(days / 30)}个月前`
  return `${Math.floor(days / 365)}年前`
}
</script>

<style scoped>
/* ====== Hero Banner ====== */
.hero-banner {
  position: relative;
  height: 45vh;
  overflow: hidden;
}
.banner-image {
  position: absolute;
  inset: 0;
}
.banner-image::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background-color: var(--translucent);
  z-index: 2;
}
.essay-title {
  z-index: 10;
  letter-spacing: 4px;
  line-height: 40px;
  font-weight: bold;
  text-align: center;
  color: var(--white);
}
.essay-title-main {
  font-size: 30px;
}
.essay-title-sub {
  font-size: 20px;
}
.essay-add-btn {
  position: absolute;
  bottom: 80px;
  right: 15%;
  cursor: pointer;
  z-index: 10;
  animation: scale 1.5s ease-in-out infinite;
}
@media screen and (max-width: 700px) {
  .essay-add-btn {
    right: 20px;
  }
}

/* ====== Content Container ====== */
.essay-container {
  background: var(--background);
  animation: slide-bottom 1s;
  background-image: linear-gradient(90deg, rgba(60, 10, 30, 0.05) 5%, transparent 0),
    linear-gradient(1turn, rgba(60, 10, 30, 0.05) 5%, transparent 0);
  background-size: 20px 20px;
}
.essay-content {
  max-width: 700px;
  width: 80%;
  background: var(--background);
  border-radius: 12px;
  margin: 0 auto;
  padding: 15px 10px 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.07);
  position: relative;
  top: -60px;
}
@media screen and (max-width: 700px) {
  .essay-content {
    width: 100%;
  }
}
@media screen and (max-width: 450px) {
  .essay-content {
    padding: 15px 0 30px;
  }
}

/* ====== Essay Items ====== */
.essay-item-inner {
  display: flex;
}
.essay-avatar-col {
  margin: 10px 15px 10px 10px;
}
.essay-body-col {
  flex: 1;
}
.essay-user-row {
  margin: 10px 0 15px;
}
.essay-username {
  color: #607199;
  font-size: 16px;
  font-weight: 600;
  margin-right: 5px;
}
.essay-lv {
  color: var(--green);
  border: 1px solid var(--green);
  border-radius: 0.2rem;
  font-size: 12px;
  padding: 2px 4px;
  margin-right: 5px;
}
.essay-content-text {
  margin-right: 20px;
  letter-spacing: 1px;
  line-height: 1.4;
}
.essay-footer-row {
  display: flex;
  justify-content: space-between;
  margin: 25px 0 10px;
}
.essay-left-actions {
  display: flex;
}
.essay-time {
  margin-top: 5px;
  font-size: 14px;
  color: var(--greyFont);
  user-select: none;
}
.essay-delete {
  margin-left: 10px;
  cursor: pointer;
}
.essay-comment-btn {
  cursor: pointer;
  margin-right: 30px;
  padding: 3px 9px;
  background: var(--azure);
  border-radius: 3px;
}
.comment-count-num {
  color: rgb(4, 176, 15);
}

/* ====== Inline Comments ====== */
.essay-comment-inline {
  padding: 0 25px 20px 60px;
}
.comment-section {
  background: var(--commentContent, #f7f9fe);
  border-radius: 8px;
  padding: 12px;
}
.comment-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 10px;
}
.comment-body {
  flex: 1;
}
.comment-user {
  font-weight: 600;
  font-size: 13px;
  color: #607199;
  margin-right: 8px;
}
.comment-text {
  font-size: 13px;
  color: var(--fontColor);
}
.comment-time {
  display: block;
  font-size: 11px;
  color: var(--greyFont);
  margin-top: 2px;
}
.comment-empty {
  font-size: 13px;
  color: var(--greyFont);
  text-align: center;
  padding: 10px;
}
.comment-input-row {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

/* ====== Divider ====== */
.essay-divider {
  position: relative;
  margin: 10px auto;
  border: 1px solid #f5f5f5;
  overflow: visible;
}

/* ====== Pagination ====== */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  font-size: 14px;
  font-weight: bold;
}
.pagination-btn {
  padding: 7px 14px;
  border: 1px solid var(--lightGray);
  border-radius: 1rem;
  color: var(--greyFont);
  user-select: none;
  cursor: pointer;
}
.pagination-btn:hover {
  border: 1px solid var(--themeBackground);
  color: var(--themeBackground);
  box-shadow: 0 0 5px var(--themeBackground);
}
.pagination-end {
  user-select: none;
  color: var(--greyFont);
}

/* ====== Dialog ====== */
.dialog-radio-wrap {
  text-align: center;
  padding-bottom: 20px;
}
.dialog-textarea {
  width: 100%;
  border: 2px solid var(--lightGreen);
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  resize: vertical;
  outline: none;
  background: var(--background);
  color: var(--fontColor);
  box-sizing: border-box;
}
.dialog-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.char-count {
  font-size: 12px;
  color: var(--greyFont);
}
</style>
